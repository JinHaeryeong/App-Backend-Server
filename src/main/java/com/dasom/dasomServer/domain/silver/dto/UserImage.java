package com.dasom.dasomServer.domain.silver.dto;

import com.dasom.dasomServer.global.dto.BaseImage;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
/* 
* callSuper = true: 부모의 데이터가 다르면 다른 객체로 취급함
* callSuper = false: 부모 데이터가 달라도 내 필드만 같으면 같은 객체로 취급함
* */
public class UserImage extends BaseImage {
    private String silverId; // 'silvers.login_id'를 참조
}