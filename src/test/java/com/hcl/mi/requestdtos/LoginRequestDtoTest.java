package com.hcl.mi.requestdtos;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class LoginRequestDtoPojoTest {

    @Test
    void gettersSetters_work() {
        LoginRequestDto dto = new LoginRequestDto();
        dto.setUsername("siva");
        dto.setPassword("secret");

        assertEquals("siva", dto.getUsername());
        assertEquals("secret", dto.getPassword());
    }
}
