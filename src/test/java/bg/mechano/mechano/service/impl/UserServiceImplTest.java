package bg.mechano.mechano.service.impl;

import bg.mechano.mechano.domain.entity.User;
import bg.mechano.mechano.domain.repository.UserRepository;
import bg.mechano.mechano.service.security.CurrentUserService;
import bg.mechano.mechano.web.dto.user.UserProfileUpdateRequest;
import bg.mechano.mechano.web.dto.user.UserResponse;
import bg.mechano.mechano.web.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private UserServiceImpl service;

    @Test
    void getCurrentUser_shouldReturnCurrentUser() {
        User user = createUser(3L);

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        UserResponse response =
                service.getCurrentUser();

        assertEquals(3L, response.id());
        assertEquals(1L, response.authUserId());
        assertEquals(
                "Test User",
                response.fullName()
        );
        assertEquals(
                "0888123456",
                response.phone()
        );
    }

    @Test
    void getById_shouldReturnUser_whenExists() {
        User user = createUser(5L);

        when(userRepository.findById(5L))
                .thenReturn(Optional.of(user));

        UserResponse response =
                service.getById(5L);

        assertEquals(5L, response.id());
        assertEquals(1L, response.authUserId());
    }

    @Test
    void getById_shouldThrowNotFound_whenMissing() {
        when(userRepository.findById(5L))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> service.getById(5L)
        );
    }

    @Test
    void getById_shouldThrowNotFound_whenDeleted() {
        User user = createUser(5L);
        user.setDeletedAt(Instant.now());

        when(userRepository.findById(5L))
                .thenReturn(Optional.of(user));

        assertThrows(
                NotFoundException.class,
                () -> service.getById(5L)
        );
    }

    @Test
    void list_shouldReturnOnlyNotDeletedUsers() {
        User active = createUser(1L);

        User deleted = createUser(2L);
        deleted.setDeletedAt(Instant.now());

        when(userRepository.findAll())
                .thenReturn(
                        List.of(active, deleted)
                );

        List<UserResponse> result =
                service.list();

        assertEquals(1, result.size());
        assertEquals(
                1L,
                result.getFirst().id()
        );
    }

    @Test
    void updateCurrentUser_shouldUpdateProvidedFields() {
        User user = createUser(3L);

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(userRepository.save(user))
                .thenReturn(user);

        UserProfileUpdateRequest request =
                new UserProfileUpdateRequest(
                        "  Updated Name  ",
                        "  +359888123456  "
                );

        UserResponse response =
                service.updateCurrentUser(request);

        assertEquals(
                "Updated Name",
                response.fullName()
        );

        assertEquals(
                "+359888123456",
                response.phone()
        );

        verify(userRepository).save(user);
    }

    @Test
    void updateCurrentUser_shouldNormalizeBlankToNull() {
        User user = createUser(3L);

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(userRepository.save(user))
                .thenReturn(user);

        UserProfileUpdateRequest request =
                new UserProfileUpdateRequest(
                        "   ",
                        "   "
                );

        UserResponse response =
                service.updateCurrentUser(request);

        assertNull(response.fullName());
        assertNull(response.phone());

        verify(userRepository).save(user);
    }

    @Test
    void update_shouldUpdateProvidedFields() {
        User user = createUser(7L);

        when(userRepository.findById(7L))
                .thenReturn(Optional.of(user));

        when(userRepository.save(user))
                .thenReturn(user);

        UserProfileUpdateRequest request =
                new UserProfileUpdateRequest(
                        "  New Name  ",
                        "  222  "
                );

        UserResponse response =
                service.update(7L, request);

        assertEquals(
                "New Name",
                response.fullName()
        );

        assertEquals(
                "222",
                response.phone()
        );

        verify(userRepository).save(user);
    }

    @Test
    void update_shouldLeaveFieldsUnchanged_whenNull() {
        User user = createUser(7L);

        when(userRepository.findById(7L))
                .thenReturn(Optional.of(user));

        when(userRepository.save(user))
                .thenReturn(user);

        UserProfileUpdateRequest request =
                new UserProfileUpdateRequest(
                        null,
                        null
                );

        UserResponse response =
                service.update(7L, request);

        assertEquals(
                "Test User",
                response.fullName()
        );

        assertEquals(
                "0888123456",
                response.phone()
        );

        verify(userRepository).save(user);
    }

    @Test
    void update_shouldThrowNotFound_whenDeleted() {
        User user = createUser(7L);
        user.setDeletedAt(Instant.now());

        when(userRepository.findById(7L))
                .thenReturn(Optional.of(user));

        assertThrows(
                NotFoundException.class,
                () -> service.update(
                        7L,
                        new UserProfileUpdateRequest(
                                "X",
                                "Y"
                        )
                )
        );

        verify(
                userRepository,
                never()
        ).save(any());
    }

    private User createUser(Long id) {
        return User.builder()
                .id(id)
                .authUserId(1L)
                .fullName("Test User")
                .phone("0888123456")
                .avatarImageId(null)
                .createdAt(Instant.now())
                .deletedAt(null)
                .build();
    }
}