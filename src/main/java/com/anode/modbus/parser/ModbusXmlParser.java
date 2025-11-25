package com.anode.modbus.parser;

import com.anode.modbus.model.Accessor;
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

        // Security: Disable XXE (XML External Entity) attacks
        try {
            this.factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            this.factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            this.factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            this.factory.setXIncludeAware(false);
            this.factory.setExpandEntityReferences(false);
        } catch (Exception e) {
            throw new RuntimeException("Failed to configure secure XML parser", e);
        }
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
            try {
                builder.port(Integer.parseInt(port));
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid port value: " + port, e);
            }
        }

        String timeout = getElementTextContent(element, "Timeout");
        if (!timeout.isEmpty()) {
            try {
                builder.timeout(Integer.parseInt(timeout));
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid timeout value: " + timeout, e);
            }
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
            try {
                builder.baudRate(Integer.parseInt(baudRate));
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid baud rate value: " + baudRate, e);
            }
        }

        String dataBits = getElementTextContent(element, "DataBits");
        if (!dataBits.isEmpty()) {
            try {
                builder.dataBits(Integer.parseInt(dataBits));
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid data bits value: " + dataBits, e);
            }
        }

        String stopBits = getElementTextContent(element, "StopBits");
        if (!stopBits.isEmpty()) {
            try {
                builder.stopBits(Integer.parseInt(stopBits));
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid stop bits value: " + stopBits, e);
            }
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
        String unitIdStr = element.getAttribute("unitId");
        int unitId;
        try {
            unitId = Integer.parseInt(unitIdStr);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid unit ID value for device '" + id + "': " + unitIdStr, e);
        }

        Device.Builder builder = Device.builder()
                .id(id)
                .unitId(unitId);

        parseAccessors(element, builder);
        parseRegisters(element, "HoldingRegisters", builder::addHoldingRegister);
        parseRegisters(element, "InputRegisters", builder::addInputRegister);
        parseRegisters(element, "Coils", builder::addCoil);
        parseRegisters(element, "DiscreteInputs", builder::addDiscreteInput);

        return builder.build();
    }

    private void parseAccessors(Element deviceElement, Device.Builder builder) {
        NodeList containers = deviceElement.getElementsByTagName("Accessors");
        if (containers.getLength() == 0) {
            return;
        }

        Element container = (Element) containers.item(0);
        NodeList accessorNodes = container.getElementsByTagName("Accessor");

        java.util.Set<String> accessorNames = new java.util.HashSet<>();

        for (int i = 0; i < accessorNodes.getLength(); i++) {
            Element accessorElement = (Element) accessorNodes.item(i);
            Accessor accessor = parseAccessor(accessorElement);

            // Check for duplicate accessor names
            if (!accessorNames.add(accessor.getName())) {
                throw new RuntimeException("Duplicate accessor name '" + accessor.getName() + "' in device");
            }

            builder.addAccessor(accessor);
        }
    }

    private Accessor parseAccessor(Element element) {
        String name = element.getAttribute("name");
        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("Accessor 'name' attribute is required and must not be empty");
        }

        String function = getElementTextContent(element, "Function");
        if (function == null || function.trim().isEmpty()) {
            throw new RuntimeException("Accessor '" + name + "' must have a non-empty Function element");
        }

        String dataClass = getElementTextContent(element, "DataClass");
        if (dataClass == null || dataClass.trim().isEmpty()) {
            throw new RuntimeException("Accessor '" + name + "' must have a non-empty DataClass element");
        }

        String addressRange = getElementTextContent(element, "AddressRange");
        if (addressRange == null || addressRange.trim().isEmpty()) {
            throw new RuntimeException("Accessor '" + name + "' must have a non-empty AddressRange element");
        }

        try {
            return Accessor.builder()
                    .name(name)
                    .function(function)
                    .dataClass(dataClass)
                    .addressRange(addressRange)
                    .build();
        } catch (NumberFormatException e) {
            throw new RuntimeException("Failed to parse accessor '" + name + "': Invalid address format in '" + addressRange + "'", e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Failed to parse accessor '" + name + "': " + e.getMessage(), e);
        }
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
        String addressStr = element.getAttribute("address");
        int address;
        try {
            address = Integer.parseInt(addressStr);
            if (address < 0 || address > 65535) {
                throw new RuntimeException("Register address out of range (0-65535) for '" + name + "': " + address);
            }
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid address value for register '" + name + "': " + addressStr, e);
        }
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
