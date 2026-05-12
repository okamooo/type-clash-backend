package com.example.typing.repository;

import com.example.typing.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 指定したIDのユーザーを取得する（論理削除済みを除外）
     *
     * @param id ユーザーID
     * @return ユーザー（deleted_at が null のもののみ）
     */
    Optional<User> findByIdAndDeletedAtIsNull(Long id);

    /**
     * 指定したメールアドレスのユーザーを取得する（論理削除済みを除外）
     *
     * @param email メールアドレス
     * @return ユーザー（deleted_at が null のもののみ）
     */
    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    /**
     * 指定したメールアドレスが既に登録されているか確認する（論理削除済みを除外）
     *
     * @param email メールアドレス
     * @return 存在する場合 true
     */
    boolean existsByEmailAndDeletedAtIsNull(String email);

}
