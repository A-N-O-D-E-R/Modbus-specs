package com.anode.modbus.parsers;

import com.anode.modbus.parser.ModbusParseException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ModbusSpecParser accessor validation and edge cases.
 */
class ModbusSpecParserValidationTest {

    @Test
    void testAccessorWithNullName() {
        ModbusSpecParser parser = new ModbusSpecParser();

        assertThrows(IllegalArgumentException.class, () -> {
            parser.accessor(null);
        });
    }

    @Test
    void testAccessorWithEmptyName() {
        ModbusSpecParser parser = new ModbusSpecParser();

        assertThrows(IllegalArgumentException.class, () -> {
            parser.accessor("   ");
        });
    }

    @Test
    void testAccessorWithEmptyDeviceId() {
        ModbusSpecParser parser = new ModbusSpecParser();

        assertThrows(IllegalArgumentException.class, () -> {
            parser.accessor("test", "  ");
        });
    }

    @Test
    void testAccessorNotFound() {
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <ModbusSpec>
                <RegisterMap>
                    <Device id="TestDevice" unitId="1">
                        <Accessors>
                            <Accessor name="existing">
                                <Function>ReadHoldingRegisters</Function>
                                <DataClass>java.lang.Integer</DataClass>
                                <AddressRange>0</AddressRange>
                            </Accessor>
                        </Accessors>
                    </Device>
                </RegisterMap>
            </ModbusSpec>
            """;

        ModbusSpecParser parser = new ModbusSpecParser();
        try {
            parser.load(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        } catch (ModbusParseException e) {
            fail("Failed to parse XML: " + e.getMessage());
        }

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            parser.accessor("nonExistent");
        });

        assertTrue(ex.getMessage().contains("Accessor not found"));
        assertTrue(ex.getMessage().contains("nonExistent"));
    }

    @Test
    void testAccessorNotFoundInSpecifiedDevice() {
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <ModbusSpec>
                <RegisterMap>
                    <Device id="Device1" unitId="1">
                        <Accessors>
                            <Accessor name="temp">
                                <Function>ReadHoldingRegisters</Function>
                                <DataClass>java.lang.Integer</DataClass>
                                <AddressRange>0</AddressRange>
                            </Accessor>
                        </Accessors>
                    </Device>
                    <Device id="Device2" unitId="2">
                        <Accessors>
                            <Accessor name="pressure">
                                <Function>ReadHoldingRegisters</Function>
                                <DataClass>java.lang.Integer</DataClass>
                                <AddressRange>10</AddressRange>
                            </Accessor>
                        </Accessors>
                    </Device>
                </RegisterMap>
            </ModbusSpec>
            """;

        ModbusSpecParser parser = new ModbusSpecParser();
        try {
            parser.load(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        } catch (ModbusParseException e) {
            fail("Failed to parse XML: " + e.getMessage());
        }

        // pressure exists in Device2, but not in Device1
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            parser.accessor("pressure", "Device1");
        });

        assertTrue(ex.getMessage().contains("Accessor not found"));
        assertTrue(ex.getMessage().contains("pressure"));
        assertTrue(ex.getMessage().contains("Device1"));
    }

    @Test
    void testAmbiguousAccessorName() {
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <ModbusSpec>
                <RegisterMap>
                    <Device id="Device1" unitId="1">
                        <Accessors>
                            <Accessor name="getTemp">
                                <Function>ReadHoldingRegisters</Function>
                                <DataClass>java.lang.Integer</DataClass>
                                <AddressRange>0</AddressRange>
                            </Accessor>
                        </Accessors>
                    </Device>
                    <Device id="Device2" unitId="2">
                        <Accessors>
                            <Accessor name="getTemp">
                                <Function>ReadHoldingRegisters</Function>
                                <DataClass>java.lang.Integer</DataClass>
                                <AddressRange>10</AddressRange>
                            </Accessor>
                        </Accessors>
                    </Device>
                </RegisterMap>
            </ModbusSpec>
            """;

        ModbusSpecParser parser = new ModbusSpecParser();
        try {
            parser.load(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        } catch (ModbusParseException e) {
            fail("Failed to parse XML: " + e.getMessage());
        }

        // Should throw because same accessor name exists in multiple devices
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            parser.accessor("getTemp");
        });

        assertTrue(ex.getMessage().contains("Ambiguous"));
        assertTrue(ex.getMessage().contains("getTemp"));
        assertTrue(ex.getMessage().contains("Device1"));
        assertTrue(ex.getMessage().contains("Device2"));
        assertTrue(ex.getMessage().contains("specify deviceId"));
    }

    @Test
    void testAmbiguousAccessorResolvedWithDeviceId() {
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <ModbusSpec>
                <RegisterMap>
                    <Device id="Device1" unitId="1">
                        <Accessors>
                            <Accessor name="getTemp">
                                <Function>ReadHoldingRegisters</Function>
                                <DataClass>java.lang.Integer</DataClass>
                                <AddressRange>0</AddressRange>
                            </Accessor>
                        </Accessors>
                    </Device>
                    <Device id="Device2" unitId="2">
                        <Accessors>
                            <Accessor name="getTemp">
                                <Function>ReadHoldingRegisters</Function>
                                <DataClass>java.lang.Integer</DataClass>
                                <AddressRange>10</AddressRange>
                            </Accessor>
                        </Accessors>
                    </Device>
                </RegisterMap>
            </ModbusSpec>
            """;

        ModbusSpecParser parser = new ModbusSpecParser();
        try {
            parser.load(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        } catch (ModbusParseException e) {
            fail("Failed to parse XML: " + e.getMessage());
        }

        // Should work when deviceId is specified
        var op1 = parser.accessor("getTemp", "Device1");
        assertEquals(0, op1.getAccessor().getStartAddress());

        var op2 = parser.accessor("getTemp", "Device2");
        assertEquals(10, op2.getAccessor().getStartAddress());
    }

    @Test
    void testDeviceNotFound() {
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <ModbusSpec>
                <RegisterMap>
                    <Device id="Device1" unitId="1">
                        <Accessors></Accessors>
                    </Device>
                </RegisterMap>
            </ModbusSpec>
            """;

        ModbusSpecParser parser = new ModbusSpecParser();
        try {
            parser.load(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        } catch (ModbusParseException e) {
            fail("Failed to parse XML: " + e.getMessage());
        }

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            parser.accessor("test", "NonExistentDevice");
        });

        assertTrue(ex.getMessage().contains("Device not found"));
        assertTrue(ex.getMessage().contains("NonExistentDevice"));
    }
}
