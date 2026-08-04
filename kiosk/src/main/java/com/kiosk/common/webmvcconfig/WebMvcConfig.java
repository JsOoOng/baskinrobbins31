package com.kiosk.common.webmvcconfig;

import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload.flavor-directory}")
    private String flavorUploadDirectory;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        /*
         * 쉬운주석: DB에는 /images/flavors/사진이름만 저장되어 있다.
         * 브라우저가 그 주소를 요청하면 아래 순서대로 실제 사진을 찾아 건네준다.
         * 1순위는 운영 서버의 업로드 폴더, 2순위는 예전 폴더,
         * 마지막은 프로젝트 안에 처음부터 들어 있던 기본 사진 폴더다.
         */
        String persistentLocation = Paths.get(flavorUploadDirectory)
                .toAbsolutePath()
                .normalize()
                .toUri()
                .toString();
        String legacyUploadLocation = Paths.get("uploads", "flavors")
                .toAbsolutePath()
                .normalize()
                .toUri()
                .toString();
        registry.addResourceHandler("/images/flavors/**")
                .addResourceLocations(
                        persistentLocation,
                        legacyUploadLocation,
                        "classpath:/static/images/flavors/"
                );
    }
}
