package com.mymap.controller;

import com.mymap.auth.JwtProvider;
import com.mymap.domain.user.UserDTO;
import com.mymap.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@Controller
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
        String token = jwtProvider.generateToken(Long.toString(userNo));
        return ResponseEntity.ok(Map.of("accessToken", token));
    }
}
