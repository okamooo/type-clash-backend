package com.example.typing.controller;

import com.example.typing.dto.request.UserRegisterRequest;
import com.example.typing.dto.request.UserUpdateRequest;
import com.example.typing.dto.response.UserResponse;
import com.example.typing.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 【ユーザー情報取得】
     * 指定したユーザーIDのユーザー情報を取得する
     *
     * @param userId パスパラメータ（ユーザーID）
     * @return 200 OK + UserResponse / 404 Not Found
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long userId) {
        UserResponse response = userService.getUserById(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 【ユーザー登録】
     * 新規ユーザーを登録する
     *
     * @param request リクエストボディ（ユーザー登録情報）
     * @return 201 Created + UserResponse / 400 Bad Request / 409 Conflict
     */
    @PostMapping("/users/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserRegisterRequest request) {
        UserResponse response = userService.registerUser(request);
        return ResponseEntity.status(201).body(response);
    }

    /**
     * 【ユーザー情報更新】
     * 指定したユーザーのプロフィール情報を更新する
     *
     * @param userId  パスパラメータ（ユーザーID）
     * @param request リクエストボディ（更新情報）
     * @return 200 OK + UserResponse / 400 Bad Request / 404 Not Found / 409 Conflict
     */
    @PatchMapping("/users/{userId}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long userId,
            @Valid @RequestBody UserUpdateRequest request) {
        UserResponse response = userService.updateUser(userId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 【ユーザー削除】
     * 指定したユーザーを論理削除する
     *
     * @param userId パスパラメータ（ユーザーID）
     * @return 204 No Content / 404 Not Found
     */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
