package com.hcl.mi.requestdtos;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ErrorResponseDtoTest {

    @Test
    void gettersSetters_work() {
        ErrorResponseDto dto = new ErrorResponseDto();
        dto.setApiPath("/api/test");
        dto.setErrorCode(HttpStatus.BAD_REQUEST);
        dto.setErrorMessage("Something went wrong");
        dto.setErrorTime(LocalDateTime.now());

        assertEquals("/api/test", dto.getApiPath());
        assertEquals(HttpStatus.BAD_REQUEST, dto.getErrorCode());
        assertEquals("Something went wrong", dto.getErrorMessage());
        assertNotNull(dto.getErrorTime());
    }

    @Test
    void allArgsConstructor_works() {
        LocalDateTime now = LocalDateTime.now();
        ErrorResponseDto dto = new ErrorResponseDto("/x", HttpStatus.NOT_FOUND, "Missing", now);
        assertEquals("/x", dto.getApiPath());
        assertEquals(HttpStatus.NOT_FOUND, dto.getErrorCode());
        assertEquals("Missing", dto.getErrorMessage());
        assertEquals(now, dto.getErrorTime());
    }
}