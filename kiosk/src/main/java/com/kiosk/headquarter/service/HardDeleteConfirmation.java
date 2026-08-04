package com.kiosk.headquarter.service;

/** 쉬운주석: 실수로 누른 삭제 버튼이 실제 삭제로 이어지지 않도록 이름 확인 문구를 검사한다. */
final class HardDeleteConfirmation {

    private HardDeleteConfirmation() {
    }

    static void verify(String databaseName, String confirmation) {
        String expected = databaseName + "/삭제한다";
        if (confirmation == null || !expected.equals(confirmation.trim())) {
            throw new IllegalArgumentException(
                    "삭제 확인 문구가 일치하지 않습니다. \"" + expected + "\"를 정확히 입력해주세요."
            );
        }
    }
}
