package org.jetlinks.community.standalone.web;

import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class TimestampTokenService {

    /**
     * 生成时间戳 Token
     */
    public String generateTimestampToken() {
        long timestamp = System.currentTimeMillis();
        long random = ThreadLocalRandom.current().nextLong(100000, 999999);
        return timestamp + "" + random;
    }

    /**
     * 生成可验证的时间戳 Token
     */
    public String generateVerifiableToken(String secret) {
        long timestamp = System.currentTimeMillis();
        String random = UUID.randomUUID().toString().substring(0, 8);
        String data = timestamp + ":" + random;

        // 添加签名
        String signature = generateSignature(data, secret);
        return data + ":" + signature;
    }

    private String generateSignature(String data, String secret) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] bytes = sha256_HMAC.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            throw new RuntimeException("生成签名失败", e);
        }
    }
}