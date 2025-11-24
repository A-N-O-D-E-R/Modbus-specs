package com.anode.modbus.model;

import java.util.Objects;

/**
 * Represents a Modbus register with its properties.
 * Registers are memory locations in Modbus devices that store data.
 */
public final class Register {

    private final String name;
    private final int address;
    private final String dataType;
    private final String access;

    public Register(String name, int address, String dataType, String access) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.address = address;
        this.dataType = dataType != null ? dataType : "";
        this.access = access != null ? access : "";
    }

    public String getName() {
        return name;
    }

    public int getAddress() {
        return address;
    }

    public String getDataType() {
        return dataType;
    }

    public String getAccess() {
        return access;
    }

    public boolean isReadable() {
        return access.contains("R") || access.toLowerCase().contains("read");
    }

    public boolean isWritable() {
        return access.contains("W") || access.toLowerCase().contains("write");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Register register = (Register) o;
        return address == register.address && Objects.equals(name, register.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, address);
    }

    @Override
    public String toString() {
        return String.format("Register{name='%s', address=%d, dataType='%s', access='%s'}",
                name, address, dataType, access);
    }
}
