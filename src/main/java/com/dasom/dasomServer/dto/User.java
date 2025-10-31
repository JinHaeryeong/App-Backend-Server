package com.dasom.dasomServer.dto;
import lombok.Data;

@Data
public class User {
    private Long id;
    private Integer caregiverId;
    private String loginid;
    private String password;
    private String name;
    private String gender;
    private String created_at;
    private String birth_date;
}
