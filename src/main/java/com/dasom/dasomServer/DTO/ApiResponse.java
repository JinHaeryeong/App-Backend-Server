package com.dasom.dasomServer.DTO;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {
    private final String result;
    private final String message;
    private final String errorCode;
    private final T data;

    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>("success", message, null, null);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("success", message, null, data);
    }

    // 에러 응답
    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return new ApiResponse<>("fail", message, errorCode, null);
    }
}