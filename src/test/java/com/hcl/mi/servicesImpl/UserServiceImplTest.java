package com.hcl.mi.servicesImpl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.hcl.mi.entities.User;
import com.hcl.mi.exceptions.GenericAlreadyExistsException;
import com.hcl.mi.repositories.UserRepository;
import com.hcl.mi.requestdtos.NewUser;

class UserServiceImplTest {

    @Mock
    private UserRepository userRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private NewUser newUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        newUser = new NewUser();
        newUser.setUsername("Siva");         
        newUser.setPassword("Plain@123"); 
        newUser.setEmail("Siva@Example.com");
        newUser.setMobileNum("9999999999");
        newUser.setRole("admin");           
    }

    @Test
    void testSaveUser_Success_WithProvidedRole() {
        when(userRepo.findByUsernameOrEmail("siva", "siva@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Plain@123")).thenReturn("ENC(P@123)");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        userService.saveUser(newUser);

        verify(userRepo).findByUsernameOrEmail("siva", "siva@example.com");
        verify(passwordEncoder).encode("Plain@123");
        verify(userRepo).save(captor.capture());

        User saved = captor.getValue();
        assertEquals("siva", saved.getUsername(), "Username should be lowercased");
        assertEquals("siva@example.com", saved.getEmail(), "Email should be lowercased");
        assertEquals("ENC(P@123)", saved.getPassword(), "Password should be encoded");
        assertEquals("ADMIN", saved.getRole(), "Provided role should be uppercased");
        assertEquals("9999999999", saved.getMobileNum());
    }

    @Test
    void testSaveUser_Success_DefaultRoleWhenNullOrBlank() {
        NewUser dto = new NewUser();
        dto.setUsername("NewUser");
        dto.setPassword("Secret#1");
        dto.setEmail("NewUser@Mail.COM");
        dto.setMobileNum("8888888888");
        dto.setRole("   "); // blank role -> default to USER

        when(userRepo.findByUsernameOrEmail("newuser", "newuser@mail.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Secret#1")).thenReturn("ENC(S#1)");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        userService.saveUser(dto);

        verify(userRepo).findByUsernameOrEmail("newuser", "newuser@mail.com");
        verify(passwordEncoder).encode("Secret#1");
        verify(userRepo).save(captor.capture());

        User saved = captor.getValue();
        assertEquals("USER", saved.getRole(), "Should assign default USER role");
        assertEquals("newuser", saved.getUsername());
        assertEquals("newuser@mail.com", saved.getEmail());
        assertEquals("ENC(S#1)", saved.getPassword());
    }

    @Test
    void testSaveUser_UsernameAlreadyExists() {
        User existing = User.builder()
                .id(1).username("siva").email("someone@else.com")
                .password("x").role("USER").build();

        when(userRepo.findByUsernameOrEmail("siva", "siva@example.com"))
                .thenReturn(Optional.of(existing));

        GenericAlreadyExistsException ex = assertThrows(GenericAlreadyExistsException.class,
                () -> userService.saveUser(newUser));

        assertEquals("Username already exists", ex.getMessage());
        verify(userRepo, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void testSaveUser_EmailAlreadyExists() {
        User existing = User.builder()
                .id(2).username("other").email("siva@example.com")
                .password("x").role("USER").build();

        when(userRepo.findByUsernameOrEmail("siva", "siva@example.com"))
                .thenReturn(Optional.of(existing));

        GenericAlreadyExistsException ex = assertThrows(GenericAlreadyExistsException.class,
                () -> userService.saveUser(newUser));

        assertEquals("Email already exists", ex.getMessage());
        verify(userRepo, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void testLoadUserByUsername_Success() {
        User u = User.builder()
                .id(1).username("siva").password("ENC(P@123)").email("siva@example.com")
                .mobileNum("9999999999").role("ADMIN").build();

        when(userRepo.findByUsername("siva")).thenReturn(Optional.of(u));

        UserDetails ud = userService.loadUserByUsername("Siva"); 

        assertEquals("siva", ud.getUsername());
        assertEquals("ENC(P@123)", ud.getPassword());
        assertTrue(ud.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        verify(userRepo).findByUsername("siva");
    }

    @Test
    void testLoadUserByUsername_NotFound() {
        when(userRepo.findByUsername("unknown")).thenReturn(Optional.empty());

        UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
                () -> userService.loadUserByUsername("unknown"));

        assertEquals("User not found: unknown", ex.getMessage());
    }

    @Test
    void testCheckUserCredentails_Success() {
        User u = User.builder()
                .id(1).username("siva").password("ENC(P@123)").email("siva@example.com")
                .mobileNum("9999999999").role("USER").build();

        when(userRepo.findByUsername("siva")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("Plain@123", "ENC(P@123)")).thenReturn(true);

        User out = userService.checkUserCredentails("siva", "Plain@123");

        assertNotNull(out);
        assertEquals("siva", out.getUsername());
        verify(userRepo).findByUsername("siva");
        verify(passwordEncoder).matches("Plain@123", "ENC(P@123)");
    }

    @Test
    void testCheckUserCredentails_WrongPassword() {
        User u = User.builder().id(1).username("siva").password("ENC(P@123)").build();

        when(userRepo.findByUsername("siva")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("Wrong", "ENC(P@123)")).thenReturn(false);

        User out = userService.checkUserCredentails("siva", "Wrong");

        assertNull(out);
        verify(passwordEncoder).matches("Wrong", "ENC(P@123)");
    }

    @Test
    void testCheckUserCredentails_UserNotFound() {
        when(userRepo.findByUsername("nope")).thenReturn(Optional.empty());

        User out = userService.checkUserCredentails("nope", "any");

        assertNull(out);
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void testGetAllUsers() {
        User u1 = User.builder().id(1).username("alice").password("ENC1").email("alice@mail.com").mobileNum("111").role("USER").build();
        User u2 = User.builder().id(2).username("bob").password("ENC2").email("bob@mail.com").mobileNum("222").role("ADMIN").build();

        when(userRepo.findAll()).thenReturn(List.of(u1, u2));

        List<NewUser> list = userService.getAllUsers();

        assertEquals(2, list.size());
        assertEquals("alice", list.get(0).getUsername());
        assertEquals("ENC1", list.get(0).getPassword()); 
        assertEquals("bob", list.get(1).getUsername());
        assertEquals("ADMIN", list.get(1).getRole());
        verify(userRepo).findAll();
    }

    @Test
    void testUpdateUser_Success_WithPasswordChange() {
        User existing = User.builder()
                .id(10).username("olduser").password("OLD").email("old@mail.com").mobileNum("000").role("USER").build();

        when(userRepo.findById(10)).thenReturn(Optional.of(existing));
        when(passwordEncoder.encode("New@123")).thenReturn("ENC(NEW)");

        NewUser dto = new NewUser();
        dto.setUsername("NewName");       
        dto.setEmail("New@Mail.com");
        dto.setMobileNum("12345");
        dto.setPassword("New@123");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        userService.updateUser(10, dto);

        verify(passwordEncoder).encode("New@123");
        verify(userRepo).save(captor.capture());

        User saved = captor.getValue();
        assertEquals("NewName", saved.getUsername());       
        assertEquals("New@Mail.com", saved.getEmail());
        assertEquals("12345", saved.getMobileNum());
        assertEquals("ENC(NEW)", saved.getPassword());         
    }

    @Test
    void testUpdateUser_Success_WithoutPasswordChange() {
        User existing = User.builder()
                .id(11).username("old").password("KEEP").email("old@mail.com").mobileNum("000").role("USER").build();

        when(userRepo.findById(11)).thenReturn(Optional.of(existing));

        NewUser dto = new NewUser();
        dto.setUsername("KeepName");
        dto.setEmail("keep@mail.com");
        dto.setMobileNum("777");
        dto.setPassword(null); // no change

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        userService.updateUser(11, dto);

        verify(passwordEncoder, never()).encode(any());
        verify(userRepo).save(captor.capture());

        User saved = captor.getValue();
        assertEquals("KEEP", saved.getPassword(), "Password should remain unchanged when dto.password is null");
        assertEquals("KeepName", saved.getUsername());
        assertEquals("keep@mail.com", saved.getEmail());
        assertEquals("777", saved.getMobileNum());
    }

    @Test
    void testUpdateUser_UserNotFound_ThrowsNoSuchElementException() {
        when(userRepo.findById(999)).thenReturn(Optional.empty());

        NewUser dto = new NewUser();
        dto.setUsername("x");

        assertThrows(NoSuchElementException.class, () -> userService.updateUser(999, dto));
        verify(userRepo, never()).save(any());
    }
    
}
