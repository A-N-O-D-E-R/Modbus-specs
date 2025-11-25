package com.anode.modbus.service;

import com.anode.modbus.connection.ModbusConnection;
import com.anode.modbus.connection.ModbusConnectionException;
import com.anode.modbus.model.FunctionCode;
import com.anode.modbus.service.handlers.ModbusOperationHandler.ModbusRequest;
import com.anode.modbus.service.handlers.ModbusOperationHandler.ModbusResult;
import com.anode.modbus.service.handlers.J2ModOperationHandler;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;
import com.ghgande.j2mod.modbus.util.BitVector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("J2ModOperationHandler Tests")
@ExtendWith(MockitoExtension.class)
class J2ModOperationHandlerTest {

    @Mock
    private ModbusConnection mockConnection;

    private J2ModOperationHandler handler;

    @BeforeEach
    void setUp() {
        handler = new J2ModOperationHandler(mockConnection);
    }

    @Test
    @DisplayName("Should return failure when not connected")
    void shouldReturnFailureWhenNotConnected() {
        when(mockConnection.isConnected()).thenReturn(false);
        ModbusRequest request = createRequest("3", 1, 0, 5);

        ModbusResult result = handler.execute(request);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("Not connected");
    }

    @Test
    @DisplayName("Should execute read coils (FC 1)")
    void shouldExecuteReadCoils() throws ModbusConnectionException {
        when(mockConnection.isConnected()).thenReturn(true);
        BitVector bits = new BitVector(8);
        bits.setBit(0, true);
        when(mockConnection.readCoils(1, 0, 8)).thenReturn(bits);

        ModbusRequest request = createRequest("1", 1, 0, 8);
        ModbusResult result = handler.execute(request);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMessage()).contains("coils");
        verify(mockConnection).readCoils(1, 0, 8);
    }

    @Test
    @DisplayName("Should execute read discrete inputs (FC 2)")
    void shouldExecuteReadDiscreteInputs() throws ModbusConnectionException {
        when(mockConnection.isConnected()).thenReturn(true);
        when(mockConnection.readDiscreteInputs(1, 0, 8)).thenReturn(new BitVector(8));

        ModbusRequest request = createRequest("2", 1, 0, 8);
        ModbusResult result = handler.execute(request);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMessage()).contains("discrete inputs");
    }

    @Test
    @DisplayName("Should execute read holding registers (FC 3)")
    void shouldExecuteReadHoldingRegisters() throws ModbusConnectionException {
        when(mockConnection.isConnected()).thenReturn(true);
        com.ghgande.j2mod.modbus.procimg.Register[] regs = {
                new SimpleRegister(100),
                new SimpleRegister(200)
        };
        when(mockConnection.readHoldingRegisters(1, 0, 2)).thenReturn(regs);

        ModbusRequest request = createRequest("3", 1, 0, 2);
        ModbusResult result = handler.execute(request);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMessage()).contains("holding registers");
        assertThat(result.getData()).isNotNull();
    }

    @Test
    @DisplayName("Should execute read input registers (FC 4)")
    void shouldExecuteReadInputRegisters() throws ModbusConnectionException {
        when(mockConnection.isConnected()).thenReturn(true);
        com.ghgande.j2mod.modbus.procimg.InputRegister[] regs = {
                new SimpleRegister(100)
        };
        when(mockConnection.readInputRegisters(1, 0, 1)).thenReturn(regs);

        ModbusRequest request = createRequest("4", 1, 0, 1);
        ModbusResult result = handler.execute(request);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMessage()).contains("input registers");
    }

    @Test
    @DisplayName("Should execute write single coil (FC 5)")
    void shouldExecuteWriteSingleCoil() throws ModbusConnectionException {
        when(mockConnection.isConnected()).thenReturn(true);
        when(mockConnection.writeSingleCoil(1, 0, true)).thenReturn(true);

        ModbusRequest request = createRequest("5", 1, 0, 1);
        ModbusResult result = handler.execute(request);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMessage()).contains("coil");
    }

    @Test
    @DisplayName("Should execute write single register (FC 6)")
    void shouldExecuteWriteSingleRegister() throws ModbusConnectionException {
        when(mockConnection.isConnected()).thenReturn(true);
        when(mockConnection.writeSingleRegister(eq(1), eq(0), any())).thenReturn(1);

        ModbusRequest request = createRequest("6", 1, 0, 1);
        ModbusResult result = handler.execute(request);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMessage()).contains("register");
    }

    @Test
    @DisplayName("Should execute write multiple coils (FC 15)")
    void shouldExecuteWriteMultipleCoils() throws ModbusConnectionException {
        when(mockConnection.isConnected()).thenReturn(true);
        doNothing().when(mockConnection).writeMultipleCoils(eq(1), eq(0), any());

        ModbusRequest request = createRequest("15", 1, 0, 8);
        ModbusResult result = handler.execute(request);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMessage()).contains("coils");
    }

    @Test
    @DisplayName("Should execute write multiple registers (FC 16)")
    void shouldExecuteWriteMultipleRegisters() throws ModbusConnectionException {
        when(mockConnection.isConnected()).thenReturn(true);
        when(mockConnection.writeMultipleRegisters(eq(1), eq(0), any())).thenReturn(5);

        ModbusRequest request = createRequest("16", 1, 0, 5);
        ModbusResult result = handler.execute(request);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMessage()).contains("registers");
    }

    @Test
    @DisplayName("Should return failure for unsupported function code")
    void shouldReturnFailureForUnsupportedFunctionCode() {
        when(mockConnection.isConnected()).thenReturn(true);

        ModbusRequest request = createRequest("99", 1, 0, 1);
        ModbusResult result = handler.execute(request);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("Unsupported");
    }

    @Test
    @DisplayName("Should return failure on connection exception")
    void shouldReturnFailureOnConnectionException() throws ModbusConnectionException {
        when(mockConnection.isConnected()).thenReturn(true);
        when(mockConnection.readHoldingRegisters(anyInt(), anyInt(), anyInt()))
                .thenThrow(new ModbusConnectionException("Connection lost"));

        ModbusRequest request = createRequest("3", 1, 0, 5);
        ModbusResult result = handler.execute(request);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("Modbus error");
    }

    @Test
    @DisplayName("Should expose connection")
    void shouldExposeConnection() {
        assertThat(handler.getConnection()).isEqualTo(mockConnection);
    }

    @Test
    @DisplayName("Should throw exception for null connection")
    void shouldThrowExceptionForNullConnection() {
        assertThatThrownBy(() -> new J2ModOperationHandler(null))
                .isInstanceOf(NullPointerException.class);
    }

    private ModbusRequest createRequest(String code, int unitId, int address, int quantity) {
        FunctionCode fc = new FunctionCode(code, "TestFunction", "");
        return new ModbusRequest(fc, unitId, address, quantity);
    }
}
