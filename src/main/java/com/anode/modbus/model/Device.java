package com.anode.modbus.model;

import java.util.*;

/**
 * Represents a Modbus device with its register map.
 * A device has a unique ID and unit ID (slave address) along with
 * collections of different register types.
 */
public final class Device {

    private final String id;
    private final int unitId;
    private final List<Accessor> accessors;
    private final List<Register> holdingRegisters;
    private final List<Register> inputRegisters;
    private final List<Register> coils;
    private final List<Register> discreteInputs;

    private Device(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "id must not be null");
        this.unitId = builder.unitId;
        this.accessors = Collections.unmodifiableList(new ArrayList<>(builder.accessors));
        this.holdingRegisters = Collections.unmodifiableList(new ArrayList<>(builder.holdingRegisters));
        this.inputRegisters = Collections.unmodifiableList(new ArrayList<>(builder.inputRegisters));
        this.coils = Collections.unmodifiableList(new ArrayList<>(builder.coils));
        this.discreteInputs = Collections.unmodifiableList(new ArrayList<>(builder.discreteInputs));
    }

    public String getId() {
        return id;
    }

    public int getUnitId() {
        return unitId;
    }

    public List<Accessor> getAccessors() {
        return accessors;
    }

    public List<Register> getHoldingRegisters() {
        return holdingRegisters;
    }

    public List<Register> getInputRegisters() {
        return inputRegisters;
    }

    public List<Register> getCoils() {
        return coils;
    }

    public List<Register> getDiscreteInputs() {
        return discreteInputs;
    }

    /**
     * Finds an accessor by its name.
     */
    public Optional<Accessor> findAccessorByName(String name) {
        return accessors.stream()
                .filter(a -> a.getName().equals(name))
                .findFirst();
    }

    /**
     * Finds a holding register by its address.
     */
    public Optional<Register> findHoldingRegisterByAddress(int address) {
        return holdingRegisters.stream()
                .filter(r -> r.getAddress() == address)
                .findFirst();
    }

    /**
     * Finds a holding register by its name.
     */
    public Optional<Register> findHoldingRegisterByName(String name) {
        return holdingRegisters.stream()
                .filter(r -> r.getName().equals(name))
                .findFirst();
    }

    /**
     * Finds an input register by its address.
     */
    public Optional<Register> findInputRegisterByAddress(int address) {
        return inputRegisters.stream()
                .filter(r -> r.getAddress() == address)
                .findFirst();
    }

    /**
     * Finds an input register by its name.
     */
    public Optional<Register> findInputRegisterByName(String name) {
        return inputRegisters.stream()
                .filter(r -> r.getName().equals(name))
                .findFirst();
    }

    /**
     * Finds a coil by its address.
     */
    public Optional<Register> findCoilByAddress(int address) {
        return coils.stream()
                .filter(r -> r.getAddress() == address)
                .findFirst();
    }

    /**
     * Finds a coil by its name.
     */
    public Optional<Register> findCoilByName(String name) {
        return coils.stream()
                .filter(r -> r.getName().equals(name))
                .findFirst();
    }

    /**
     * Finds a discrete input by its address.
     */
    public Optional<Register> findDiscreteInputByAddress(int address) {
        return discreteInputs.stream()
                .filter(r -> r.getAddress() == address)
                .findFirst();
    }

    /**
     * Finds a discrete input by its name.
     */
    public Optional<Register> findDiscreteInputByName(String name) {
        return discreteInputs.stream()
                .filter(r -> r.getName().equals(name))
                .findFirst();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Device device = (Device) o;
        return Objects.equals(id, device.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Device{id='%s', unitId=%d, holdingRegisters=%d}",
                id, unitId, holdingRegisters.size());
    }

    /**
     * Builder for creating Device instances.
     */
    public static class Builder {
        private String id;
        private int unitId;
        private final List<Accessor> accessors = new ArrayList<>();
        private final List<Register> holdingRegisters = new ArrayList<>();
        private final List<Register> inputRegisters = new ArrayList<>();
        private final List<Register> coils = new ArrayList<>();
        private final List<Register> discreteInputs = new ArrayList<>();

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder unitId(int unitId) {
            this.unitId = unitId;
            return this;
        }

        public Builder addAccessor(Accessor accessor) {
            this.accessors.add(accessor);
            return this;
        }

        public Builder addHoldingRegister(Register register) {
            this.holdingRegisters.add(register);
            return this;
        }

        public Builder addInputRegister(Register register) {
            this.inputRegisters.add(register);
            return this;
        }

        public Builder addCoil(Register register) {
            this.coils.add(register);
            return this;
        }

        public Builder addDiscreteInput(Register register) {
            this.discreteInputs.add(register);
            return this;
        }

        public Device build() {
            return new Device(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
