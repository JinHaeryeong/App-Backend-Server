package com.dasom.dasomServer.shared.error;

import com.dasom.dasomServer.silver.presentation.dto.LoginResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class})
    public ResponseEntity<LoginResponse> handleBadRequest(Exception e) {
        log.warn("잘못된 요청: {}", e.getMessage());
        return ResponseEntity.badRequest().body(LoginResponse.builder()
                .success(false).message(e.getMessage()).build());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<LoginResponse> handleConflict(IllegalStateException e) {
        log.warn("중복 상황 발생: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(LoginResponse.builder()
                .success(false).message(e.getMessage()).build());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<LoginResponse> handleRuntimeError(RuntimeException e) {
        log.error("서버 내부 오류: ", e);
        return ResponseEntity.internalServerError().body(LoginResponse.builder()
                .success(false).message("서버 오류가 발생했습니다.").build());
    }
}