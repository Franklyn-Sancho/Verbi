package br.com.verbi.verbi.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.verbi.verbi.entity.TokenBlacklist;
import br.com.verbi.verbi.repository.TokenBlacklistRepository;

@Service
public class TokenBlacklistService {
    
    @Autowired
    private TokenBlacklistRepository tokenBlacklistRepository;

    public void blacklistToken(String token) {
        TokenBlacklist blacklistedToken = new TokenBlacklist();

        blacklistedToken.setToken(token);
        blacklistedToken.setExpirationDate(LocalDateTime.now().plusMinutes(30));
        tokenBlacklistRepository.save(blacklistedToken);
    }

    public boolean isTokenBlacklisted(String token) {
        return tokenBlacklistRepository.findByToken(token).isPresent();
    }
}
