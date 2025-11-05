package com.dasom.dasomServer.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Getter, Setter, toString 등을 자동 생성
@NoArgsConstructor // 기본 생성자
@AllArgsConstructor // 모든 필드를 받는 생성자
public class GuardianResponseDTO {
    private String name;
    private String tel;
    private String relationship;
    private String address;
}