package com.dasom.dasomServer.domain.user.service;

import com.dasom.dasomServer.domain.user.mapper.UserMapper;
import com.dasom.dasomServer.DTO.LoginResponse;
import com.dasom.dasomServer.DTO.RegisterRequest;
import com.dasom.dasomServer.DTO.User;
import com.dasom.dasomServer.DTO.UserImage;
import com.dasom.dasomServer.infra.storage.ImageService;
import com.dasom.dasomServer.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);


    @Lazy
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper; // MyBatis DAO (Mapper.xml과 연결)
    private final PasswordEncoder passwordEncoder;
    private final ImageService imageService; //  파일 저장/URL 변환 서비스

    @Transactional // 💡 회원가입/파일 저장을 하나의 트랜잭션으로 묶음
    @Override
    public LoginResponse createUser(RegisterRequest request, List<MultipartFile> imageFiles) {
        log.info("[START] createUser. LoginId: {}", request.getLoginId());

        if (userMapper.existsByLoginId(request.getLoginId()) > 0) {
            throw new IllegalStateException("이미 존재하는 아이디입니다: " + request.getLoginId());
        }

        User user = new User();
        user.setLoginId(request.getLoginId());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setGender(request.getGender());
        user.setBirthday(request.getBirthday());
        // 💡 [수정] 중복 호출되는 userMapper.insertUser(user); 삭제 (이것이 오류의 원인)

        // 3. 💡 [핵심] 부모 테이블(silvers)에 사용자를 먼저 INSERT
        userMapper.insertUser(user);
        log.info("[INFO] 'silvers' 테이블 저장 완료.");

        // 4. 💡 [핵심] 자식 테이블(silvers_images)에 이미지 정보 INSERT
        if (imageFiles != null && !imageFiles.isEmpty()) {
            // 💡 이미지 파일이 있을 경우 로그 추가
            log.info("[INFO] {}개의 이미지 파일 처리 시작.", imageFiles.size());
            for (MultipartFile file : imageFiles) {
                if (file.isEmpty()) continue;

                try {
                    String storedFilename = imageService.saveFile(file);

                    UserImage userImage = new UserImage();
                    userImage.setSilverId(user.getLoginId()); // 💡 FK로 login_id 사용
                    userImage.setOriginalFilename(file.getOriginalFilename());
                    userImage.setStoredFilename(storedFilename);

                    userMapper.insertUserImage(userImage);
                    log.info("[INFO] 'silvers_images' 테이블에 이미지 저장 완료: {}", storedFilename);

                } catch (IOException e) {
                    log.error("[ERROR] 파일 저장 오류. 롤백됩니다. LoginId: {}", request.getLoginId(), e);
                    // 💡 IOException 발생 시 @Transactional에 의해 user INSERT까지 롤백됨
                    throw new RuntimeException("이미지 저장에 실패했습니다.", e);
                }
            }
        } else {
            // 💡 이미지 파일이 없을 경우 로그 추가
            log.info("[INFO] 업로드된 이미지 파일 없음.");
        }


        log.info("[SUCCESS] 회원가입 완료. LoginId: {}", request.getLoginId());

        return LoginResponse.builder()
                .success(true)
                .message("회원가입 성공")
                .loginId(user.getLoginId())
                .name(user.getName())
                .gender(user.getGender())
                .birthday(user.getBirthday())
                .accessToken(null)
                .images(null)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> getUserByLoginId(String loginId) {
        // 💡 [핵심] UserMapper.xml의 'resultMap'이 1:N 조인을 처리하여
        //    User 객체 안의 'images' 리스트까지 채워줌
        return Optional.ofNullable(userMapper.findByLoginId(loginId));
    }


    /**
     * 💡 [수정됨] 반환 타입을 Optional<User>로 변경 (일관성 유지, NPE 방지)
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<User> getUserById(Long id) {
        return Optional.ofNullable(userMapper.selectUserById(id));
    }

    /**
     * 💡 [수정됨] UserService 인터페이스 구현을 위해 누락되었던 메소드 추가
     */
    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        // 💡 (주의) 1:N 조인 쿼리이므로 페이징 처리 권장
        return userMapper.selectAllUsers();
    }




    @Override
    @Transactional
    public LoginResponse authenticateUser(String loginId, String rawPassword) {
        // 💡 이미지 목록을 포함한 User 정보 조회
        Optional<User> optionalUser = getUserByLoginId(loginId);

        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        User user = optionalUser.get();
        boolean passwordMatches;

        passwordMatches = rawPassword.equals(user.getPassword());

        // 💡 [핵심] 입력된 비밀번호(rawPassword)와 DB의 암호화된 비밀번호 비교

        if (passwordEncoder.matches(rawPassword, user.getPassword())) {
//        if (passwordMatches) {
            log.info("LOGIN SUCCESS: User ID={}, loginId={}", user.getId(), user.getLoginId());

            // 💡 JWT 토큰 생성 (별도 서비스/Provider에서 구현 필요)
            JwtTokenProvider.LoginTokenDto tokenDto = jwtTokenProvider.createToken(user.getLoginId());
            String jwtAccessToken = tokenDto.accessToken;

            // 💡 [핵심] 'storedFilename'을 실제 접근 가능한 URL로 변환
            List<String> imageUrls = null;
            if (user.getImages() != null && !user.getImages().isEmpty()) {
                imageUrls = user.getImages().stream()
                        .map(image -> imageService.getFileUrl(image.getStoredFilename()))
                        .collect(Collectors.toList());
            }

            log.info("로그인 성공: User ID={}, loginId={}, birthday={}", user.getId(), user.getLoginId(), user.getBirthday());

            // 💡 로그인 성공 응답 (토큰, 사용자 정보, 이미지 URL 목록 포함)
            return LoginResponse.builder()
                    .success(true)
                    .message("로그인 성공")
                    .accessToken(jwtAccessToken)
                    .loginId(user.getLoginId())
                    .name(user.getName())
                    .gender(user.getGender())
                    .birthday(user.getBirthday())
                    .images(imageUrls)
                    .build();
        } else {
            log.warn("LOGIN FAILED: Password mismatch for ID={}", loginId);
            throw new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }
    }
}