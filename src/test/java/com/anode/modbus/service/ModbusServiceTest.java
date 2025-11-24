package com.anode.modbus.service;

import com.anode.modbus.connection.ModbusConnection;
import com.anode.modbus.connection.ModbusConnectionException;
import com.anode.modbus.model.Device;
import com.anode.modbus.model.FunctionCode;
import com.anode.modbus.model.Register;
import com.anode.modbus.repository.ModbusSpecRepository;
import com.anode.modbus.service.ModbusService.ModbusOperationHandler;
import com.anode.modbus.service.ModbusService.ModbusRequest;
import com.anode.modbus.service.ModbusService.ModbusResult;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;
import com.ghgande.j2mod.modbus.util.BitVector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("ModbusService Tests")
@ExtendWith(MockitoExtension.class)
class ModbusServiceTest {

    private ModbusSpecRepository repository;
    private ModbusService service;

    @Mock
    private ModbusOperationHandler mockHandler;

    @Mock
    private ModbusConnection mockConnection;

    @BeforeEach
    void setUp() {
        repository = new ModbusSpecRepository();
        service = new ModbusService(repository, mockHandler);
    }

    // ==================== callFunction Tests ====================

    @Test
    @DisplayName("Should call function successfully")
    void shouldCallFunctionSuccessfully() {
        repository.addFunctionCode(new FunctionCode("3", "ReadHoldingRegisters", ""));
        when(mockHandler.execute(any())).thenReturn(ModbusResult.success("OK"));

        ModbusResult result = service.callFunction("3", 10, 0, 5);

        assertThat(result.isSuccess()).isTrue();
        verify(mockHandler).execute(argThat(req ->
                req.getFunctionCode().getCode().equals("3") &&
                        req.getUnitId() == 10 &&
                        req.getAddress() == 0 &&
                        req.getQuantity() == 5
        ));
    }

    @Test
    @DisplayName("Should return failure for unknown function code")
    void shouldReturnFailureForUnknownFunctionCode() {
        ModbusResult result = service.callFunction("99", 10, 0, 5);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("Unknown function code");
        verifyNoInteractions(mockHandler);
    }

    @Test
    @DisplayName("Should call function by name")
    void shouldCallFunctionByName() {
        repository.addFunctionCode(new FunctionCode("3", "ReadHoldingRegisters", ""));
        when(mockHandler.execute(any())).thenReturn(ModbusResult.success("OK"));

        ModbusResult result = service.callFunctionByName("ReadHoldingRegisters", 10, 0, 5);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    @DisplayName("Should return failure for unknown function name")
    void shouldReturnFailureForUnknownFunctionName() {
        ModbusResult result = service.callFunctionByName("NonExistent", 10, 0, 5);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("Unknown function name");
    }

    // ==================== readRegister Tests ====================

    @Test
    @DisplayName("Should read register from device")
    void shouldReadRegisterFromDevice() {
        repository.addFunctionCode(new FunctionCode("3", "ReadHoldingRegisters", ""));
        Device device = Device.builder()
                .id("Controller")
                .unitId(10)
                .addHoldingRegister(new Register("Temp", 100, "uint16", "RW"))
                .build();
        repository.addDevice(device);
        when(mockHandler.execute(any())).thenReturn(ModbusResult.success("OK"));

        ModbusResult result = service.readRegister("Controller", "Temp");

        assertThat(result.isSuccess()).isTrue();
        verify(mockHandler).execute(argThat(req ->
                req.getUnitId() == 10 && req.getAddress() == 100
        ));
    }

    @Test
    @DisplayName("Should return failure for unknown device")
    void shouldReturnFailureForUnknownDevice() {
        ModbusResult result = service.readRegister("NonExistent", "Temp");

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("Unknown device");
    }

    @Test
    @DisplayName("Should return failure for unknown register")
    void shouldReturnFailureForUnknownRegister() {
        Device device = Device.builder().id("Controller").unitId(10).build();
        repository.addDevice(device);

        ModbusResult result = service.readRegister("Controller", "NonExistent");

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("Unknown register");
    }

    @Test
    @DisplayName("Should return failure for non-readable register")
    void shouldReturnFailureForNonReadableRegister() {
        Device device = Device.builder()
                .id("Controller")
                .unitId(10)
                .addHoldingRegister(new Register("WriteOnly", 100, "uint16", "W"))
                .build();
        repository.addDevice(device);

        ModbusResult result = service.readRegister("Controller", "WriteOnly");

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("not readable");
    }

    // ==================== Direct Connection Operations Tests ====================

    @Test
    @DisplayName("Should read coils via connection")
    void shouldReadCoilsViaConnection() throws ModbusConnectionException {
        BitVector expected = new BitVector(8);
        when(mockConnection.readCoils(1, 0, 8)).thenReturn(expected);

        BitVector result = service.readCoils(mockConnection, 1, 0, 8);

        assertThat(result).isEqualTo(expected);
        verify(mockConnection).readCoils(1, 0, 8);
    }

    @Test
    @DisplayName("Should read holding registers via connection")
    void shouldReadHoldingRegistersViaConnection() throws ModbusConnectionException {
        com.ghgande.j2mod.modbus.procimg.Register[] expected = {
                new SimpleRegister(100),
                new SimpleRegister(200)
        };
        when(mockConnection.readHoldingRegisters(1, 0, 2)).thenReturn(expected);

        var result = service.readHoldingRegisters(mockConnection, 1, 0, 2);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("Should write single coil via connection")
    void shouldWriteSingleCoilViaConnection() throws ModbusConnectionException {
        when(mockConnection.writeSingleCoil(1, 0, true)).thenReturn(true);

        boolean result = service.writeSingleCoil(mockConnection, 1, 0, true);

        assertThat(result).isTrue();
        verify(mockConnection).writeSingleCoil(1, 0, true);
    }

    @Test
    @DisplayName("Should write single register via connection")
    void shouldWriteSingleRegisterViaConnection() throws ModbusConnectionException {
        when(mockConnection.writeSingleRegister(eq(1), eq(0), any())).thenReturn(1);

        int result = service.writeSingleRegister(mockConnection, 1, 0, 12345);

        assertThat(result).isEqualTo(1);
        verify(mockConnection).writeSingleRegister(eq(1), eq(0), argThat(reg ->
                reg.getValue() == 12345
        ));
    }

    @Test
    @DisplayName("Should write multiple registers via connection")
    void shouldWriteMultipleRegistersViaConnection() throws ModbusConnectionException {
        when(mockConnection.writeMultipleRegisters(eq(1), eq(0), any())).thenReturn(3);

        int result = service.writeMultipleRegisters(mockConnection, 1, 0, new int[]{100, 200, 300});

        assertThat(result).isEqualTo(3);
        verify(mockConnection).writeMultipleRegisters(eq(1), eq(0), argThat(regs ->
                regs.length == 3 &&
                        regs[0].getValue() == 100 &&
                        regs[1].getValue() == 200 &&
                        regs[2].getValue() == 300
        ));
    }

    // ==================== Helper Classes Tests ====================

    @Test
    @DisplayName("ModbusResult should store data")
    void modbusResultShouldStoreData() {
        byte[] data = {0x01, 0x02, 0x03};
        ModbusResult result = ModbusResult.success("OK", data);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMessage()).isEqualTo("OK");
        assertThat(result.getData()).isEqualTo(data);
    }

    @Test
    @DisplayName("ModbusRequest should store all properties")
    void modbusRequestShouldStoreAllProperties() {
        FunctionCode fc = new FunctionCode("3", "Read", "");
        ModbusRequest request = new ModbusRequest(fc, 10, 100, 5);

        assertThat(request.getFunctionCode()).isEqualTo(fc);
        assertThat(request.getUnitId()).isEqualTo(10);
        assertThat(request.getAddress()).isEqualTo(100);
        assertThat(request.getQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("ConsoleOperationHandler should return success")
    void consoleOperationHandlerShouldReturnSuccess() {
        ModbusService.ConsoleOperationHandler handler = new ModbusService.ConsoleOperationHandler();
        FunctionCode fc = new FunctionCode("3", "ReadHoldingRegisters", "Test");
        ModbusRequest request = new ModbusRequest(fc, 10, 0, 5);

        ModbusResult result = handler.execute(request);

        assertThat(result.isSuccess()).isTrue();
    }
}
