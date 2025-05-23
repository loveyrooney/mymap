package com.mymap.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping("/view")
@RequiredArgsConstructor
public class RenderController {

    @GetMapping("/main")
    public String mainRender(){
        return "main";
    }

    @GetMapping("/create")
    public String createRender(){return "create";}

    @GetMapping("/edit/{jno}")
    public String editRender(){return "edit";}

    @GetMapping("/map/{jno}")
    public String mapRender(@PathVariable Long jno){
        return "map";
    }


}
