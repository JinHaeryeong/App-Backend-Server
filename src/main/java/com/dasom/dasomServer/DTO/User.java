package com.dasom.dasomServer.DTO;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
public class User {
    private Long id;
    private Long caregiverId;
    private String loginId;
    private String password;
    private String name;
    private char gender;
    private LocalDateTime createdAt;
    private Date birthday;
    // 1:N 관계 (Silver 1 : SilverImage N)
    private List<UserImage> images;
}