package com.dasom.dasomServer.silver.presentation.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.Date;


@Getter
@AllArgsConstructor
public class SignupRequest {

    @NotBlank(message = "아이디는 필수입니다.")
    @Size(min = 4, max = 20, message = "아이디는 4~20자 사이여야 합니다.")
    @Pattern(regexp = "^[^/\\\\..]*$", message = "아이디에 유효하지 않은 문자(/, \\, ..)가 포함되어 있습니다.")
    private String loginId;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
    private String password;

    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @NotNull(message = "생년월일은 필수입니다.")
    @Past(message = "생년월일은 과거 날짜여야 합니다.")
    private Date birthday;

    private char gender;

    @NotNull(message = "보호자 ID는 필수입니다.")
    private Long caregiverId;
}