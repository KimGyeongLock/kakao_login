package com.example.kakaologin.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/")
public class LoginRestController {

    public ResponseEntity<?> callback(@RequestParam("code") String code) {
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
