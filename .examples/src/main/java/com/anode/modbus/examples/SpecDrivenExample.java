package com.anode.modbus.examples;

import com.anode.modbus.connection.ModbusConnection;
import com.anode.modbus.connection.ModbusConnectionException;
import com.anode.modbus.model.Device;
import com.anode.modbus.model.FunctionCode;
import com.anode.modbus.parser.ModbusParseException;
import com.anode.modbus.parsers.ModbusSpecParser;
import com.anode.modbus.service.ModbusService.ModbusResult;
import com.ghgande.j2mod.modbus.procimg.Register;

import java.io.File;
import java.io.InputStream;

/**
 * Example demonstrating specification-driven Modbus operations.
 *
 * <p>This example shows how to:</p>
 * <ul>
 *   <li>Load a Modbus specification XML file</li>
 *   <li>Query function codes and devices from the spec</li>
 *   <li>Use the spec-defined devices for operations</li>
 *   <li>Combine spec metadata with real device communication</li>
 * </ul>
 */
public class SpecDrivenExample {

    public static void main(String[] args) {
        System.out.println("===========================================");
        System.out.println("    Specification-Driven Modbus Example");
        System.out.println("===========================================");
        System.out.println();

        ModbusSpecParser parser = new ModbusSpecParser();

        // Load the specification
        try {
            loadSpecification(parser);
        } catch (ModbusParseException e) {
            System.err.println("Failed to load specification: " + e.getMessage());
            return;
        }

        // Show loaded specification info
        showSpecificationInfo(parser);

        // Demonstrate simulated operations (no real device needed)
        demonstrateSimulatedOperations(parser);

        // Demonstrate real device operations (uncomment to test)
        // demonstrateRealDeviceOperations(parser);
    }

    private static void loadSpecification(ModbusSpecParser parser) throws ModbusParseException {
        System.out.println("--- Loading Specification ---");

        // Try to load from file first
        File specFile = new File("modbus-spec.xml");
        if (specFile.exists()) {
            System.out.println("Loading from file: " + specFile.getAbsolutePath());
            parser.load(specFile);
        } else {
            // Load from classpath resource
            System.out.println("Loading from classpath resource");
            InputStream stream = SpecDrivenExample.class.getResourceAsStream("/modbus-spec.xml");
            if (stream != null) {
                parser.load(stream);
            } else {
                // Create minimal spec programmatically for demo
                System.out.println("No spec file found, using minimal example");
                createMinimalSpec(parser);
            }
        }
        System.out.println();
    }

    private static void createMinimalSpec(ModbusSpecParser parser) throws ModbusParseException {
        // Create a minimal XML spec inline
        String minimalXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <ModbusSpec>
                    <FunctionCodes>
                        <FunctionCode code="1" name="ReadCoils">
                            <Description>Read coil status (FC 01)</Description>
                        </FunctionCode>
                        <FunctionCode code="2" name="ReadDiscreteInputs">
                            <Description>Read discrete input status (FC 02)</Description>
                        </FunctionCode>
                        <FunctionCode code="3" name="ReadHoldingRegisters">
                            <Description>Read holding register values (FC 03)</Description>
                        </FunctionCode>
                        <FunctionCode code="4" name="ReadInputRegisters">
                            <Description>Read input register values (FC 04)</Description>
                        </FunctionCode>
                        <FunctionCode code="5" name="WriteSingleCoil">
                            <Description>Write single coil (FC 05)</Description>
                        </FunctionCode>
                        <FunctionCode code="6" name="WriteSingleRegister">
                            <Description>Write single register (FC 06)</Description>
                        </FunctionCode>
                        <FunctionCode code="15" name="WriteMultipleCoils">
                            <Description>Write multiple coils (FC 15)</Description>
                        </FunctionCode>
                        <FunctionCode code="16" name="WriteMultipleRegisters">
                            <Description>Write multiple registers (FC 16)</Description>
                        </FunctionCode>
                    </FunctionCodes>
                    <RegisterMap>
                        <Device id="TemperatureController" unitId="1">
                            <HoldingRegisters>
                                <Register name="SetpointTemp" address="0">
                                    <DataType>uint16</DataType>
                                    <Access>RW</Access>
                                </Register>
                                <Register name="CurrentTemp" address="1">
                                    <DataType>uint16</DataType>
                                    <Access>R</Access>
                                </Register>
                                <Register name="MaxTemp" address="2">
                                    <DataType>uint16</DataType>
                                    <Access>RW</Access>
                                </Register>
                            </HoldingRegisters>
                        </Device>
                        <Device id="PumpController" unitId="2">
                            <HoldingRegisters>
                                <Register name="Speed" address="0">
                                    <DataType>uint16</DataType>
                                    <Access>RW</Access>
                                </Register>
                                <Register name="Pressure" address="1">
                                    <DataType>uint16</DataType>
                                    <Access>R</Access>
                                </Register>
                            </HoldingRegisters>
                        </Device>
                    </RegisterMap>
                </ModbusSpec>
                """;

        parser.load(new java.io.ByteArrayInputStream(minimalXml.getBytes()));
    }

    private static void showSpecificationInfo(ModbusSpecParser parser) {
        System.out.println("--- Specification Info ---");

        // Show function codes
        System.out.println("Function Codes:");
        for (FunctionCode fc : parser.getAllFunctionCodes()) {
            System.out.printf("  [%s] %s - %s%n",
                    fc.getCode(),
                    fc.getName(),
                    fc.getDescription());
        }
        System.out.println();

        // Show devices
        System.out.println("Devices:");
        for (Device device : parser.getAllDevices()) {
            System.out.printf("  %s (Unit ID: %d)%n", device.getId(), device.getUnitId());

            if (!device.getHoldingRegisters().isEmpty()) {
                System.out.println("    Holding Registers:");
                device.getHoldingRegisters().forEach(reg ->
                        System.out.printf("      [%d] %s (%s, %s)%n",
                                reg.getAddress(),
                                reg.getName(),
                                reg.getDataType(),
                                reg.getAccess())
                );
            }

            if (!device.getCoils().isEmpty()) {
                System.out.println("    Coils:");
                device.getCoils().forEach(reg ->
                        System.out.printf("      [%d] %s%n", reg.getAddress(), reg.getName())
                );
            }
        }
        System.out.println();
    }

    private static void demonstrateSimulatedOperations(ModbusSpecParser parser) {
        System.out.println("--- Simulated Operations (No Device Required) ---");

        // Use the default console handler (simulation mode)
        // This demonstrates how callFunction works with the spec

        // Call by function code
        System.out.println("Calling function code 3 (ReadHoldingRegisters):");
        ModbusResult result = parser.callFunction("3", 1, 0, 5);
        System.out.println("Success: " + result.isSuccess());
        System.out.println();

        // Call by function name
        System.out.println("Calling function by name (ReadCoils):");
        result = parser.callFunctionByName("ReadCoils", 1, 0, 16);
        System.out.println("Success: " + result.isSuccess());
        System.out.println();

        // Look up device info
        parser.getDevice("TemperatureController").ifPresent(device -> {
            System.out.println("Device lookup: " + device.getId());
            System.out.println("  Unit ID: " + device.getUnitId());
            System.out.println("  Registers: " + device.getHoldingRegisters().size());

            device.findHoldingRegisterByName("SetpointTemp").ifPresent(reg -> {
                System.out.println("  SetpointTemp register:");
                System.out.println("    Address: " + reg.getAddress());
                System.out.println("    Type: " + reg.getDataType());
                System.out.println("    Access: " + reg.getAccess());
                System.out.println("    Readable: " + reg.isReadable());
                System.out.println("    Writable: " + reg.isWritable());
            });
        });
        System.out.println();
    }

    private static void demonstrateRealDeviceOperations(ModbusSpecParser parser) {
        System.out.println("--- Real Device Operations ---");

        // Connect to a real device
        try (ModbusConnection connection = parser.connectTcp("localhost", 502)) {
            System.out.println("Connected to device");

            // Configure parser to use this connection
            parser.useConnection(connection);

            // Now callFunction will use the real connection
            System.out.println("Reading holding registers using spec-based call:");
            ModbusResult result = parser.callFunction("3", 1, 0, 5);
            System.out.println("Success: " + result.isSuccess());
            System.out.println("Message: " + result.getMessage());

            if (result.isSuccess() && result.getData() != null) {
                System.out.println("Data received: " + result.getData().length + " bytes");
            }

            // Or use direct methods for more control
            System.out.println();
            System.out.println("Direct register read:");
            Register[] registers = parser.readHoldingRegisters(connection, 1, 0, 5);
            for (int i = 0; i < registers.length; i++) {
                System.out.printf("  Register %d: %d%n", i, registers[i].getValue());
            }

            // Use spec info to read specific registers
            parser.getDevice("TemperatureController").ifPresent(device -> {
                device.findHoldingRegisterByName("SetpointTemp").ifPresent(reg -> {
                    try {
                        Register[] regs = parser.readHoldingRegisters(
                                connection,
                                device.getUnitId(),
                                reg.getAddress(),
                                1
                        );
                        System.out.printf("SetpointTemp value: %d%n", regs[0].getValue());
                    } catch (ModbusConnectionException e) {
                        System.err.println("Error reading register: " + e.getMessage());
                    }
                });
            });

        } catch (ModbusConnectionException e) {
            System.err.println("Connection error: " + e.getMessage());
            System.err.println("(This is expected if no Modbus simulator is running)");
        }
    }
}
