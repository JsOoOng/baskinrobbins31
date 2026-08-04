package com.kiosk.headquarter.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class HardDeleteConfirmationTest {

    /* 쉬운주석: DB 이름과 '/삭제한다'를 정확히 붙인 문구만 영구 삭제를 통과한다. */
    @Test
    void acceptsExactDatabaseNameAndDeleteSuffix() {
        assertThatCode(() ->
                HardDeleteConfirmation.verify("아메리카노", "아메리카노/삭제한다")
        ).doesNotThrowAnyException();
    }

    /* 쉬운주석: 이름이 다르거나 문구가 빠지면 실수로 보고 삭제를 막는다. */
    @Test
    void rejectsIncorrectConfirmationText() {
        assertThatThrownBy(() ->
                HardDeleteConfirmation.verify("강남점", "강남점 삭제한다")
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("강남점/삭제한다");
    }
}
