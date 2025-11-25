package com.anode.modbus.parser;

import com.anode.modbus.model.Accessor;
import com.anode.modbus.model.Device;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class AccessorParsingTest {

    @Test
    void testParseAccessorsFromXml() throws ModbusParseException {
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <ModbusSpec>
                <RegisterMap>
                    <Device id="TestDevice" unitId="1">
                        <Accessors>
                            <Accessor name="getTemperature1">
                                <Function>ReadHoldingRegisters</Function>
                                <DataClass>java.lang.Integer</DataClass>
                                <AddressRange>39</AddressRange>
                            </Accessor>
                            <Accessor name="getAllTemperatures">
                                <Function>ReadHoldingRegisters</Function>
                                <DataClass>java.lang.Integer[]</DataClass>
                                <AddressRange>39-42</AddressRange>
                            </Accessor>
                            <Accessor name="setValvePositions">
                                <Function>WriteMultipleRegisters</Function>
                                <DataClass>java.lang.Integer[]</DataClass>
                                <AddressRange>70-85</AddressRange>
                            </Accessor>
                        </Accessors>
                        <HoldingRegisters>
                            <Register name="Temperature1" address="39">
                                <DataType>int16</DataType>
                                <Access>R</Access>
                            </Register>
                        </HoldingRegisters>
                    </Device>
                </RegisterMap>
            </ModbusSpec>
            """;

        ModbusXmlParser parser = new ModbusXmlParser();
        ModbusXmlParser.ParseResult result = parser.parse(
                new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))
        );

        assertNotNull(result);
        assertEquals(1, result.getDevices().size());

        Device device = result.getDevices().get(0);
        assertEquals("TestDevice", device.getId());
        assertEquals(3, device.getAccessors().size());

        // Test first accessor
        Accessor accessor1 = device.getAccessors().get(0);
        assertEquals("getTemperature1", accessor1.getName());
        assertEquals("ReadHoldingRegisters", accessor1.getFunction());
        assertEquals("java.lang.Integer", accessor1.getDataClass());
        assertEquals(39, accessor1.getStartAddress());
        assertEquals(39, accessor1.getEndAddress());
        assertEquals(1, accessor1.getRegisterCount());

        // Test second accessor
        Accessor accessor2 = device.getAccessors().get(1);
        assertEquals("getAllTemperatures", accessor2.getName());
        assertEquals("ReadHoldingRegisters", accessor2.getFunction());
        assertEquals("java.lang.Integer[]", accessor2.getDataClass());
        assertEquals(39, accessor2.getStartAddress());
        assertEquals(42, accessor2.getEndAddress());
        assertEquals(4, accessor2.getRegisterCount());

        // Test third accessor
        Accessor accessor3 = device.getAccessors().get(2);
        assertEquals("setValvePositions", accessor3.getName());
        assertEquals("WriteMultipleRegisters", accessor3.getFunction());
        assertEquals("java.lang.Integer[]", accessor3.getDataClass());
        assertEquals(70, accessor3.getStartAddress());
        assertEquals(85, accessor3.getEndAddress());
        assertEquals(16, accessor3.getRegisterCount());
    }

    @Test
    void testFindAccessorByName() throws ModbusParseException {
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <ModbusSpec>
                <RegisterMap>
                    <Device id="TestDevice" unitId="1">
                        <Accessors>
                            <Accessor name="getTemperature">
                                <Function>ReadHoldingRegisters</Function>
                                <DataClass>java.lang.Integer</DataClass>
                                <AddressRange>39</AddressRange>
                            </Accessor>
                        </Accessors>
                    </Device>
                </RegisterMap>
            </ModbusSpec>
            """;

        ModbusXmlParser parser = new ModbusXmlParser();
        ModbusXmlParser.ParseResult result = parser.parse(
                new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))
        );

        Device device = result.getDevices().get(0);
        assertTrue(device.findAccessorByName("getTemperature").isPresent());
        assertFalse(device.findAccessorByName("nonExistent").isPresent());

        Accessor accessor = device.findAccessorByName("getTemperature").get();
        assertEquals("getTemperature", accessor.getName());
    }

    @Test
    void testDeviceWithoutAccessors() throws ModbusParseException {
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <ModbusSpec>
                <RegisterMap>
                    <Device id="TestDevice" unitId="1">
                        <HoldingRegisters>
                            <Register name="Temperature1" address="39">
                                <DataType>int16</DataType>
                                <Access>R</Access>
                            </Register>
                        </HoldingRegisters>
                    </Device>
                </RegisterMap>
            </ModbusSpec>
            """;

        ModbusXmlParser parser = new ModbusXmlParser();
        ModbusXmlParser.ParseResult result = parser.parse(
                new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))
        );

        Device device = result.getDevices().get(0);
        assertNotNull(device.getAccessors());
        assertEquals(0, device.getAccessors().size());
    }
}
