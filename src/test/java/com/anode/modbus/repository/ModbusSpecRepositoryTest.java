package com.anode.modbus.repository;

import com.anode.modbus.model.Device;
import com.anode.modbus.model.FunctionCode;
import com.anode.modbus.model.Register;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ModbusSpecRepository Tests")
class ModbusSpecRepositoryTest {

    private ModbusSpecRepository repository;

    @BeforeEach
    void setUp() {
        repository = new ModbusSpecRepository();
    }

    // ==================== Function Code Tests ====================

    @Test
    @DisplayName("Should add and find function code by code")
    void shouldAddAndFindFunctionCodeByCode() {
        FunctionCode fc = new FunctionCode("3", "ReadHoldingRegisters", "Reads registers");
        repository.addFunctionCode(fc);

        Optional<FunctionCode> found = repository.findFunctionCodeByCode("3");

        assertThat(found).isPresent();
        assertThat(found.get()).isEqualTo(fc);
    }

    @Test
    @DisplayName("Should find function code by name (case insensitive)")
    void shouldFindFunctionCodeByNameCaseInsensitive() {
        FunctionCode fc = new FunctionCode("3", "ReadHoldingRegisters", "Reads registers");
        repository.addFunctionCode(fc);

        assertThat(repository.findFunctionCodeByName("ReadHoldingRegisters")).isPresent();
        assertThat(repository.findFunctionCodeByName("readholdingregisters")).isPresent();
        assertThat(repository.findFunctionCodeByName("READHOLDINGREGISTERS")).isPresent();
    }

    @Test
    @DisplayName("Should return empty for non-existent function code")
    void shouldReturnEmptyForNonExistentFunctionCode() {
        assertThat(repository.findFunctionCodeByCode("99")).isEmpty();
        assertThat(repository.findFunctionCodeByName("NonExistent")).isEmpty();
    }

    @Test
    @DisplayName("Should add multiple function codes")
    void shouldAddMultipleFunctionCodes() {
        List<FunctionCode> codes = List.of(
                new FunctionCode("1", "ReadCoils", ""),
                new FunctionCode("3", "ReadHoldingRegisters", ""),
                new FunctionCode("6", "WriteSingleRegister", "")
        );
        repository.addFunctionCodes(codes);

        assertThat(repository.getAllFunctionCodes()).hasSize(3);
        assertThat(repository.hasFunctionCode("1")).isTrue();
        assertThat(repository.hasFunctionCode("3")).isTrue();
        assertThat(repository.hasFunctionCode("6")).isTrue();
    }

    @Test
    @DisplayName("Should return function code count")
    void shouldReturnFunctionCodeCount() {
        repository.addFunctionCode(new FunctionCode("1", "FC1", ""));
        repository.addFunctionCode(new FunctionCode("2", "FC2", ""));

        assertThat(repository.getFunctionCodeCount()).isEqualTo(2);
    }

    // ==================== Device Tests ====================

    @Test
    @DisplayName("Should add and find device by ID")
    void shouldAddAndFindDeviceById() {
        Device device = Device.builder().id("Device1").unitId(10).build();
        repository.addDevice(device);

        Optional<Device> found = repository.findDeviceById("Device1");

        assertThat(found).isPresent();
        assertThat(found.get()).isEqualTo(device);
    }

    @Test
    @DisplayName("Should find device by unit ID")
    void shouldFindDeviceByUnitId() {
        Device device = Device.builder().id("Device1").unitId(10).build();
        repository.addDevice(device);

        Optional<Device> found = repository.findDeviceByUnitId(10);

        assertThat(found).isPresent();
        assertThat(found.get()).isEqualTo(device);
    }

    @Test
    @DisplayName("Should return empty for non-existent device")
    void shouldReturnEmptyForNonExistentDevice() {
        assertThat(repository.findDeviceById("NonExistent")).isEmpty();
        assertThat(repository.findDeviceByUnitId(99)).isEmpty();
    }

    @Test
    @DisplayName("Should add multiple devices")
    void shouldAddMultipleDevices() {
        List<Device> devices = List.of(
                Device.builder().id("D1").unitId(1).build(),
                Device.builder().id("D2").unitId(2).build()
        );
        repository.addDevices(devices);

        assertThat(repository.getAllDevices()).hasSize(2);
        assertThat(repository.hasDevice("D1")).isTrue();
        assertThat(repository.hasDevice("D2")).isTrue();
    }

    @Test
    @DisplayName("Should return device count")
    void shouldReturnDeviceCount() {
        repository.addDevice(Device.builder().id("D1").unitId(1).build());
        repository.addDevice(Device.builder().id("D2").unitId(2).build());

        assertThat(repository.getDeviceCount()).isEqualTo(2);
    }

    // ==================== Clear Tests ====================

    @Test
    @DisplayName("Should clear all data")
    void shouldClearAllData() {
        repository.addFunctionCode(new FunctionCode("1", "FC1", ""));
        repository.addDevice(Device.builder().id("D1").unitId(1).build());

        repository.clear();

        assertThat(repository.getFunctionCodeCount()).isEqualTo(0);
        assertThat(repository.getDeviceCount()).isEqualTo(0);
        assertThat(repository.getAllFunctionCodes()).isEmpty();
        assertThat(repository.getAllDevices()).isEmpty();
    }

    // ==================== Immutability Tests ====================

    @Test
    @DisplayName("getAllFunctionCodes should return unmodifiable collection")
    void getAllFunctionCodesShouldReturnUnmodifiableCollection() {
        repository.addFunctionCode(new FunctionCode("1", "FC1", ""));

        assertThatThrownBy(() -> repository.getAllFunctionCodes().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("getAllDevices should return unmodifiable collection")
    void getAllDevicesShouldReturnUnmodifiableCollection() {
        repository.addDevice(Device.builder().id("D1").unitId(1).build());

        assertThatThrownBy(() -> repository.getAllDevices().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
