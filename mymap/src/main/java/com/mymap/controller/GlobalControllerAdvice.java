package com.mymap.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Value("${map.provider.kakao-key}")
    private String kakaoKey;

    @Value("${map.provider.naver-client-id}")
    private String naverClientId;

    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("kakaoKey", kakaoKey);
        model.addAttribute("naverClientId", naverClientId);
    }
}
