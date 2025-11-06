package com.dasom.dasomServer.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaregiverImage {
    private Long id;
    private Long caregiverId;
    private String originalFilename;
    private String storedFilename;
}