package bg.mechano.mechano.service.impl;

import bg.mechano.mechano.domain.entity.User;
import bg.mechano.mechano.domain.repository.UserRepository;
import bg.mechano.mechano.service.UserService;
import bg.mechano.mechano.service.security.CurrentUserService;
import bg.mechano.mechano.web.dto.user.UserProfileUpdateRequest;
import bg.mechano.mechano.web.dto.user.UserResponse;
import bg.mechano.mechano.web.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        return toResponse(
                currentUserService.getCurrentUser()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        return toResponse(getExisting(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> list() {
        return userRepository.findAll()
                .stream()
                .filter(user ->
                        user.getDeletedAt() == null
                )
                .map(this::toResponse)
                .toList();
    }

    @Override
    public UserResponse updateCurrentUser(
            UserProfileUpdateRequest request
    ) {
        User user =
                currentUserService.getCurrentUser();

        applyProfileUpdate(user, request);

        return toResponse(
                userRepository.save(user)
        );
    }

    @Override
    public UserResponse update(
            Long id,
            UserProfileUpdateRequest request
    ) {
        User user = getExisting(id);

        applyProfileUpdate(user, request);

        return toResponse(
                userRepository.save(user)
        );
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

    private void applyProfileUpdate(
            User user,
            UserProfileUpdateRequest request
    ) {
        if (request.fullName() != null) {
            user.setFullName(
                    normalizeNullable(
                            request.fullName()
                    )
            );
        }

        if (request.phone() != null) {
            user.setPhone(
                    normalizeNullable(
                            request.phone()
                    )
            );
        }
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getAuthUserId(),
                user.getFullName(),
                user.getPhone(),
                user.getAvatarImageId(),
                user.getCreatedAt()
        );
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