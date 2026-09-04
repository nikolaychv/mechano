package bg.mechano.mechano.service;

import bg.mechano.mechano.web.dto.user.UserProfileUpdateRequest;
import bg.mechano.mechano.web.dto.user.UserResponse;

import java.util.List;

public interface UserService {

    UserResponse getCurrentUser();

    UserResponse getById(Long id);

    List<UserResponse> list();

    UserResponse updateCurrentUser(
            UserProfileUpdateRequest request
    );

    UserResponse update(
            Long id,
            UserProfileUpdateRequest request
    );
}