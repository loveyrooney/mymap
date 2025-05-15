package com.mymap.controller;

import com.mymap.auth.JwtProvider;
import com.mymap.domain.user.UserDTO;
import com.mymap.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Date;
import java.util.Map;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final JwtProvider jwtProvider;
    private final UserService userService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserDTO userDTO) throws Exception{
        System.out.println(userDTO.getUserId()+userDTO.getPassword());
        long userNo = userService.login(userDTO);
        String accessToken = jwtProvider.generateAccessToken(Long.toString(userNo));
        String refreshToken = jwtProvider.generateRefreshToken(Long.toString(userNo));
        userService.storeRefreshToken(refreshToken, userNo, jwtProvider.validateToken(refreshToken).getExpiration());
        // HttpOnly 쿠키로 refresh token 전송
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true) // HTTPS 환경에서만 사용
                .path("/auth") // 쿠키 전송 대상 경로 제한
                .maxAge(Duration.ofDays(7))
                .sameSite("Strict") // 또는 "Lax", "None"
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of("accessToken", accessToken));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@CookieValue("refreshToken")String oldToken) throws Exception {
        String userNoFromToken = jwtProvider.validateToken(oldToken).getSubject();
        long userNo = Long.parseLong(userNoFromToken);
        if (userNoFromToken==null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
        if (!userService.isValid(userNo)) {
            // 이미 사용되었거나 탈취된 것
            userService.deleteToken(userNo); // 모든 세션 로그아웃
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token reuse detected");
        }
        // 유효한 토큰이면
        userService.deleteToken(userNo); // 기존 토큰 무효화
        String newAccessToken = jwtProvider.generateAccessToken(userNoFromToken);
        String newRefreshToken = jwtProvider.generateRefreshToken(userNoFromToken);
        userService.storeRefreshToken(newRefreshToken, userNo, jwtProvider.validateToken(newRefreshToken).getExpiration());
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
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@CookieValue("refreshToken") String oldToken) throws Exception{
        System.out.println("logout: "+oldToken);
        String userNo = jwtProvider.validateToken(oldToken).getSubject();
        if (userNo!=null)
            userService.deleteToken(Long.parseLong(userNo));

        ResponseCookie cookie = ResponseCookie.from("refreshToken", oldToken)
                .httpOnly(true)
                .secure(true) // HTTPS 환경에서만 사용
                .path("/auth") // 쿠키 전송 대상 경로 제한
                .maxAge(0)
                .sameSite("Strict") // 또는 "Lax", "None"
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body("Logged out");
    }
}
