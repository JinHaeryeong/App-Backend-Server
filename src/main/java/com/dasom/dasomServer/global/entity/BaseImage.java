package com.dasom.dasomServer.global.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@MappedSuperclass
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class BaseImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK: BIG INT

    @Column(name = "original_filename", nullable = false, length = 255) //Not null
    private String originalFileName;

    @Column(name = "stored_filename", nullable = false, length = 255) //Not null
    private String storedFileName;
}
