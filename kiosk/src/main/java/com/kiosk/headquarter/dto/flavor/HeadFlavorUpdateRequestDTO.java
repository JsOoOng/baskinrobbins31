package com.kiosk.headquarter.dto.flavor;

import org.springframework.web.multipart.MultipartFile;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HeadFlavorUpdateRequestDTO {

    private String flavorName;
    private Boolean isActive;
    
    // 파일이 수정되었을 때만 전송됨
    private MultipartFile imageFile;
}