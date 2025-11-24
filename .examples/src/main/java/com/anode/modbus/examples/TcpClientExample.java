package com.anode.modbus.examples;

import com.anode.modbus.connection.ModbusConnection;
import com.anode.modbus.connection.ModbusConnectionException;
import com.anode.modbus.parsers.ModbusSpecParser;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;
import com.ghgande.j2mod.modbus.util.BitVector;

/**
 * Example demonstrating Modbus TCP client operations.
 *
 * <p>This example shows how to:</p>
 * <ul>
 *   <li>Connect to a Modbus TCP device</li>
 *   <li>Read holding registers</li>
 *   <li>Read input registers</li>
 *   <li>Read/write coils</li>
 *   <li>Write registers</li>
 * </ul>
 *
 * <p>To test this example, you need a Modbus TCP simulator or device.
 * Popular simulators include:</p>
 * <ul>
 *   <li>ModRSsim2 (Windows)</li>
 *   <li>diagslave (Linux/Windows)</li>
 *   <li>pyModbusTCP (Python)</li>
 * </ul>
 */
public class TcpClientExample {

    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 502;
    private static final int DEFAULT_UNIT_ID = 1;

    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : DEFAULT_HOST;
        int port = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_PORT;
        int unitId = args.length > 2 ? Integer.parseInt(args[2]) : DEFAULT_UNIT_ID;

        System.out.println("===========================================");
        System.out.println("    Modbus TCP Client Example");
        System.out.println("===========================================");
        System.out.println("Host: " + host);
        System.out.println("Port: " + port);
        System.out.println("Unit ID: " + unitId);
        System.out.println();

        ModbusSpecParser parser = new ModbusSpecParser();

        try (ModbusConnection connection = parser.connectTcp(host, port)) {
            System.out.println("Connected successfully!");
            System.out.println();

            // Example 1: Read Holding Registers
            readHoldingRegistersExample(parser, connection, unitId);

            // Example 2: Read Input Registers
            readInputRegistersExample(parser, connection, unitId);

            // Example 3: Read Coils
            readCoilsExample(parser, connection, unitId);

            // Example 4: Write Single Register
            writeSingleRegisterExample(parser, connection, unitId);

            // Example 5: Write Single Coil
            writeSingleCoilExample(parser, connection, unitId);

            // Example 6: Write Multiple Registers
            writeMultipleRegistersExample(parser, connection, unitId);

            System.out.println("All examples completed successfully!");

        } catch (ModbusConnectionException e) {
            System.err.println("Connection error: " + e.getMessage());
            System.err.println();
            System.err.println("Make sure a Modbus TCP slave/simulator is running at " + host + ":" + port);
            e.printStackTrace();
        }
    }

    private static void readHoldingRegistersExample(ModbusSpecParser parser,
                                                     ModbusConnection connection,
                                                     int unitId) throws ModbusConnectionException {
        System.out.println("--- Reading Holding Registers (FC 3) ---");

        int startAddress = 0;
        int quantity = 10;

        Register[] registers = parser.readHoldingRegisters(connection, unitId, startAddress, quantity);

        System.out.println("Read " + registers.length + " registers starting at address " + startAddress + ":");
        for (int i = 0; i < registers.length; i++) {
            int address = startAddress + i;
            int value = registers[i].getValue();
            System.out.printf("  Register %d: %d (0x%04X)%n", address, value, value);
        }
        System.out.println();
    }

    private static void readInputRegistersExample(ModbusSpecParser parser,
                                                   ModbusConnection connection,
                                                   int unitId) throws ModbusConnectionException {
        System.out.println("--- Reading Input Registers (FC 4) ---");

        int startAddress = 0;
        int quantity = 5;

        var registers = parser.readInputRegisters(connection, unitId, startAddress, quantity);

        System.out.println("Read " + registers.length + " input registers starting at address " + startAddress + ":");
        for (int i = 0; i < registers.length; i++) {
            int address = startAddress + i;
            int value = registers[i].getValue();
            System.out.printf("  Input Register %d: %d (0x%04X)%n", address, value, value);
        }
        System.out.println();
    }

    private static void readCoilsExample(ModbusSpecParser parser,
                                          ModbusConnection connection,
                                          int unitId) throws ModbusConnectionException {
        System.out.println("--- Reading Coils (FC 1) ---");

        int startAddress = 0;
        int quantity = 16;

        BitVector coils = parser.readCoils(connection, unitId, startAddress, quantity);

        System.out.println("Read " + quantity + " coils starting at address " + startAddress + ":");
        for (int i = 0; i < quantity; i++) {
            boolean state = coils.getBit(i);
            System.out.printf("  Coil %d: %s%n", startAddress + i, state ? "ON" : "OFF");
        }
        System.out.println();
    }

    private static void writeSingleRegisterExample(ModbusSpecParser parser,
                                                    ModbusConnection connection,
                                                    int unitId) throws ModbusConnectionException {
        System.out.println("--- Writing Single Register (FC 6) ---");

        int address = 0;
        int value = 12345;

        Register register = new SimpleRegister(value);
        parser.writeSingleRegister(connection, unitId, address, register);

        System.out.printf("Wrote value %d to register %d%n", value, address);

        // Verify by reading back
        Register[] readBack = parser.readHoldingRegisters(connection, unitId, address, 1);
        System.out.printf("Read back: %d%n", readBack[0].getValue());
        System.out.println();
    }

    private static void writeSingleCoilExample(ModbusSpecParser parser,
                                                ModbusConnection connection,
                                                int unitId) throws ModbusConnectionException {
        System.out.println("--- Writing Single Coil (FC 5) ---");

        int address = 0;

        // Turn ON
        parser.writeSingleCoil(connection, unitId, address, true);
        System.out.printf("Set coil %d to ON%n", address);

        // Verify
        BitVector coils = parser.readCoils(connection, unitId, address, 1);
        System.out.printf("Read back: %s%n", coils.getBit(0) ? "ON" : "OFF");

        // Turn OFF
        parser.writeSingleCoil(connection, unitId, address, false);
        System.out.printf("Set coil %d to OFF%n", address);

        // Verify
        coils = parser.readCoils(connection, unitId, address, 1);
        System.out.printf("Read back: %s%n", coils.getBit(0) ? "ON" : "OFF");
        System.out.println();
    }

    private static void writeMultipleRegistersExample(ModbusSpecParser parser,
                                                       ModbusConnection connection,
                                                       int unitId) throws ModbusConnectionException {
        System.out.println("--- Writing Multiple Registers (FC 16) ---");

        int startAddress = 10;
        Register[] registers = {
                new SimpleRegister(100),
                new SimpleRegister(200),
                new SimpleRegister(300),
                new SimpleRegister(400),
                new SimpleRegister(500)
        };

        int count = parser.writeMultipleRegisters(connection, unitId, startAddress, registers);
        System.out.printf("Wrote %d registers starting at address %d%n", count, startAddress);

        // Verify by reading back
        Register[] readBack = parser.readHoldingRegisters(connection, unitId, startAddress, registers.length);
        System.out.println("Read back values:");
        for (int i = 0; i < readBack.length; i++) {
            System.out.printf("  Register %d: %d%n", startAddress + i, readBack[i].getValue());
        }
        System.out.println();
    }
}
