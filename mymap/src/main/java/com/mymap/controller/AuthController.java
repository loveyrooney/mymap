package com.mymap.controller;

import com.mymap.auth.JwtProvider;
import com.mymap.domain.user.UserDTO;
import com.mymap.domain.user.UserService;
import com.mymap.exception.BusinessException;
import com.mymap.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final JwtProvider jwtProvider;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserDTO userDTO) {
        System.out.println(userDTO.getUserId()+userDTO.getPassword());
        long userNo = userService.login(userDTO);
        return userService.generateToken(userNo);
    }

    @GetMapping("/refresh")
    public ResponseEntity<?> refresh(@CookieValue("refreshToken")String oldToken) {
        try{
            String userNoFromToken = jwtProvider.validateToken(oldToken).getSubject();
            long userNo = Long.parseLong(userNoFromToken);
            if (userNoFromToken==null){
                System.out.println("refresh: Client token is null");
                throw new BusinessException(ErrorCode.NOT_REGISTERED_TOKEN);
            }

            if (!userService.isValid(userNo)) {
                System.out.println("refresh: DB token is not exist");
                throw new BusinessException(ErrorCode.NOT_AUTHENTICATED_TOKEN);
            }
            // 유효한 토큰이면
            System.out.println("refresh: Client token is authorized");
            return userService.generateToken(userNo);
        } catch (Exception e){
            throw new BusinessException(ErrorCode.TOKEN_PROCESSING_FAILED);
        }

    }

    @GetMapping("/logout")
    public ResponseEntity<?> logout(@CookieValue("refreshToken") String oldToken) {
        System.out.println("logout: "+oldToken);
        try{
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
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TOKEN_PROCESSING_FAILED);
        }
    }
}
