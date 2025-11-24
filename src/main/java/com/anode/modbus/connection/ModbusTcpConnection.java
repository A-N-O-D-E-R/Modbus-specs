package com.anode.modbus.connection;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.facade.ModbusTCPMaster;
import com.ghgande.j2mod.modbus.procimg.InputRegister;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.util.BitVector;

/**
 * Modbus TCP connection implementation using j2mod.
 */
public class ModbusTcpConnection implements ModbusConnection {

    private final String host;
    private final int port;
    private final int timeout;
    private final boolean reconnect;
    private ModbusTCPMaster master;

    /**
     * Creates a TCP connection with default port (502) and timeout (3000ms).
     */
    public ModbusTcpConnection(String host) {
        this(host, 502, 3000, true);
    }

    /**
     * Creates a TCP connection with specified port and default timeout.
     */
    public ModbusTcpConnection(String host, int port) {
        this(host, port, 3000, true);
    }

    /**
     * Creates a TCP connection with full configuration.
     */
    public ModbusTcpConnection(String host, int port, int timeout, boolean reconnect) {
        this.host = host;
        this.port = port;
        this.timeout = timeout;
        this.reconnect = reconnect;
    }

    @Override
    public void connect() throws ModbusConnectionException {
        try {
            master = new ModbusTCPMaster(host, port, timeout, reconnect);
            master.connect();
        } catch (Exception e) {
            throw new ModbusConnectionException("Failed to connect to " + host + ":" + port, e);
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

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
