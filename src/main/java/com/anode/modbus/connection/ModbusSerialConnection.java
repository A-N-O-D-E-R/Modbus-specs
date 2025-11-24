package com.anode.modbus.connection;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.facade.ModbusSerialMaster;
import com.ghgande.j2mod.modbus.procimg.InputRegister;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.util.BitVector;
import com.ghgande.j2mod.modbus.util.SerialParameters;

/**
 * Modbus Serial (RTU/ASCII) connection implementation using j2mod.
 */
public class ModbusSerialConnection implements ModbusConnection {

    private final SerialParameters parameters;
    private ModbusSerialMaster master;

    /**
     * Creates a serial connection with the specified port and default settings.
     * Default: 9600 baud, 8 data bits, 1 stop bit, no parity, RTU encoding.
     */
    public ModbusSerialConnection(String portName) {
        this(createDefaultParameters(portName));
    }

    /**
     * Creates a serial connection with custom parameters.
     */
    public ModbusSerialConnection(SerialParameters parameters) {
        this.parameters = parameters;
    }

    /**
     * Creates a serial connection with common parameters.
     */
    public ModbusSerialConnection(String portName, int baudRate, int dataBits, int stopBits, int parity) {
        this(portName, baudRate, dataBits, stopBits, parity, "rtu");
    }

    /**
     * Creates a serial connection with all parameters including encoding.
     *
     * @param portName The serial port name (e.g., "/dev/ttyUSB0" or "COM1")
     * @param baudRate The baud rate (e.g., 9600, 19200)
     * @param dataBits Number of data bits (typically 8)
     * @param stopBits Number of stop bits (typically 1)
     * @param parity   Parity: 0=None, 1=Odd, 2=Even
     * @param encoding Protocol encoding: "rtu" or "ascii"
     */
    public ModbusSerialConnection(String portName, int baudRate, int dataBits, int stopBits, int parity, String encoding) {
        this.parameters = new SerialParameters();
        this.parameters.setPortName(portName);
        this.parameters.setBaudRate(baudRate);
        this.parameters.setDatabits(dataBits);
        this.parameters.setStopbits(stopBits);
        this.parameters.setParity(parity);
        this.parameters.setEncoding(encoding != null ? encoding : "rtu");
    }

    private static SerialParameters createDefaultParameters(String portName) {
        SerialParameters params = new SerialParameters();
        params.setPortName(portName);
        params.setBaudRate(9600);
        params.setDatabits(8);
        params.setStopbits(1);
        params.setParity(0); // None
        params.setEncoding("rtu");
        return params;
    }

    @Override
    public void connect() throws ModbusConnectionException {
        try {
            master = new ModbusSerialMaster(parameters);
            master.connect();
        } catch (Exception e) {
            throw new ModbusConnectionException("Failed to connect to serial port: " + parameters.getPortName(), e);
        }
    }

    @Override
    public void disconnect() {
        if (master != null) {
            master.disconnect();
            master = null;
        }
    }

    @Override
    public boolean isConnected() {
        return master != null && master.isConnected();
    }

    @Override
    public BitVector readCoils(int unitId, int address, int quantity) throws ModbusConnectionException {
        ensureConnected();
        try {
            return master.readCoils(unitId, address, quantity);
        } catch (ModbusException e) {
            throw new ModbusConnectionException("Failed to read coils", e);
        }
    }

    @Override
    public BitVector readDiscreteInputs(int unitId, int address, int quantity) throws ModbusConnectionException {
        ensureConnected();
        try {
            return master.readInputDiscretes(unitId, address, quantity);
        } catch (ModbusException e) {
            throw new ModbusConnectionException("Failed to read discrete inputs", e);
        }
    }

    @Override
    public Register[] readHoldingRegisters(int unitId, int address, int quantity) throws ModbusConnectionException {
        ensureConnected();
        try {
            return master.readMultipleRegisters(unitId, address, quantity);
        } catch (ModbusException e) {
            throw new ModbusConnectionException("Failed to read holding registers", e);
        }
    }

    @Override
    public InputRegister[] readInputRegisters(int unitId, int address, int quantity) throws ModbusConnectionException {
        ensureConnected();
        try {
            return master.readInputRegisters(unitId, address, quantity);
        } catch (ModbusException e) {
            throw new ModbusConnectionException("Failed to read input registers", e);
        }
    }

    @Override
    public boolean writeSingleCoil(int unitId, int address, boolean state) throws ModbusConnectionException {
        ensureConnected();
        try {
            return master.writeCoil(unitId, address, state);
        } catch (ModbusException e) {
            throw new ModbusConnectionException("Failed to write coil", e);
        }
    }

    @Override
    public int writeSingleRegister(int unitId, int address, Register register) throws ModbusConnectionException {
        ensureConnected();
        try {
            return master.writeSingleRegister(unitId, address, register);
        } catch (ModbusException e) {
            throw new ModbusConnectionException("Failed to write register", e);
        }
    }

    @Override
    public void writeMultipleCoils(int unitId, int address, BitVector coils) throws ModbusConnectionException {
        ensureConnected();
        try {
            master.writeMultipleCoils(unitId, address, coils);
        } catch (ModbusException e) {
            throw new ModbusConnectionException("Failed to write multiple coils", e);
        }
    }

    @Override
    public int writeMultipleRegisters(int unitId, int address, Register[] registers) throws ModbusConnectionException {
        ensureConnected();
        try {
            return master.writeMultipleRegisters(unitId, address, registers);
        } catch (ModbusException e) {
            throw new ModbusConnectionException("Failed to write multiple registers", e);
        }
    }

    @Override
    public void close() {
        disconnect();
    }

    private void ensureConnected() throws ModbusConnectionException {
        if (!isConnected()) {
            throw new ModbusConnectionException("Not connected to Modbus device");
        }
    }

    public SerialParameters getParameters() {
        return parameters;
    }
}
