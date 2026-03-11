package com.sap.vcs.server.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/test")
<<<<<<< HEAD
    public String check() {
        return "Project is running";
    }
}
=======
    public String health() {
        return "project is running";
    }
}
>>>>>>> f4d0bbd (fix: tech stack - SpringBoot version changed from 4.0.3 to 3.5.11)
