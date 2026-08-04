package com.kiosk.headquarter.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

class FlavorImageFileStorageTest {

    @TempDir
    Path temporaryDirectory;

    /*
     * 쉬운주석: 이름이 같은 이미지 두 장을 넣어도 UUID 이름이 서로 달라
     * 첫 번째 사진을 두 번째 사진이 덮어쓰지 않는지 확인한다.
     */
    @Test
    void storesSameOriginalFilenameWithDifferentUuidNames() throws Exception {
        FlavorImageFileStorage storage = storage();

        String firstUrl = storage.store(pngFile("same-name.png"));
        String secondUrl = storage.store(pngFile("same-name.png"));

        assertThat(firstUrl).isNotEqualTo(secondUrl);
        assertThat(firstUrl).matches("/images/flavors/[a-f0-9]{32}\\.png");
        assertThat(secondUrl).matches("/images/flavors/[a-f0-9]{32}\\.png");
        try (var storedFiles = Files.list(temporaryDirectory)) {
            assertThat(storedFiles).hasSize(2);
        }
    }

    /* 쉬운주석: danger.exe처럼 허용 목록에 없는 확장자는 저장 전에 거절해야 한다. */
    @Test
    void rejectsUnsupportedExtension() {
        MockMultipartFile file = new MockMultipartFile(
                "imageFile", "danger.exe", "image/png", pngBytes()
        );

        assertThatThrownBy(() -> storage().store(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("확장자만 허용");
    }

    /*
     * 쉬운주석: 파일 이름과 Content-Type을 PNG라고 속여도 실제 내용이 글자라면
     * PNG 고유 번호가 없으므로 가짜 이미지로 판단해야 한다.
     */
    @Test
    void rejectsTextDisguisedAsPng() {
        MockMultipartFile fakeImage = new MockMultipartFile(
                "imageFile", "fake.png", "image/png", "not an image".getBytes()
        );

        assertThatThrownBy(() -> storage().store(fakeImage))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("실제 내용");
        assertThat(temporaryDirectory).isEmptyDirectory();
    }

    /* 쉬운주석: 실제 내용은 PNG인데 이름만 JPG인 서로 안 맞는 파일도 거절한다. */
    @Test
    void rejectsExtensionThatDoesNotMatchActualBytes() {
        MockMultipartFile mismatchedImage = new MockMultipartFile(
                "imageFile", "wrong.jpg", "image/jpeg", pngBytes()
        );

        assertThatThrownBy(() -> storage().store(mismatchedImage))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("실제 내용");
    }

    private FlavorImageFileStorage storage() {
        return new FlavorImageFileStorage(temporaryDirectory.toString());
    }

    private MockMultipartFile pngFile(String originalFilename) {
        return new MockMultipartFile(
                "imageFile", originalFilename, "image/png", pngBytes()
        );
    }

    private byte[] pngBytes() {
        try {
            // 쉬운주석: Java가 가로 1픽셀·세로 1픽셀짜리 진짜 PNG를 직접 만들어 준다.
            BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(image, "png", output);
            return output.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("테스트 PNG 생성 실패", e);
        }
    }
}
