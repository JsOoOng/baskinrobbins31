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
