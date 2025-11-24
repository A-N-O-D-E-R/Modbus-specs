package com.anode.modbus.model;

import java.util.Objects;

/**
 * Represents the connection configuration for a Modbus device.
 * Supports both TCP and Serial (RTU/ASCII) connection types.
 */
public final class ConnectionConfig {

    /**
     * The type of Modbus connection.
     */
    public enum ConnectionType {
        TCP,
        RTU,
        ASCII
    }

    private final ConnectionType type;

    // TCP settings
    private final String host;
    private final int port;
    private final int timeout;
    private final boolean reconnect;

    // Serial settings
    private final String portName;
    private final int baudRate;
    private final int dataBits;
    private final int stopBits;
    private final int parity;

    private ConnectionConfig(Builder builder) {
        this.type = Objects.requireNonNull(builder.type, "type must not be null");
        this.host = builder.host;
        this.port = builder.port;
        this.timeout = builder.timeout;
        this.reconnect = builder.reconnect;
        this.portName = builder.portName;
        this.baudRate = builder.baudRate;
        this.dataBits = builder.dataBits;
        this.stopBits = builder.stopBits;
        this.parity = builder.parity;
    }

    public ConnectionType getType() {
        return type;
    }

    public boolean isTcp() {
        return type == ConnectionType.TCP;
    }

    public boolean isSerial() {
        return type == ConnectionType.RTU || type == ConnectionType.ASCII;
    }

    // TCP getters
    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public int getTimeout() {
        return timeout;
    }

    public boolean isReconnect() {
        return reconnect;
    }

    // Serial getters
    public String getPortName() {
        return portName;
    }

    public int getBaudRate() {
        return baudRate;
    }

    public int getDataBits() {
        return dataBits;
    }

    public int getStopBits() {
        return stopBits;
    }

    public int getParity() {
        return parity;
    }

    /**
     * Returns the encoding string for serial connections ("rtu" or "ascii").
     */
    public String getEncoding() {
        return type == ConnectionType.ASCII ? "ascii" : "rtu";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConnectionConfig that = (ConnectionConfig) o;
        return port == that.port &&
                timeout == that.timeout &&
                reconnect == that.reconnect &&
                baudRate == that.baudRate &&
                dataBits == that.dataBits &&
                stopBits == that.stopBits &&
                parity == that.parity &&
                type == that.type &&
                Objects.equals(host, that.host) &&
                Objects.equals(portName, that.portName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, host, port, timeout, reconnect, portName, baudRate, dataBits, stopBits, parity);
    }

    @Override
    public String toString() {
        if (isTcp()) {
            return String.format("ConnectionConfig{type=%s, host='%s', port=%d, timeout=%d, reconnect=%s}",
                    type, host, port, timeout, reconnect);
        } else {
            return String.format("ConnectionConfig{type=%s, portName='%s', baudRate=%d, dataBits=%d, stopBits=%d, parity=%d}",
                    type, portName, baudRate, dataBits, stopBits, parity);
        }
    }

    /**
     * Builder for creating ConnectionConfig instances.
     */
    public static class Builder {
        private ConnectionType type;

        // TCP settings with defaults
        private String host = "localhost";
        private int port = 502;
        private int timeout = 3000;
        private boolean reconnect = true;

        // Serial settings with defaults
        private String portName;
        private int baudRate = 9600;
        private int dataBits = 8;
        private int stopBits = 1;
        private int parity = 0; // 0=None, 1=Odd, 2=Even

        public Builder type(ConnectionType type) {
            this.type = type;
            return this;
        }

        public Builder type(String type) {
            if (type == null || type.isBlank()) {
                this.type = ConnectionType.TCP;
            } else {
                this.type = ConnectionType.valueOf(type.toUpperCase());
            }
            return this;
        }

        // TCP settings
        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder timeout(int timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder reconnect(boolean reconnect) {
            this.reconnect = reconnect;
            return this;
        }

        // Serial settings
        public Builder portName(String portName) {
            this.portName = portName;
            return this;
        }

        public Builder baudRate(int baudRate) {
            this.baudRate = baudRate;
            return this;
        }

        public Builder dataBits(int dataBits) {
            this.dataBits = dataBits;
            return this;
        }

        public Builder stopBits(int stopBits) {
            this.stopBits = stopBits;
            return this;
        }

        public Builder parity(int parity) {
            this.parity = parity;
            return this;
        }

        public Builder parity(String parity) {
            if (parity == null || parity.isBlank() || parity.equalsIgnoreCase("none")) {
                this.parity = 0;
            } else if (parity.equalsIgnoreCase("odd")) {
                this.parity = 1;
            } else if (parity.equalsIgnoreCase("even")) {
                this.parity = 2;
            } else {
                this.parity = Integer.parseInt(parity);
            }
            return this;
        }

        public ConnectionConfig build() {
            return new ConnectionConfig(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a TCP connection config with minimal settings.
     */
    public static ConnectionConfig tcp(String host, int port) {
        return builder()
                .type(ConnectionType.TCP)
                .host(host)
                .port(port)
                .build();
    }

    /**
     * Creates a Serial RTU connection config with minimal settings.
     */
    public static ConnectionConfig rtu(String portName, int baudRate) {
        return builder()
                .type(ConnectionType.RTU)
                .portName(portName)
                .baudRate(baudRate)
                .build();
    }

    /**
     * Creates a Serial ASCII connection config with minimal settings.
     */
    public static ConnectionConfig ascii(String portName, int baudRate) {
        return builder()
                .type(ConnectionType.ASCII)
                .portName(portName)
                .baudRate(baudRate)
                .build();
    }
}
