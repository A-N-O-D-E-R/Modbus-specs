package com.anode.modbus.connection;

import com.ghgande.j2mod.modbus.procimg.InputRegister;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.util.BitVector;

/**
 * Abstraction for Modbus connections.
 * Supports both TCP and Serial (RTU/ASCII) connections.
 */
public interface ModbusConnection extends AutoCloseable {

    /**
     * Connects to the Modbus device.
     *
     * @throws ModbusConnectionException if connection fails
     */
    void connect() throws ModbusConnectionException;

    /**
     * Disconnects from the Modbus device.
     */
    void disconnect();

    /**
     * Checks if the connection is active.
     */
    boolean isConnected();

    /**
     * Reads coils (function code 1).
     *
     * @param unitId   The unit/slave ID
     * @param address  The starting address
     * @param quantity The number of coils to read
     * @return BitVector containing coil states
     */
    BitVector readCoils(int unitId, int address, int quantity) throws ModbusConnectionException;

    /**
     * Reads discrete inputs (function code 2).
     *
     * @param unitId   The unit/slave ID
     * @param address  The starting address
     * @param quantity The number of inputs to read
     * @return BitVector containing input states
     */
    BitVector readDiscreteInputs(int unitId, int address, int quantity) throws ModbusConnectionException;

    /**
     * Reads holding registers (function code 3).
     *
     * @param unitId   The unit/slave ID
     * @param address  The starting address
     * @param quantity The number of registers to read
     * @return Array of registers
     */
    Register[] readHoldingRegisters(int unitId, int address, int quantity) throws ModbusConnectionException;

    /**
     * Reads input registers (function code 4).
     *
     * @param unitId   The unit/slave ID
     * @param address  The starting address
     * @param quantity The number of registers to read
     * @return Array of input registers
     */
    InputRegister[] readInputRegisters(int unitId, int address, int quantity) throws ModbusConnectionException;

    /**
     * Writes a single coil (function code 5).
     *
     * @param unitId  The unit/slave ID
     * @param address The coil address
     * @param state   The state to write
     * @return true if successful
     */
    boolean writeSingleCoil(int unitId, int address, boolean state) throws ModbusConnectionException;

    /**
     * Writes a single register (function code 6).
     *
     * @param unitId   The unit/slave ID
     * @param address  The register address
     * @param register The register to write
     * @return The number of registers written
     */
    int writeSingleRegister(int unitId, int address, Register register) throws ModbusConnectionException;

    /**
     * Writes multiple coils (function code 15).
     *
     * @param unitId  The unit/slave ID
     * @param address The starting address
     * @param coils   The coil states to write
     */
    void writeMultipleCoils(int unitId, int address, BitVector coils) throws ModbusConnectionException;

    /**
     * Writes multiple registers (function code 16).
     *
     * @param unitId    The unit/slave ID
     * @param address   The starting address
     * @param registers The registers to write
     * @return The number of registers written
     */
    int writeMultipleRegisters(int unitId, int address, Register[] registers) throws ModbusConnectionException;

    @Override
    void close();
}
