package com.dasom.dasomServer.domain.health.dto;


import lombok.Getter;

import java.util.Date;

@Getter
public class UserHealthResponse {
    private String gender;
    private Double rhr;
    private Date birthDay;
}
