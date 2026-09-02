package bg.mechano.mechano.service;

import bg.mechano.mechano.web.dto.user.UserProfileUpdateRequest;
import bg.mechano.mechano.web.dto.user.UserResponse;
import bg.mechano.mechano.web.dto.user.UserUpdateRequest;

import java.util.List;

public interface UserService {

    UserResponse getCurrentUser();

    UserResponse getById(Long id);

    List<UserResponse> list();

    UserResponse updateCurrentUser(UserProfileUpdateRequest request);

    UserResponse update(Long id, UserUpdateRequest request);

    void softDelete(Long id);
}