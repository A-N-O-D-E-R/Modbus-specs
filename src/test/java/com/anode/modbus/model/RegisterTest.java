package com.anode.modbus.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Register Tests")
class RegisterTest {

    @Test
    @DisplayName("Should create Register with all properties")
    void shouldCreateRegisterWithAllProperties() {
        Register reg = new Register("Temperature", 100, "uint16", "RW");

        assertThat(reg.getName()).isEqualTo("Temperature");
        assertThat(reg.getAddress()).isEqualTo(100);
        assertThat(reg.getDataType()).isEqualTo("uint16");
        assertThat(reg.getAccess()).isEqualTo("RW");
    }

    @Test
    @DisplayName("Should handle null dataType and access")
    void shouldHandleNullDataTypeAndAccess() {
        Register reg = new Register("Test", 0, null, null);

        assertThat(reg.getDataType()).isEqualTo("");
        assertThat(reg.getAccess()).isEqualTo("");
    }

    @Test
    @DisplayName("Should throw exception for null name")
    void shouldThrowExceptionForNullName() {
        assertThatThrownBy(() -> new Register(null, 0, "uint16", "R"))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("name");
    }

    @ParameterizedTest
    @DisplayName("Should correctly identify readable registers")
    @CsvSource({
            "R, true",
            "RW, true",
            "Read-only, true",
            "ReadWrite, true",
            "W, false",
            "Write-only, false",
            "'', false"
    })
    void shouldCorrectlyIdentifyReadableRegisters(String access, boolean expected) {
        Register reg = new Register("Test", 0, "uint16", access);

        assertThat(reg.isReadable()).isEqualTo(expected);
    }

    @ParameterizedTest
    @DisplayName("Should correctly identify writable registers")
    @CsvSource({
            "W, true",
            "RW, true",
            "Write-only, true",
            "ReadWrite, true",
            "R, false",
            "Read-only, false",
            "'', false"
    })
    void shouldCorrectlyIdentifyWritableRegisters(String access, boolean expected) {
        Register reg = new Register("Test", 0, "uint16", access);

        assertThat(reg.isWritable()).isEqualTo(expected);
    }

    @Test
    @DisplayName("Should be equal when name and address match")
    void shouldBeEqualWhenNameAndAddressMatch() {
        Register reg1 = new Register("Temp", 100, "uint16", "R");
        Register reg2 = new Register("Temp", 100, "int16", "RW");

        assertThat(reg1).isEqualTo(reg2);
        assertThat(reg1.hashCode()).isEqualTo(reg2.hashCode());
    }

    @Test
    @DisplayName("Should not be equal when address differs")
    void shouldNotBeEqualWhenAddressDiffers() {
        Register reg1 = new Register("Temp", 100, "uint16", "R");
        Register reg2 = new Register("Temp", 101, "uint16", "R");

        assertThat(reg1).isNotEqualTo(reg2);
    }

    @Test
    @DisplayName("Should have meaningful toString")
    void shouldHaveMeaningfulToString() {
        Register reg = new Register("Temperature", 100, "uint16", "RW");

        assertThat(reg.toString())
                .contains("Temperature")
                .contains("100")
                .contains("uint16")
                .contains("RW");
    }
}
