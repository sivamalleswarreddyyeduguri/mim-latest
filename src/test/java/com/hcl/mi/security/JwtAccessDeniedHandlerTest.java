package com.hcl.mi.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtAccessDeniedHandlerTest {

    private JwtAccessDeniedHandler handler;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        handler = new JwtAccessDeniedHandler();
        mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    void handle_shouldReturn403WithExpectedJsonBody_whenAccessDenied() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/admin/secure-resource");

        MockHttpServletResponse response = new MockHttpServletResponse();
        AccessDeniedException ex = new AccessDeniedException("Insufficient privileges");

        handler.handle(request, response, ex);

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentType()).isEqualTo("application/json");

        String json = response.getContentAsString(StandardCharsets.UTF_8);
        JsonNode root = mapper.readTree(json);

        assertThat(root.get("apiPath").asText()).isEqualTo("/api/admin/secure-resource");
        assertThat(root.get("errorMessage").asText())
                .isEqualTo("You don’t have permission to access this resource");

        assertThat(root.get("errorCode").asText()).isEqualTo("FORBIDDEN");
        assertThat(root.get("errorTime").isTextual()).isTrue();
        String errorTimeText = root.get("errorTime").asText();

  
        OffsetDateTime parsed = OffsetDateTime.parse(
                              normalizeToOffset(errorTimeText)
        );
        assertThat(parsed).isNotNull();
    }

    @Test
    void handle_shouldNotThrow_whenExceptionHasNullMessage() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/user/resource");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AccessDeniedException ex = new AccessDeniedException(null);

        handler.handle(request, response, ex);

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentType()).isEqualTo("application/json");

        String json = response.getContentAsString(StandardCharsets.UTF_8);
        JsonNode root = mapper.readTree(json);

        assertThat(root.get("apiPath").asText()).isEqualTo("/api/user/resource");
        assertThat(root.get("errorCode").asText()).isEqualTo("FORBIDDEN");
        assertThat(root.get("errorMessage").asText())
                .isEqualTo("You don’t have permission to access this resource");
        assertThat(root.get("errorTime").isTextual()).isTrue();
    }


    /**
     * Helper: try to parse LocalDateTime-like strings by appending 'Z' if no zone is present.
     * If the string already has offset info, we keep it as-is.
     */
    private static String normalizeToOffset(String dt) {
        if (dt.endsWith("Z") || dt.matches(".*[+-]\\d{2}:\\d{2}$")) {
            return dt;
        }
        if (dt.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}(:\\d{2}(\\.\\d{1,9})?)?")) {
            return dt + "Z";
        }
        throw new DateTimeParseException("Unsupported date format", dt, 0);
    }
}
