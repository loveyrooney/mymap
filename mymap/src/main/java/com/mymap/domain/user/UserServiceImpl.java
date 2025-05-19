package com.mymap.domain.user;

import com.mymap.auth.JwtProvider;
import com.mymap.exception.BusinessException;
import com.mymap.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@Log4j2
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public long login(UserDTO userDTO) {
        User user = userRepository.findByUserId(userDTO.getUserId())
                .orElseThrow(()-> new BusinessException(ErrorCode.NOT_REGISTERED));
        if(userDTO.getPassword().equals(user.getPassword()))
            //if (passwordEncoder.matches(userDTO.getPassword(),user.getPassword()))
            return user.getNo();
        else
            throw new BusinessException(ErrorCode.NOT_AUTHENTICATED);
    }

    @Override
    @Transactional
    public ResponseEntity<?> generateToken(long userNo) {
        deleteToken(userNo); // 기존 토큰 무효화
        try {
            String newAccessToken = jwtProvider.generateAccessToken(String.valueOf(userNo));
            String newRefreshToken = jwtProvider.generateRefreshToken(String.valueOf(userNo));
            storeRefreshToken(newRefreshToken, userNo, jwtProvider.validateToken(newRefreshToken).getExpiration());
            ResponseCookie cookie = ResponseCookie.from("refreshToken", newRefreshToken)
                    .httpOnly(true)
                    .secure(true) // HTTPS 환경에서만 사용
                    .path("/auth") // 쿠키 전송 대상 경로 제한
                    .maxAge(Duration.ofDays(7))
                    .sameSite("Strict") // 또는 "Lax", "None"
                    .build();
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(Map.of("accessToken", newAccessToken));
        } catch (Exception e){
            throw new BusinessException(ErrorCode.TOKEN_PROCESSING_FAILED);
        }
    }

    @Override
    public void storeRefreshToken(String token, long userNo, Date expiration) {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .userNo(userNo)
                .expiryDate(expiration)
                .build();
        refreshTokenRepository.save(refreshToken);
    }

    @Override
    public boolean isValid(long userNo) {
        return refreshTokenRepository.findByUserNo(userNo).isPresent();
    }

    @Override
    @Transactional
    public void deleteToken(long userNo) {
        refreshTokenRepository.deleteAllByUserNo(userNo);
    }
}
