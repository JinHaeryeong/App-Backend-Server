package com.dasom.dasomServer.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RegisterRequest {
    private String loginId;
    private String password;
    private String username;
    private String birth;
    private String gender;
    private Long caregiverId;


}
