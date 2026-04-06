package com.dasom.dasomServer.silver.application;

import com.dasom.dasomServer.shared.security.TokenDto;
import com.dasom.dasomServer.silver.domain.RefreshToken;
import com.dasom.dasomServer.silver.presentation.dto.LoginResponse;
import com.dasom.dasomServer.silver.presentation.dto.SignupRequest;
import com.dasom.dasomServer.silver.presentation.dto.SilverResponse;
import com.dasom.dasomServer.silver.domain.Silver;
import com.dasom.dasomServer.silver.infrastructure.RefreshTokenRepository;
import com.dasom.dasomServer.silver.infrastructure.SilverRepository;
import com.dasom.dasomServer.shared.security.JwtTokenProvider;
import com.dasom.dasomServer.infra.storage.ImageService;
import com.dasom.dasomServer.shared.error.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final SilverRepository silverRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final ImageService imageService;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public void signupUser(SignupRequest request, MultipartFile imageFile) {
        validateDuplicateLoginId(request.getLoginId());
        String profileImageUrl = uploadProfileImage(imageFile);
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        Silver silver = Silver.createSilver(request, encodedPassword, profileImageUrl);
        silverRepository.save(silver);
    }

    @Transactional
    public LoginResponse authenticateUser(String loginId, String rawPassword) {
        Silver silver = findSilverByLoginId(loginId);
        silver.checkPassword(passwordEncoder, rawPassword);
        TokenDto tokenDto = jwtTokenProvider.createToken(silver.getLoginId(), "ROLE_USER");
        saveOrUpdateRefreshToken(silver.getLoginId(), tokenDto.getRefreshToken());

        return LoginResponse.from(
                silver,
                tokenDto.getAccessToken(),
                tokenDto.getRefreshToken(),
                "로그인 성공"
        );
    }

    public SilverResponse getUser(Long id, String currentLoginId) {
        Silver silver = silverRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        if (!silver.getLoginId().equals(currentLoginId)) {
            throw new AccessDeniedException("본인 정보만 조회할 수 있습니다.");
        }

        String fullUrl = imageService.getFileUrl(silver.getProfileImageUrl());

        return SilverResponse.from(silver, fullUrl);
    }

    public List<SilverResponse> getAllUsers() {
        return silverRepository.findAll().stream()
                .map(silver -> {
                    String fullUrl = imageService.getFileUrl(silver.getProfileImageUrl());
                    return SilverResponse.from(silver, fullUrl);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void logout(String loginId) {
        refreshTokenRepository.deleteByLoginId(loginId);
    }

    private void saveOrUpdateRefreshToken(String loginId, String token) {
        refreshTokenRepository.findByLoginId(loginId)
                .ifPresentOrElse(
                        existing -> existing.updateToken(token),
                        () -> refreshTokenRepository.save(new RefreshToken(loginId, token))
                );
    }

    private void validateDuplicateLoginId(String loginId) {
        if (silverRepository.existsByLoginId(loginId)) {
            throw new IllegalStateException("이미 존재하는 아이디입니다: " + loginId);
        }
    }

    private Silver findSilverByLoginId(String loginId) {
        return silverRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다."));
    }

    private String uploadProfileImage(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;

        try {
            return imageService.saveFile(file);
        } catch (IOException e) {
            throw new RuntimeException("프로필 이미지 업로드 실패", e);
        }
    }
}