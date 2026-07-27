package com.kiosk.headquarter.dto.deliverie;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 쉬운주석: 배송 관리 화면에서 입력한 취소 사유를 컨트롤러와 서비스로 전달한다.
 */
@Getter
@NoArgsConstructor
public class HeadDeliveryCancelRequestDTO {

    private String reason;
}
