package com.dasom.dasomServer.DTO;

import lombok.Getter;
import lombok.Setter;
import java.util.Date;

@Setter
@Getter
public class RegisterRequest {
    private String loginId;
    private String password;
    private String name;
    private Date birthday;
    private char gender;
    private Long caregiverId;
}