package com.anode.modbus.model;

import java.util.Objects;

/**
 * Represents an accessor that defines how to access Modbus data.
 * Accessors provide a high-level interface for reading/writing data
 * by specifying the function to use, the data type, and the address range.
 */
public final class Accessor {

    private final String name;
    private final String function;
    private final String dataClass;
    private final int startAddress;
    private final int endAddress;

    private static final int MAX_MODBUS_ADDRESS = 65535;

    private Accessor(Builder builder) {
        this.name = Objects.requireNonNull(builder.name, "name must not be null");
        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("name must not be empty");
        }

        this.function = Objects.requireNonNull(builder.function, "function must not be null");
        if (function.trim().isEmpty()) {
            throw new IllegalArgumentException("function must not be empty");
        }

        this.dataClass = Objects.requireNonNull(builder.dataClass, "dataClass must not be null");
        if (dataClass.trim().isEmpty()) {
            throw new IllegalArgumentException("dataClass must not be empty");
        }

        this.startAddress = builder.startAddress;
        this.endAddress = builder.endAddress;

        validateAddress(startAddress, "startAddress");
        validateAddress(endAddress, "endAddress");

        if (endAddress < startAddress) {
            throw new IllegalArgumentException("endAddress must be greater than or equal to startAddress");
        }
    }

    private static void validateAddress(int address, String fieldName) {
        if (address < 0) {
            throw new IllegalArgumentException(fieldName + " must be non-negative, got: " + address);
        }
        if (address > MAX_MODBUS_ADDRESS) {
            throw new IllegalArgumentException(fieldName + " must not exceed " + MAX_MODBUS_ADDRESS + ", got: " + address);
        }
    }

    public String getName() {
        return name;
    }

    public String getFunction() {
        return function;
    }

    public String getDataClass() {
        return dataClass;
    }

    public int getStartAddress() {
        return startAddress;
    }

    public int getEndAddress() {
        return endAddress;
    }

    /**
     * Returns the number of registers this accessor spans.
     */
    public int getRegisterCount() {
        return endAddress - startAddress + 1;
    }

    /**
     * Checks if this accessor uses a read function.
     */
    public boolean isReadFunction() {
        return function.toLowerCase().contains("read");
    }

    /**
     * Checks if this accessor uses a write function.
     */
    public boolean isWriteFunction() {
        return function.toLowerCase().contains("write");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Accessor accessor = (Accessor) o;
        return startAddress == accessor.startAddress
            && endAddress == accessor.endAddress
            && Objects.equals(name, accessor.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, startAddress, endAddress);
    }

    @Override
    public String toString() {
        return String.format("Accessor{name='%s', function='%s', dataClass='%s', addresses=%d-%d}",
                name, function, dataClass, startAddress, endAddress);
    }

    /**
     * Builder for creating Accessor instances.
     */
    public static class Builder {
        private String name;
        private String function;
        private String dataClass;
        private int startAddress;
        private int endAddress;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder function(String function) {
            this.function = function;
            return this;
        }

        public Builder dataClass(String dataClass) {
            this.dataClass = dataClass;
            return this;
        }

        public Builder startAddress(int startAddress) {
            this.startAddress = startAddress;
            return this;
        }

        public Builder endAddress(int endAddress) {
            this.endAddress = endAddress;
            return this;
        }

        /**
         * Sets both start and end address to the same value (single register).
         */
        public Builder address(int address) {
            this.startAddress = address;
            this.endAddress = address;
            return this;
        }

        /**
         * Sets the address range using a string like "1-2" or "5".
         *
         * @param range The address range string
         * @return This builder for chaining
         * @throws IllegalArgumentException if the range is invalid
         */
        public Builder addressRange(String range) {
            if (range == null || range.trim().isEmpty()) {
                throw new IllegalArgumentException("addressRange must not be null or empty");
            }

            String trimmed = range.trim();
            try {
                if (trimmed.contains("-")) {
                    String[] parts = trimmed.split("-");
                    if (parts.length != 2) {
                        throw new IllegalArgumentException("Invalid address range format: " + range);
                    }
                    this.startAddress = Integer.parseInt(parts[0].trim());
                    this.endAddress = Integer.parseInt(parts[1].trim());

                    validateAddress(startAddress, "startAddress");
                    validateAddress(endAddress, "endAddress");

                    if (endAddress < startAddress) {
                        throw new IllegalArgumentException("End address must be >= start address: " + range);
                    }
                } else {
                    int addr = Integer.parseInt(trimmed);
                    validateAddress(addr, "address");
                    this.startAddress = addr;
                    this.endAddress = addr;
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid address format: " + range, e);
            }
            return this;
        }

        public Accessor build() {
            return new Accessor(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
