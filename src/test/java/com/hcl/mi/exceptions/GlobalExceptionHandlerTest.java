package com.hcl.mi.exceptions;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcl.mi.requestdtos.ErrorResponseDto;

/**
 * Tests GlobalExceptionHandler using a lightweight test controller
 * that throws the exceptions handled by the advice.
 */
class GlobalExceptionHandlerTest {
 
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private TestThrowingController testController; 
    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();

        testController = new TestThrowingController();
        exceptionHandler = new GlobalExceptionHandler();

        mockMvc = MockMvcBuilders
                .standaloneSetup(testController)
                .setControllerAdvice(exceptionHandler)
                .build();
    }


    @Test
    void handleRuntimeException_Returns400WithErrorsArray() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/test/throw-runtime"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value("Bad runtime"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void handleGenericException_Returns400WithErrorsArray() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/test/throw-exception"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value("Checked exception"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @RestController
    @RequestMapping("/test")
    static class TestThrowingController {

        @GetMapping("/throw-duplicate")
        public String throwDuplicate() {
            throw new DuplicateCharacteristicException("Characteristic 'LENGTH' already exists");
        }

        @GetMapping("/throw-already-exists")
        public String throwAlreadyExists() {
            throw new GenericAlreadyExistsException("Vendor already exists");
        }

        @GetMapping("/throw-not-found")
        public String throwNotFound() {
            throw new GenericNotFoundException("Material not found with id: M404");
        }

        @GetMapping("/throw-runtime")
        public String throwRuntime() {
            throw new RuntimeException("Bad runtime");
        }

        @GetMapping("/throw-exception")
        public String throwException() throws Exception {
            throw new Exception("Checked exception");
        }

        @PostMapping("/validate")
        public Map<String, String> validate(@Valid @RequestBody TestValidatedDto dto) {
            if (dto.getFromDate() != null && dto.getToDate() != null && dto.getFromDate().isAfter(dto.getToDate())) {
                           }
            return Map.of("ok", "true");
        } 
    }

    static class TestValidatedDto {
        @NotBlank
        private String name;

        @Size(min = 3)
        private String desc;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate fromDate;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate toDate;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDesc() { return desc; }
        public void setDesc(String desc) { this.desc = desc; }
        public LocalDate getFromDate() { return fromDate; }
        public void setFromDate(LocalDate fromDate) { this.fromDate = fromDate; }
        public LocalDate getToDate() { return toDate; }
        public void setToDate(LocalDate toDate) { this.toDate = toDate; }
    }
}