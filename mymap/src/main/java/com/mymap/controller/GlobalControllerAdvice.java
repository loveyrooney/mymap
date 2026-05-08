package com.mymap.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Value("${map.provider.key1}")
    private String key1;

    @Value("${map.provider.key2-front}")
    private String key2Front;

    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("key1", key1);
        model.addAttribute("key2Front", key2Front);
    }
}
