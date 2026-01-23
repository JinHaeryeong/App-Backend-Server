package com.dasom.dasomServer.domain.caregiver.dto;

import com.dasom.dasomServer.global.dto.BaseImage;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true) // 부모 클래스의 필드까지 비교 대상에 넣음
public class CaregiverImage extends BaseImage {
    private String caregiverId;
}