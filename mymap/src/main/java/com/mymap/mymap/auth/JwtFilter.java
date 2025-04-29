package com.mymap.mymap.auth;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // 1. 요청 헤더에서 JWT 토큰을 추출
        String token = getJwtFromRequest(request);

        if (token != null && !token.isEmpty()) {
            try {
                // 2. validateToken 메서드를 사용하여 토큰을 검증하고 Claims 추출
                Claims claims = jwtProvider.validateToken(token);
                // 3. Claims에서 사용자 정보 추출
                String userNo = claims.getSubject(); // 예: sub 필드에 사용자가 저장되어 있다고 가정
                System.out.println("validate token: "+userNo);
                // 4. 사용자 인증 처리
                if (userNo != null) {
                    // 5. 사용자 정보를 기반으로 Authentication 객체 생성
                    Authentication authentication = new UsernamePasswordAuthenticationToken(Long.parseLong(userNo), null, new ArrayList<>());
                    // 6. SecurityContext에 인증 정보를 설정
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    System.out.println("if 안: "+SecurityContextHolder.getContext().getAuthentication());
                }
            } catch (Exception e) {
                // 토큰이 유효하지 않은 경우 예외 처리
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        // 7. 필터 체인으로 계속 요청을 전달
        chain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " 부분을 제외한 토큰만 반환
        }
        return null;
    }
}

