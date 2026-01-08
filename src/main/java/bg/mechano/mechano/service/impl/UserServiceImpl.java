package bg.mechano.mechano.service.impl;

import bg.mechano.mechano.domain.entity.User;
import bg.mechano.mechano.domain.enums.UserRole;
import bg.mechano.mechano.domain.repository.UserRepository;
import bg.mechano.mechano.service.UserService;
import bg.mechano.mechano.web.dto.user.UserCreateRequest;
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

    @Override
    public UserResponse create(UserCreateRequest request) {
        String email = normalize(request.email());
        String username = normalize(request.username());

        userRepository.findByEmail(email).ifPresent(u -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        });

        userRepository.findByUsername(username).ifPresent(u -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
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
    public UserResponse getById(Long id) {
        return toResponse(getExisting(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> list() {
        return userRepository.findAll().stream()
                .filter(u -> u.getDeletedAt() == null)
                .map(this::toResponse)
                .toList();
    }

    @Override
    public UserResponse update(Long id, UserUpdateRequest request) {
        User user = getExisting(id);

        if (request.fullName() != null) {
            user.setFullName(request.fullName().trim());
        }

        if (request.phone() != null) {
            user.setPhone(request.phone().trim());
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
                .orElseThrow(() -> new NotFoundException("User not found: " + id));

        if (user.getDeletedAt() != null) {
            throw new NotFoundException("User not found: " + id);
        }
        return user;
    }

    private UserResponse toResponse(User u) {
        return new UserResponse(
                u.getId(),
                u.getEmail(),
                u.getFullName(),
                u.getUsername(),
                u.getPhone(),
                u.getRole(),
                u.isActive(),
                u.getCreatedAt()
        );
    }

    private String normalize(String s) {
        if (s == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Required field is missing");
        }
        String t = s.trim();
        if (t.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Required field is blank");
        }
        return t;
    }

    private String normalizeNullable(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isBlank() ? null : t;
    }
}