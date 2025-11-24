package com.anode.modbus.connection;

/**
 * Exception thrown when a Modbus connection operation fails.
 */
public class ModbusConnectionException extends Exception {

    public ModbusConnectionException(String message) {
        super(message);
    }

    public ModbusConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
