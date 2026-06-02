package com.example.typing.service;

import com.example.typing.dto.request.UserRegisterRequest;
import com.example.typing.dto.request.UserUpdateRequest;
import com.example.typing.dto.response.UserResponse;
import com.example.typing.entity.User;
import com.example.typing.exception.EmailAlreadyExistsException;
import com.example.typing.exception.InvalidImageFileException;
import com.example.typing.exception.InvalidPasswordException;
import com.example.typing.exception.UserNotFoundException;
import com.example.typing.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of("png", "jpg", "jpeg", "webp");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.upload-dir}")
    private String uploadDir;

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
        user.setPassword(passwordEncoder.encode(request.getPassword()));

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

        // パスワードの更新（現在のパスワード照合 → ハッシュ化）
        if (request.getPassword() != null) {
            if (request.getCurrentPassword() == null ||
                    !passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                throw new InvalidPasswordException();
            }
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
     * 【ユーザーアイコン画像アップロード】
     * 画像ファイルを保存し、ユーザーの iconImage に配信用パスを保存する
     *
     * @param userId ユーザーID
     * @param file   アップロードされた画像ファイル
     * @return UserResponse
     */
    @Transactional
    public UserResponse uploadUserIcon(Long userId, MultipartFile file) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (file == null || file.isEmpty()) {
            throw new InvalidImageFileException("画像ファイルが空です");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = getAllowedImageExtension(originalFilename);

        Path userUploadDir = Path.of(uploadDir, "users", userId.toString());
        Path iconPath = userUploadDir.resolve("icon." + extension);

        try {
            Files.createDirectories(userUploadDir);
            Files.copy(file.getInputStream(), iconPath, StandardCopyOption.REPLACE_EXISTING);
            deleteOldIconFiles(userUploadDir, iconPath);
        } catch (IOException e) {
            throw new IllegalStateException("画像ファイルの保存に失敗しました", e);
        }

        String iconImage = "/uploads/users/" + userId + "/icon." + extension;
        user.setIconImage(iconImage);

        return new UserResponse(userRepository.save(user));
    }

    private String getAllowedImageExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new InvalidImageFileException("画像ファイルの形式が正しくありません");
        }

        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase(Locale.ROOT);

        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            throw new InvalidImageFileException("アップロードできる画像形式は png, jpg, jpeg, webp のみです");
        }

        return extension;
    }

    private void deleteOldIconFiles(Path userUploadDir, Path currentIconPath) throws IOException {
        try (DirectoryStream<Path> iconFiles = Files.newDirectoryStream(userUploadDir, "icon.*")) {
            for (Path iconFile : iconFiles) {
                if (!iconFile.equals(currentIconPath)) {
                    Files.deleteIfExists(iconFile);
                }
            }
        }
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
