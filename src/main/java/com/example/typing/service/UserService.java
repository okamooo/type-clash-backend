package com.example.typing.service;

import com.example.typing.dto.request.UserRegisterRequest;
import com.example.typing.dto.request.UserUpdateRequest;
import com.example.typing.dto.response.UserResponse;
import com.example.typing.entity.User;
import com.example.typing.exception.EmailAlreadyExistsException;
import com.example.typing.exception.UserNotFoundException;
import com.example.typing.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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

    /**
     * 【ユーザー登録】
     * パスワードをハッシュ化してユーザーを新規登録する
     *
     * @param request 登録リクエストDTO
     * @return UserResponse
     * @throws EmailAlreadyExistsException メールアドレスが既に登録されている場合
     */
    @Transactional
    public UserResponse registerUser(UserRegisterRequest request) {
        // メールアドレスの重複チェック
        if (userRepository.existsByEmailAndDeletedAtIsNull(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // パスワードをハッシュ化
        user.setIconImage(request.getIconImage());
        user.setBackgroundImage(request.getBackgroundImage());

        return new UserResponse(userRepository.save(user));
    }

    /**
     * 【ユーザー更新】
     * 指定したユーザーのプロフィール情報を更新する
     *
     * @param userId  ユーザーID
     * @param request 更新リクエストDTO
     * @return UserResponse
     * @throws UserNotFoundException       ユーザーが存在しない、または論理削除済みの場合
     * @throws EmailAlreadyExistsException メールアドレスが既に登録されている場合
     */
    @Transactional
    public UserResponse updateUser(Long userId, UserUpdateRequest request) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // 名前の更新
        if (request.getName() != null) {
            user.setName(request.getName());
        }

        // メールアドレスの更新（重複チェックあり）
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmailAndDeletedAtIsNull(request.getEmail())) {
                throw new EmailAlreadyExistsException(request.getEmail());
            }
            user.setEmail(request.getEmail());
        }

        // パスワードの更新（ハッシュ化）
        if (request.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        // アイコン画像の更新
        if (request.getIconImage() != null) {
            user.setIconImage(request.getIconImage());
        }

        // 背景画像の更新
        if (request.getBackgroundImage() != null) {
            user.setBackgroundImage(request.getBackgroundImage());
        }

        user.setUpdatedAt(LocalDateTime.now());

        return new UserResponse(userRepository.save(user));
    }

    /**
     * 【ユーザー削除】
     * 指定したユーザーを論理削除する（deleted_at に削除日時をセット、email を無効化）
     *
     * @param userId ユーザーID
     * @throws UserNotFoundException ユーザーが存在しない、または論理削除済みの場合
     */
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
                
        user.setEmail("deleted_" + userId + "@deleted");
        user.setDeletedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
    }
}
