package com.kiosk.headquarter.service;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Component
public class FlavorImageUrlResolver {

    private final S3Presigner s3Presigner;
    private final String bucket;
    private final long presignedUrlMinutes;

    public FlavorImageUrlResolver(
            S3Presigner s3Presigner,
            @Value("${aws.s3.bucket}") String bucket,
            @Value("${aws.s3.presigned-url-minutes:30}")
            long presignedUrlMinutes
    ) {
        this.s3Presigner = s3Presigner;
        this.bucket = bucket;
        this.presignedUrlMinutes = presignedUrlMinutes;
    }

    public String resolve(String storedValue) {
        if (storedValue == null || storedValue.isBlank()) {
            return storedValue;
        }

        String normalizedValue = storedValue.trim();

        /*
         * 기존 이미지:
         * /images/flavors/파일명.png
         *
         * 이미 완성된 외부 URL:
         * https://...
         *
         * 이러한 값은 변경하지 않습니다.
         */
        if (normalizedValue.startsWith("/")
                || normalizedValue.startsWith("http://")
                || normalizedValue.startsWith("https://")) {
            return normalizedValue;
        }

        /*
         * 그 외 값은 S3 객체 키로 판단합니다.
         *
         * 예:
         * uploads/flavors/abc.png
         * legacy/data/flavors/abc.png
         */
        GetObjectRequest getObjectRequest =
                GetObjectRequest.builder()
                        .bucket(bucket)
                        .key(normalizedValue)
                        .build();

        GetObjectPresignRequest presignRequest =
                GetObjectPresignRequest.builder()
                        .signatureDuration(
                                Duration.ofMinutes(
                                        presignedUrlMinutes
                                )
                        )
                        .getObjectRequest(getObjectRequest)
                        .build();

        return s3Presigner
                .presignGetObject(presignRequest)
                .url()
                .toString();
    }
}