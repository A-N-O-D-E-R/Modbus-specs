package com.anode.modbus.service;

import com.anode.modbus.connection.ModbusConnection;
import com.anode.modbus.connection.ModbusConnectionException;
import com.anode.modbus.model.Device;
import com.anode.modbus.model.FunctionCode;
import com.anode.modbus.model.Register;
import com.anode.modbus.repository.ModbusSpecRepository;
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

    public ModbusService(ModbusSpecRepository repository) {
        this(repository, new ConsoleOperationHandler());
    }

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

    /**
     * Represents a Modbus request.
     */
    public static class ModbusRequest {
        private final FunctionCode functionCode;
        private final int unitId;
        private final int address;
        private final int quantity;

        public ModbusRequest(FunctionCode functionCode, int unitId, int address, int quantity) {
            this.functionCode = functionCode;
            this.unitId = unitId;
            this.address = address;
            this.quantity = quantity;
        }

        public FunctionCode getFunctionCode() {
            return functionCode;
        }

        public int getUnitId() {
            return unitId;
        }

        public int getAddress() {
            return address;
        }

        public int getQuantity() {
            return quantity;
        }
    }

    /**
     * Represents the result of a Modbus operation.
     */
    public static class ModbusResult {
        private final boolean success;
        private final String message;
        private final byte[] data;

        private ModbusResult(boolean success, String message, byte[] data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public static ModbusResult success(String message) {
            return new ModbusResult(true, message, null);
        }

        public static ModbusResult success(String message, byte[] data) {
            return new ModbusResult(true, message, data);
        }

        public static ModbusResult failure(String message) {
            return new ModbusResult(false, message, null);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public byte[] getData() {
            return data;
        }
    }

    /**
     * Interface for handling Modbus operations.
     * Implement this to provide actual Modbus communication.
     */
    public interface ModbusOperationHandler {
        ModbusResult execute(ModbusRequest request);
    }

    /**
     * Default handler that logs operations to console (simulation).
     */
    public static class ConsoleOperationHandler implements ModbusOperationHandler {
        @Override
        public ModbusResult execute(ModbusRequest request) {
            FunctionCode fc = request.getFunctionCode();

            System.out.println("Executing Modbus function: " + fc.getName() + " (code: " + fc.getCode() + ")");
            System.out.println("Description: " + fc.getDescription());
            System.out.println("Unit ID: " + request.getUnitId());
            System.out.println("Address: " + request.getAddress());
            System.out.println("Quantity: " + request.getQuantity());
            System.out.println("(Simulated) Modbus request sent.");

            return ModbusResult.success("Request simulated successfully");
        }
    }
}
