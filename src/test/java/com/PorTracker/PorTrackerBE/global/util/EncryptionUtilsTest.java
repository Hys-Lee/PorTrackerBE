package com.PorTracker.PorTrackerBE.global.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class EncryptionUtilsTest {

    private EncryptionUtils encryptionUtils;

    @BeforeEach
    void setUp() {
        encryptionUtils = new EncryptionUtils();
        // 32바이트 대칭키 모의 설정
        ReflectionTestUtils.setField(encryptionUtils, "rawKey", "my_secure_random_key_value_32bytes");
        encryptionUtils.init();
    }

    @Test
    @DisplayName("정상적인 문자열 암호화 및 복호화 성공 검증")
    void encryptAndDecryptSuccess() {
        // given
        String originalText = "Hello, PorTracker! OAuth Token 12345";

        // when
        String encryptedText = encryptionUtils.encrypt(originalText);
        String decryptedText = encryptionUtils.decrypt(encryptedText);

        // then
        assertThat(encryptedText).isNotNull();
        assertThat(encryptedText).isNotEqualTo(originalText); // 암호화되었으므로 달라야 함
        assertThat(decryptedText).isEqualTo(originalText);    // 복호화 시 원본과 같아야 함
    }

    @Test
    @DisplayName("복호화 실패 시 기존 평문을 그대로 반환하는 폴백 기능 검증")
    void decryptFallbackWithPlainText() {
        // given
        String plainText = "unencrypted_legacy_oauth_token";

        // when
        String decryptedText = encryptionUtils.decrypt(plainText);

        // then
        // Base64 디코딩 실패 또는 GCM 복호화 에러 발생 시 원본 평문을 그대로 반환해야 함
        assertThat(decryptedText).isEqualTo(plainText);
    }

    @Test
    @DisplayName("null 입력 시 null 반환 검증")
    void encryptDecryptNull() {
        assertThat(encryptionUtils.encrypt(null)).isNull();
        assertThat(encryptionUtils.decrypt(null)).isNull();
    }
}
