package com.example.weekly_report.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenService {

    private static class Entry {
        String value;
        Instant expiresAt;
        String subject;
    }

    private final Map<String, Entry> resetTokensByToken = new ConcurrentHashMap<>();
    private final Map<Long, Entry> twoFactorByUserId = new ConcurrentHashMap<>();

    public String createPasswordResetToken(String subject, long ttlSeconds) {
        String token = UUID.randomUUID().toString();
        Entry e = new Entry();
        e.value = token;
        e.expiresAt = Instant.now().plusSeconds(ttlSeconds);
        e.subject = subject;
        resetTokensByToken.put(token, e);
        return token;
    }

    public String consumePasswordResetToken(String token) {
        Entry e = resetTokensByToken.remove(token);
        if (e == null) return null;
        if (e.expiresAt.isBefore(Instant.now())) return null;
        return e.subject;
    }

    public String createTwoFactorCode(Long userId, long ttlSeconds) {
        String code = String.valueOf(100000 + (int)(Math.random() * 900000));
        Entry e = new Entry();
        e.value = code;
        e.expiresAt = Instant.now().plusSeconds(ttlSeconds);
        e.subject = String.valueOf(userId);
        twoFactorByUserId.put(userId, e);
        return code;
    }

    public boolean verifyTwoFactorCode(Long userId, String code) {
        Entry e = twoFactorByUserId.get(userId);
        if (e == null) return false;
        if (e.expiresAt.isBefore(Instant.now())) {
            twoFactorByUserId.remove(userId);
            return false;
        }
        boolean ok = e.value.equals(code);
        if (ok) twoFactorByUserId.remove(userId);
        return ok;
    }
}




