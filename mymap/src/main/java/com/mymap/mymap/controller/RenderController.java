package com.mymap.mymap.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


@Controller
@RequiredArgsConstructor
public class RenderController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/main")
    public String mainRender(){
        return "main";
    }

    @GetMapping("/map")
    public String mapRender(){
        return "map";
    }


}
