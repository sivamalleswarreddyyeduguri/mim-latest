package com.hcl.mi.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcl.mi.requestdtos.NewUser;
import com.hcl.mi.responsedtos.ResponseDto;
import com.hcl.mi.services.UserService;

class AdminControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private AdminController adminController;
    
    private MockMvc mockMvc;

    private NewUser newUser;
    private ObjectMapper objectMapper;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();
        objectMapper = new ObjectMapper();
        newUser = new NewUser();
        newUser.setUsername("inspector1");
        newUser.setPassword("Pass@123");
        newUser.setEmail("insp1@example.com");
        newUser.setMobileNum("9876543210");
    }

    @Test
    void testCreateInspector_Success() {
        doNothing().when(userService).saveUser(any(NewUser.class));

        ResponseEntity<ResponseDto> response = adminController.createInspector(newUser);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("201", response.getBody().getStatusCode());
        assertEquals("registration successfull", response.getBody().getStatusMsg());
        assertEquals("INSPECTOR", newUser.getRole());
        verify(userService, times(1)).saveUser(any(NewUser.class)); 
    }

    @Test
    void testCreateInspector_Exception() {
        doThrow(new RuntimeException("Database error")).when(userService).saveUser(any(NewUser.class));

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> adminController.createInspector(newUser));

        assertEquals("Database error", exception.getMessage());
        verify(userService, times(1)).saveUser(any(NewUser.class));
    }
 
    @Test
    void testCreateInspector_AlwaysSetsRoleToInspector() {
        newUser.setRole("ADMIN"); // even if someone tries to pass ADMIN
        doNothing().when(userService).saveUser(any(NewUser.class));

        ResponseEntity<ResponseDto> response = adminController.createInspector(newUser);

        assertEquals("INSPECTOR", newUser.getRole());
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(userService, times(1)).saveUser(any(NewUser.class));
    }
    
    @Test
    void testUpdateUser_Success() throws Exception {
        NewUser dto = new NewUser();
        dto.setUsername("newname");
        dto.setEmail("new@mail.com");
        dto.setMobileNum("12345");
        dto.setPassword("New@123");

        doNothing().when(userService).updateUser(eq(10), any(NewUser.class));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .put("/api/v1/admin/update/{id}", 10)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("200"))
                .andExpect(jsonPath("$.statusMsg").value("user updated successfully"));

        verify(userService).updateUser(eq(10), argThat(n ->
                "newname".equals(n.getUsername()) &&
                "new@mail.com".equals(n.getEmail()) &&
                "12345".equals(n.getMobileNum()) &&
                "New@123".equals(n.getPassword())
        ));
    }
}