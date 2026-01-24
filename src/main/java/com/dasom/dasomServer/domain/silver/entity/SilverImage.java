package com.dasom.dasomServer.domain.silver.entity;

import com.dasom.dasomServer.global.entity.BaseImage;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "silvers_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SilverImage extends BaseImage {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "silver_id", referencedColumnName = "login_id")
    private Silver silver;
}