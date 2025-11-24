package com.anode.modbus.examples;

import com.anode.modbus.connection.ModbusConnection;
import com.anode.modbus.connection.ModbusConnectionException;
import com.anode.modbus.connection.ModbusSerialConnection;
import com.anode.modbus.parsers.ModbusSpecParser;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.util.BitVector;
import com.ghgande.j2mod.modbus.util.SerialParameters;

/**
 * Example demonstrating Modbus Serial RTU client operations.
 *
 * <p>This example shows how to:</p>
 * <ul>
 *   <li>Connect to a Modbus RTU device via serial port</li>
 *   <li>Configure serial parameters (baud rate, parity, etc.)</li>
 *   <li>Read and write registers</li>
 * </ul>
 *
 * <p>Common serial port names:</p>
 * <ul>
 *   <li>Linux: /dev/ttyUSB0, /dev/ttyS0, /dev/ttyACM0</li>
 *   <li>Windows: COM1, COM2, COM3, etc.</li>
 *   <li>macOS: /dev/tty.usbserial-*, /dev/cu.usbserial-*</li>
 * </ul>
 */
public class SerialClientExample {

    private static final String DEFAULT_PORT = "/dev/ttyUSB0";
    private static final int DEFAULT_BAUD_RATE = 9600;
    private static final int DEFAULT_UNIT_ID = 1;

    public static void main(String[] args) {
        String portName = args.length > 0 ? args[0] : DEFAULT_PORT;
        int baudRate = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_BAUD_RATE;
        int unitId = args.length > 2 ? Integer.parseInt(args[2]) : DEFAULT_UNIT_ID;

        System.out.println("===========================================");
        System.out.println("    Modbus Serial RTU Client Example");
        System.out.println("===========================================");
        System.out.println("Port: " + portName);
        System.out.println("Baud Rate: " + baudRate);
        System.out.println("Unit ID: " + unitId);
        System.out.println();

        // Example 1: Simple connection with defaults
        simpleConnectionExample(portName, baudRate, unitId);

        // Example 2: Custom serial parameters
        // customParametersExample(portName, unitId);
    }

    /**
     * Simple connection using ModbusSpecParser convenience methods.
     */
    private static void simpleConnectionExample(String portName, int baudRate, int unitId) {
        System.out.println("--- Simple Connection Example ---");

        ModbusSpecParser parser = new ModbusSpecParser();

        try (ModbusConnection connection = parser.connectSerial(portName, baudRate)) {
            System.out.println("Connected successfully!");
            System.out.println();

            // Read holding registers
            System.out.println("Reading Holding Registers (FC 3):");
            Register[] registers = parser.readHoldingRegisters(connection, unitId, 0, 5);
            for (int i = 0; i < registers.length; i++) {
                System.out.printf("  Register %d: %d%n", i, registers[i].getValue());
            }
            System.out.println();

            // Read coils
            System.out.println("Reading Coils (FC 1):");
            BitVector coils = parser.readCoils(connection, unitId, 0, 8);
            for (int i = 0; i < 8; i++) {
                System.out.printf("  Coil %d: %s%n", i, coils.getBit(i) ? "ON" : "OFF");
            }
            System.out.println();

            // Read discrete inputs
            System.out.println("Reading Discrete Inputs (FC 2):");
            BitVector inputs = parser.readDiscreteInputs(connection, unitId, 0, 8);
            for (int i = 0; i < 8; i++) {
                System.out.printf("  Input %d: %s%n", i, inputs.getBit(i) ? "ON" : "OFF");
            }

        } catch (ModbusConnectionException e) {
            System.err.println("Connection error: " + e.getMessage());
            System.err.println();
            System.err.println("Troubleshooting tips:");
            System.err.println("  1. Check that the serial port exists: ls -la " + portName);
            System.err.println("  2. Check permissions: sudo chmod 666 " + portName);
            System.err.println("  3. Add user to dialout group: sudo usermod -a -G dialout $USER");
            System.err.println("  4. Verify baud rate and other settings match the device");
            e.printStackTrace();
        }
    }

    /**
     * Example with custom serial parameters for special configurations.
     */
    private static void customParametersExample(String portName, int unitId) {
        System.out.println("--- Custom Parameters Example ---");

        // Create custom serial parameters
        SerialParameters params = new SerialParameters();
        params.setPortName(portName);
        params.setBaudRate(19200);          // Higher baud rate
        params.setDatabits(8);              // 8 data bits
        params.setStopbits(1);              // 1 stop bit
        params.setParity(0);                // No parity (0=None, 1=Odd, 2=Even)
        params.setEncoding("rtu");          // RTU encoding (or "ascii" for ASCII mode)
        params.setEcho(false);              // No echo
        params.setFlowControlIn(0);         // No flow control
        params.setFlowControlOut(0);

        // Create connection with custom parameters
        try (ModbusSerialConnection connection = new ModbusSerialConnection(params)) {
            connection.connect();
            System.out.println("Connected with custom parameters!");

            // Create parser and use connection
            ModbusSpecParser parser = new ModbusSpecParser();

            // Read some data
            Register[] registers = parser.readHoldingRegisters(connection, unitId, 0, 10);
            System.out.println("Read " + registers.length + " registers");

            for (int i = 0; i < registers.length; i++) {
                System.out.printf("  Register %d: %d%n", i, registers[i].getValue());
            }

        } catch (ModbusConnectionException e) {
            System.err.println("Connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Lists available serial ports (platform dependent).
     */
    public static void listSerialPorts() {
        System.out.println("Available serial ports:");

        // On Linux, list /dev/tty* devices
        java.io.File devDir = new java.io.File("/dev");
        if (devDir.exists()) {
            String[] ports = devDir.list((dir, name) ->
                    name.startsWith("ttyUSB") ||
                            name.startsWith("ttyACM") ||
                            name.startsWith("ttyS")
            );
            if (ports != null) {
                for (String port : ports) {
                    System.out.println("  /dev/" + port);
                }
            }
        }

        // Note: For a more robust solution, use jSerialComm's SerialPort.getCommPorts()
    }
}
