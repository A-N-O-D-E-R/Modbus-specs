package com.anode.modbus.repository;

import com.anode.modbus.model.Device;
import com.anode.modbus.model.FunctionCode;

import java.util.*;

/**
 * Repository for storing and retrieving Modbus specification data.
 * Provides efficient lookup operations for function codes and devices.
 */
public class ModbusSpecRepository {

    private final Map<String, FunctionCode> functionCodesByCode = new HashMap<>();
    private final Map<String, FunctionCode> functionCodesByName = new HashMap<>();
    private final Map<String, Device> devicesById = new HashMap<>();
    private final Map<Integer, Device> devicesByUnitId = new HashMap<>();

    /**
     * Adds a function code to the repository.
     */
    public void addFunctionCode(FunctionCode functionCode) {
        functionCodesByCode.put(functionCode.getCode(), functionCode);
        functionCodesByName.put(functionCode.getName().toLowerCase(), functionCode);
    }

    /**
     * Adds multiple function codes to the repository.
     */
    public void addFunctionCodes(Collection<FunctionCode> functionCodes) {
        functionCodes.forEach(this::addFunctionCode);
    }

    /**
     * Adds a device to the repository.
     */
    public void addDevice(Device device) {
        devicesById.put(device.getId(), device);
        devicesByUnitId.put(device.getUnitId(), device);
    }

    /**
     * Adds multiple devices to the repository.
     */
    public void addDevices(Collection<Device> devices) {
        devices.forEach(this::addDevice);
    }

    /**
     * Finds a function code by its numeric code (e.g., "3").
     */
    public Optional<FunctionCode> findFunctionCodeByCode(String code) {
        return Optional.ofNullable(functionCodesByCode.get(code));
    }

    /**
     * Finds a function code by its name (case-insensitive).
     */
    public Optional<FunctionCode> findFunctionCodeByName(String name) {
        return Optional.ofNullable(functionCodesByName.get(name.toLowerCase()));
    }

    /**
     * Finds a device by its ID.
     */
    public Optional<Device> findDeviceById(String id) {
        return Optional.ofNullable(devicesById.get(id));
    }

    /**
     * Finds a device by its unit ID (slave address).
     */
    public Optional<Device> findDeviceByUnitId(int unitId) {
        return Optional.ofNullable(devicesByUnitId.get(unitId));
    }

    /**
     * Returns all function codes.
     */
    public Collection<FunctionCode> getAllFunctionCodes() {
        return Collections.unmodifiableCollection(functionCodesByCode.values());
    }

    /**
     * Returns all devices.
     */
    public Collection<Device> getAllDevices() {
        return Collections.unmodifiableCollection(devicesById.values());
    }

    /**
     * Checks if a function code exists.
     */
    public boolean hasFunctionCode(String code) {
        return functionCodesByCode.containsKey(code);
    }

    /**
     * Checks if a device exists by ID.
     */
    public boolean hasDevice(String id) {
        return devicesById.containsKey(id);
    }

    /**
     * Clears all data from the repository.
     */
    public void clear() {
        functionCodesByCode.clear();
        functionCodesByName.clear();
        devicesById.clear();
        devicesByUnitId.clear();
    }

    /**
     * Returns the number of function codes in the repository.
     */
    public int getFunctionCodeCount() {
        return functionCodesByCode.size();
    }

    /**
     * Returns the number of devices in the repository.
     */
    public int getDeviceCount() {
        return devicesById.size();
    }
}
