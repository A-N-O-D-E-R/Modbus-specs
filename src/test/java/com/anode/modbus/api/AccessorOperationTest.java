package com.anode.modbus.api;

import com.anode.modbus.model.Accessor;
import com.anode.modbus.parsers.ModbusSpecParser;
import com.anode.modbus.parser.ModbusParseException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class AccessorOperationTest {

    @Test
    void testAccessorApiWithReadOperation() throws ModbusParseException {
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <ModbusSpec>
                <Connection type="TCP">
                    <Host>localhost</Host>
                    <Port>502</Port>
                </Connection>
                <FunctionCodes>
                    <FunctionCode code="3" name="ReadHoldingRegisters">
                        <Description>Read holding registers</Description>
                    </FunctionCode>
                </FunctionCodes>
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
                        </Accessors>
                    </Device>
                </RegisterMap>
            </ModbusSpec>
            """;

        ModbusSpecParser parser = new ModbusSpecParser();
        parser.load(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

        // Test accessor retrieval
        AccessorOperation op = parser.accessor("getTemperature1");
        assertNotNull(op);
        assertEquals("getTemperature1", op.getAccessor().getName());
        assertEquals(39, op.getAccessor().getStartAddress());

        // Test accessor with multiple registers
        AccessorOperation op2 = parser.accessor("getAllTemperatures");
        assertNotNull(op2);
        assertEquals(4, op2.getAccessor().getRegisterCount());
    }

    @Test
    void testAccessorNotFound() throws ModbusParseException {
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
        parser.load(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

        assertThrows(IllegalArgumentException.class, () -> {
            parser.accessor("nonExistent");
        });
    }

    @Test
    void testAccessorWithDeviceId() throws ModbusParseException {
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
        parser.load(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

        // Test accessor from Device1
        AccessorOperation op1 = parser.accessor("getTemp", "Device1");
        assertEquals(0, op1.getAccessor().getStartAddress());

        // Test accessor from Device2
        AccessorOperation op2 = parser.accessor("getTemp", "Device2");
        assertEquals(10, op2.getAccessor().getStartAddress());
    }

    @Test
    void testAccessorProperties() {
        Accessor accessor = Accessor.builder()
                .name("testAccessor")
                .function("ReadHoldingRegisters")
                .dataClass("java.lang.Integer")
                .addressRange("5-10")
                .build();

        assertEquals("testAccessor", accessor.getName());
        assertEquals("ReadHoldingRegisters", accessor.getFunction());
        assertEquals("java.lang.Integer", accessor.getDataClass());
        assertEquals(5, accessor.getStartAddress());
        assertEquals(10, accessor.getEndAddress());
        assertEquals(6, accessor.getRegisterCount());
        assertTrue(accessor.isReadFunction());
        assertFalse(accessor.isWriteFunction());
    }
}
