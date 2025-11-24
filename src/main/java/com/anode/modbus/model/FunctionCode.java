package com.anode.modbus.model;

import java.util.Objects;

/**
 * Represents a Modbus function code with its metadata.
 * Function codes define the type of action to be performed
 * (e.g., read coils, write registers).
 */
public final class FunctionCode {

    private final String code;
    private final String name;
    private final String description;

    public FunctionCode(String code, String name, String description) {
        this.code = Objects.requireNonNull(code, "code must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.description = description != null ? description : "";
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FunctionCode that = (FunctionCode) o;
        return Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return String.format("FunctionCode{code='%s', name='%s'}", code, name);
    }
}
