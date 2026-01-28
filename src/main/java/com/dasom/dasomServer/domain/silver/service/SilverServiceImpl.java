package com.dasom.dasomServer.domain.silver.service;

import com.dasom.dasomServer.domain.silver.dto.LoginResponse;
import com.dasom.dasomServer.domain.silver.dto.RegisterRequest;
import com.dasom.dasomServer.domain.silver.dto.SilverResponse;
import com.dasom.dasomServer.domain.silver.entity.Silver;
import com.dasom.dasomServer.domain.silver.entity.SilverImage;
import com.dasom.dasomServer.domain.silver.repository.SilverImageRepository;
import com.dasom.dasomServer.domain.silver.repository.SilverRepository;
import com.dasom.dasomServer.global.security.JwtTokenProvider;
import com.dasom.dasomServer.infra.storage.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SilverServiceImpl implements UserService { // 인터페이스 명칭은 그대로 유지

    private final SilverRepository silverRepository;
    private final SilverImageRepository silverImageRepository;
    private final PasswordEncoder passwordEncoder;
    private final ImageService imageService;
    private final JwtTokenProvider jwtTokenProvider;



    @Transactional
    @Override
    public LoginResponse createUser(RegisterRequest request, List<MultipartFile> imageFiles) {
        log.info("[START] createUser. LoginId: {}", request.getLoginId());

        // 중복 체크 (JPA 리포지토리 활용)
        if (silverRepository.existsByLoginId(request.getLoginId())) {
            throw new IllegalStateException("이미 존재하는 아이디입니다: " + request.getLoginId());
        }

        LocalDateTime birthdayLDT = null;
        if (request.getBirthday() != null) {
            birthdayLDT = request.getBirthday().toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime();
        }


        // 엔티티 빌더
        Silver silver = Silver.builder()
                .loginId(request.getLoginId())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .gender(request.getGender())
                .birthday(birthdayLDT)
                .build();


        // 이미지 처리 (자식 테이블 저장)
        if (imageFiles != null && !imageFiles.isEmpty()) {
            for (MultipartFile file : imageFiles) {
                if (file.isEmpty()) continue;
                try {
                    String storedFilename = imageService.saveFile(file);

                    SilverImage silverImage = new SilverImage(
                            silver, // 부모 객체 전달
                            file.getOriginalFilename(),
                            storedFilename
                    );

                    // 핵심: silver 객체 내부의 리스트에 추가 (JPA가 cascade로 관리)
                    silver.getImages().add(silverImage);

                    log.info("[INFO] 이미지 객체 생성 및 리스트 추가: {}", storedFilename);
                } catch (IOException e) {
                    throw new RuntimeException("이미지 저장 실패로 회원가입이 롤백됩니다.", e);
                }
            }
        }

        // 부모만 저장 (cascade = CascadeType.ALL 덕분에 images 리스트도 한 번에 저장됨)
        silverRepository.save(silver);
        log.info("[INFO] Silver 엔티티 및 관련 이미지 전체 저장 완료.");

        return LoginResponse.builder()
                .success(true)
                .message("회원가입 성공")
                .loginId(silver.getLoginId())
                .name(silver.getName())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse authenticateUser(String loginId, String rawPassword) {
        // 1. 사용자 및 이미지 한방에 조회 (@EntityGraph 적용된 메서드)
        Silver silver = silverRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다."));

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(rawPassword, silver.getPassword())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        log.info("LOGIN SUCCESS: LoginId={}", silver.getLoginId());

        // 3. JWT 토큰 생성
        String jwtAccessToken = jwtTokenProvider.createToken(silver.getLoginId()).accessToken;

        // 4. 이미지 URL 리스트 생성 (엔티티의 images 리스트 활용)
        List<String> imageUrls = silver.getImages().stream()
                .map(image -> imageService.getFileUrl(image.getStoredFileName()))
                .collect(Collectors.toList());

        return LoginResponse.builder()
                .success(true)
                .message("로그인 성공")
                .accessToken(jwtAccessToken)
                .loginId(silver.getLoginId())
                .name(silver.getName())
                .gender(silver.getGender())
                .birthday(silver.getBirthday())
                .images(imageUrls)
                .build();
    }

    // --- 기존 조회 메서드들의 JPA화 ---

    @Override
    @Transactional(readOnly = true)
    public Optional<SilverResponse> getUserByLoginId(String loginId) {
        // 엔티티를 찾아서 DTO로 변환하여 반환
        return silverRepository.findByLoginId(loginId)
                .map(this::convertToResponse);
    }


    // 공통 변환 메서드
    private SilverResponse convertToResponse(Silver silver) {
        return SilverResponse.builder()
                .id(silver.getId())
                .loginId(silver.getLoginId())
                .name(silver.getName())
                .gender(silver.getGender())
                .birthday(silver.getBirthday())
                // 지원사 객체에서 ID만 추출
                .caregiverId(silver.getCaregiver() != null ? silver.getCaregiver().getId() : null)
                // 이미지 엔티티 리스트를 URL 리스트로 변환
                .imageUrls(silver.getImages().stream()
                        .map(image -> imageService.getFileUrl(image.getStoredFileName()))
                        .collect(Collectors.toList()))
                .build();
    }
    @Override
    @Transactional(readOnly = true)
    public Optional<SilverResponse> getUserById(Long id) {
        return silverRepository.findById(id)
                .map(this::convertToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SilverResponse> getAllUsers() {
        List<Silver> silvers = silverRepository.findAll();
        // 중복 코드를 지우고 convertToResponse를 호출하도록 변경
        return silvers.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
}

