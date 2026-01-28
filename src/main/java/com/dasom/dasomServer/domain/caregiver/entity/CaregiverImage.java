package com.dasom.dasomServer.domain.caregiver.entity;

import com.dasom.dasomServer.global.entity.BaseImage;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "caregivers_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CaregiverImage extends BaseImage {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caregiver_id")
    private Caregiver caregiver;
}