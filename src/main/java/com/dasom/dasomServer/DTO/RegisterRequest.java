package com.dasom.dasomServer.DTO;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Setter
@Getter
public class RegisterRequest {
    private String loginId;
    private String password;
    private String username;
    private LocalDate birthday;
    private char gender;
    private Long caregiverId;
}
