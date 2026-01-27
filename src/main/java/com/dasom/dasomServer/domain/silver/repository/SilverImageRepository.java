package com.dasom.dasomServer.domain.silver.repository;

import com.dasom.dasomServer.domain.silver.entity.SilverImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SilverImageRepository extends JpaRepository<SilverImage, Long> {
    // 특정 어르신의 loginId로 등록된 이미지들 찾기 (필요시)
    List<SilverImage> findAllBySilverLoginId(String loginId);
}