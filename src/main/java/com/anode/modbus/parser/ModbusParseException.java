package com.anode.modbus.parser;

/**
 * Exception thrown when parsing a Modbus specification fails.
 */
public class ModbusParseException extends Exception {

    public ModbusParseException(String message) {
        super(message);
    }

    public ModbusParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
