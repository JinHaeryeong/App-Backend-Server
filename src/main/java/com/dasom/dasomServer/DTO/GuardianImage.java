package com.dasom.dasomServer.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuardianImage {
    private Long id;
    private Long guardianId;
    private String originalFilename;
    private String storedFilename;
}