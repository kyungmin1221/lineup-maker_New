package org.example.lineupmaker_be.domain.util;

import org.springframework.stereotype.Component;
import java.security.SecureRandom;

@Component
public class EditTokenGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    // 인스턴스화 방지
    private EditTokenGenerator (){}

    public static String generateToken() {
        byte[] bytes = new byte[12];
        RANDOM.nextBytes(bytes);
        StringBuilder sb = new StringBuilder();
        for(byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
