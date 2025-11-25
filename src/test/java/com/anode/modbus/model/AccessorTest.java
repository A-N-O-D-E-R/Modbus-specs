package com.anode.modbus.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AccessorTest {

    @Test
    void testBasicAccessorCreation() {
        Accessor accessor = Accessor.builder()
                .name("getTemperature")
                .function("ReadHoldingRegisters")
                .dataClass("java.lang.Double")
                .startAddress(1)
                .endAddress(2)
                .build();

        assertEquals("getTemperature", accessor.getName());
        assertEquals("ReadHoldingRegisters", accessor.getFunction());
        assertEquals("java.lang.Double", accessor.getDataClass());
        assertEquals(1, accessor.getStartAddress());
        assertEquals(2, accessor.getEndAddress());
        assertEquals(2, accessor.getRegisterCount());
    }

    @Test
    void testAccessorWithSingleRegister() {
        Accessor accessor = Accessor.builder()
                .name("getTurbidity")
                .function("ReadHoldingRegisters")
                .dataClass("java.lang.Integer")
                .address(35)
                .build();

        assertEquals(35, accessor.getStartAddress());
        assertEquals(35, accessor.getEndAddress());
        assertEquals(1, accessor.getRegisterCount());
    }

    @Test
    void testAccessorWithAddressRange() {
        Accessor accessor = Accessor.builder()
                .name("getAllTemperatures")
                .function("ReadHoldingRegisters")
                .dataClass("java.lang.Integer[]")
                .addressRange("39-42")
                .build();

        assertEquals(39, accessor.getStartAddress());
        assertEquals(42, accessor.getEndAddress());
        assertEquals(4, accessor.getRegisterCount());
    }

    @Test
    void testAccessorWithSingleAddressRange() {
        Accessor accessor = Accessor.builder()
                .name("getTemperature1")
                .function("ReadHoldingRegisters")
                .dataClass("java.lang.Integer")
                .addressRange("39")
                .build();

        assertEquals(39, accessor.getStartAddress());
        assertEquals(39, accessor.getEndAddress());
        assertEquals(1, accessor.getRegisterCount());
    }

    @Test
    void testIsReadFunction() {
        Accessor readAccessor = Accessor.builder()
                .name("getTemperature")
                .function("ReadHoldingRegisters")
                .dataClass("java.lang.Integer")
                .address(39)
                .build();

        assertTrue(readAccessor.isReadFunction());
        assertFalse(readAccessor.isWriteFunction());
    }

    @Test
    void testIsWriteFunction() {
        Accessor writeAccessor = Accessor.builder()
                .name("setValvePosition")
                .function("WriteMultipleRegisters")
                .dataClass("java.lang.Integer[]")
                .addressRange("70-85")
                .build();

        assertFalse(writeAccessor.isReadFunction());
        assertTrue(writeAccessor.isWriteFunction());
    }

    @Test
    void testInvalidAddressRange() {
        assertThrows(IllegalArgumentException.class, () -> {
            Accessor.builder()
                    .name("invalid")
                    .function("ReadHoldingRegisters")
                    .dataClass("java.lang.Integer")
                    .startAddress(10)
                    .endAddress(5)
                    .build();
        });
    }

    @Test
    void testNegativeAddress() {
        assertThrows(IllegalArgumentException.class, () -> {
            Accessor.builder()
                    .name("invalid")
                    .function("ReadHoldingRegisters")
                    .dataClass("java.lang.Integer")
                    .startAddress(-1)
                    .endAddress(2)
                    .build();
        });
    }

    @Test
    void testAccessorEquality() {
        Accessor accessor1 = Accessor.builder()
                .name("getTemperature")
                .function("ReadHoldingRegisters")
                .dataClass("java.lang.Integer")
                .addressRange("39-42")
                .build();

        Accessor accessor2 = Accessor.builder()
                .name("getTemperature")
                .function("ReadHoldingRegisters")
                .dataClass("java.lang.Integer")
                .addressRange("39-42")
                .build();

        assertEquals(accessor1, accessor2);
        assertEquals(accessor1.hashCode(), accessor2.hashCode());
    }
}
