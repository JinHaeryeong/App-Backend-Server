package com.dasom.dasomServer.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString; // Optional, but useful for debugging

@Getter
@Setter
@NoArgsConstructor // 기본 생성자 자동 생성
@AllArgsConstructor // 모든 필드를 인수로 받는 생성자 자동 생성
@ToString // 객체의 상태를 문자열로 쉽게 출력하기 위해 추가 (선택 사항)
public class CaregiverlResponseDTO {

    private String name;
    private String tel;
    private String gender;
    private String affiliation;
    private String storedFilename;
}