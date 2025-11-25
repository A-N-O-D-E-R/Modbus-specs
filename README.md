# ModbusParser

XML-based Modbus specification parser for Java. Define devices, registers, and accessors in XML, then interact with them programmatically.

## Requirements

- Java 17+
- Maven 3.6+

## Quick Start

### 1. Define specification in XML

```xml
<?xml version="1.0" encoding="UTF-8"?>
<ModbusSpec>
  <Connection type="TCP">
    <Host>192.168.1.100</Host>
    <Port>502</Port>
  </Connection>

  <RegisterMap>
    <Device id="Sensor" unitId="1">
      <!-- High-level accessors -->
      <Accessors>
        <Accessor name="getTemperature">
          <Function>ReadHoldingRegisters</Function>
          <DataClass>java.lang.Integer</DataClass>
          <AddressRange>0</AddressRange>
        </Accessor>
      </Accessors>

      <!-- Register definitions -->
      <HoldingRegisters>
        <Register name="Temperature" address="0">
          <DataType>int16</DataType>
          <Access>R</Access>
        </Register>
      </HoldingRegisters>
    </Device>
  </RegisterMap>
</ModbusSpec>
```

### 2. Use in Java

```java
// Parse spec
ModbusSpecParser parser = new ModbusSpecParser();
parser.load(new File("spec.xml"));

// ===== Accessor API (Recommended) =====

// Read single value using accessor
int temp = parser.accessor("getTemperature")
    .unitId(1)
    .readSingle();

// Read multiple values using accessor
int[] temps = parser.accessor("getAllTemperatures")
    .unitId(1)
    .read();

// Write using accessor
parser.accessor("setValvePositions")
    .unitId(1)
    .write(new int[]{1, 0, 1, 0});

// ===== Function API (Low-level) =====

// Read registers by function
int[] values = parser.function("ReadHoldingRegisters")
    .unitId(1)
    .address(0)
    .quantity(10)
    .read();

// Write single register
parser.function("WriteSingleRegister")
    .unitId(1)
    .address(0)
    .write(1234);

parser.close();
```

## Accessors

Accessors provide high-level abstractions over register addresses. They encapsulate the function, data type, and address range, making your code cleaner and more maintainable.

### Define in XML

```xml
<Accessors>
  <!-- Single register -->
  <Accessor name="getTemperature1">
    <Function>ReadHoldingRegisters</Function>
    <DataClass>java.lang.Integer</DataClass>
    <AddressRange>39</AddressRange>
  </Accessor>

  <!-- Multiple registers -->
  <Accessor name="getAllTemperatures">
    <Function>ReadHoldingRegisters</Function>
    <DataClass>java.lang.Integer[]</DataClass>
    <AddressRange>39-42</AddressRange>
  </Accessor>

  <!-- Write accessor -->
  <Accessor name="setValvePositions">
    <Function>WriteMultipleRegisters</Function>
    <DataClass>java.lang.Integer[]</DataClass>
    <AddressRange>70-85</AddressRange>
  </Accessor>
</Accessors>
```

### Use in Code

```java
// Read single value
int temp1 = parser.accessor("getTemperature1").unitId(1).readSingle();

// Read multiple values
int[] allTemps = parser.accessor("getAllTemperatures").unitId(1).read();

// Write values
parser.accessor("setValvePositions").unitId(1).write(new int[]{1, 0, 1, 0});

// Access from specific device (when multiple devices have same accessor name)
int temp = parser.accessor("getTemperature", "Device1").unitId(1).readSingle();
```

### Benefits

- **Abstraction**: Hide register addresses behind meaningful names
- **Maintainability**: Change addresses in XML without touching code
- **Self-documenting**: Code reads like `getTemperature()` instead of `readRegister(39)`
- **Type-safe**: Data type is defined in XML spec

## Supported Function Codes

| Code | Function |
|------|----------|
| 01 | Read Coils |
| 02 | Read Discrete Inputs |
| 03 | Read Holding Registers |
| 04 | Read Input Registers |
| 05 | Write Single Coil |
| 06 | Write Single Register |
| 15 | Write Multiple Coils |
| 16 | Write Multiple Registers |

## Connection Types

**TCP:**
```xml
<Connection type="TCP">
  <Host>192.168.1.100</Host>
  <Port>502</Port>
</Connection>
```

**Serial RTU:**
```xml
<Connection type="RTU">
  <PortName>/dev/ttyUSB0</PortName>
  <BaudRate>9600</BaudRate>
</Connection>
```

## Building

```bash
mvn clean install
```

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md)

## License

MIT License - see [LICENSE](LICENSE)
