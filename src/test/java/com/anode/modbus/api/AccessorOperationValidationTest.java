package com.anode.modbus.api;

import com.anode.modbus.connection.ModbusConnection;
import com.anode.modbus.model.Accessor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for AccessorOperation validation.
 */
class AccessorOperationValidationTest {

    @Test
    void testNullAccessorInConstructor() {
        assertThrows(NullPointerException.class, () -> {
            new AccessorOperation(null, () -> null);
        });
    }

    @Test
    void testNullConnectionSupplierInConstructor() {
        Accessor accessor = Accessor.builder()
                .name("test")
                .function("ReadHoldingRegisters")
                .dataClass("java.lang.Integer")
                .address(0)
                .build();

        assertThrows(NullPointerException.class, () -> {
            new AccessorOperation(accessor, null);
        });
    }

    @Test
    void testUnitIdTooLow() {
        Accessor accessor = Accessor.builder()
                .name("test")
                .function("ReadHoldingRegisters")
                .dataClass("java.lang.Integer")
                .address(0)
                .build();

        AccessorOperation op = new AccessorOperation(accessor, () -> null);

        assertThrows(IllegalArgumentException.class, () -> {
            op.unitId(-1);
        });
    }

    @Test
    void testUnitIdTooHigh() {
        Accessor accessor = Accessor.builder()
                .name("test")
                .function("ReadHoldingRegisters")
                .dataClass("java.lang.Integer")
                .address(0)
                .build();

        AccessorOperation op = new AccessorOperation(accessor, () -> null);

        assertThrows(IllegalArgumentException.class, () -> {
            op.unitId(248);
        });
    }

    @Test
    void testValidUnitIdRange() {
        Accessor accessor = Accessor.builder()
                .name("test")
                .function("ReadHoldingRegisters")
                .dataClass("java.lang.Integer")
                .address(0)
                .build();

        AccessorOperation op = new AccessorOperation(accessor, () -> null);

        // Should not throw
        op.unitId(0);
        op.unitId(1);
        op.unitId(127);
        op.unitId(247);
    }

    @Test
    void testWriteNullIntArray() {
        Accessor accessor = Accessor.builder()
                .name("test")
                .function("WriteMultipleRegisters")
                .dataClass("java.lang.Integer[]")
                .addressRange("0-10")
                .build();

        AccessorOperation op = new AccessorOperation(accessor, () -> null);

        assertThrows(IllegalArgumentException.class, () -> {
            op.write((int[]) null);
        });
    }

    @Test
    void testWriteNullBooleanArray() {
        Accessor accessor = Accessor.builder()
                .name("test")
                .function("WriteMultipleCoils")
                .dataClass("java.lang.Boolean[]")
                .addressRange("0-10")
                .build();

        AccessorOperation op = new AccessorOperation(accessor, () -> null);

        assertThrows(IllegalArgumentException.class, () -> {
            op.write((boolean[]) null);
        });
    }

    @Test
    void testWriteArrayLengthMismatchTooFew() {
        Accessor accessor = Accessor.builder()
                .name("test")
                .function("WriteMultipleRegisters")
                .dataClass("java.lang.Integer[]")
                .addressRange("0-10")  // 11 registers
                .build();

        AccessorOperation op = new AccessorOperation(accessor, () -> null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            op.write(new int[]{1, 2, 3});  // Only 3 values
        });

        assertTrue(ex.getMessage().contains("expects 11"));
        assertTrue(ex.getMessage().contains("got 3"));
    }

    @Test
    void testWriteArrayLengthMismatchTooMany() {
        Accessor accessor = Accessor.builder()
                .name("test")
                .function("WriteMultipleRegisters")
                .dataClass("java.lang.Integer[]")
                .addressRange("0-2")  // 3 registers
                .build();

        AccessorOperation op = new AccessorOperation(accessor, () -> null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            op.write(new int[]{1, 2, 3, 4, 5});  // 5 values
        });

        assertTrue(ex.getMessage().contains("expects 3"));
        assertTrue(ex.getMessage().contains("got 5"));
    }

    @Test
    void testWriteBooleanArrayLengthMismatch() {
        Accessor accessor = Accessor.builder()
                .name("test")
                .function("WriteMultipleCoils")
                .dataClass("java.lang.Boolean[]")
                .addressRange("0-7")  // 8 coils
                .build();

        AccessorOperation op = new AccessorOperation(accessor, () -> null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            op.write(new boolean[]{true, false});  // Only 2 values
        });

        assertTrue(ex.getMessage().contains("expects 8"));
        assertTrue(ex.getMessage().contains("got 2"));
    }
}
