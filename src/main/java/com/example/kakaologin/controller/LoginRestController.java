package com.example.kakaologin.controller;

import com.example.kakaologin.dto.KakaoUserInfoResponseDto;
import com.example.kakaologin.service.KakaoService;
import com.sun.net.httpserver.HttpsServer;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class LoginRestController {

    private final KakaoService kakaoService;

    @GetMapping("/kakao-login-url")
    public ResponseEntity<String> login() {
        return new ResponseEntity<>(kakaoService.getKakaoLogin(), HttpStatus.OK);
    }

    @GetMapping("/callback")
    public void callback(@RequestParam("code") String code, HttpSession session, HttpServletResponse response) throws IOException {
        String accessToken = kakaoService.getAccessTokenFromKakao(code); // code를 이용해서 accessToken을 받아올 수 있게 됐다

        KakaoUserInfoResponseDto userInfo = kakaoService.getUserInfo(accessToken, session); // 액세스 토큰을 사용하여 카카오 사용자 정보를 조회
        session.setAttribute("userId", userInfo.getId());
        session.setAttribute("userInfo", userInfo);

        // Return the user information as response body
        //조회한 사용자 정보를 HTTP 응답 본문으로 반환하고, HTTP 상태 코드를 200 OK로 설정합니다.
        response.sendRedirect("/");
    }

    @GetMapping("/login-status")
    public ResponseEntity<?> checkLoginStatus(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId"); // 세션에서 userId 확인

        if (userId != null) {
            KakaoUserInfoResponseDto userInfo = (KakaoUserInfoResponseDto) session.getAttribute("userInfo");
            return new ResponseEntity<>(userInfo, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("User is not logged in", HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate(); //세션 무효화
        return new ResponseEntity<>("User logged out", HttpStatus.OK);
    }
}
