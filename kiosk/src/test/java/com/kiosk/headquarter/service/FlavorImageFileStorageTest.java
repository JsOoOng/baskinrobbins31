package com.kiosk.headquarter.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

class FlavorImageFileStorageTest {

    private S3Client s3Client;
    private FlavorImageFileStorage storage;

    @BeforeEach
    void setUp() {
        /*
         * 테스트 중에는 실제 AWS S3에 접속하지 않습니다.
         * 가짜 S3Client를 만들어 업로드 요청만 검증합니다.
         */
        s3Client = mock(S3Client.class);

        when(
                s3Client.putObject(
                        any(PutObjectRequest.class),
                        any(RequestBody.class)
                )
        ).thenReturn(
                PutObjectResponse.builder()
                        .eTag("test-etag")
                        .build()
        );

        storage = new FlavorImageFileStorage(
                s3Client,
                "test-bucket",
                "uploads/flavors"
        );
    }

    /*
     * 같은 원본 파일명으로 두 번 올려도
     * 서로 다른 UUID 객체 키를 생성해야 합니다.
     */
    @Test
    void storesSameOriginalFilenameWithDifferentUuidNames() {
        String firstKey =
                storage.store(
                        pngFile("same-name.png")
                );

        String secondKey =
                storage.store(
                        pngFile("same-name.png")
                );

        assertThat(firstKey)
                .isNotEqualTo(secondKey);

        assertThat(firstKey)
                .matches(
                        "^uploads/flavors/"
                        + "[a-f0-9]{32}"
                        + "\\.png$"
                );

        assertThat(secondKey)
                .matches(
                        "^uploads/flavors/"
                        + "[a-f0-9]{32}"
                        + "\\.png$"
                );

        /*
         * S3 업로드 요청이 정확히 두 번 호출됐는지 확인합니다.
         */
        ArgumentCaptor<PutObjectRequest> requestCaptor =
                ArgumentCaptor.forClass(
                        PutObjectRequest.class
                );

        verify(
                s3Client,
                times(2)
        ).putObject(
                requestCaptor.capture(),
                any(RequestBody.class)
        );

        List<PutObjectRequest> requests =
                requestCaptor.getAllValues();

        assertThat(requests)
                .extracting(PutObjectRequest::bucket)
                .containsOnly("test-bucket");

        assertThat(requests)
                .extracting(PutObjectRequest::contentType)
                .containsOnly("image/png");

        assertThat(requests)
                .extracting(PutObjectRequest::key)
                .containsExactly(
                        firstKey,
                        secondKey
                );
    }

    /*
     * exe 등 허용하지 않은 확장자는
     * S3에 업로드하기 전에 차단해야 합니다.
     */
    @Test
    void rejectsUnsupportedExtension() {
        MockMultipartFile file =
                new MockMultipartFile(
                        "imageFile",
                        "danger.exe",
                        "image/png",
                        pngBytes()
                );

        assertThatThrownBy(
                () -> storage.store(file)
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "확장자만 허용"
                );

        verify(
                s3Client,
                never()
        ).putObject(
                any(PutObjectRequest.class),
                any(RequestBody.class)
        );
    }

    /*
     * 파일명과 Content-Type만 PNG이고 실제 내용이 일반 문자열이면
     * 이미지로 인정하지 않아야 합니다.
     */
    @Test
    void rejectsTextDisguisedAsPng() {
        MockMultipartFile fakeImage =
                new MockMultipartFile(
                        "imageFile",
                        "fake.png",
                        "image/png",
                        "not an image".getBytes()
                );

        assertThatThrownBy(
                () -> storage.store(fakeImage)
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "실제 내용"
                );

        verify(
                s3Client,
                never()
        ).putObject(
                any(PutObjectRequest.class),
                any(RequestBody.class)
        );
    }

    /*
     * 실제 내용은 PNG인데 확장자를 JPG로 위장한 경우
     * 파일 시그니처가 일치하지 않으므로 차단해야 합니다.
     */
    @Test
    void rejectsExtensionThatDoesNotMatchActualBytes() {
        MockMultipartFile mismatchedImage =
                new MockMultipartFile(
                        "imageFile",
                        "wrong.jpg",
                        "image/jpeg",
                        pngBytes()
                );

        assertThatThrownBy(
                () -> storage.store(mismatchedImage)
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "실제 내용"
                );

        verify(
                s3Client,
                never()
        ).putObject(
                any(PutObjectRequest.class),
                any(RequestBody.class)
        );
    }

    /*
     * 10MB를 초과하는 파일은 S3 업로드 전에 차단해야 합니다.
     */
    @Test
    void rejectsFileLargerThanTenMegabytes() {
        byte[] oversizedBytes =
                new byte[
                        (10 * 1024 * 1024) + 1
                ];

        MockMultipartFile oversizedFile =
                new MockMultipartFile(
                        "imageFile",
                        "large.png",
                        "image/png",
                        oversizedBytes
                );

        assertThatThrownBy(
                () -> storage.store(oversizedFile)
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "10MB"
                );

        verify(
                s3Client,
                never()
        ).putObject(
                any(PutObjectRequest.class),
                any(RequestBody.class)
        );
    }

    private MockMultipartFile pngFile(
            String originalFilename
    ) {
        return new MockMultipartFile(
                "imageFile",
                originalFilename,
                "image/png",
                pngBytes()
        );
    }

    /*
     * 테스트에서 사용할 실제 PNG 바이트를 생성합니다.
     * 단순 문자열이 아니라 Java ImageIO로 정상 PNG를 만듭니다.
     */
    private byte[] pngBytes() {
        try {
            BufferedImage image =
                    new BufferedImage(
                            1,
                            1,
                            BufferedImage.TYPE_INT_RGB
                    );

            ByteArrayOutputStream output =
                    new ByteArrayOutputStream();

            ImageIO.write(
                    image,
                    "png",
                    output
            );

            return output.toByteArray();

        } catch (IOException e) {
            throw new IllegalStateException(
                    "테스트 PNG 생성 실패",
                    e
            );
        }
    }
}