package com.anode.modbus.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Accessor validation (edge cases and error conditions).
 */
class AccessorValidationTest {

    @Test
    void testNullName() {
        assertThrows(NullPointerException.class, () -> {
            Accessor.builder()
                    .name(null)
                    .function("ReadHoldingRegisters")
                    .dataClass("java.lang.Integer")
                    .address(0)
                    .build();
        });
    }

    @Test
    void testEmptyName() {
        assertThrows(IllegalArgumentException.class, () -> {
            Accessor.builder()
                    .name("   ")
                    .function("ReadHoldingRegisters")
                    .dataClass("java.lang.Integer")
                    .address(0)
                    .build();
        });
    }

    @Test
    void testNullFunction() {
        assertThrows(NullPointerException.class, () -> {
            Accessor.builder()
                    .name("test")
                    .function(null)
                    .dataClass("java.lang.Integer")
                    .address(0)
                    .build();
        });
    }

    @Test
    void testEmptyFunction() {
        assertThrows(IllegalArgumentException.class, () -> {
            Accessor.builder()
                    .name("test")
                    .function("")
                    .dataClass("java.lang.Integer")
                    .address(0)
                    .build();
        });
    }

    @Test
    void testNullDataClass() {
        assertThrows(NullPointerException.class, () -> {
            Accessor.builder()
                    .name("test")
                    .function("ReadHoldingRegisters")
                    .dataClass(null)
                    .address(0)
                    .build();
        });
    }

    @Test
    void testEmptyDataClass() {
        assertThrows(IllegalArgumentException.class, () -> {
            Accessor.builder()
                    .name("test")
                    .function("ReadHoldingRegisters")
                    .dataClass("  ")
                    .address(0)
                    .build();
        });
    }

    @Test
    void testNegativeStartAddress() {
        assertThrows(IllegalArgumentException.class, () -> {
            Accessor.builder()
                    .name("test")
                    .function("ReadHoldingRegisters")
                    .dataClass("java.lang.Integer")
                    .startAddress(-1)
                    .endAddress(10)
                    .build();
        });
    }

    @Test
    void testAddressExceedsMaximum() {
        assertThrows(IllegalArgumentException.class, () -> {
            Accessor.builder()
                    .name("test")
                    .function("ReadHoldingRegisters")
                    .dataClass("java.lang.Integer")
                    .startAddress(65536)
                    .endAddress(65537)
                    .build();
        });
    }

    @Test
    void testEndAddressLessThanStartAddress() {
        assertThrows(IllegalArgumentException.class, () -> {
            Accessor.builder()
                    .name("test")
                    .function("ReadHoldingRegisters")
                    .dataClass("java.lang.Integer")
                    .startAddress(10)
                    .endAddress(5)
                    .build();
        });
    }

    @Test
    void testMaxValidAddress() {
        Accessor accessor = Accessor.builder()
                .name("test")
                .function("ReadHoldingRegisters")
                .dataClass("java.lang.Integer")
                .address(65535)
                .build();

        assertEquals(65535, accessor.getStartAddress());
        assertEquals(65535, accessor.getEndAddress());
    }

    @Test
    void testAddressRangeNullString() {
        assertThrows(IllegalArgumentException.class, () -> {
            Accessor.builder()
                    .name("test")
                    .function("ReadHoldingRegisters")
                    .dataClass("java.lang.Integer")
                    .addressRange(null)
                    .build();
        });
    }

    @Test
    void testAddressRangeEmptyString() {
        assertThrows(IllegalArgumentException.class, () -> {
            Accessor.builder()
                    .name("test")
                    .function("ReadHoldingRegisters")
                    .dataClass("java.lang.Integer")
                    .addressRange("   ")
                    .build();
        });
    }

    @Test
    void testAddressRangeInvalidFormat() {
        assertThrows(IllegalArgumentException.class, () -> {
            Accessor.builder()
                    .name("test")
                    .function("ReadHoldingRegisters")
                    .dataClass("java.lang.Integer")
                    .addressRange("abc")
                    .build();
        });
    }

    @Test
    void testAddressRangeInvalidRange() {
        assertThrows(IllegalArgumentException.class, () -> {
            Accessor.builder()
                    .name("test")
                    .function("ReadHoldingRegisters")
                    .dataClass("java.lang.Integer")
                    .addressRange("10-5")
                    .build();
        });
    }

    @Test
    void testAddressRangeTooManyParts() {
        assertThrows(IllegalArgumentException.class, () -> {
            Accessor.builder()
                    .name("test")
                    .function("ReadHoldingRegisters")
                    .dataClass("java.lang.Integer")
                    .addressRange("1-2-3")
                    .build();
        });
    }

    @Test
    void testAddressRangeNegativeValue() {
        assertThrows(IllegalArgumentException.class, () -> {
            Accessor.builder()
                    .name("test")
                    .function("ReadHoldingRegisters")
                    .dataClass("java.lang.Integer")
                    .addressRange("-1-10")
                    .build();
        });
    }

    @Test
    void testAddressRangeExceedsMaximum() {
        assertThrows(IllegalArgumentException.class, () -> {
            Accessor.builder()
                    .name("test")
                    .function("ReadHoldingRegisters")
                    .dataClass("java.lang.Integer")
                    .addressRange("65536-65540")
                    .build();
        });
    }

    @Test
    void testValidAddressRangeAtBoundary() {
        Accessor accessor = Accessor.builder()
                .name("test")
                .function("ReadHoldingRegisters")
                .dataClass("java.lang.Integer")
                .addressRange("65530-65535")
                .build();

        assertEquals(65530, accessor.getStartAddress());
        assertEquals(65535, accessor.getEndAddress());
        assertEquals(6, accessor.getRegisterCount());
    }
}
