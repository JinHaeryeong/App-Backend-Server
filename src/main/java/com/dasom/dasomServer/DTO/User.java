package com.dasom.dasomServer.DTO;
import lombok.Data;

@Data
public class User {
    private Long userId;
    private Integer caregiber_id;
    private String login_id;
    private String password;
    private String name;
    private String gender;
    private String created_at;
}