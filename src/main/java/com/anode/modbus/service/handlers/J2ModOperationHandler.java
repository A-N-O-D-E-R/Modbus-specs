package com.anode.modbus.service.handlers;

import com.anode.modbus.connection.ModbusConnection;
import com.anode.modbus.connection.ModbusConnectionException;
import com.anode.modbus.model.FunctionCode;
import com.ghgande.j2mod.modbus.procimg.InputRegister;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;
import com.ghgande.j2mod.modbus.util.BitVector;

import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * Modbus operation handler using j2mod library for real device communication.
 */
public class J2ModOperationHandler implements ModbusOperationHandler {

    private final ModbusConnection connection;

    public J2ModOperationHandler(ModbusConnection connection) {
        this.connection = Objects.requireNonNull(connection, "connection must not be null");
    }

    @Override
    public ModbusResult execute(ModbusRequest request) {
        if (!connection.isConnected()) {
            return ModbusResult.failure("Not connected to Modbus device");
        }

        FunctionCode fc = request.getFunctionCode();
        int code = parseCode(fc.getCode());

        try {
            return switch (code) {
                case 1 -> readCoils(request);
                case 2 -> readDiscreteInputs(request);
                case 3 -> readHoldingRegisters(request);
                case 4 -> readInputRegisters(request);
                case 5 -> writeSingleCoil(request);
                case 6 -> writeSingleRegister(request);
                case 15 -> writeMultipleCoils(request);
                case 16 -> writeMultipleRegisters(request);
                default -> ModbusResult.failure("Unsupported function code: " + fc.getCode());
            };
        } catch (ModbusConnectionException e) {
            return ModbusResult.failure("Modbus error: " + e.getMessage());
        }
    }

    private int parseCode(String code) {
        try {
            return Integer.parseInt(code);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private ModbusResult readCoils(ModbusRequest request) throws ModbusConnectionException {
        BitVector result = connection.readCoils(
                request.getUnitId(),
                request.getAddress(),
                request.getQuantity()
        );
        return ModbusResult.success(
                "Read " + request.getQuantity() + " coils from address " + request.getAddress(),
                bitVectorToBytes(result)
        );
    }

    private ModbusResult readDiscreteInputs(ModbusRequest request) throws ModbusConnectionException {
        BitVector result = connection.readDiscreteInputs(
                request.getUnitId(),
                request.getAddress(),
                request.getQuantity()
        );
        return ModbusResult.success(
                "Read " + request.getQuantity() + " discrete inputs from address " + request.getAddress(),
                bitVectorToBytes(result)
        );
    }

    private ModbusResult readHoldingRegisters(ModbusRequest request) throws ModbusConnectionException {
        Register[] registers = connection.readHoldingRegisters(
                request.getUnitId(),
                request.getAddress(),
                request.getQuantity()
        );
        return ModbusResult.success(
                "Read " + registers.length + " holding registers from address " + request.getAddress(),
                registersToBytes(registers)
        );
    }

    private ModbusResult readInputRegisters(ModbusRequest request) throws ModbusConnectionException {
        InputRegister[] registers = connection.readInputRegisters(
                request.getUnitId(),
                request.getAddress(),
                request.getQuantity()
        );
        return ModbusResult.success(
                "Read " + registers.length + " input registers from address " + request.getAddress(),
                inputRegistersToBytes(registers)
        );
    }

    private ModbusResult writeSingleCoil(ModbusRequest request) throws ModbusConnectionException {
        // For write operations, we need additional data (the value to write)
        // This is a simplified version that writes TRUE
        boolean success = connection.writeSingleCoil(
                request.getUnitId(),
                request.getAddress(),
                true
        );
        return success
                ? ModbusResult.success("Wrote coil at address " + request.getAddress())
                : ModbusResult.failure("Failed to write coil at address " + request.getAddress());
    }

    private ModbusResult writeSingleRegister(ModbusRequest request) throws ModbusConnectionException {
        // Simplified version - writes 0
        Register register = new SimpleRegister(0);
        int count = connection.writeSingleRegister(
                request.getUnitId(),
                request.getAddress(),
                register
        );
        return ModbusResult.success("Wrote " + count + " register at address " + request.getAddress());
    }

    private ModbusResult writeMultipleCoils(ModbusRequest request) throws ModbusConnectionException {
        // Simplified version - writes all FALSE
        BitVector coils = new BitVector(request.getQuantity());
        connection.writeMultipleCoils(
                request.getUnitId(),
                request.getAddress(),
                coils
        );
        return ModbusResult.success("Wrote " + request.getQuantity() + " coils from address " + request.getAddress());
    }

    private ModbusResult writeMultipleRegisters(ModbusRequest request) throws ModbusConnectionException {
        // Simplified version - writes zeros
        Register[] registers = new Register[request.getQuantity()];
        for (int i = 0; i < registers.length; i++) {
            registers[i] = new SimpleRegister(0);
        }
        int count = connection.writeMultipleRegisters(
                request.getUnitId(),
                request.getAddress(),
                registers
        );
        return ModbusResult.success("Wrote " + count + " registers from address " + request.getAddress());
    }

    private byte[] bitVectorToBytes(BitVector vector) {
        return vector.getBytes();
    }

    private byte[] registersToBytes(Register[] registers) {
        ByteBuffer buffer = ByteBuffer.allocate(registers.length * 2);
        for (Register reg : registers) {
            buffer.putShort((short) reg.getValue());
        }
        return buffer.array();
    }

    private byte[] inputRegistersToBytes(InputRegister[] registers) {
        ByteBuffer buffer = ByteBuffer.allocate(registers.length * 2);
        for (InputRegister reg : registers) {
            buffer.putShort((short) reg.getValue());
        }
        return buffer.array();
    }

    public ModbusConnection getConnection() {
        return connection;
    }
}
