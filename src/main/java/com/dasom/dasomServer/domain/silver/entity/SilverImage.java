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

    public SilverImage(Silver silver, String originalFileName, String storedFileName) {
        super(null, originalFileName, storedFileName); // 부모(BaseImage) 생성자 호출
        this.silver = silver;
    }
}