package com.anode.modbus.api;

import com.anode.modbus.connection.ModbusConnection;
import com.anode.modbus.connection.ModbusConnectionException;
import com.anode.modbus.model.FunctionCode;
import com.ghgande.j2mod.modbus.procimg.InputRegister;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;
import com.ghgande.j2mod.modbus.util.BitVector;

import java.util.function.Supplier;

/**
 * Fluent API for executing Modbus operations.
 * Provides read() and write() methods based on the function code type.
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * // Read holding registers
 * int[] values = parser.function("ReadHoldingRegisters")
 *     .unitId(1)
 *     .address(0)
 *     .quantity(10)
 *     .read();
 *
 * // Write single register
 * parser.function("WriteSingleRegister")
 *     .unitId(1)
 *     .address(0)
 *     .write(1234);
 * }</pre>
 */
public class ModbusOperation {

    private final FunctionCode functionCode;
    private final Supplier<ModbusConnection> connectionSupplier;

    private int unitId = 1;
    private int address = 0;
    private int quantity = 1;

    public ModbusOperation(FunctionCode functionCode, Supplier<ModbusConnection> connectionSupplier) {
        this.functionCode = functionCode;
        this.connectionSupplier = connectionSupplier;
    }

    /**
     * Sets the unit/slave ID for the operation.
     */
    public ModbusOperation unitId(int unitId) {
        if (unitId < 0 || unitId > 247) {
            throw new IllegalArgumentException("Unit ID must be between 0 and 247, got: " + unitId);
        }
        this.unitId = unitId;
        return this;
    }

    /**
     * Sets the starting address for the operation.
     */
    public ModbusOperation address(int address) {
        if (address < 0 || address > 65535) {
            throw new IllegalArgumentException("Address must be between 0 and 65535, got: " + address);
        }
        this.address = address;
        return this;
    }

    /**
     * Sets the quantity of registers/coils to read.
     */
    public ModbusOperation quantity(int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1, got: " + quantity);
        }
        // Modbus protocol limits: max 125 registers (read), 2000 coils
        if (quantity > 2000) {
            throw new IllegalArgumentException("Quantity exceeds Modbus protocol limit (max 2000), got: " + quantity);
        }
        this.quantity = quantity;
        return this;
    }

    /**
     * Gets the function code for this operation.
     */
    public FunctionCode getFunctionCode() {
        return functionCode;
    }

    // ==================== Read Operations ====================

    /**
     * Executes a read operation and returns the result as int array.
     * Works with: ReadCoils (FC1), ReadDiscreteInputs (FC2),
     * ReadHoldingRegisters (FC3), ReadInputRegisters (FC4).
     *
     * @return Array of integer values
     * @throws ModbusConnectionException if the operation fails
     */
    public int[] read() throws ModbusConnectionException {
        ModbusConnection conn = connectionSupplier.get();
        int code = Integer.parseInt(functionCode.getCode());

        switch (code) {
            case 1: // Read Coils
                BitVector coils = conn.readCoils(unitId, address, quantity);
                return bitVectorToIntArray(coils, quantity);

            case 2: // Read Discrete Inputs
                BitVector inputs = conn.readDiscreteInputs(unitId, address, quantity);
                return bitVectorToIntArray(inputs, quantity);

            case 3: // Read Holding Registers
                Register[] holdingRegs = conn.readHoldingRegisters(unitId, address, quantity);
                return registersToIntArray(holdingRegs);

            case 4: // Read Input Registers
                InputRegister[] inputRegs = conn.readInputRegisters(unitId, address, quantity);
                return inputRegistersToIntArray(inputRegs);

            default:
                throw new ModbusConnectionException("Function code " + code + " does not support read operations");
        }
    }

    /**
     * Reads a single value. Convenience method for quantity=1.
     *
     * @return The single value read
     * @throws ModbusConnectionException if the operation fails
     */
    public int readSingle() throws ModbusConnectionException {
        this.quantity = 1;
        int[] result = read();
        return result.length > 0 ? result[0] : 0;
    }

    /**
     * Reads coils and returns as boolean array.
     *
     * @return Array of boolean values (true=ON, false=OFF)
     * @throws ModbusConnectionException if the operation fails
     */
    public boolean[] readBooleans() throws ModbusConnectionException {
        ModbusConnection conn = connectionSupplier.get();
        int code = Integer.parseInt(functionCode.getCode());

        BitVector bits;
        switch (code) {
            case 1: // Read Coils
                bits = conn.readCoils(unitId, address, quantity);
                break;
            case 2: // Read Discrete Inputs
                bits = conn.readDiscreteInputs(unitId, address, quantity);
                break;
            default:
                throw new ModbusConnectionException("Function code " + code + " does not support boolean read");
        }

        boolean[] result = new boolean[quantity];
        for (int i = 0; i < quantity; i++) {
            result[i] = bits.getBit(i);
        }
        return result;
    }

    // ==================== Write Operations ====================

    /**
     * Writes a single value.
     * Works with: WriteSingleCoil (FC5), WriteSingleRegister (FC6).
     *
     * @param value The value to write
     * @throws ModbusConnectionException if the operation fails
     */
    public void write(int value) throws ModbusConnectionException {
        ModbusConnection conn = connectionSupplier.get();
        int code = Integer.parseInt(functionCode.getCode());

        switch (code) {
            case 5: // Write Single Coil
                conn.writeSingleCoil(unitId, address, value != 0);
                break;

            case 6: // Write Single Register
                conn.writeSingleRegister(unitId, address, new SimpleRegister(value));
                break;

            case 15: // Write Multiple Coils (single value)
                BitVector singleCoil = new BitVector(1);
                singleCoil.setBit(0, value != 0);
                conn.writeMultipleCoils(unitId, address, singleCoil);
                break;

            case 16: // Write Multiple Registers (single value)
                conn.writeMultipleRegisters(unitId, address, new Register[]{new SimpleRegister(value)});
                break;

            default:
                throw new ModbusConnectionException("Function code " + code + " does not support write operations");
        }
    }

    /**
     * Writes a boolean value (for coils).
     *
     * @param value The boolean value to write
     * @throws ModbusConnectionException if the operation fails
     */
    public void write(boolean value) throws ModbusConnectionException {
        write(value ? 1 : 0);
    }

    /**
     * Writes multiple values.
     * Works with: WriteMultipleCoils (FC15), WriteMultipleRegisters (FC16).
     *
     * @param values The values to write
     * @throws ModbusConnectionException if the operation fails
     */
    public void write(int[] values) throws ModbusConnectionException {
        ModbusConnection conn = connectionSupplier.get();
        int code = Integer.parseInt(functionCode.getCode());

        switch (code) {
            case 15: // Write Multiple Coils
                BitVector coils = new BitVector(values.length);
                for (int i = 0; i < values.length; i++) {
                    coils.setBit(i, values[i] != 0);
                }
                conn.writeMultipleCoils(unitId, address, coils);
                break;

            case 16: // Write Multiple Registers
                Register[] registers = new Register[values.length];
                for (int i = 0; i < values.length; i++) {
                    registers[i] = new SimpleRegister(values[i]);
                }
                conn.writeMultipleRegisters(unitId, address, registers);
                break;

            case 5: // Write Single Coil (use first value)
                if (values.length > 0) {
                    conn.writeSingleCoil(unitId, address, values[0] != 0);
                }
                break;

            case 6: // Write Single Register (use first value)
                if (values.length > 0) {
                    conn.writeSingleRegister(unitId, address, new SimpleRegister(values[0]));
                }
                break;

            default:
                throw new ModbusConnectionException("Function code " + code + " does not support write operations");
        }
    }

    /**
     * Writes multiple boolean values (for coils).
     *
     * @param values The boolean values to write
     * @throws ModbusConnectionException if the operation fails
     */
    public void write(boolean[] values) throws ModbusConnectionException {
        int[] intValues = new int[values.length];
        for (int i = 0; i < values.length; i++) {
            intValues[i] = values[i] ? 1 : 0;
        }
        write(intValues);
    }

    // ==================== Helper Methods ====================

    private int[] bitVectorToIntArray(BitVector bits, int count) {
        int[] result = new int[count];
        for (int i = 0; i < count; i++) {
            result[i] = bits.getBit(i) ? 1 : 0;
        }
        return result;
    }

    private int[] registersToIntArray(Register[] registers) {
        int[] result = new int[registers.length];
        for (int i = 0; i < registers.length; i++) {
            result[i] = registers[i].getValue();
        }
        return result;
    }

    private int[] inputRegistersToIntArray(InputRegister[] registers) {
        int[] result = new int[registers.length];
        for (int i = 0; i < registers.length; i++) {
            result[i] = registers[i].getValue();
        }
        return result;
    }
}
