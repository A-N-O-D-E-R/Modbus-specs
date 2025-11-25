package com.anode.modbus.service.handlers;

import com.anode.modbus.model.FunctionCode;

/**
 * Interface for handling Modbus operations.
 */
@FunctionalInterface
public interface ModbusOperationHandler {
    ModbusResult execute(ModbusRequest request);

    class ModbusRequest {
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

        public FunctionCode getFunctionCode() { return functionCode; }
        public int getUnitId() { return unitId; }
        public int getAddress() { return address; }
        public int getQuantity() { return quantity; }
    }

    class ModbusResult {
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

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public byte[] getData() { return data; }
    }
}
