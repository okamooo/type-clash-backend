package com.example.typing.dto.response;

import com.example.typing.entity.User;
import lombok.Data;

// ユーザー情報のレスポンスDTO（パスワードを含まない）
@Data
public class UserResponse {

    private Long id;                 // ID
    private String name;             // ユーザー名
    private String email;            // メールアドレス
    private String iconImage;        // アイコン画像URL
    private Integer backgroundImage; // 背景画像ID（0 or 1）

    // UserエンティティからDTOを生成
    public UserResponse(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.iconImage = user.getIconImage();
        this.backgroundImage = user.getBackgroundImage();
    }
}
