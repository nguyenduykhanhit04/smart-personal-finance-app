package com.example.financebackend.service;

import com.example.financebackend.dto.UserDTO;
import com.example.financebackend.exception.ResourceNotFoundException;
import com.example.financebackend.model.Account;
import com.example.financebackend.model.User;
import com.example.financebackend.repository.UserRepository;
import com.google.firebase.auth.FirebaseToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountService accountService;

    @Mock
    private FirebaseToken firebaseToken;

    @InjectMocks
    private UserService userService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .userId(1)
                .firebaseUid("firebase_uid_123")
                .fullName("Nguyen Van A")
                .email("vana@example.com")
                .phone("0912345678")
                .avatarUrl("https://example.com/avatar.jpg")
                .authProvider("google")
                .build();
    }

    @Test
    void testSyncFirebaseUserWithToken_NewUser_GoogleProvider() {
        when(firebaseToken.getUid()).thenReturn("firebase_uid_new");
        when(firebaseToken.getEmail()).thenReturn("newuser@gmail.com");

        Map<String, Object> claims = new HashMap<>();
        claims.put("name", "New Google User");
        claims.put("picture", "https://photo.url/pic.jpg");
        Map<String, Object> firebaseClaim = new HashMap<>();
        firebaseClaim.put("sign_in_provider", "google.com");
        claims.put("firebase", firebaseClaim);
        when(firebaseToken.getClaims()).thenReturn(claims);

        when(userRepository.findByFirebaseUid("firebase_uid_new")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("newuser@gmail.com")).thenReturn(Optional.empty());

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setUserId(2);
            return u;
        });
        when(accountService.getOrCreateDefaultAccount(2)).thenReturn(Account.builder().accountId(20).build());

        UserDTO result = userService.syncFirebaseUserWithToken(firebaseToken);

        assertNotNull(result);
        assertEquals(2, result.getUserId());
        assertEquals("firebase_uid_new", result.getFirebaseUid());
        assertEquals("New Google User", result.getFullName());
        assertEquals("newuser@gmail.com", result.getEmail());
        assertEquals("google", result.getAuthProvider());

        verify(userRepository).save(any(User.class));
        verify(accountService).getOrCreateDefaultAccount(2);
    }

    @Test
    void testSyncFirebaseUserWithToken_ExistingUserByFirebaseUid_UpdatesInfo() {
        when(firebaseToken.getUid()).thenReturn("firebase_uid_123");
        when(firebaseToken.getEmail()).thenReturn("vana_updated@example.com");

        Map<String, Object> claims = new HashMap<>();
        claims.put("name", "Nguyen Van A Updated");
        claims.put("picture", "https://photo.url/new.jpg");
        Map<String, Object> firebaseClaim = new HashMap<>();
        firebaseClaim.put("sign_in_provider", "password");
        claims.put("firebase", firebaseClaim);
        when(firebaseToken.getClaims()).thenReturn(claims);

        when(userRepository.findByFirebaseUid("firebase_uid_123")).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(accountService.getOrCreateDefaultAccount(1)).thenReturn(Account.builder().accountId(10).build());

        UserDTO result = userService.syncFirebaseUserWithToken(firebaseToken);

        assertNotNull(result);
        assertEquals(1, result.getUserId());
        assertEquals("Nguyen Van A Updated", result.getFullName());
        assertEquals("vana_updated@example.com", result.getEmail());
        assertEquals("https://photo.url/new.jpg", result.getAvatarUrl());
        assertEquals("firebase", result.getAuthProvider());

        verify(userRepository).save(sampleUser);
        verify(accountService).getOrCreateDefaultAccount(1);
    }

    @Test
    void testSyncFirebaseUserWithToken_ExistingUserByEmail_LinksFirebaseUid() {
        User userWithoutUid = User.builder()
                .userId(3)
                .email("linkuser@example.com")
                .fullName("Link User")
                .build();

        when(firebaseToken.getUid()).thenReturn("firebase_uid_link");
        when(firebaseToken.getEmail()).thenReturn("linkuser@example.com");

        Map<String, Object> claims = new HashMap<>();
        claims.put("name", "Link User");
        claims.put("firebase", Map.of("sign_in_provider", "google.com"));
        when(firebaseToken.getClaims()).thenReturn(claims);

        when(userRepository.findByFirebaseUid("firebase_uid_link")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("linkuser@example.com")).thenReturn(Optional.of(userWithoutUid));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(accountService.getOrCreateDefaultAccount(3)).thenReturn(Account.builder().accountId(30).build());

        UserDTO result = userService.syncFirebaseUserWithToken(firebaseToken);

        assertNotNull(result);
        assertEquals(3, result.getUserId());
        assertEquals("firebase_uid_link", result.getFirebaseUid());
        assertEquals("google", result.getAuthProvider());

        verify(userRepository).save(userWithoutUid);
    }

    @Test
    void testGetUserById_Success() {
        when(userRepository.findById(1)).thenReturn(Optional.of(sampleUser));

        UserDTO result = userService.getUserById(1);

        assertNotNull(result);
        assertEquals(1, result.getUserId());
        assertEquals("Nguyen Van A", result.getFullName());
        assertEquals("vana@example.com", result.getEmail());
    }

    @Test
    void testGetUserById_NotFound_ThrowsException() {
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(999));
    }

    @Test
    void testUpdateUser_Success() {
        when(userRepository.findById(1)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDTO updateDTO = UserDTO.builder()
                .fullName("Nguyen Van B")
                .phone("0987654321")
                .avatarUrl("https://newavatar.png")
                .build();

        UserDTO result = userService.updateUser(1, updateDTO);

        assertNotNull(result);
        assertEquals("Nguyen Van B", result.getFullName());
        assertEquals("0987654321", result.getPhone());
        assertEquals("https://newavatar.png", result.getAvatarUrl());
        verify(userRepository).save(sampleUser);
    }

    @Test
    void testUpdateUser_NotFound_ThrowsException() {
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        UserDTO updateDTO = UserDTO.builder().fullName("Test").build();

        assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(999, updateDTO));
        verify(userRepository, never()).save(any(User.class));
    }
}
