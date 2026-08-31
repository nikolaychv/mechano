package bg.mechano.mechano.service.impl;

import bg.mechano.mechano.domain.entity.User;
import bg.mechano.mechano.domain.repository.UserRepository;
import bg.mechano.mechano.service.UserService;
import bg.mechano.mechano.service.security.CurrentUserService;
import bg.mechano.mechano.web.dto.user.UserCreateRequest;
import bg.mechano.mechano.web.dto.user.UserProfileUpdateRequest;
import bg.mechano.mechano.web.dto.user.UserResponse;
import bg.mechano.mechano.web.dto.user.UserUpdateRequest;
import bg.mechano.mechano.web.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    @Override
    public UserResponse create(UserCreateRequest request) {
        String email = normalize(request.email());
        String username = normalize(request.username());

        userRepository.findByEmail(email).ifPresent(user -> {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email already exists"
            );
        });

        userRepository.findByUsername(username).ifPresent(user -> {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Username already exists"
            );
        });

        User user = User.builder()
                .email(email)
                .fullName(normalizeNullable(request.fullName()))
                .username(username)
                .phone(normalizeNullable(request.phone()))
                .role(request.role())
                .isActive(true)
                .createdAt(Instant.now())
                .deletedAt(null)
                .build();

        return toResponse(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        return toResponse(currentUserService.getCurrentUser());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        return toResponse(getExisting(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> list() {
        return userRepository.findAll().stream()
                .filter(user -> user.getDeletedAt() == null)
                .map(this::toResponse)
                .toList();
    }

    @Override
    public UserResponse updateCurrentUser(
            UserProfileUpdateRequest request
    ) {
        User user = currentUserService.getCurrentUser();

        if (request.fullName() != null) {
            user.setFullName(
                    normalizeNullable(request.fullName())
            );
        }

        if (request.phone() != null) {
            user.setPhone(
                    normalizeNullable(request.phone())
            );
        }

        return toResponse(userRepository.save(user));
    }

    @Override
    public UserResponse update(
            Long id,
            UserUpdateRequest request
    ) {
        User user = getExisting(id);

        if (request.fullName() != null) {
            user.setFullName(
                    normalizeNullable(request.fullName())
            );
        }

        if (request.phone() != null) {
            user.setPhone(
                    normalizeNullable(request.phone())
            );
        }

        if (request.isActive() != null) {
            user.setActive(request.isActive());
        }

        return toResponse(userRepository.save(user));
    }

    @Override
    public void softDelete(Long id) {
        User user = getExisting(id);

        user.setDeletedAt(Instant.now());
        user.setActive(false);

        userRepository.save(user);
    }

    private User getExisting(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(
                        () -> new NotFoundException(
                                "User not found: " + id
                        )
                );

        if (user.getDeletedAt() != null) {
            throw new NotFoundException(
                    "User not found: " + id
            );
        }

        return user;
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getUsername(),
                user.getPhone(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt()
        );
    }

    private String normalize(String value) {
        if (value == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Required field is missing"
            );
        }

        String normalized = value.trim();

        if (normalized.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Required field is blank"
            );
        }

        return normalized;
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();

        return normalized.isBlank()
                ? null
                : normalized;
    }
}