package com.dasom.dasomServer.DTO;


import lombok.Getter;

import java.util.Date;

@Getter
public class UserHealthResponse {
    private String gender;
    private Double rhr;
    private Date birthDay;
}
