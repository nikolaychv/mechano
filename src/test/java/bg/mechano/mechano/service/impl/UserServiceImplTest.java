package bg.mechano.mechano.service.impl;

import bg.mechano.mechano.domain.entity.User;
import bg.mechano.mechano.domain.enums.UserRole;
import bg.mechano.mechano.domain.repository.UserRepository;
import bg.mechano.mechano.web.dto.user.UserCreateRequest;
import bg.mechano.mechano.web.dto.user.UserResponse;
import bg.mechano.mechano.web.dto.user.UserUpdateRequest;
import bg.mechano.mechano.web.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl service;

    @Test
    void create_shouldCreateUser_whenValidRequest() {
        UserCreateRequest request = new UserCreateRequest(
                "  mail@test.com  ",
                "  John Doe  ",
                "  john  ",
                "  +359 888 123 456  ",
                UserRole.CLIENT
        );

        when(userRepository.findByEmail("mail@test.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("john")).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        UserResponse response = service.create(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User saved = captor.getValue();
        assertEquals("mail@test.com", saved.getEmail());
        assertEquals("John Doe", saved.getFullName());
        assertEquals("john", saved.getUsername());
        assertEquals("+359 888 123 456", saved.getPhone());
        assertEquals(UserRole.CLIENT, saved.getRole());
        assertTrue(saved.isActive());
        assertNotNull(saved.getCreatedAt());
        assertNull(saved.getDeletedAt());

        assertEquals(1L, response.id());
        assertEquals("mail@test.com", response.email());
        assertEquals("John Doe", response.fullName());
        assertEquals("john", response.username());
        assertEquals("+359 888 123 456", response.phone());
        assertEquals(UserRole.CLIENT, response.role());
        assertTrue(response.isActive());
        assertNotNull(response.createdAt());
    }

    @Test
    void create_shouldNormalizeNullableFields_toNull_whenBlank() {
        UserCreateRequest request = new UserCreateRequest(
                "mail@test.com",
                "   ",
                "john",
                "   ",
                UserRole.CLIENT
        );

        when(userRepository.findByEmail("mail@test.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("john")).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(2L);
            return u;
        });

        UserResponse response = service.create(request);

        assertNull(response.fullName());
        assertNull(response.phone());
    }

    @Test
    void create_shouldThrowBadRequest_whenEmailMissing() {
        UserCreateRequest request = new UserCreateRequest(
                null,
                "John",
                "john",
                null,
                UserRole.CLIENT
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.create(request));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowBadRequest_whenEmailBlank() {
        UserCreateRequest request = new UserCreateRequest(
                "   ",
                "John",
                "john",
                null,
                UserRole.CLIENT
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.create(request));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowBadRequest_whenUsernameMissing() {
        UserCreateRequest request = new UserCreateRequest(
                "mail@test.com",
                "John",
                null,
                null,
                UserRole.CLIENT
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.create(request));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowBadRequest_whenUsernameBlank() {
        UserCreateRequest request = new UserCreateRequest(
                "mail@test.com",
                "John",
                "   ",
                null,
                UserRole.CLIENT
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.create(request));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowConflict_whenEmailExists() {
        UserCreateRequest request = new UserCreateRequest(
                "mail@test.com",
                "John",
                "john",
                null,
                UserRole.CLIENT
        );

        when(userRepository.findByEmail("mail@test.com"))
                .thenReturn(Optional.of(User.builder().id(10L).email("mail@test.com").build()));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.create(request));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowConflict_whenUsernameExists() {
        UserCreateRequest request = new UserCreateRequest(
                "mail@test.com",
                "John",
                "john",
                null,
                UserRole.CLIENT
        );

        when(userRepository.findByEmail("mail@test.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(User.builder().id(11L).username("john").build()));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.create(request));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    void getById_shouldReturnUser_whenExistsAndNotDeleted() {
        User user = User.builder()
                .id(5L)
                .email("a@b.com")
                .fullName("A B")
                .username("ab")
                .phone(null)
                .role(UserRole.CLIENT)
                .isActive(true)
                .createdAt(Instant.now())
                .deletedAt(null)
                .build();

        when(userRepository.findById(5L)).thenReturn(Optional.of(user));

        UserResponse response = service.getById(5L);

        assertEquals(5L, response.id());
        assertEquals("a@b.com", response.email());
        assertEquals("ab", response.username());
    }

    @Test
    void getById_shouldThrowNotFound_whenDeleted() {
        User user = User.builder()
                .id(5L)
                .deletedAt(Instant.now())
                .build();

        when(userRepository.findById(5L)).thenReturn(Optional.of(user));

        assertThrows(NotFoundException.class, () -> service.getById(5L));
    }

    @Test
    void list_shouldReturnOnlyNotDeleted() {
        User active = User.builder()
                .id(1L)
                .email("a@b.com")
                .username("a")
                .role(UserRole.CLIENT)
                .isActive(true)
                .createdAt(Instant.now())
                .deletedAt(null)
                .build();

        User deleted = User.builder()
                .id(2L)
                .email("d@b.com")
                .username("d")
                .role(UserRole.CLIENT)
                .isActive(false)
                .createdAt(Instant.now())
                .deletedAt(Instant.now())
                .build();

        when(userRepository.findAll()).thenReturn(List.of(active, deleted));

        List<UserResponse> result = service.list();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).id());
    }

    @Test
    void update_shouldUpdateProvidedFields_andSave() {
        User user = User.builder()
                .id(7L)
                .email("mail@test.com")
                .fullName("Old Name")
                .username("john")
                .phone("111")
                .role(UserRole.CLIENT)
                .isActive(true)
                .createdAt(Instant.now())
                .deletedAt(null)
                .build();

        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UserUpdateRequest request = new UserUpdateRequest(
                "  New Name  ",
                "  222  ",
                false
        );

        UserResponse response = service.update(7L, request);

        assertEquals("New Name", response.fullName());
        assertEquals("222", response.phone());
        assertFalse(response.isActive());
        verify(userRepository).save(user);
    }

    @Test
    void update_shouldThrowNotFound_whenDeleted() {
        User user = User.builder()
                .id(7L)
                .deletedAt(Instant.now())
                .build();

        when(userRepository.findById(7L)).thenReturn(Optional.of(user));

        assertThrows(NotFoundException.class,
                () -> service.update(7L, new UserUpdateRequest("X", "Y", true)));

        verify(userRepository, never()).save(any());
    }

    @Test
    void softDelete_shouldSetDeletedAtAndDeactivate_andSave() {
        User user = User.builder()
                .id(9L)
                .email("x@y.com")
                .username("x")
                .role(UserRole.CLIENT)
                .isActive(true)
                .createdAt(Instant.now())
                .deletedAt(null)
                .build();

        when(userRepository.findById(9L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.softDelete(9L);

        assertNotNull(user.getDeletedAt());
        assertFalse(user.isActive());
        verify(userRepository).save(user);
    }

    @Test
    void softDelete_shouldThrowNotFound_whenAlreadyDeleted() {
        User user = User.builder()
                .id(9L)
                .deletedAt(Instant.now())
                .build();

        when(userRepository.findById(9L)).thenReturn(Optional.of(user));

        assertThrows(NotFoundException.class, () -> service.softDelete(9L));
        verify(userRepository, never()).save(any());
    }
}
