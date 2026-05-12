package com.example.typing.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.typing.dto.request.LoginRequest;
import com.example.typing.entity.User;
import com.example.typing.exception.AuthenticationFailedException;
import com.example.typing.repository.UserRepository;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User authenticate(LoginRequest loginRequest) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(loginRequest.getEmail())
                .orElseThrow(() -> new AuthenticationFailedException("メールアドレスまたはパスワードが違います"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new AuthenticationFailedException("メールアドレスまたはパスワードが違います");
        }

        return user;
    }
}
