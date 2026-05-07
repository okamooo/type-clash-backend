package com.example.typing.controller;

import com.example.typing.dto.UserResponse;
import com.example.typing.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = { "http://localhost:3000", "http://127.0.0.1:3000" })
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
}
