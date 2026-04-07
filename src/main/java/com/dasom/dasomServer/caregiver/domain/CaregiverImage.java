package com.dasom.dasomServer.caregiver.domain;

import com.dasom.dasomServer.shared.domain.BaseImage;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "caregivers_images")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CaregiverImage extends BaseImage {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caregiver_id")
    private Caregiver caregiver;
}
