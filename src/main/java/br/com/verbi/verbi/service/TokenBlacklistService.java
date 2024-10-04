package br.com.verbi.verbi.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import br.com.verbi.verbi.entity.TokenBlacklist;
import br.com.verbi.verbi.repository.TokenBlacklistRepository;

@Service
public class TokenBlacklistService {

    private final TokenBlacklistRepository tokenBlacklistRepository;

    public TokenBlacklistService(TokenBlacklistRepository tokenBlacklistRepository) {
        this.tokenBlacklistRepository = tokenBlacklistRepository;
    }

    /**
     * Adds a token to the blacklist with a default expiration time.
     * 
     * @param token the token to blacklist
     */
    public void blacklistToken(String token) {
        TokenBlacklist blacklistedToken = new TokenBlacklist();
        blacklistedToken.setToken(token);
        blacklistedToken.setExpirationDate(LocalDateTime.now().plusMinutes(30)); // Default expiration: 30 min
        tokenBlacklistRepository.save(blacklistedToken);
    }

    /**
     * Checks if a token is blacklisted and ensures it hasn't expired.
     * 
     * @param token the token to check
     * @return true if the token is blacklisted and valid, false otherwise
     */
    public boolean isTokenBlacklisted(String token) {
        Optional<TokenBlacklist> tokenOptional = tokenBlacklistRepository.findByToken(token);

        if (tokenOptional.isPresent()) {
            TokenBlacklist blacklistedToken = tokenOptional.get();

            // If the token has expired, remove it from the blacklist
            if (blacklistedToken.getExpirationDate().isBefore(LocalDateTime.now())) {
                tokenBlacklistRepository.delete(blacklistedToken);
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Cleans up expired tokens from the blacklist to maintain optimal performance.
     */
    @Scheduled(cron = "0 0 * * * *") // Executes once an hour
    public void cleanUpExpiredTokens() {
        List<TokenBlacklist> expiredTokens = tokenBlacklistRepository.findAll().stream()
                .filter(token -> token.getExpirationDate().isBefore(LocalDateTime.now()))
                .collect(Collectors.toList());
        tokenBlacklistRepository.deleteAll(expiredTokens);
    }
}
