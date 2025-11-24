package com.anode.modbus.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

@DisplayName("FunctionCode Tests")
class FunctionCodeTest {

    @Test
    @DisplayName("Should create FunctionCode with all properties")
    void shouldCreateFunctionCodeWithAllProperties() {
        FunctionCode fc = new FunctionCode("3", "ReadHoldingRegisters", "Reads holding registers");

        assertThat(fc.getCode()).isEqualTo("3");
        assertThat(fc.getName()).isEqualTo("ReadHoldingRegisters");
        assertThat(fc.getDescription()).isEqualTo("Reads holding registers");
    }

    @Test
    @DisplayName("Should handle null description")
    void shouldHandleNullDescription() {
        FunctionCode fc = new FunctionCode("1", "ReadCoils", null);

        assertThat(fc.getDescription()).isEqualTo("");
    }

    @Test
    @DisplayName("Should throw exception for null code")
    void shouldThrowExceptionForNullCode() {
        assertThatThrownBy(() -> new FunctionCode(null, "Name", "Desc"))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("code");
    }

    @Test
    @DisplayName("Should throw exception for null name")
    void shouldThrowExceptionForNullName() {
        assertThatThrownBy(() -> new FunctionCode("1", null, "Desc"))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("name");
    }

    @Test
    @DisplayName("Should be equal when codes match")
    void shouldBeEqualWhenCodesMatch() {
        FunctionCode fc1 = new FunctionCode("3", "ReadHoldingRegisters", "Desc1");
        FunctionCode fc2 = new FunctionCode("3", "DifferentName", "Desc2");

        assertThat(fc1).isEqualTo(fc2);
        assertThat(fc1.hashCode()).isEqualTo(fc2.hashCode());
    }

    @Test
    @DisplayName("Should not be equal when codes differ")
    void shouldNotBeEqualWhenCodesDiffer() {
        FunctionCode fc1 = new FunctionCode("3", "ReadHoldingRegisters", "Desc");
        FunctionCode fc2 = new FunctionCode("4", "ReadHoldingRegisters", "Desc");

        assertThat(fc1).isNotEqualTo(fc2);
    }

    @Test
    @DisplayName("Should have meaningful toString")
    void shouldHaveMeaningfulToString() {
        FunctionCode fc = new FunctionCode("3", "ReadHoldingRegisters", "Desc");

        assertThat(fc.toString())
                .contains("3")
                .contains("ReadHoldingRegisters");
    }
}
