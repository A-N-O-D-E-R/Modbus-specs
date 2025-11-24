package com.anode.modbus.parser;

import com.anode.modbus.model.ConnectionConfig;
import com.anode.modbus.model.Device;
import com.anode.modbus.model.FunctionCode;
import com.anode.modbus.model.Register;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Responsible for parsing Modbus specification XML files.
 * This class handles all XML parsing logic and converts XML elements
 * into domain model objects.
 */
public class ModbusXmlParser {

    private final DocumentBuilderFactory factory;

    public ModbusXmlParser() {
        this.factory = DocumentBuilderFactory.newInstance();
        this.factory.setNamespaceAware(true);
    }

    /**
     * Parses a Modbus specification from a file.
     */
    public ParseResult parse(File xmlFile) throws ModbusParseException {
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);
            return parseDocument(doc);
        } catch (Exception e) {
            throw new ModbusParseException("Failed to parse XML file: " + xmlFile.getPath(), e);
        }
    }

    /**
     * Parses a Modbus specification from an input stream.
     */
    public ParseResult parse(InputStream inputStream) throws ModbusParseException {
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputStream);
            return parseDocument(doc);
        } catch (Exception e) {
            throw new ModbusParseException("Failed to parse XML from input stream", e);
        }
    }

    private ParseResult parseDocument(Document doc) {
        doc.getDocumentElement().normalize();

        ConnectionConfig connectionConfig = parseConnectionConfig(doc);
        List<FunctionCode> functionCodes = parseFunctionCodes(doc);
        List<Device> devices = parseDevices(doc);

        return new ParseResult(connectionConfig, functionCodes, devices);
    }

    private ConnectionConfig parseConnectionConfig(Document doc) {
        NodeList nodes = doc.getElementsByTagName("Connection");
        if (nodes.getLength() == 0) {
            return null;
        }

        Element element = (Element) nodes.item(0);
        String type = element.getAttribute("type");

        ConnectionConfig.Builder builder = ConnectionConfig.builder()
                .type(type.isEmpty() ? "TCP" : type);

        // Parse TCP settings
        String host = getElementTextContent(element, "Host");
        if (!host.isEmpty()) {
            builder.host(host);
        }

        String port = getElementTextContent(element, "Port");
        if (!port.isEmpty()) {
            builder.port(Integer.parseInt(port));
        }

        String timeout = getElementTextContent(element, "Timeout");
        if (!timeout.isEmpty()) {
            builder.timeout(Integer.parseInt(timeout));
        }

        String reconnect = getElementTextContent(element, "Reconnect");
        if (!reconnect.isEmpty()) {
            builder.reconnect(Boolean.parseBoolean(reconnect));
        }

        // Parse Serial settings
        String portName = getElementTextContent(element, "PortName");
        if (!portName.isEmpty()) {
            builder.portName(portName);
        }

        String baudRate = getElementTextContent(element, "BaudRate");
        if (!baudRate.isEmpty()) {
            builder.baudRate(Integer.parseInt(baudRate));
        }

        String dataBits = getElementTextContent(element, "DataBits");
        if (!dataBits.isEmpty()) {
            builder.dataBits(Integer.parseInt(dataBits));
        }

        String stopBits = getElementTextContent(element, "StopBits");
        if (!stopBits.isEmpty()) {
            builder.stopBits(Integer.parseInt(stopBits));
        }

        String parity = getElementTextContent(element, "Parity");
        if (!parity.isEmpty()) {
            builder.parity(parity);
        }

        return builder.build();
    }

    private List<FunctionCode> parseFunctionCodes(Document doc) {
        List<FunctionCode> result = new ArrayList<>();
        NodeList nodes = doc.getElementsByTagName("FunctionCode");

        for (int i = 0; i < nodes.getLength(); i++) {
            Element element = (Element) nodes.item(i);
            FunctionCode fc = parseFunctionCode(element);
            result.add(fc);
        }

        return result;
    }

    private FunctionCode parseFunctionCode(Element element) {
        String code = element.getAttribute("code");
        String name = element.getAttribute("name");
        String description = getElementTextContent(element, "Description");

        return new FunctionCode(code, name, description);
    }

    private List<Device> parseDevices(Document doc) {
        List<Device> result = new ArrayList<>();
        NodeList nodes = doc.getElementsByTagName("Device");

        for (int i = 0; i < nodes.getLength(); i++) {
            Element element = (Element) nodes.item(i);
            Device device = parseDevice(element);
            result.add(device);
        }

        return result;
    }

    private Device parseDevice(Element element) {
        String id = element.getAttribute("id");
        int unitId = Integer.parseInt(element.getAttribute("unitId"));

        Device.Builder builder = Device.builder()
                .id(id)
                .unitId(unitId);

        parseRegisters(element, "HoldingRegisters", builder::addHoldingRegister);
        parseRegisters(element, "InputRegisters", builder::addInputRegister);
        parseRegisters(element, "Coils", builder::addCoil);
        parseRegisters(element, "DiscreteInputs", builder::addDiscreteInput);

        return builder.build();
    }

    private void parseRegisters(Element deviceElement, String containerTag,
                                java.util.function.Consumer<Register> adder) {
        NodeList containers = deviceElement.getElementsByTagName(containerTag);
        if (containers.getLength() == 0) {
            return;
        }

        Element container = (Element) containers.item(0);
        NodeList registerNodes = container.getElementsByTagName("Register");

        for (int i = 0; i < registerNodes.getLength(); i++) {
            Element regElement = (Element) registerNodes.item(i);
            Register register = parseRegister(regElement);
            adder.accept(register);
        }
    }

    private Register parseRegister(Element element) {
        String name = element.getAttribute("name");
        int address = Integer.parseInt(element.getAttribute("address"));
        String dataType = getElementTextContent(element, "DataType");
        String access = getElementTextContent(element, "Access");

        return new Register(name, address, dataType, access);
    }

    private String getElementTextContent(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) {
            return "";
        }
        Node node = nodes.item(0);
        return node != null ? node.getTextContent().trim() : "";
    }

    /**
     * Result of parsing a Modbus specification XML.
     */
    public static class ParseResult {
        private final ConnectionConfig connectionConfig;
        private final List<FunctionCode> functionCodes;
        private final List<Device> devices;

        public ParseResult(ConnectionConfig connectionConfig, List<FunctionCode> functionCodes, List<Device> devices) {
            this.connectionConfig = connectionConfig;
            this.functionCodes = functionCodes;
            this.devices = devices;
        }

        /**
         * Returns the connection configuration, or null if not specified in the XML.
         */
        public ConnectionConfig getConnectionConfig() {
            return connectionConfig;
        }

        public List<FunctionCode> getFunctionCodes() {
            return functionCodes;
        }

        public List<Device> getDevices() {
            return devices;
        }
    }
}
