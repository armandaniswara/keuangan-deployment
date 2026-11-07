package com.example.Keuangan.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HalamanController {

    @GetMapping("/")
    public String dashboard(){
        return "dashboard";
    }
}
