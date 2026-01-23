package com.dasom.dasomServer.domain.guardian.dto;

import com.dasom.dasomServer.global.dto.BaseImage;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GuardianImage extends BaseImage {
    private Long guardianId;
}