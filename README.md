# ModbusParser

A Java library for parsing Modbus specifications and facilitating communication with Modbus devices. Define your device configurations in XML and interact with them programmatically.

## Features

- **Simple fluent API** - `parser.function("ReadHoldingRegisters").read()`
- XML-based Modbus device and register specification parsing
- **Connection configuration in XML** - define host, port, baud rate, and all connection parameters in your spec file
- Support for TCP and Serial (RTU/ASCII) connections
- All standard Modbus function codes (FC 1-6, 15-16)
- Auto-connect using XML configuration
- Built on [j2mod](https://github.com/steveohara/j2mod) for protocol communication

## Requirements

- Java 17 or higher
- Maven 3.6+

## Installation

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.anode</groupId>
    <artifactId>modbus-parser</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

## Quick Start

### 1. Define Your Modbus Specification

Create an XML file describing your connection, devices, and registers:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<ModbusSpec>
    <!-- Connection configuration -->
    <Connection type="TCP">
        <Host>192.168.1.100</Host>
        <Port>502</Port>
        <Timeout>3000</Timeout>
        <Reconnect>true</Reconnect>
    </Connection>

    <FunctionCodes>
        <FunctionCode code="3" name="ReadHoldingRegisters">
            <Description>Read holding register values (FC 03)</Description>
        </FunctionCode>
        <FunctionCode code="6" name="WriteSingleRegister">
            <Description>Write single register value (FC 06)</Description>
        </FunctionCode>
    </FunctionCodes>

    <RegisterMap>
        <Device id="TemperatureSensor" unitId="1">
            <HoldingRegisters>
                <Register name="SetpointTemp" address="0">
                    <DataType>uint16</DataType>
                    <Access>RW</Access>
                </Register>
                <Register name="CurrentTemp" address="1">
                    <DataType>int16</DataType>
                    <Access>R</Access>
                </Register>
            </HoldingRegisters>
            <InputRegisters>
                <Register name="AmbientTemp" address="0">
                    <DataType>int16</DataType>
                    <Access>R</Access>
                </Register>
            </InputRegisters>
            <Coils>
                <Register name="HeaterOn" address="0">
                    <DataType>bool</DataType>
                    <Access>RW</Access>
                </Register>
            </Coils>
            <DiscreteInputs>
                <Register name="OverheatAlarm" address="0">
                    <DataType>bool</DataType>
                    <Access>R</Access>
                </Register>
            </DiscreteInputs>
        </Device>
    </RegisterMap>
</ModbusSpec>
```

### 2. Use the Simple Fluent API

```java
import com.anode.modbus.parsers.ModbusSpecParser;

// Create parser and load specification
ModbusSpecParser parser = new ModbusSpecParser();
parser.load(new File("modbus-spec.xml"));

// Read holding registers (auto-connects using XML config)
int[] values = parser.function("ReadHoldingRegisters")
    .unitId(1)
    .address(0)
    .quantity(10)
    .read();

// Write a single register
parser.function("WriteSingleRegister")
    .unitId(1)
    .address(0)
    .write(1234);

// Read coils as booleans
boolean[] coils = parser.function("ReadCoils")
    .unitId(1)
    .address(0)
    .quantity(8)
    .readBooleans();

// Write multiple registers
parser.function("WriteMultipleRegisters")
    .unitId(1)
    .address(0)
    .write(new int[]{100, 200, 300});

// Don't forget to close when done
parser.close();
```

### 3. Using Try-With-Resources

```java
try (ModbusSpecParser parser = new ModbusSpecParser()) {
    parser.load(new File("modbus-spec.xml"));

    // Read a single value
    int temperature = parser.function("ReadHoldingRegisters")
        .unitId(1)
        .address(0)
        .readSingle();

    System.out.println("Temperature: " + temperature);
} // Connection automatically closed
```

## Connection Configuration

The connection can be configured directly in the XML specification file. This allows you to keep all device configuration in one place.

### TCP Connection Configuration

```xml
<Connection type="TCP">
    <Host>192.168.1.100</Host>
    <Port>502</Port>
    <Timeout>3000</Timeout>
    <Reconnect>true</Reconnect>
</Connection>
```

| Element | Description | Default |
|---------|-------------|---------|
| `type` | Connection type: `TCP`, `RTU`, or `ASCII` | `TCP` |
| `Host` | IP address or hostname | `localhost` |
| `Port` | TCP port number | `502` |
| `Timeout` | Connection timeout in milliseconds | `3000` |
| `Reconnect` | Auto-reconnect on connection loss | `true` |

### Serial RTU/ASCII Connection Configuration

```xml
<Connection type="RTU">
    <PortName>/dev/ttyUSB0</PortName>
    <BaudRate>9600</BaudRate>
    <DataBits>8</DataBits>
    <StopBits>1</StopBits>
    <Parity>none</Parity>
</Connection>
```

| Element | Description | Default |
|---------|-------------|---------|
| `type` | `RTU` for Modbus RTU, `ASCII` for Modbus ASCII | - |
| `PortName` | Serial port (e.g., `/dev/ttyUSB0`, `COM1`) | - |
| `BaudRate` | Baud rate (e.g., 9600, 19200, 115200) | `9600` |
| `DataBits` | Number of data bits | `8` |
| `StopBits` | Number of stop bits | `1` |
| `Parity` | Parity: `none`, `odd`, or `even` | `none` |

### Using Connection from XML

```java
ModbusSpecParser parser = new ModbusSpecParser();
parser.load(new File("modbus-spec.xml"));

// Connect using settings from XML
try (ModbusConnection conn = parser.connect()) {
    // Connection type and parameters are read from the XML
    Register[] registers = parser.readHoldingRegisters(conn, 1, 0, 10);
}

// You can also check if connection config exists
if (parser.hasConnectionConfig()) {
    ConnectionConfig config = parser.getConnectionConfig();
    System.out.println("Connection type: " + config.getType());
}
```

## Usage Examples

### TCP Connection (Manual/Override)

You can also specify connection parameters programmatically to override the XML configuration:

```java
ModbusSpecParser parser = new ModbusSpecParser();

try (ModbusConnection conn = parser.connectTcp("localhost", 502)) {
    // Read 10 holding registers starting at address 0
    Register[] registers = parser.readHoldingRegisters(conn, 1, 0, 10);

    for (Register reg : registers) {
        System.out.println("Value: " + reg.getValue());
    }

    // Write a single register
    parser.writeSingleRegister(conn, 1, 0, 1234);

    // Write multiple registers
    int[] values = {100, 200, 300};
    parser.writeMultipleRegisters(conn, 1, 0, values);
}
```

### Serial RTU Connection

```java
ModbusSpecParser parser = new ModbusSpecParser();

try (ModbusConnection conn = parser.connectSerial("/dev/ttyUSB0", 9600)) {
    // Read 8 coils starting at address 0
    BitVector coils = parser.readCoils(conn, 1, 0, 8);

    for (int i = 0; i < 8; i++) {
        System.out.println("Coil " + i + ": " + (coils.getBit(i) ? "ON" : "OFF"));
    }

    // Write a single coil
    parser.writeSingleCoil(conn, 1, 0, true);
}
```

### Simulated Mode (No Hardware)

```java
ModbusSpecParser parser = new ModbusSpecParser();
parser.load(new File("modbus-spec.xml"));

// Without a connection, uses console logging handler
ModbusService.ModbusResult result = parser.callFunction("3", 1, 0, 10);

System.out.println("Success: " + result.isSuccess());
System.out.println("Message: " + result.getMessage());
```

### Custom Operation Handler

```java
parser.setOperationHandler(request -> {
    // Custom implementation - mock responses, logging, etc.
    System.out.println("Executing: " + request);
    return ModbusService.ModbusResult.success("Custom operation completed");
});
```

## Supported Function Codes

| Code | Name | Description |
|------|------|-------------|
| 01 | Read Coils | Read discrete output coils |
| 02 | Read Discrete Inputs | Read discrete input contacts |
| 03 | Read Holding Registers | Read analog output holding registers |
| 04 | Read Input Registers | Read analog input registers |
| 05 | Write Single Coil | Write single discrete output |
| 06 | Write Single Register | Write single analog output |
| 15 | Write Multiple Coils | Write multiple discrete outputs |
| 16 | Write Multiple Registers | Write multiple analog outputs |

## Architecture

```
ModbusParser/
├── model/           # Domain objects (Device, Register, FunctionCode)
├── parser/          # XML parsing (ModbusXmlParser)
├── repository/      # In-memory storage (ModbusSpecRepository)
├── connection/      # Protocol abstraction (TCP/Serial)
├── service/         # Business logic (ModbusService)
└── parsers/         # Public API facade (ModbusSpecParser)
```

### Key Components

- **ModbusSpecParser**: Main entry point and facade for all operations
- **Device**: Immutable representation of a Modbus device with its registers
- **Register**: Metadata for a single register/coil (name, address, type, access)
- **ModbusConnection**: Interface for TCP and Serial connections
- **ModbusService**: Core business logic for Modbus operations

## Building

```bash
# Compile
mvn compile

# Run tests
mvn test

# Package
mvn package

# Install to local repository
mvn install
```

## API Reference

### ModbusSpecParser

```java
// Loading specifications
void load(File xmlFile)
void load(InputStream inputStream)

// Simple Fluent API (recommended)
ModbusOperation function(String name)       // Get operation by function name
void close()                                // Close connection
boolean isConnected()                       // Check connection status

// Connection configuration
ConnectionConfig getConnectionConfig()      // Get config from XML
boolean hasConnectionConfig()               // Check if config exists

// Manual connections (optional - auto-connect is default)
ModbusConnection connect()                  // Connect using XML config
ModbusConnection connectTcp(String host, int port)
ModbusConnection connectSerial(String portName, int baudRate)

// Lookups
Optional<FunctionCode> getFunctionCode(String code)
Optional<Device> getDevice(String deviceId)
Optional<Device> getDeviceByUnitId(int unitId)
List<Device> getAllDevices()
```

### ModbusOperation (Fluent API)

```java
// Configuration (chainable)
ModbusOperation unitId(int unitId)          // Set unit/slave ID (default: 1)
ModbusOperation address(int address)        // Set starting address (default: 0)
ModbusOperation quantity(int quantity)      // Set quantity to read (default: 1)

// Read operations
int[] read()                                // Read values as int array
int readSingle()                            // Read single value
boolean[] readBooleans()                    // Read coils as boolean array

// Write operations
void write(int value)                       // Write single value
void write(boolean value)                   // Write single coil
void write(int[] values)                    // Write multiple values
void write(boolean[] values)                // Write multiple coils
```

### Device

```java
// Getters
String getId()
int getUnitId()
List<Register> getHoldingRegisters()
List<Register> getInputRegisters()
List<Register> getCoils()
List<Register> getDiscreteInputs()

// Lookups
Optional<Register> findHoldingRegisterByName(String name)
Optional<Register> findHoldingRegisterByAddress(int address)
// Similar methods for other register types...
```

## License

[Add your license here]

## Contributing

[Add contribution guidelines here]
