package com.kiosk.headquarter.dto.common;

/**
 * [코드 흐름 안내] HeadApiResponse
 *
 * <p>역할: 본사 공통 API 응답 처리 결과를 프론트에 반환하기 위한 응답 DTO다.</p>
 * <p>호출 흐름: Entity/조회 결과 -> Service 변환 -> 이 DTO -> Controller JSON 응답 순서로 사용한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
public class HeadApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    public HeadApiResponse() {
    }

    public HeadApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static <T> HeadApiResponse<T> ok(String message, T data) {
        return new HeadApiResponse<>(true, message, data);
    }

    public static <T> HeadApiResponse<T> fail(String message) {
        return new HeadApiResponse<>(false, message, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}