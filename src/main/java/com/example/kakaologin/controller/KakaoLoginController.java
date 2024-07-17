package com.example.kakaologin.controller;

import com.example.kakaologin.dto.KakaoUserInfoResponseDto;
import com.example.kakaologin.service.KakaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("")
@Tag(name = "Kakao Login API", description = "Kakao Login and Profile Management API")
public class KakaoLoginController {

    private final KakaoService kakaoService;

    @GetMapping("/login")
    @Operation(summary = "카카오 로그인 요청", description = "로그인 및 동의 후 인가 코드 발급")
    @ApiResponse(responseCode = "200", description = "로그인 URL이 성공적으로 생성되었습니다.", content = @Content(mediaType = "application/json"))
    public ResponseEntity<Map<String, String>> login() {
        String loginUrl = kakaoService.getKakaoLogin();
        return ResponseEntity.ok(Map.of("location", loginUrl));
    }

    @PostMapping("/callback")
    @Operation(summary = "카카오 로그인 콜백", description = "카카오 로그인 후 콜백 처리하여 사용자 정보 획득")
    @ApiResponse(responseCode = "200", description = "사용자 정보가 성공적으로 반환되었습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = KakaoUserInfoResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "잘못된 요청입니다.", content = @Content)
    public ResponseEntity<KakaoUserInfoResponseDto> handleKakaoCallback(@RequestBody Map<String, String> requestBody, HttpSession session) {
        String code = requestBody.get("code");
        if (code == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        String accessToken = kakaoService.getAccessTokenFromKakao(code);
        KakaoUserInfoResponseDto userInfo = kakaoService.getUserInfo(accessToken, session);


        return ResponseEntity.ok(userInfo);
    }

    // 사용자 정보 가져오기
    @GetMapping("/profile")
    @Operation(summary = "사용자 프로필 가져오기", description = "세션에 저장된 사용자 정보를 반환")
    @ApiResponse(responseCode = "200", description = "사용자 정보가 성공적으로 반환되었습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = KakaoUserInfoResponseDto.class)))
    @ApiResponse(responseCode = "401", description = "세션에 사용자 정보가 없습니다.", content = @Content)
    public ResponseEntity<KakaoUserInfoResponseDto> getUserProfile(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId != null) {
            KakaoUserInfoResponseDto userInfo = (KakaoUserInfoResponseDto) session.getAttribute("userInfo");
            return new ResponseEntity<>(userInfo, HttpStatus.OK);
        } else {
            log.warn("No userId found in session");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "세션을 무효화하여 로그아웃 처리")
    @ApiResponse(responseCode = "200", description = "성공적으로 로그아웃되었습니다.", content = @Content)
    public ResponseEntity<Void> logout(HttpSession session) {
        session.invalidate();
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
