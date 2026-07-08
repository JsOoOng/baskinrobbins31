package com.kiosk.headquarter.dto.common;

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