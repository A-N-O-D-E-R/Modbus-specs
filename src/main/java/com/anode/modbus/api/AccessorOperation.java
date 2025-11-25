package com.anode.modbus.api;

import com.anode.modbus.connection.ModbusConnection;
import com.anode.modbus.connection.ModbusConnectionException;
import com.anode.modbus.model.Accessor;
import com.ghgande.j2mod.modbus.procimg.InputRegister;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;
import com.ghgande.j2mod.modbus.util.BitVector;

import java.util.function.Supplier;

/**
 * Fluent API for executing Modbus operations using Accessors.
 * This class provides a high-level interface that uses accessor definitions
 * to automatically determine addresses, data types, and operation details.
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * // Read using accessor
 * int[] temps = parser.accessor("getAllTemperatures")
 *     .unitId(1)
 *     .read();
 *
 * // Write using accessor
 * parser.accessor("setValvePositions")
 *     .unitId(1)
 *     .write(new int[]{1, 0, 1, 0});
 * }</pre>
 */
public class AccessorOperation {

    private static final int MIN_UNIT_ID = 0;
    private static final int MAX_UNIT_ID = 247;

    private final Accessor accessor;
    private final Supplier<ModbusConnection> connectionSupplier;
    private int unitId = 1;

    public AccessorOperation(Accessor accessor, Supplier<ModbusConnection> connectionSupplier) {
        this.accessor = java.util.Objects.requireNonNull(accessor, "accessor must not be null");
        this.connectionSupplier = java.util.Objects.requireNonNull(connectionSupplier, "connectionSupplier must not be null");
    }

    /**
     * Sets the unit ID (slave address) for this operation.
     *
     * @param unitId The unit/slave ID (0-247)
     * @return This operation for chaining
     * @throws IllegalArgumentException if unitId is out of range
     */
    public AccessorOperation unitId(int unitId) {
        if (unitId < MIN_UNIT_ID || unitId > MAX_UNIT_ID) {
            throw new IllegalArgumentException("unitId must be between " + MIN_UNIT_ID + " and " + MAX_UNIT_ID + ", got: " + unitId);
        }
        this.unitId = unitId;
        return this;
    }

    /**
     * Executes a read operation and returns the values as an int array.
     * The address and quantity are determined by the accessor.
     *
     * @return Array of register values
     * @throws RuntimeException if the operation fails
     */
    public int[] read() {
        try {
            ModbusConnection connection = connectionSupplier.get();
            int address = accessor.getStartAddress();
            int quantity = accessor.getRegisterCount();

            String function = accessor.getFunction();
            if (isFunction(function, "ReadHoldingRegisters")) {
                Register[] registers = connection.readHoldingRegisters(unitId, address, quantity);
                return convertRegistersToInts(registers);
            } else if (isFunction(function, "ReadInputRegisters")) {
                InputRegister[] registers = connection.readInputRegisters(unitId, address, quantity);
                return convertInputRegistersToInts(registers);
            } else {
                throw new IllegalArgumentException("Accessor function is not a read register operation: " + function);
            }
        } catch (ModbusConnectionException e) {
            throw new RuntimeException("Failed to execute read operation for accessor '" + accessor.getName() + "'", e);
        }
    }

    /**
     * Executes a read operation and returns a single value.
     * Useful for accessors that read a single register.
     *
     * @return The register value
     * @throws RuntimeException if the operation fails or multiple registers are returned
     */
    public int readSingle() {
        int[] values = read();
        if (values.length != 1) {
            throw new IllegalStateException("Expected single value but got " + values.length + " values");
        }
        return values[0];
    }

    /**
     * Executes a read operation for coils and returns boolean array.
     *
     * @return Array of coil states
     * @throws RuntimeException if the operation fails
     */
    public boolean[] readBooleans() {
        try {
            ModbusConnection connection = connectionSupplier.get();
            int address = accessor.getStartAddress();
            int quantity = accessor.getRegisterCount();

            String function = accessor.getFunction();
            BitVector bitVector;
            if (isFunction(function, "ReadCoils")) {
                bitVector = connection.readCoils(unitId, address, quantity);
            } else if (isFunction(function, "ReadDiscreteInputs")) {
                bitVector = connection.readDiscreteInputs(unitId, address, quantity);
            } else {
                throw new IllegalArgumentException("Accessor function is not a boolean read operation: " + function);
            }

            boolean[] result = new boolean[quantity];
            for (int i = 0; i < quantity; i++) {
                result[i] = bitVector.getBit(i);
            }
            return result;
        } catch (ModbusConnectionException e) {
            throw new RuntimeException("Failed to execute boolean read for accessor '" + accessor.getName() + "'", e);
        }
    }

    /**
     * Writes a single integer value.
     *
     * @param value The value to write
     * @throws RuntimeException if the operation fails
     */
    public void write(int value) {
        try {
            ModbusConnection connection = connectionSupplier.get();
            int address = accessor.getStartAddress();

            String function = accessor.getFunction();
            if (isFunction(function, "WriteSingleRegister")) {
                connection.writeSingleRegister(unitId, address, new SimpleRegister(value));
            } else {
                throw new IllegalArgumentException("Accessor function is not WriteSingleRegister: " + function);
            }
        } catch (ModbusConnectionException e) {
            throw new RuntimeException("Failed to execute write operation for accessor '" + accessor.getName() + "'", e);
        }
    }

    /**
     * Writes multiple integer values.
     *
     * @param values The values to write
     * @throws RuntimeException if the operation fails
     */
    public void write(int[] values) {
        if (values == null) {
            throw new IllegalArgumentException("values must not be null");
        }

        int expectedCount = accessor.getRegisterCount();
        if (values.length != expectedCount) {
            throw new IllegalArgumentException(
                "Array length mismatch: accessor '" + accessor.getName() +
                "' expects " + expectedCount + " values, got " + values.length);
        }

        try {
            ModbusConnection connection = connectionSupplier.get();
            int address = accessor.getStartAddress();

            String function = accessor.getFunction();
            if (isFunction(function, "WriteMultipleRegisters")) {
                Register[] registers = new Register[values.length];
                for (int i = 0; i < values.length; i++) {
                    registers[i] = new SimpleRegister(values[i]);
                }
                connection.writeMultipleRegisters(unitId, address, registers);
            } else {
                throw new IllegalArgumentException("Accessor function is not WriteMultipleRegisters: " + function);
            }
        } catch (ModbusConnectionException e) {
            throw new RuntimeException("Failed to execute write operation for accessor '" + accessor.getName() + "'", e);
        }
    }

    /**
     * Writes a single boolean value (coil).
     *
     * @param value The boolean value to write
     * @throws RuntimeException if the operation fails
     */
    public void write(boolean value) {
        try {
            ModbusConnection connection = connectionSupplier.get();
            int address = accessor.getStartAddress();

            String function = accessor.getFunction();
            if (isFunction(function, "WriteSingleCoil")) {
                connection.writeSingleCoil(unitId, address, value);
            } else {
                throw new IllegalArgumentException("Accessor function is not WriteSingleCoil: " + function);
            }
        } catch (ModbusConnectionException e) {
            throw new RuntimeException("Failed to execute coil write for accessor '" + accessor.getName() + "'", e);
        }
    }

    /**
     * Writes multiple boolean values (coils).
     *
     * @param values The boolean values to write
     * @throws RuntimeException if the operation fails
     */
    public void write(boolean[] values) {
        if (values == null) {
            throw new IllegalArgumentException("values must not be null");
        }

        int expectedCount = accessor.getRegisterCount();
        if (values.length != expectedCount) {
            throw new IllegalArgumentException(
                "Array length mismatch: accessor '" + accessor.getName() +
                "' expects " + expectedCount + " values, got " + values.length);
        }

        try {
            ModbusConnection connection = connectionSupplier.get();
            int address = accessor.getStartAddress();

            String function = accessor.getFunction();
            if (isFunction(function, "WriteMultipleCoils")) {
                BitVector bitVector = new BitVector(values.length);
                for (int i = 0; i < values.length; i++) {
                    bitVector.setBit(i, values[i]);
                }
                connection.writeMultipleCoils(unitId, address, bitVector);
            } else {
                throw new IllegalArgumentException("Accessor function is not WriteMultipleCoils: " + function);
            }
        } catch (ModbusConnectionException e) {
            throw new RuntimeException("Failed to execute multiple coil write for accessor '" + accessor.getName() + "'", e);
        }
    }

    /**
     * Returns the accessor being used.
     */
    public Accessor getAccessor() {
        return accessor;
    }

    private int[] convertRegistersToInts(Register[] registers) {
        int[] result = new int[registers.length];
        for (int i = 0; i < registers.length; i++) {
            result[i] = registers[i].getValue();
        }
        return result;
    }

    private int[] convertInputRegistersToInts(InputRegister[] registers) {
        int[] result = new int[registers.length];
        for (int i = 0; i < registers.length; i++) {
            result[i] = registers[i].getValue();
        }
        return result;
    }

    /**
     * Checks if the function name matches the expected function (case-insensitive).
     * This method provides exact matching for function names to avoid false positives.
     *
     * @param actual   The actual function name from accessor
     * @param expected The expected function name
     * @return true if functions match
     */
    private boolean isFunction(String actual, String expected) {
        return actual != null && actual.equalsIgnoreCase(expected);
    }
}
