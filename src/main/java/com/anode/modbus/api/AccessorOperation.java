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

    private final Accessor accessor;
    private final Supplier<ModbusConnection> connectionSupplier;
    private int unitId = 1;

    public AccessorOperation(Accessor accessor, Supplier<ModbusConnection> connectionSupplier) {
        this.accessor = accessor;
        this.connectionSupplier = connectionSupplier;
    }

    /**
     * Sets the unit ID (slave address) for this operation.
     *
     * @param unitId The unit/slave ID (typically 1-247)
     * @return This operation for chaining
     */
    public AccessorOperation unitId(int unitId) {
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

            if (accessor.getFunction().toLowerCase().contains("holding")) {
                Register[] registers = connection.readHoldingRegisters(unitId, address, quantity);
                return convertRegistersToInts(registers);
            } else if (accessor.getFunction().toLowerCase().contains("input")) {
                InputRegister[] registers = connection.readInputRegisters(unitId, address, quantity);
                return convertInputRegistersToInts(registers);
            } else {
                throw new IllegalArgumentException("Accessor function is not a read operation: " + accessor.getFunction());
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

            BitVector bitVector;
            if (accessor.getFunction().toLowerCase().contains("coil")) {
                bitVector = connection.readCoils(unitId, address, quantity);
            } else if (accessor.getFunction().toLowerCase().contains("discrete")) {
                bitVector = connection.readDiscreteInputs(unitId, address, quantity);
            } else {
                throw new IllegalArgumentException("Accessor function is not a boolean read operation: " + accessor.getFunction());
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

            if (accessor.getFunction().toLowerCase().contains("single")) {
                connection.writeSingleRegister(unitId, address, new SimpleRegister(value));
            } else {
                throw new IllegalArgumentException("Accessor function is not a single write operation: " + accessor.getFunction());
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
        try {
            ModbusConnection connection = connectionSupplier.get();
            int address = accessor.getStartAddress();

            if (accessor.getFunction().toLowerCase().contains("multiple")) {
                Register[] registers = new Register[values.length];
                for (int i = 0; i < values.length; i++) {
                    registers[i] = new SimpleRegister(values[i]);
                }
                connection.writeMultipleRegisters(unitId, address, registers);
            } else {
                throw new IllegalArgumentException("Accessor function is not a multiple write operation: " + accessor.getFunction());
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

            if (accessor.getFunction().toLowerCase().contains("coil")) {
                connection.writeSingleCoil(unitId, address, value);
            } else {
                throw new IllegalArgumentException("Accessor function is not a coil write operation: " + accessor.getFunction());
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
        try {
            ModbusConnection connection = connectionSupplier.get();
            int address = accessor.getStartAddress();

            if (accessor.getFunction().toLowerCase().contains("coil")) {
                BitVector bitVector = new BitVector(values.length);
                for (int i = 0; i < values.length; i++) {
                    bitVector.setBit(i, values[i]);
                }
                connection.writeMultipleCoils(unitId, address, bitVector);
            } else {
                throw new IllegalArgumentException("Accessor function is not a coil write operation: " + accessor.getFunction());
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
}
