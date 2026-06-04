package com.example.typing.service;

import com.example.typing.dto.response.BattleResultResponse;
import com.example.typing.entity.BattleResult;

/**
 * 対戦結果の参照・保存前の認可結果（404 / 403 / 200 の切り分け用）
 */
public record BattleResultAccess(
        Status status,
        BattleResult record,
        BattleResultResponse response) {

    public enum Status {
        NOT_FOUND,
        FORBIDDEN,
        OK
    }

    public static BattleResultAccess notFound() {
        return new BattleResultAccess(Status.NOT_FOUND, null, null);
    }

    public static BattleResultAccess forbidden(BattleResult record) {
        return new BattleResultAccess(Status.FORBIDDEN, record, null);
    }

    public static BattleResultAccess ok(BattleResult record, BattleResultResponse response) {
        return new BattleResultAccess(Status.OK, record, response);
    }
}
