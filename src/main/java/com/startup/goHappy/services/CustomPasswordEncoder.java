package com.startup.goHappy.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.spec.KeySpec;
import java.util.Base64;

public class CustomPasswordEncoder implements PasswordEncoder {

    @Value("${password.salt}")
    private String FIXED_SALT;
    @Value("${password.pepper}")
    private String SECRET_PEPPER;
    private static final int ITERATIONS = 100000;
    private static final int KEY_LENGTH = 256 * 8;

    @Override
    public String encode(CharSequence rawPassword) {
        return hashPassword(rawPassword.toString());
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        String newHash = hashPassword(rawPassword.toString()).trim();
        return newHash.equals(encodedPassword);
    }

    private String hashPassword(String password) {
        try {
            String pepperedPassword = password + SECRET_PEPPER;
            KeySpec spec = new PBEKeySpec(pepperedPassword.toCharArray(),
                    Base64.getDecoder().decode(FIXED_SALT),
                    ITERATIONS, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = factory.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
}