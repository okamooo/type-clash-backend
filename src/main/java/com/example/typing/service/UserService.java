package com.example.typing.service;

import com.example.typing.dto.UserResponse;
import com.example.typing.entity.User;
import com.example.typing.exception.UserNotFoundException;
import com.example.typing.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * 指定したIDのユーザーを取得する（論理削除済みを除外）
     *
     * @param userId ユーザーID
     * @return UserResponse
     * @throws UserNotFoundException ユーザーが存在しない、または論理削除済みの場合
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return new UserResponse(user);
    }
}
