package com.anode.modbus.service;

import com.anode.modbus.connection.ModbusConnection;
import com.anode.modbus.connection.ModbusConnectionException;
import com.anode.modbus.model.Device;
import com.anode.modbus.model.FunctionCode;
import com.anode.modbus.model.Register;
import com.anode.modbus.repository.ModbusSpecRepository;
import com.anode.modbus.service.handlers.ModbusOperationHandler;
import static com.anode.modbus.service.handlers.ModbusOperationHandler.ModbusRequest;
import static com.anode.modbus.service.handlers.ModbusOperationHandler.ModbusResult;
import com.ghgande.j2mod.modbus.procimg.InputRegister;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;
import com.ghgande.j2mod.modbus.util.BitVector;

import java.util.Optional;

/**
 * Service layer for Modbus operations.
 * Contains business logic for working with Modbus function codes and devices.
 */
public class ModbusService {

    private final ModbusSpecRepository repository;
    private final ModbusOperationHandler operationHandler;


    public ModbusService(ModbusSpecRepository repository, ModbusOperationHandler operationHandler) {
        this.repository = repository;
        this.operationHandler = operationHandler;
    }

    /**
     * Executes a Modbus function call.
     *
     * @param functionCode The function code to execute (e.g., "3" for ReadHoldingRegisters)
     * @param unitId       The target device's unit ID (slave address)
     * @param address      The starting register address
     * @param quantity     The number of registers to read/write
     * @return Result of the operation
     */
    public ModbusResult callFunction(String functionCode, int unitId, int address, int quantity) {
        Optional<FunctionCode> fc = repository.findFunctionCodeByCode(functionCode);

        if (fc.isEmpty()) {
            return ModbusResult.failure("Unknown function code: " + functionCode);
        }

        ModbusRequest request = new ModbusRequest(fc.get(), unitId, address, quantity);
        return operationHandler.execute(request);
    }

    /**
     * Executes a Modbus function call by function name.
     *
     * @param functionName The function name (e.g., "ReadHoldingRegisters")
     * @param unitId       The target device's unit ID
     * @param address      The starting register address
     * @param quantity     The number of registers
     * @return Result of the operation
     */
    public ModbusResult callFunctionByName(String functionName, int unitId, int address, int quantity) {
        Optional<FunctionCode> fc = repository.findFunctionCodeByName(functionName);

        if (fc.isEmpty()) {
            return ModbusResult.failure("Unknown function name: " + functionName);
        }

        ModbusRequest request = new ModbusRequest(fc.get(), unitId, address, quantity);
        return operationHandler.execute(request);
    }

    /**
     * Reads a specific register from a device.
     *
     * @param deviceId     The device ID
     * @param registerName The register name
     * @return Result of the operation
     */
    public ModbusResult readRegister(String deviceId, String registerName) {
        Optional<Device> device = repository.findDeviceById(deviceId);
        if (device.isEmpty()) {
            return ModbusResult.failure("Unknown device: " + deviceId);
        }

        Optional<Register> register = device.get().findHoldingRegisterByName(registerName);
        if (register.isEmpty()) {
            return ModbusResult.failure("Unknown register: " + registerName);
        }

        Register reg = register.get();
        if (!reg.isReadable()) {
            return ModbusResult.failure("Register is not readable: " + registerName);
        }

        // Use function code 3 (Read Holding Registers) for holding registers
        return callFunction("3", device.get().getUnitId(), reg.getAddress(), 1);
    }

    /**
     * Gets information about a function code.
     */
    public Optional<FunctionCode> getFunctionCode(String code) {
        return repository.findFunctionCodeByCode(code);
    }

    /**
     * Gets information about a device.
     */
    public Optional<Device> getDevice(String deviceId) {
        return repository.findDeviceById(deviceId);
    }

    /**
     * Gets a device by its unit ID.
     */
    public Optional<Device> getDeviceByUnitId(int unitId) {
        return repository.findDeviceByUnitId(unitId);
    }

    // ==================== Direct Connection Operations ====================

    /**
     * Reads coils directly from a connection (function code 1).
     *
     * @param connection The Modbus connection
     * @param unitId     The unit/slave ID
     * @param address    The starting address
     * @param quantity   The number of coils to read
     * @return BitVector containing coil states
     */
    public BitVector readCoils(ModbusConnection connection, int unitId, int address, int quantity)
            throws ModbusConnectionException {
        return connection.readCoils(unitId, address, quantity);
    }

    /**
     * Reads discrete inputs directly from a connection (function code 2).
     */
    public BitVector readDiscreteInputs(ModbusConnection connection, int unitId, int address, int quantity)
            throws ModbusConnectionException {
        return connection.readDiscreteInputs(unitId, address, quantity);
    }

    /**
     * Reads holding registers directly from a connection (function code 3).
     */
    public com.ghgande.j2mod.modbus.procimg.Register[] readHoldingRegisters(
            ModbusConnection connection, int unitId, int address, int quantity)
            throws ModbusConnectionException {
        return connection.readHoldingRegisters(unitId, address, quantity);
    }

    /**
     * Reads input registers directly from a connection (function code 4).
     */
    public InputRegister[] readInputRegisters(ModbusConnection connection, int unitId, int address, int quantity)
            throws ModbusConnectionException {
        return connection.readInputRegisters(unitId, address, quantity);
    }

    /**
     * Writes a single coil (function code 5).
     */
    public boolean writeSingleCoil(ModbusConnection connection, int unitId, int address, boolean state)
            throws ModbusConnectionException {
        return connection.writeSingleCoil(unitId, address, state);
    }

    /**
     * Writes a single register (function code 6).
     */
    public int writeSingleRegister(ModbusConnection connection, int unitId, int address, int value)
            throws ModbusConnectionException {
        return connection.writeSingleRegister(unitId, address, new SimpleRegister(value));
    }

    /**
     * Writes multiple coils (function code 15).
     */
    public void writeMultipleCoils(ModbusConnection connection, int unitId, int address, BitVector coils)
            throws ModbusConnectionException {
        connection.writeMultipleCoils(unitId, address, coils);
    }

    /**
     * Writes multiple registers (function code 16).
     */
    public int writeMultipleRegisters(ModbusConnection connection, int unitId, int address, int[] values)
            throws ModbusConnectionException {
        com.ghgande.j2mod.modbus.procimg.Register[] registers =
                new com.ghgande.j2mod.modbus.procimg.Register[values.length];
        for (int i = 0; i < values.length; i++) {
            registers[i] = new SimpleRegister(values[i]);
        }
        return connection.writeMultipleRegisters(unitId, address, registers);
    }
    
}
