package com.kiosk.headquarter.service;


import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.kiosk.entity.IcecreamFlavor;
import com.kiosk.headquarter.dto.flavor.HeadFlavorCreateRequestDTO;
import com.kiosk.headquarter.dto.flavor.HeadFlavorResponseDTO;
import com.kiosk.headquarter.dto.flavor.HeadFlavorUpdateRequestDTO;
import com.kiosk.headquarter.repository.HeadFlavorMapper;

import lombok.RequiredArgsConstructor;


/**
 * [코드 흐름 안내] HeadFlavorService
 *
 * <p>역할: 본사 관리의 아이스크림 맛 업무 규칙과 상태 변경을 처리한다.</p>
 * <p>호출 흐름: Controller 호출 -> 이 서비스 -> Pattern, HeadFlavorMapper -> Entity/DTO 변환 -> Controller 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HeadFlavorService {

	private final HeadFlavorMapper headFlavorMapper;
	private final AdminLogService adminLogService;
	private final FlavorImageFileStorage flavorImageFileStorage;
	private final FlavorImageUrlResolver flavorImageUrlResolver;
	private final DatabaseHardDeleteService databaseHardDeleteService;

    /*
     * 아이스크림 맛 등록
     */
    @Transactional
    /**
     * [메서드 흐름] createFlavor
     * Controller 또는 상위 서비스에서 호출되어 Pattern, HeadFlavorMapper을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
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
        // 쉬운주석: 중복 맛 이름 검사가 끝난 뒤 저장해야 실패한 요청의 사진이 폴더에 남지 않는다.
        String imageUrl = flavorImageFileStorage.store(imageFile);
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

        adminLogService.logAction("맛", savedFlavor.getFlavorName() + " 맛 신규 등록");
        return toResponseDTO(
                savedFlavor
        );
    }

    /*
     * 아이스크림 맛 전체 목록 조회
     */
    /**
     * [메서드 흐름] getFlavorList
     * Controller 또는 상위 서비스에서 호출되어 Pattern, HeadFlavorMapper을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public List<HeadFlavorResponseDTO>
            getFlavorList() {

        return headFlavorMapper
                .findByIsDeletedFalseOrderByIdDesc()
                .stream()
                .map(
                        this::toResponseDTO
                )
                .toList();
    }

    /*
     * 운영 중인 아이스크림 맛 목록 조회
     */
    /**
     * [메서드 흐름] getActiveFlavorList
     * Controller 또는 상위 서비스에서 호출되어 Pattern, HeadFlavorMapper을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public List<HeadFlavorResponseDTO>
            getActiveFlavorList() {

        return headFlavorMapper
                .findByIsActiveTrueAndIsDeletedFalseOrderByIdDesc()
                .stream()
                .map(
                        this::toResponseDTO
                )
                .toList();
    }

    /*
     * 아이스크림 맛 상세 조회
     */
    /**
     * [메서드 흐름] getFlavorDetail
     * Controller 또는 상위 서비스에서 호출되어 Pattern, HeadFlavorMapper을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
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
    /**
     * [메서드 흐름] updateFlavor
     * Controller 또는 상위 서비스에서 호출되어 Pattern, HeadFlavorMapper을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
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

        String imageUrl = flavor.getImageUrl();
        MultipartFile imageFile = requestDTO.getImageFile();

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

        if (imageFile != null && !imageFile.isEmpty()) {
            // 쉬운주석: 이름 중복 검사가 끝난 뒤에만 새 이미지를 저장하고 URL을 갈아 끼운다.
            imageUrl = flavorImageFileStorage.store(imageFile);
        }

        flavor.updateFlavor(
                flavorName,

                requestDTO.getIsActive() != null
                        ? requestDTO.getIsActive()
                        : flavor.getIsActive(),

                imageUrl
        );

        adminLogService.logAction("맛", flavor.getFlavorName() + " 맛 정보 수정");
        return toResponseDTO(
                flavor
        );
    }

    /*
     * 아이스크림 맛과 연결 데이터 영구 삭제
     */
    @Transactional
    /**
     * [메서드 흐름] deleteFlavor
     * Controller 또는 상위 서비스에서 호출되어 Pattern, HeadFlavorMapper을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public String deleteFlavor(
            Integer flavorId,
            String confirmation
    ) {

        IcecreamFlavor flavor =
                findFlavor(
                        flavorId
                );

        String flavorName = flavor.getFlavorName();
        HardDeleteConfirmation.verify(flavorName, confirmation);

        // 쉬운주석: is_deleted 표시만 바꾸지 않고 자식 DB 행부터 지운 뒤 맛 행까지 실제 삭제한다.
        int deleted = databaseHardDeleteService.deleteTree(
                "icecream_flavors", "flavor_id", flavorId
        );
        if (deleted != 1) {
            throw new IllegalStateException("아이스크림 맛 DB 행을 삭제하지 못했습니다.");
        }
        adminLogService.logAction("맛", flavorName + " 맛 영구 삭제");
        return "아이스크림 맛 삭제 성공";
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
                .findByIdAndIsDeletedFalse(
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
                        flavorImageUrlResolver.resolve(
                                flavor.getImageUrl()
                        )
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
}
