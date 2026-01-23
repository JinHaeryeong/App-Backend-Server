package com.dasom.dasomServer.domain.guardian.entity;

import com.dasom.dasomServer.domain.guardian.dto.Guardian;
import com.dasom.dasomServer.global.entity.BaseImage;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

//@Entity
@Table(name = "guardians_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GuardianImage extends BaseImage {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guardian_id")
    private Guardian guardian;
}
