package com.anode.modbus.parsers;

import com.anode.modbus.api.ModbusOperation;
import com.anode.modbus.connection.ModbusConnection;
import com.anode.modbus.connection.ModbusConnectionException;
import com.anode.modbus.connection.ModbusTcpConnection;
import com.anode.modbus.connection.ModbusSerialConnection;
import com.anode.modbus.model.ConnectionConfig;
import com.anode.modbus.model.Device;
import com.anode.modbus.model.FunctionCode;
import com.anode.modbus.parser.ModbusParseException;
import com.anode.modbus.parser.ModbusXmlParser;
import com.anode.modbus.repository.ModbusSpecRepository;
import com.anode.modbus.service.J2ModOperationHandler;
import com.anode.modbus.service.ModbusService;
import com.anode.modbus.service.ModbusService.ModbusResult;
import com.ghgande.j2mod.modbus.procimg.InputRegister;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.util.BitVector;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.Optional;

/**
 * Facade for the Modbus specification parser and communication.
 * Provides a simple API for loading Modbus specifications and executing operations
 * using the j2mod library for real device communication.
 *
 * <p>This class coordinates the parsing, storage, and service layers to provide
 * a clean, easy-to-use interface for working with Modbus specifications.</p>
 *
 * <p>Example usage with TCP connection:</p>
 * <pre>{@code
 * ModbusSpecParser parser = new ModbusSpecParser();
 * parser.load(new File("modbus-spec.xml"));
 *
 * // Connect to device via TCP
 * try (ModbusConnection conn = parser.connectTcp("192.168.1.100", 502)) {
 *     Register[] registers = parser.readHoldingRegisters(conn, 1, 0, 10);
 *     for (Register reg : registers) {
 *         System.out.println("Value: " + reg.getValue());
 *     }
 * }
 * }</pre>
 *
 * <p>Example usage with Serial connection:</p>
 * <pre>{@code
 * try (ModbusConnection conn = parser.connectSerial("/dev/ttyUSB0", 9600)) {
 *     BitVector coils = parser.readCoils(conn, 1, 0, 8);
 *     System.out.println("Coil 0: " + coils.getBit(0));
 * }
 * }</pre>
 */
public class ModbusSpecParser implements AutoCloseable {

    private final ModbusXmlParser xmlParser;
    private final ModbusSpecRepository repository;
    private ModbusService service;
    private ConnectionConfig connectionConfig;
    private ModbusConnection activeConnection;

    /**
     * Creates a new ModbusSpecParser with default components.
     */
    public ModbusSpecParser() {
        this.xmlParser = new ModbusXmlParser();
        this.repository = new ModbusSpecRepository();
        this.service = new ModbusService(repository);
    }


    public ModbusSpecParser(ModbusService.ModbusOperationHandler operationHandler) {
        this.xmlParser = new ModbusXmlParser();
        this.repository = new ModbusSpecRepository();
        this.service = new ModbusService(repository, operationHandler);
    }

    /**
     * Creates a new ModbusSpecParser with custom components.
     * Useful for testing or custom implementations.
     */
    public ModbusSpecParser(ModbusXmlParser xmlParser, ModbusSpecRepository repository, ModbusService service) {
        this.xmlParser = xmlParser;
        this.repository = repository;
        this.service = service;
    }

    /**
     * Loads and parses a Modbus specification from a file.
     *
     * @param xmlFile The XML specification file to load
     * @throws ModbusParseException if parsing fails
     */
    public void load(File xmlFile) throws ModbusParseException {
        ModbusXmlParser.ParseResult result = xmlParser.parse(xmlFile);
        populateRepository(result);
    }

    /**
     * Loads and parses a Modbus specification from an input stream.
     *
     * @param inputStream The input stream containing XML data
     * @throws ModbusParseException if parsing fails
     */
    public void load(InputStream inputStream) throws ModbusParseException {
        ModbusXmlParser.ParseResult result = xmlParser.parse(inputStream);
        populateRepository(result);
    }

    private void populateRepository(ModbusXmlParser.ParseResult result) {
        repository.clear();
        repository.addFunctionCodes(result.getFunctionCodes());
        repository.addDevices(result.getDevices());
        this.connectionConfig = result.getConnectionConfig();
    }

    /**
     * Executes a Modbus function call (simulated).
     *
     * @param functionCode The function code (e.g., "3" for ReadHoldingRegisters)
     * @param unitId       The target device's unit ID (slave address)
     * @param address      The starting register address
     * @param quantity     The number of registers
     * @return The result of the operation
     */
    public ModbusResult callFunction(String functionCode, int unitId, int address, int quantity) {
        return service.callFunction(functionCode, unitId, address, quantity);
    }

    /**
     * Executes a Modbus function call by function name.
     *
     * @param functionName The function name (e.g., "ReadHoldingRegisters")
     * @param unitId       The target device's unit ID
     * @param address      The starting register address
     * @param quantity     The number of registers
     * @return The result of the operation
     */
    public ModbusResult callFunctionByName(String functionName, int unitId, int address, int quantity) {
        return service.callFunctionByName(functionName, unitId, address, quantity);
    }

    /**
     * Gets a function code by its code.
     */
    public Optional<FunctionCode> getFunctionCode(String code) {
        return repository.findFunctionCodeByCode(code);
    }

    /**
     * Gets all loaded function codes.
     */
    public Collection<FunctionCode> getAllFunctionCodes() {
        return repository.getAllFunctionCodes();
    }

    /**
     * Gets a device by its ID.
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

    /**
     * Gets all loaded devices.
     */
    public Collection<Device> getAllDevices() {
        return repository.getAllDevices();
    }

    /**
     * Returns the underlying repository for advanced operations.
     */
    public ModbusSpecRepository getRepository() {
        return repository;
    }

    /**
     * Returns the underlying service for advanced operations.
     */
    public ModbusService getService() {
        return service;
    }

    /**
     * Sets a custom operation handler for the service.
     * Use this to provide actual Modbus communication implementation.
     */
    public void setOperationHandler(ModbusService.ModbusOperationHandler handler) {
        this.service = new ModbusService(repository, handler);
    }

    // ==================== Simple Fluent API ====================

    /**
     * Returns a ModbusOperation for the given function name.
     * This provides a fluent API for executing Modbus operations.
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
     *
     * @param functionName The function name (e.g., "ReadHoldingRegisters")
     * @return A ModbusOperation for fluent chaining
     * @throws IllegalArgumentException if the function is not found
     */
    public ModbusOperation function(String functionName) {
        FunctionCode fc = repository.findFunctionCodeByName(functionName)
                .or(() -> repository.findFunctionCodeByCode(functionName))
                .orElseThrow(() -> new IllegalArgumentException("Function not found: " + functionName));

        return new ModbusOperation(fc, this::getOrCreateConnection);
    }

    /**
     * Ensures a connection is available, creating one if necessary.
     * Uses the connection configuration from the XML spec.
     *
     * @return The active connection
     * @throws RuntimeException if connection fails
     */
    private ModbusConnection getOrCreateConnection() {
        if (activeConnection != null && activeConnection.isConnected()) {
            return activeConnection;
        }

        try {
            activeConnection = connect();
            return activeConnection;
        } catch (ModbusConnectionException e) {
            throw new RuntimeException("Failed to establish Modbus connection", e);
        }
    }

    /**
     * Closes the active connection if one exists.
     */
    @Override
    public void close() {
        if (activeConnection != null) {
            activeConnection.close();
            activeConnection = null;
        }
    }

    /**
     * Disconnects the active connection.
     */
    public void disconnect() {
        close();
    }

    /**
     * Returns whether there is an active connection.
     */
    public boolean isConnected() {
        return activeConnection != null && activeConnection.isConnected();
    }

    // ==================== Connection Configuration ====================

    /**
     * Returns the connection configuration loaded from the XML spec.
     *
     * @return The connection configuration, or null if not specified
     */
    public ConnectionConfig getConnectionConfig() {
        return connectionConfig;
    }

    /**
     * Returns whether a connection configuration was loaded from the XML spec.
     */
    public boolean hasConnectionConfig() {
        return connectionConfig != null;
    }

    // ==================== Connection Factory Methods ====================

    /**
     * Creates and connects using the configuration from the loaded XML spec.
     * The connection type (TCP/RTU/ASCII) and all parameters are determined
     * by the Connection element in the XML.
     *
     * @return A connected ModbusConnection
     * @throws ModbusConnectionException if connection fails or no config is present
     */
    public ModbusConnection connect() throws ModbusConnectionException {
        if (connectionConfig == null) {
            throw new ModbusConnectionException("No connection configuration loaded from XML spec");
        }

        if (connectionConfig.isTcp()) {
            ModbusTcpConnection connection = new ModbusTcpConnection(
                    connectionConfig.getHost(),
                    connectionConfig.getPort(),
                    connectionConfig.getTimeout(),
                    connectionConfig.isReconnect()
            );
            connection.connect();
            return connection;
        } else {
            ModbusSerialConnection connection = new ModbusSerialConnection(
                    connectionConfig.getPortName(),
                    connectionConfig.getBaudRate(),
                    connectionConfig.getDataBits(),
                    connectionConfig.getStopBits(),
                    connectionConfig.getParity(),
                    connectionConfig.getEncoding()
            );
            connection.connect();
            return connection;
        }
    }

    /**
     * Creates and connects to a Modbus TCP device.
     *
     * @param host The host address
     * @param port The TCP port (typically 502)
     * @return A connected ModbusConnection
     * @throws ModbusConnectionException if connection fails
     */
    public ModbusConnection connectTcp(String host, int port) throws ModbusConnectionException {
        ModbusTcpConnection connection = new ModbusTcpConnection(host, port);
        connection.connect();
        return connection;
    }

    /**
     * Creates and connects to a Modbus TCP device with default port (502).
     */
    public ModbusConnection connectTcp(String host) throws ModbusConnectionException {
        return connectTcp(host, 502);
    }

    /**
     * Creates and connects to a Modbus Serial RTU device.
     *
     * @param portName The serial port name (e.g., "/dev/ttyUSB0" or "COM1")
     * @param baudRate The baud rate (e.g., 9600, 19200)
     * @return A connected ModbusConnection
     * @throws ModbusConnectionException if connection fails
     */
    public ModbusConnection connectSerial(String portName, int baudRate) throws ModbusConnectionException {
        ModbusSerialConnection connection = new ModbusSerialConnection(portName, baudRate, 8, 1, 0);
        connection.connect();
        return connection;
    }

    /**
     * Creates and connects to a Modbus Serial RTU device with default settings (9600 baud).
     */
    public ModbusConnection connectSerial(String portName) throws ModbusConnectionException {
        return connectSerial(portName, 9600);
    }

    /**
     * Configures the service to use a specific connection for operations.
     * After calling this, callFunction() will use the provided connection.
     */
    public void useConnection(ModbusConnection connection) {
        this.service = new ModbusService(repository, new J2ModOperationHandler(connection));
    }

    // ==================== Direct Modbus Operations ====================

    /**
     * Reads coils from a device (function code 1).
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
     * Reads discrete inputs from a device (function code 2).
     */
    public BitVector readDiscreteInputs(ModbusConnection connection, int unitId, int address, int quantity)
            throws ModbusConnectionException {
        return connection.readDiscreteInputs(unitId, address, quantity);
    }

    /**
     * Reads holding registers from a device (function code 3).
     */
    public Register[] readHoldingRegisters(ModbusConnection connection, int unitId, int address, int quantity)
            throws ModbusConnectionException {
        return connection.readHoldingRegisters(unitId, address, quantity);
    }

    /**
     * Reads input registers from a device (function code 4).
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
    public int writeSingleRegister(ModbusConnection connection, int unitId, int address, Register register)
            throws ModbusConnectionException {
        return connection.writeSingleRegister(unitId, address, register);
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
    public int writeMultipleRegisters(ModbusConnection connection, int unitId, int address, Register[] registers)
            throws ModbusConnectionException {
        return connection.writeMultipleRegisters(unitId, address, registers);
    }

    /**
     * Example main: load XML and demonstrate both simulated and real operations.
     */
    public static void main(String[] args) {
        try {
            ModbusSpecParser parser = new ModbusSpecParser();
            parser.load(new File("modbus-spec.xml"));

            System.out.println("Loaded " + parser.getAllFunctionCodes().size() + " function codes");
            System.out.println("Loaded " + parser.getAllDevices().size() + " devices");
            System.out.println();

            // Simulated call (no connection)
            System.out.println("=== Simulated Operation ===");
            ModbusResult result = parser.callFunction("3", 10, 0, 2);
            System.out.println("Result: " + (result.isSuccess() ? "Success" : "Failed"));
            System.out.println("Message: " + result.getMessage());

            // Example of real connection (uncomment to test with actual device)
            /*
            System.out.println("\n=== Real TCP Operation ===");
            try (ModbusConnection conn = parser.connectTcp("192.168.1.100", 502)) {
                Register[] registers = parser.readHoldingRegisters(conn, 1, 0, 10);
                for (int i = 0; i < registers.length; i++) {
                    System.out.println("Register " + i + ": " + registers[i].getValue());
                }
            }
            */

        } catch (ModbusParseException e) {
            System.err.println("Failed to load specification: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
