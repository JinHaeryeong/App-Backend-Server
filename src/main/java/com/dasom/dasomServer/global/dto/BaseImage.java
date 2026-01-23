package com.dasom.dasomServer.global.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// global/common/dto/BaseImage.java (공통 폴더에 위치)
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseImage {
    private Long id;
    private String originalFilename;
    private String storedFilename;
}