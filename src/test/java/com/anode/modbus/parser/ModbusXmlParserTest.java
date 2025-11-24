package com.anode.modbus.parser;

import com.anode.modbus.model.Device;
import com.anode.modbus.model.FunctionCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ModbusXmlParser Tests")
class ModbusXmlParserTest {

    private ModbusXmlParser parser;

    @BeforeEach
    void setUp() {
        parser = new ModbusXmlParser();
    }

    @Test
    @DisplayName("Should parse function codes from XML")
    void shouldParseFunctionCodesFromXml() throws ModbusParseException {
        InputStream xmlStream = getClass().getResourceAsStream("/test-modbus-spec.xml");
        ModbusXmlParser.ParseResult result = parser.parse(xmlStream);

        List<FunctionCode> functionCodes = result.getFunctionCodes();

        assertThat(functionCodes).hasSize(8);
        assertThat(functionCodes)
                .extracting(FunctionCode::getCode)
                .contains("1", "2", "3", "4", "5", "6", "15", "16");
    }

    @Test
    @DisplayName("Should parse function code details correctly")
    void shouldParseFunctionCodeDetailsCorrectly() throws ModbusParseException {
        InputStream xmlStream = getClass().getResourceAsStream("/test-modbus-spec.xml");
        ModbusXmlParser.ParseResult result = parser.parse(xmlStream);

        FunctionCode fc3 = result.getFunctionCodes().stream()
                .filter(fc -> fc.getCode().equals("3"))
                .findFirst()
                .orElseThrow();

        assertThat(fc3.getName()).isEqualTo("ReadHoldingRegisters");
        assertThat(fc3.getDescription()).isEqualTo("Read holding register values");
    }

    @Test
    @DisplayName("Should parse devices from XML")
    void shouldParseDevicesFromXml() throws ModbusParseException {
        InputStream xmlStream = getClass().getResourceAsStream("/test-modbus-spec.xml");
        ModbusXmlParser.ParseResult result = parser.parse(xmlStream);

        List<Device> devices = result.getDevices();

        assertThat(devices).hasSize(2);
        assertThat(devices)
                .extracting(Device::getId)
                .contains("TestController", "SecondDevice");
    }

    @Test
    @DisplayName("Should parse device details correctly")
    void shouldParseDeviceDetailsCorrectly() throws ModbusParseException {
        InputStream xmlStream = getClass().getResourceAsStream("/test-modbus-spec.xml");
        ModbusXmlParser.ParseResult result = parser.parse(xmlStream);

        Device testController = result.getDevices().stream()
                .filter(d -> d.getId().equals("TestController"))
                .findFirst()
                .orElseThrow();

        assertThat(testController.getUnitId()).isEqualTo(10);
        assertThat(testController.getHoldingRegisters()).hasSize(3);
        assertThat(testController.getCoils()).hasSize(2);
        assertThat(testController.getDiscreteInputs()).hasSize(1);
        assertThat(testController.getInputRegisters()).hasSize(1);
    }

    @Test
    @DisplayName("Should parse register details correctly")
    void shouldParseRegisterDetailsCorrectly() throws ModbusParseException {
        InputStream xmlStream = getClass().getResourceAsStream("/test-modbus-spec.xml");
        ModbusXmlParser.ParseResult result = parser.parse(xmlStream);

        Device device = result.getDevices().stream()
                .filter(d -> d.getId().equals("TestController"))
                .findFirst()
                .orElseThrow();

        assertThat(device.getHoldingRegisters())
                .extracting(r -> r.getName())
                .contains("SetpointTemp", "MaxTemp", "MinTemp");

        var setpoint = device.findHoldingRegisterByName("SetpointTemp").orElseThrow();
        assertThat(setpoint.getAddress()).isEqualTo(0);
        assertThat(setpoint.getDataType()).isEqualTo("uint16");
        assertThat(setpoint.getAccess()).isEqualTo("RW");
    }

    @Test
    @DisplayName("Should throw exception for invalid XML")
    void shouldThrowExceptionForInvalidXml() {
        InputStream invalidXml = new java.io.ByteArrayInputStream("<invalid>".getBytes());

        assertThatThrownBy(() -> parser.parse(invalidXml))
                .isInstanceOf(ModbusParseException.class);
    }

    @Test
    @DisplayName("Should handle empty sections gracefully")
    void shouldHandleEmptySectionsGracefully() throws ModbusParseException {
        String minimalXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <ModbusSpec>
                    <FunctionCodes></FunctionCodes>
                    <RegisterMap></RegisterMap>
                </ModbusSpec>
                """;
        InputStream xmlStream = new java.io.ByteArrayInputStream(minimalXml.getBytes());

        ModbusXmlParser.ParseResult result = parser.parse(xmlStream);

        assertThat(result.getFunctionCodes()).isEmpty();
        assertThat(result.getDevices()).isEmpty();
    }
}
