package com.PorTracker.PorTrackerBE.global.util;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Component
@Slf4j
public class EncryptionUtils {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128;
    private static final int IV_LENGTH_BYTE = 12;

    @Value("${app.encryption.key:}")
    private String rawKey;

    private SecretKeySpec secretKey;

    @PostConstruct
    public void init() {
        if (rawKey == null || rawKey.trim().isEmpty()) {
            log.warn("[Encryption] APP_ENCRYPTION_KEY is empty! Data encryption will not work properly.");
            return;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = digest.digest(rawKey.getBytes("UTF-8"));
            this.secretKey = new SecretKeySpec(keyBytes, "AES");
            log.info("[Encryption] EncryptionUtils initialized successfully with AES-256.");
        } catch (Exception e) {
            log.error("[Encryption] Failed to initialize EncryptionUtils", e);
        }
    }

    /**
     * 데이터를 AES-256-GCM으로 암호화합니다.
     * 암호문 포맷: Base64([12-byte IV][GCM Ciphertext + 16-byte Tag])
     */
    public String encrypt(String plainText) {
        if (plainText == null) {
            return null;
        }
        if (secretKey == null) {
            log.error("[Encryption] SecretKey is not initialized. Returning plaintext.");
            return plainText;
        }
        try {
            byte[] iv = new byte[IV_LENGTH_BYTE];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            byte[] cipherText = cipher.doFinal(plainText.getBytes("UTF-8"));

            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
            byteBuffer.put(iv);
            byteBuffer.put(cipherText);

            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (Exception e) {
            log.error("[Encryption] Failed to encrypt data", e);
            return plainText;
        }
    }

    /**
     * AES-256-GCM으로 암호화된 데이터를 복호화합니다.
     * 복호화 실패 시 기존 평문 데이터를 위해 원본 데이터를 그대로 반환(Graceful Fallback)합니다.
     */
    public String decrypt(String cipherText) {
        if (cipherText == null) {
            return null;
        }
        if (secretKey == null) {
            return cipherText;
        }
        try {
            byte[] decoded;
            try {
                decoded = Base64.getDecoder().decode(cipherText);
            } catch (IllegalArgumentException e) {
                // Base64 디코딩 불가 시 평문으로 간주
                return cipherText;
            }

            if (decoded.length <= IV_LENGTH_BYTE) {
                return cipherText;
            }

            ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);
            byte[] iv = new byte[IV_LENGTH_BYTE];
            byteBuffer.get(iv);

            byte[] cipherBytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherBytes);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            byte[] decryptedBytes = cipher.doFinal(cipherBytes);
            return new String(decryptedBytes, "UTF-8");
        } catch (Exception e) {
            // 복호화 오류 시 평문으로 리턴하는 Graceful Fallback
            log.debug("[Encryption] Decryption failed. Returning original value as plaintext.");
            return cipherText;
        }
    }
}
