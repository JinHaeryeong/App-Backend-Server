package com.dasom.dasomServer.DTO;

import lombok.Data;

@Data
public class SilverMedication {
    private Long id; // id BIGINT
    private String silverId; // silver_id VARCHAR(50)
    private String medicationType; // medication_type ENUM (또는 String)
    private String notes; // notes VARCHAR(30)
}