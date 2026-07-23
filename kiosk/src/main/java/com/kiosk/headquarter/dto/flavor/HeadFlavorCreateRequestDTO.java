package com.kiosk.headquarter.dto.flavor;

import org.springframework.web.multipart.MultipartFile;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * [코드 흐름 안내] HeadFlavorCreateRequestDTO
 *
 * <p>역할: 아이스크림 맛 계층 사이에서 필요한 값만 전달하는 DTO다.</p>
 * <p>호출 흐름: Controller·Service·프론트 사이의 데이터 모양을 현재 필드 정의로 고정한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Getter
@Setter
@NoArgsConstructor
public class HeadFlavorCreateRequestDTO {

    private String flavorName;
    
    // 기본적으로 노출 상태
    private Boolean isActive = true;

    // 프론트엔드에서 전송하는 이미지 파일
    private MultipartFile imageFile;
}