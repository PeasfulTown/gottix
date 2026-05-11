package xyz.peasfultown.gottix.auth_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import xyz.peasfultown.ecommerce.auth_service.UserApi;
import xyz.peasfultown.ecommerce.auth_service.model.PagedUserResponse;
import xyz.peasfultown.ecommerce.auth_service.model.User;
import xyz.peasfultown.ecommerce.auth_service.model.UserUpdateRequest;

@RestController
public class UserController implements UserApi {
    @Override
    public ResponseEntity<Void> deleteUser(String xUserId) throws Exception {
        return UserApi.super.deleteUser(xUserId);
    }

    @Override
    public ResponseEntity<User> getUser(String xUserId) throws Exception {
        return UserApi.super.getUser(xUserId);
    }

    @Override
    public ResponseEntity<PagedUserResponse> getUsers(String xUserRole, Integer pageNumber, Integer pageSize) throws Exception {
        return UserApi.super.getUsers(xUserRole, pageNumber, pageSize);
    }

    @Override
    public ResponseEntity<User> updateUser(String xUserId, UserUpdateRequest userUpdateRequest) throws Exception {
        return UserApi.super.updateUser(xUserId, userUpdateRequest);
    }
}
