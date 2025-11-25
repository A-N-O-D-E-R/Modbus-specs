package com.anode.modbus.service.handlers;

import com.anode.modbus.model.FunctionCode;

public class MockModbusOperationHandler  implements ModbusOperationHandler {
    
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