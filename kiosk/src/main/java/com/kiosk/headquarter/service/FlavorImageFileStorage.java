package com.kiosk.headquarter.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * 쉬운주석: 업로드된 맛 이미지를 검사하고 디스크에 저장하는 전담 직원이다.
 * 서비스는 이 클래스에 파일을 건네고, 저장이 끝나면 화면에서 사용할 URL만 돌려받는다.
 */
@Component
public class FlavorImageFileStorage {

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("png", "jpeg", "jpg", "webp");

    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of("image/png", "image/jpeg", "image/webp");

    private final Path uploadDirectory;

    public FlavorImageFileStorage(
            @Value("${app.upload.flavor-directory}") String uploadDirectory
    ) {
        this.uploadDirectory = Paths.get(uploadDirectory)
                .toAbsolutePath()
                .normalize();
    }

    /**
     * 쉬운주석: ① 파일을 검사하고 ② 겹치지 않는 새 이름을 만든 뒤
     * ③ 지정된 폴더에 저장하고 브라우저용 주소를 돌려준다.
     */
    public String store(MultipartFile file) {
        String extension = validateAndGetExtension(file);
        String storedFilename = createStoredFilename(extension);

        try {
            Files.createDirectories(uploadDirectory);
            Path destination = uploadDirectory.resolve(storedFilename).normalize();

            // 쉬운주석: normalize 결과가 저장 폴더 밖이면 해킹성 경로이므로 저장하지 않는다.
            if (!destination.startsWith(uploadDirectory)) {
                throw new IllegalArgumentException("올바르지 않은 이미지 저장 경로입니다.");
            }

            file.transferTo(destination.toFile());
            return "/images/flavors/" + storedFilename;
        } catch (IOException e) {
            throw new IllegalStateException("이미지 파일 저장에 실패했습니다.", e);
        }
    }

    /** 쉬운주석: 같은 원본 이름을 여러 번 올려도 UUID가 달라 서로 덮어쓰지 않는다. */
    String createStoredFilename(String extension) {
        String normalizedExtension = "jpeg".equals(extension) ? "jpg" : extension;
        return UUID.randomUUID().toString().replace("-", "")
                + "." + normalizedExtension;
    }

    /**
     * 쉬운주석: 파일 이름표만 믿지 않고 파일 첫 부분의 실제 신분증도 함께 확인한다.
     * PNG·JPEG·WEBP는 파일 첫 바이트가 각각 정해져 있어 가짜 확장자를 걸러낼 수 있다.
     */
    String validateAndGetExtension(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("이미지 파일을 첨부해주세요.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("올바른 파일이 아닙니다.");
        }

        String safeFilename = Paths.get(originalFilename.replace('\\', '/'))
                .getFileName().toString();
        int dot = safeFilename.lastIndexOf('.');
        if (dot <= 0 || dot == safeFilename.length() - 1) {
            throw unsupportedExtension();
        }

        String extension = safeFilename.substring(dot + 1).toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw unsupportedExtension();
        }

        String contentType = file.getContentType();
        if (contentType == null
                || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException(
                    "PNG, JPEG, JPG, WEBP 형식의 이미지 파일만 업로드할 수 있습니다."
            );
        }

        byte[] header = readHeader(file);
        boolean signatureMatches = hasMatchingSignature(extension, header);
        boolean canDecode = "webp".equals(extension) || canDecodeRasterImage(file);
        if (!signatureMatches || !canDecode) {
            throw new IllegalArgumentException(
                    "파일 이름은 이미지이지만 실제 내용이 PNG, JPEG, WEBP 이미지가 아닙니다."
            );
        }
        return extension;
    }

    private byte[] readHeader(MultipartFile file) {
        try (InputStream input = file.getInputStream()) {
            return input.readNBytes(12);
        } catch (IOException e) {
            throw new IllegalArgumentException("이미지 파일 내용을 읽을 수 없습니다.", e);
        }
    }

    private boolean canDecodeRasterImage(MultipartFile file) {
        try (InputStream input = file.getInputStream()) {
            // 쉬운주석: PNG/JPEG의 번호표뿐 아니라 그림의 가로·세로 정보까지 읽혀야 진짜로 인정한다.
            return ImageIO.read(input) != null;
        } catch (IOException e) {
            return false;
        }
    }

    private boolean hasMatchingSignature(String extension, byte[] bytes) {
        if ("png".equals(extension)) {
            return startsWith(bytes, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A);
        }
        if ("jpg".equals(extension) || "jpeg".equals(extension)) {
            return startsWith(bytes, 0xFF, 0xD8, 0xFF);
        }
        return startsWith(bytes, 0x52, 0x49, 0x46, 0x46)
                && bytes.length >= 12
                && bytes[8] == 0x57 && bytes[9] == 0x45
                && bytes[10] == 0x42 && bytes[11] == 0x50;
    }

    private boolean startsWith(byte[] bytes, int... signature) {
        if (bytes.length < signature.length) {
            return false;
        }
        for (int index = 0; index < signature.length; index++) {
            if (Byte.toUnsignedInt(bytes[index]) != signature[index]) {
                return false;
            }
        }
        return true;
    }

    private IllegalArgumentException unsupportedExtension() {
        return new IllegalArgumentException(
                "이미지 파일은 png, jpeg, jpg, webp 확장자만 허용됩니다."
        );
    }
}
