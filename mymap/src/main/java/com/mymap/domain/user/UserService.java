package com.mymap.domain.user;

import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Date;

public interface UserService {
    long login(UserDTO userDTO);
    void storeRefreshToken(String token, long userNo, Date expiration);
    boolean isValid(long userNo);
    void deleteToken(long userNo);
    ResponseEntity<?> generateToken(long userNo);
}
