package com.dasom.dasomServer.infra.ai;

import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * ONNX 런타임 환경과 세션의 생명주기를 관리하는 컴포넌트.
 * 모델 로드/해제만 담당하며, 추론 로직은 LstmInferenceEngine에 위임한다.
 */
@Slf4j
@Component
public class LstmModelSession {

    @Value("classpath:model/lstm_personalized_model_final_v2.onnx")
    private Resource onnxModelResource;

    @Getter
    private OrtEnvironment environment;

    @Getter
    private OrtSession session;

    @PostConstruct
    public void init() {
        try {
            environment = OrtEnvironment.getEnvironment();
            session = environment.createSession(
                    extractModelToTempFile().getAbsolutePath(),
                    new OrtSession.SessionOptions()
            );
            log.info("ONNX LSTM 모델 로드 성공");
        } catch (Exception e) {
            log.error("ONNX 모델 초기화 실패", e);
            throw new RuntimeException("ONNX 모델 로드 실패", e);
        }
    }

    @PreDestroy
    public void cleanup() {
        try {
            if (session != null) session.close();
            if (environment != null) environment.close();
            log.info("ONNX 런타임 정상 종료");
        } catch (OrtException e) {
            log.error("ONNX 런타임 클린업 실패", e);
        }
    }

    public boolean isReady() {
        return session != null && environment != null;
    }

    // ── private helpers ──────────────────────────────────────────────────────

    private File extractModelToTempFile() throws IOException {
        File tempFile = File.createTempFile("onnx_model", ".onnx");
        try (InputStream inputStream = onnxModelResource.getInputStream()) {
            Files.copy(inputStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        tempFile.deleteOnExit();
        return tempFile;
    }
}
