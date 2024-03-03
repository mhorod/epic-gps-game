package com.example.server;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloWorldController {
    @GetMapping("/v1/hello-world")
    HelloWorldResponse  helloWorld() {
        return new HelloWorldResponse("Hello, world!");
    }

    record HelloWorldResponse(String message) {}
}