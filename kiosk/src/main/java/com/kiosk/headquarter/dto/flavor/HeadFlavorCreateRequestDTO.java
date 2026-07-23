package com.kiosk.headquarter.dto.flavor;

import org.springframework.web.multipart.MultipartFile;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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