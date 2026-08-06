package com.kiosk.headquarter.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * 맛 이미지를 검증한 후 Amazon S3에 저장합니다.
 *
 * 반환값은 브라우저 URL이 아니라 S3 객체 키입니다.
 * 예: uploads/flavors/abc123.png
 */
@Component
public class FlavorImageFileStorage {

    private static final long MAX_FILE_SIZE = 10L * 1024L * 1024L;

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("png", "jpeg", "jpg", "webp");

    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of("image/png", "image/jpeg", "image/webp");

    private final S3Client s3Client;
    private final String bucket;
    private final String flavorPrefix;

    public FlavorImageFileStorage(
            S3Client s3Client,
            @Value("${aws.s3.bucket}") String bucket,
            @Value("${aws.s3.flavor-prefix:uploads/flavors}") String flavorPrefix
    ) {
        this.s3Client = s3Client;
        this.bucket = requireText(bucket, "S3 버킷 이름이 설정되지 않았습니다.");
        this.flavorPrefix = normalizePrefix(flavorPrefix);
    }

    /**
     * 이미지를 검증하고 S3에 업로드한 뒤 객체 키를 반환합니다.
     */
    public String store(MultipartFile file) {
        String extension = validateAndGetExtension(file);
        String storedFilename = createStoredFilename(extension);
        String objectKey = flavorPrefix + "/" + storedFilename;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .contentType(normalizeContentType(extension))
                .contentLength(file.getSize())
                .build();

        try (InputStream inputStream = file.getInputStream()) {
            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromInputStream(
                            inputStream,
                            file.getSize()
                    )
            );

            return objectKey;

        } catch (IOException e) {
            throw new IllegalStateException(
                    "업로드할 이미지 파일을 읽지 못했습니다.",
                    e
            );
        } catch (S3Exception e) {
            String awsMessage = e.awsErrorDetails() != null
                    ? e.awsErrorDetails().errorMessage()
                    : e.getMessage();

            throw new IllegalStateException(
                    "S3 이미지 업로드에 실패했습니다: " + awsMessage,
                    e
            );
        }
    }

    String createStoredFilename(String extension) {
        String normalizedExtension =
                "jpeg".equals(extension) ? "jpg" : extension;

        return UUID.randomUUID()
                .toString()
                .replace("-", "")
                + "."
                + normalizedExtension;
    }

    String validateAndGetExtension(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "이미지 파일을 첨부해 주세요."
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "이미지 파일은 10MB를 초과할 수 없습니다."
            );
        }

        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException(
                    "올바른 파일명이 아닙니다."
            );
        }

        String safeFilename =
                Paths.get(originalFilename.replace('\\', '/'))
                        .getFileName()
                        .toString();

        int dot = safeFilename.lastIndexOf('.');

        if (dot <= 0 || dot == safeFilename.length() - 1) {
            throw unsupportedExtension();
        }

        String extension =
                safeFilename.substring(dot + 1)
                        .toLowerCase(Locale.ROOT);

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw unsupportedExtension();
        }

        String contentType = file.getContentType();

        if (contentType == null
                || !ALLOWED_CONTENT_TYPES.contains(
                        contentType.toLowerCase(Locale.ROOT)
                )) {
            throw new IllegalArgumentException(
                    "PNG, JPEG, JPG, WEBP 이미지 파일만 업로드할 수 있습니다."
            );
        }

        byte[] header = readHeader(file);

        boolean signatureMatches =
                hasMatchingSignature(extension, header);

        // 기본 Java ImageIO는 WEBP를 지원하지 않을 수 있으므로
        // WEBP는 파일 시그니처 검증까지만 진행합니다.
        boolean canDecode =
                "webp".equals(extension)
                        || canDecodeRasterImage(file);

        if (!signatureMatches || !canDecode) {
            throw new IllegalArgumentException(
                    "파일 확장자는 이미지이지만 실제 내용이 올바른 이미지가 아닙니다."
            );
        }

        return extension;
    }

    private byte[] readHeader(MultipartFile file) {
        try (InputStream input = file.getInputStream()) {
            return input.readNBytes(12);
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "이미지 파일 내용을 읽을 수 없습니다.",
                    e
            );
        }
    }

    private boolean canDecodeRasterImage(MultipartFile file) {
        try (InputStream input = file.getInputStream()) {
            return ImageIO.read(input) != null;
        } catch (IOException e) {
            return false;
        }
    }

    private boolean hasMatchingSignature(
            String extension,
            byte[] bytes
    ) {
        if ("png".equals(extension)) {
            return startsWith(
                    bytes,
                    0x89, 0x50, 0x4E, 0x47,
                    0x0D, 0x0A, 0x1A, 0x0A
            );
        }

        if ("jpg".equals(extension)
                || "jpeg".equals(extension)) {
            return startsWith(bytes, 0xFF, 0xD8, 0xFF);
        }

        return startsWith(
                bytes,
                0x52, 0x49, 0x46, 0x46
        )
                && bytes.length >= 12
                && bytes[8] == 0x57
                && bytes[9] == 0x45
                && bytes[10] == 0x42
                && bytes[11] == 0x50;
    }

    private boolean startsWith(
            byte[] bytes,
            int... signature
    ) {
        if (bytes.length < signature.length) {
            return false;
        }

        for (int index = 0;
                index < signature.length;
                index++) {

            if (Byte.toUnsignedInt(bytes[index])
                    != signature[index]) {
                return false;
            }
        }

        return true;
    }

    private String normalizeContentType(String extension) {
        return switch (extension) {
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            case "webp" -> "image/webp";
            default -> "application/octet-stream";
        };
    }

    private String normalizePrefix(String prefix) {
        String normalized =
                requireText(
                        prefix,
                        "S3 이미지 저장 경로가 설정되지 않았습니다."
                )
                        .replace('\\', '/');

        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }

        while (normalized.endsWith("/")) {
            normalized =
                    normalized.substring(
                            0,
                            normalized.length() - 1
                    );
        }

        if (normalized.isBlank()) {
            throw new IllegalArgumentException(
                    "S3 이미지 저장 경로가 올바르지 않습니다."
            );
        }

        return normalized;
    }

    private String requireText(
            String value,
            String errorMessage
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(errorMessage);
        }

        return value.trim();
    }

    private IllegalArgumentException unsupportedExtension() {
        return new IllegalArgumentException(
                "이미지는 png, jpeg, jpg, webp 확장자만 허용합니다."
        );
    }
}