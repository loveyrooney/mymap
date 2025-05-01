package com.mymap.mymap.controller;

import com.mymap.mymap.auth.JwtProvider;
import com.mymap.mymap.domain.user.UserDTO;
import com.mymap.mymap.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class AuthController {
    private final JwtProvider jwtProvider;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserDTO userDTO) throws Exception{
        System.out.println(userDTO.getUserId()+userDTO.getPassword());
        long userNo = userService.login(userDTO);
        String token = jwtProvider.generateToken(Long.toString(userNo));
        return ResponseEntity.ok(Map.of("accessToken", token));
    }
}
