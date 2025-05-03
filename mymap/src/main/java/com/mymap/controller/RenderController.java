package com.mymap.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping("/home")
@RequiredArgsConstructor
public class RenderController {

    @GetMapping("/main")
    public String mainRender(){
        return "main";
    }

    @GetMapping("/map/{jno}")
    public String mapRender(@PathVariable Long jno){
        return "map";
    }


}
