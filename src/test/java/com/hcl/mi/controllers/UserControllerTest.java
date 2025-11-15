package com.hcl.mi.controllers;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcl.mi.entities.User;
import com.hcl.mi.requestdtos.LoginRequestDto;
import com.hcl.mi.requestdtos.NewUser;
import com.hcl.mi.responsedtos.ResponseDto;
import com.hcl.mi.security.JwtUtil;
import com.hcl.mi.services.UserService;

class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks 
    private UserController userController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    
    private NewUser newUser;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        userController = new UserController(userService, userDetailsService, jwtUtil);
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper();
        
        newUser = new NewUser();
        newUser.setUsername("john");
        newUser.setPassword("Strong@123");
        newUser.setEmail("john@example.com");
        newUser.setMobileNum("9876543210");
        newUser.setRole("USER");
        newUser.setId(4); 
    }

    @Test
    void testRegisterUser() {
        doNothing().when(userService).saveUser(any(NewUser.class));

        ResponseEntity<ResponseDto> response = userController.register(newUser);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("201", response.getBody().getStatusCode());
        assertEquals("user registered successfully", response.getBody().getStatusMsg());
        verify(userService, times(1)).saveUser(any(NewUser.class));
    }

    @Test
    void testLogin_Success() throws Exception {
        LoginRequestDto req = new LoginRequestDto();
        req.setUsername("siva");
        req.setPassword("Plain@123");

        User userEntity = User.builder()
                .id(101)
                .username("siva")
                .email("siva@example.com")
                .mobileNum("9999999999")
                .password("ENC")
                .role("ADMIN") 
                .build();

        when(userService.checkUserCredentails("siva", "Plain@123")).thenReturn(userEntity);

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("siva")
                .password("ENC")
                .roles("ADMIN") 
                .build();

        when(userDetailsService.loadUserByUsername("siva")).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn("access-token-123");
        when(jwtUtil.generateRefreshToken(userDetails)).thenReturn("refresh-token-456");

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/v1/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value("access-token-123"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token-456"))
                .andExpect(jsonPath("$.username").value("siva"))
                .andExpect(jsonPath("$.email").value("siva@example.com"))
                .andExpect(jsonPath("$.mobileNumber").value("9999999999"))
                .andExpect(jsonPath("$.id").value(101))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_ADMIN"));

        verify(userService).checkUserCredentails("siva", "Plain@123");
        verify(userDetailsService).loadUserByUsername("siva");
        verify(jwtUtil).generateToken(userDetails);
        verify(jwtUtil).generateRefreshToken(userDetails);
    }

    @Test
    void testLogin_InvalidCredentials() throws Exception {
        LoginRequestDto req = new LoginRequestDto();
        req.setUsername("siva");
        req.setPassword("wrong");

        when(userService.checkUserCredentails("siva", "wrong")).thenReturn(null);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/v1/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Invalid credentials"));

        verify(userService).checkUserCredentails("siva", "wrong");
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(jwtUtil, never()).generateToken(any());
        verify(jwtUtil, never()).generateRefreshToken(any());
    }

    @Test
    void testRefreshToken_Success() throws Exception {
        Map<String, String> body = Map.of("refreshToken", "refresh-token-456");

        when(jwtUtil.extractUsername("refresh-token-456")).thenReturn("siva");
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("siva").password("ENC").roles("USER").build();
        when(userDetailsService.loadUserByUsername("siva")).thenReturn(userDetails);
        when(jwtUtil.validateToken("refresh-token-456", userDetails)).thenReturn(true);
        when(jwtUtil.generateToken(userDetails)).thenReturn("new-access-token-789");

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/v1//user/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token-789"))
                .andExpect(jsonPath("$.username").value("siva"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));

        verify(jwtUtil).extractUsername("refresh-token-456");
        verify(userDetailsService).loadUserByUsername("siva");
        verify(jwtUtil).validateToken("refresh-token-456", userDetails);
        verify(jwtUtil).generateToken(userDetails);
    }

    @Test
    void testRefreshToken_MissingToken() throws Exception {
        Map<String, String> body = Map.of();

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/v1/user/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Refresh token is missing"));
    }

    @Test
    void testRefreshToken_InvalidOrExpired() throws Exception {
        Map<String, String> body = Map.of("refreshToken", "bad-token");

        when(jwtUtil.extractUsername("bad-token")).thenReturn("siva");
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("siva").password("ENC").roles("USER").build();
        when(userDetailsService.loadUserByUsername("siva")).thenReturn(userDetails);
        when(jwtUtil.validateToken("bad-token", userDetails)).thenReturn(false);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/v1/user/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid or expired refresh token"));
    }

    @Test
    void testRefreshToken_TokenProcessingFailed() throws Exception {
        Map<String, String> body = Map.of("refreshToken", "boom");
        when(jwtUtil.extractUsername("boom")).thenThrow(new RuntimeException("decode failure"));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/v1/user/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Token processing failed"));
    }

    @Test
    void testGetAllUsers_Success() throws Exception {
        NewUser u1 = new NewUser();
        u1.setId(1); u1.setUsername("alice"); u1.setEmail("alice@mail.com"); u1.setRole("USER");
        NewUser u2 = new NewUser();
        u2.setId(2); u2.setUsername("bob"); u2.setEmail("bob@mail.com"); u2.setRole("ADMIN");

        when(userService.getAllUsers()).thenReturn(List.of(u1, u2));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/v1/user/get-all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("alice"))
                .andExpect(jsonPath("$[1].username").value("bob"));

        verify(userService).getAllUsers();
    }

    
} 
