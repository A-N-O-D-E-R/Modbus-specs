package com.anode.modbus.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Device Tests")
class DeviceTest {

    @Test
    @DisplayName("Should create Device with builder")
    void shouldCreateDeviceWithBuilder() {
        Device device = Device.builder()
                .id("Controller1")
                .unitId(10)
                .build();

        assertThat(device.getId()).isEqualTo("Controller1");
        assertThat(device.getUnitId()).isEqualTo(10);
        assertThat(device.getHoldingRegisters()).isEmpty();
    }

    @Test
    @DisplayName("Should add holding registers")
    void shouldAddHoldingRegisters() {
        Register reg1 = new Register("Temp", 0, "uint16", "R");
        Register reg2 = new Register("Pressure", 1, "uint16", "RW");

        Device device = Device.builder()
                .id("Device1")
                .unitId(1)
                .addHoldingRegister(reg1)
                .addHoldingRegister(reg2)
                .build();

        assertThat(device.getHoldingRegisters()).hasSize(2);
        assertThat(device.getHoldingRegisters()).containsExactly(reg1, reg2);
    }

    @Test
    @DisplayName("Should add different register types")
    void shouldAddDifferentRegisterTypes() {
        Register holding = new Register("Holding", 0, "uint16", "RW");
        Register input = new Register("Input", 0, "uint16", "R");
        Register coil = new Register("Coil", 0, "bool", "RW");
        Register discrete = new Register("Discrete", 0, "bool", "R");

        Device device = Device.builder()
                .id("Device1")
                .unitId(1)
                .addHoldingRegister(holding)
                .addInputRegister(input)
                .addCoil(coil)
                .addDiscreteInput(discrete)
                .build();

        assertThat(device.getHoldingRegisters()).containsExactly(holding);
        assertThat(device.getInputRegisters()).containsExactly(input);
        assertThat(device.getCoils()).containsExactly(coil);
        assertThat(device.getDiscreteInputs()).containsExactly(discrete);
    }

    @Test
    @DisplayName("Should find holding register by address")
    void shouldFindHoldingRegisterByAddress() {
        Register reg = new Register("Temp", 100, "uint16", "R");
        Device device = Device.builder()
                .id("Device1")
                .unitId(1)
                .addHoldingRegister(reg)
                .build();

        Optional<Register> found = device.findHoldingRegisterByAddress(100);

        assertThat(found).isPresent();
        assertThat(found.get()).isEqualTo(reg);
    }

    @Test
    @DisplayName("Should return empty when register not found by address")
    void shouldReturnEmptyWhenRegisterNotFoundByAddress() {
        Device device = Device.builder()
                .id("Device1")
                .unitId(1)
                .addHoldingRegister(new Register("Temp", 100, "uint16", "R"))
                .build();

        Optional<Register> found = device.findHoldingRegisterByAddress(999);

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should find holding register by name")
    void shouldFindHoldingRegisterByName() {
        Register reg = new Register("Temperature", 100, "uint16", "R");
        Device device = Device.builder()
                .id("Device1")
                .unitId(1)
                .addHoldingRegister(reg)
                .build();

        Optional<Register> found = device.findHoldingRegisterByName("Temperature");

        assertThat(found).isPresent();
        assertThat(found.get()).isEqualTo(reg);
    }

    @Test
    @DisplayName("Should return empty when register not found by name")
    void shouldReturnEmptyWhenRegisterNotFoundByName() {
        Device device = Device.builder()
                .id("Device1")
                .unitId(1)
                .addHoldingRegister(new Register("Temp", 100, "uint16", "R"))
                .build();

        Optional<Register> found = device.findHoldingRegisterByName("NonExistent");

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Register lists should be immutable")
    void registerListsShouldBeImmutable() {
        Device device = Device.builder()
                .id("Device1")
                .unitId(1)
                .addHoldingRegister(new Register("Temp", 0, "uint16", "R"))
                .build();

        assertThatThrownBy(() -> device.getHoldingRegisters().add(new Register("New", 1, "uint16", "R")))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("Should be equal when IDs match")
    void shouldBeEqualWhenIdsMatch() {
        Device device1 = Device.builder().id("Device1").unitId(1).build();
        Device device2 = Device.builder().id("Device1").unitId(2).build();

        assertThat(device1).isEqualTo(device2);
        assertThat(device1.hashCode()).isEqualTo(device2.hashCode());
    }

    @Test
    @DisplayName("Should have meaningful toString")
    void shouldHaveMeaningfulToString() {
        Device device = Device.builder()
                .id("Controller1")
                .unitId(10)
                .addHoldingRegister(new Register("Temp", 0, "uint16", "R"))
                .build();

        assertThat(device.toString())
                .contains("Controller1")
                .contains("10")
                .contains("1");
    }
}
