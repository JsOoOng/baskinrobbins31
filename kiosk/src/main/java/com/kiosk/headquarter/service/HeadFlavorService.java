package com.kiosk.headquarter.service;


import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.entity.IcecreamFlavor;
import com.kiosk.headquarter.dto.flavor.HeadFlavorCreateRequestDTO;
import com.kiosk.headquarter.dto.flavor.HeadFlavorResponseDTO;
import com.kiosk.headquarter.dto.flavor.HeadFlavorUpdateRequestDTO;
import com.kiosk.headquarter.repository.HeadFlavorMapper;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HeadFlavorService {
	
	private static final Pattern
    FLAVOR_IMAGE_URL_PATTERN =
    Pattern.compile(
            "^/images/flavors/"
            + "[a-z0-9]+"
            + "(?:_[a-z0-9]+)*"
            + "\\.(?:png|jpg)$"
    );

    private final HeadFlavorMapper
            headFlavorMapper;

    /*
     * 아이스크림 맛 등록
     */
    @Transactional
    public HeadFlavorResponseDTO createFlavor(
            HeadFlavorCreateRequestDTO requestDTO
    ) {

        if (requestDTO == null) {
            throw new IllegalArgumentException(
                    "맛 등록 요청이 없습니다."
            );
        }


        String flavorName =
                normalizeRequired(
                        requestDTO
                                .getFlavorName(),
                        "맛 이름을 입력해주세요."
                );

        MultipartFile imageFile = requestDTO.getImageFile();
        if (imageFile == null || imageFile.isEmpty()) {
            throw new IllegalArgumentException("이미지 파일을 첨부해주세요.");
        }
        
        String originalFilename = imageFile.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("올바른 파일이 아닙니다.");
        }
        
        String imageUrl = "/images/flavors/" + originalFilename;



        boolean alreadyExists =
                headFlavorMapper
                        .existsByFlavorNameIgnoreCase(
                                flavorName
                        );

        if (alreadyExists) {
            throw new IllegalArgumentException(
                    "이미 존재하는 맛 이름입니다."
            );
        }

        saveImageFile(imageFile, originalFilename);

        /*
         * 신규 맛은 항상 활성화 상태로 생성합니다.
         */
        IcecreamFlavor flavor =
                IcecreamFlavor.create(
                        flavorName,
                        imageUrl
                );
        
        // requestDTO.getIsActive()가 있으면 적용
        if (requestDTO.getIsActive() != null) {
            flavor.updateFlavor(flavorName, requestDTO.getIsActive(), imageUrl);
        }


        IcecreamFlavor savedFlavor =
                headFlavorMapper
                        .saveAndFlush(
                                flavor
                        );

        return toResponseDTO(
                savedFlavor
        );
    }

    /*
     * 아이스크림 맛 전체 목록 조회
     */
    public List<HeadFlavorResponseDTO>
            getFlavorList() {

        return headFlavorMapper
                .findAllByOrderByIdDesc()
                .stream()
                .map(
                        this::toResponseDTO
                )
                .toList();
    }

    /*
     * 운영 중인 아이스크림 맛 목록 조회
     */
    public List<HeadFlavorResponseDTO>
            getActiveFlavorList() {

        return headFlavorMapper
                .findByIsActiveTrueOrderByIdDesc()
                .stream()
                .map(
                        this::toResponseDTO
                )
                .toList();
    }

    /*
     * 아이스크림 맛 상세 조회
     */
    public HeadFlavorResponseDTO
            getFlavorDetail(
                    Integer flavorId
            ) {

        IcecreamFlavor flavor =
                findFlavor(
                        flavorId
                );

        return toResponseDTO(
                flavor
        );
    }

    /*
     * 아이스크림 맛 수정
     */
    @Transactional
    public HeadFlavorResponseDTO updateFlavor(
            Integer flavorId,
            HeadFlavorUpdateRequestDTO requestDTO
    ) {

        if (requestDTO == null) {
            throw new IllegalArgumentException(
                    "맛 수정 요청이 없습니다."
            );
        }

        IcecreamFlavor flavor =
                findFlavor(
                        flavorId
                );


        String flavorName =
                normalizeRequired(
                        requestDTO
                                .getFlavorName(),
                        "맛 이름을 입력해주세요."
                );

        MultipartFile imageFile = requestDTO.getImageFile();
        if (imageFile == null || imageFile.isEmpty()) {
            throw new IllegalArgumentException("이미지 파일을 첨부해주세요.");
        }
        
        String originalFilename = imageFile.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("올바른 파일이 아닙니다.");
        }
        
        String imageUrl = "/images/flavors/" + originalFilename;


        boolean alreadyExists =
                headFlavorMapper
                        .existsByFlavorNameIgnoreCaseAndIdNot(
                                flavorName,
                                flavorId
                        );

        if (alreadyExists) {
            throw new IllegalArgumentException(
                    "이미 존재하는 맛 이름입니다."
            );
        }

        flavor.updateFlavor(
                flavorName,

                requestDTO.getIsActive() != null
                        ? requestDTO.getIsActive()
                        : flavor.getIsActive(),

                imageUrl
        );

        return toResponseDTO(
                flavor
        );
    }

    /*
     * 아이스크림 맛 비활성화
     */
    @Transactional
    public String deleteFlavor(
            Integer flavorId
    ) {

        IcecreamFlavor flavor =
                findFlavor(
                        flavorId
                );

        flavor.deactivate();

        return "아이스크림 맛 비활성화 성공";
    }

    /*
     * 맛 엔티티 공통 조회
     */
    private IcecreamFlavor findFlavor(
            Integer flavorId
    ) {

        if (
                flavorId == null ||
                flavorId <= 0
        ) {
            throw new IllegalArgumentException(
                    "맛 번호가 올바르지 않습니다."
            );
        }

        return headFlavorMapper
                .findById(
                        flavorId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "존재하지 않는 맛입니다."
                        )
                );
    }

    /*
     * 응답 DTO 변환
     */
    private HeadFlavorResponseDTO toResponseDTO(
            IcecreamFlavor flavor
    ) {

        return HeadFlavorResponseDTO
                .builder()
                .flavorId(
                        flavor.getId()
                )
                .flavorName(
                        flavor.getFlavorName()
                )
                .isActive(
                        flavor.getIsActive()
                )
                .imageUrl(
                        flavor.getImageUrl()
                )
                .build();
    }

    /*
     * 필수 문자열 검증 및 정리
     */
    private String normalizeRequired(
            String value,
            String errorMessage
    ) {

        if (
                value == null ||
                value.isBlank()
        ) {
            throw new IllegalArgumentException(
                    errorMessage
            );
        }

        return value.trim();
    }

    /*
     * 아이스크림 맛 이미지 경로 검사
     *
     * 허용 형식:
     * /images/flavors/black_sorbet.png
     */
    private void validateImageUrl(
            String imageUrl
    ) {

        if (
                imageUrl == null ||
                imageUrl.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "맛 이미지 URL을 입력해주세요."
            );
        }

        String normalizedImageUrl =
                imageUrl.trim();

        boolean validImageUrl =
                FLAVOR_IMAGE_URL_PATTERN
                        .matcher(
                                normalizedImageUrl
                        )
                        .matches();

        if (!validImageUrl) {
            throw new IllegalArgumentException(
                    "맛 이미지 URL은 "
                    + "/images/flavors/파일명.png "
                    + "이거나"		
                    + "/images/flavors/파일명.jpg "
                    + "형식이어야 합니다. "
                    + "파일명은 영문 소문자, 숫자, "
                    + "언더스코어만 사용할 수 있습니다."
            );
        }
    }

    private void saveImageFile(MultipartFile file, String filename) {
        try {
            String uploadDir = "F:/박지성/baskinrobbins31/kiosk-frontend/public/images/flavors";
            Path uploadPath = Paths.get(uploadDir);
            
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            Path filePath = uploadPath.resolve(filename);
            file.transferTo(filePath.toFile());
        } catch (IOException e) {
            throw new RuntimeException("이미지 파일 저장에 실패했습니다.", e);
        }
    }
}
