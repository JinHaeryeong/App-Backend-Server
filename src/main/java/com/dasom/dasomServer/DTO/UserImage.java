package com.dasom.dasomServer.DTO;

import lombok.Data;

@Data
public class UserImage {
    private Long id;
    private String silverId; // 'silvers.login_id'를 참조
    private String originalFilename;
    private String storedFilename;
}