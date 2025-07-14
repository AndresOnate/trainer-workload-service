package com.epam.gymapp.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class LoginAttemptService {

    private final int MAX_ATTEMPT = 3;
    private final long BLOCK_TIME = 5 * 60 * 1000; // 5 minutos en milisegundos

    private final Map<String, Integer> attemptsCache = new ConcurrentHashMap<>();
    private final Map<String, Long> blockedUsers = new ConcurrentHashMap<>();

    public void loginSucceeded(String username) {
        attemptsCache.remove(username);
        blockedUsers.remove(username);
    }

    public void loginFailed(String username) {
        int attempts = attemptsCache.getOrDefault(username, 0);
        attempts++;
        attemptsCache.put(username, attempts);

        if (attempts >= MAX_ATTEMPT) {
            blockedUsers.put(username, System.currentTimeMillis() + BLOCK_TIME);
        }
    }

    public boolean isBlocked(String username) {
        if (!blockedUsers.containsKey(username)) return false;
        long expiryTime = blockedUsers.get(username);
        if (System.currentTimeMillis() > expiryTime) {
            // desbloquear automáticamente
            blockedUsers.remove(username);
            attemptsCache.remove(username);
            return false;
        }
        return true;
    }

    public long getRemainingBlockTime(String username) {
        if (!blockedUsers.containsKey(username)) return 0;
        return Math.max(0, (blockedUsers.get(username) - System.currentTimeMillis()) / 1000);
    }
}
