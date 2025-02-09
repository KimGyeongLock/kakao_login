package com.example.kakaologin.service;

import com.example.kakaologin.domain.KakaoUser;
import com.example.kakaologin.dto.KakaoTokenResponseDto;
import com.example.kakaologin.dto.KakaoUserInfoResponseDto;
import com.example.kakaologin.repository.KakaoUserRepository;
import io.netty.handler.codec.http.HttpHeaderValues;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@Slf4j
@RequiredArgsConstructor
@Service
public class KakaoService {

    @Value("${kakao.client_id}")
    private String KAKAO_CLIENT_ID;

    @Value("${kakao.client_secret}")
    private String KAKAO_CLIENT_SECRET;

    @Value("${kakao.redirect_uri}")
    private String KAKAO_REDIRECT_URL;

    private final static String KAUTH_TOKEN_URL_HOST = "https://kauth.kakao.com";
    private final static String KAUTH_USER_URL_HOST = "https://kapi.kakao.com";

    private final KakaoUserRepository kakaoUserRepository;

    public String getKakaoLogin() {
        return KAUTH_TOKEN_URL_HOST + "/oauth/authorize"
                + "?client_id=" + KAKAO_CLIENT_ID
                + "&redirect_uri=" + KAKAO_REDIRECT_URL
                + "&response_type=code";
    }

    public String getAccessTokenFromKakao(String code) {
        //Kakao 인증 서버의 토큰 발급 URL에 POST 요청
        KakaoTokenResponseDto kakaoTokenResponseDto = WebClient.create(KAUTH_TOKEN_URL_HOST).post()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .path("/oauth/token")
                        //요청의 콘텐츠 타입을 application/x-www-form-urlencoded로 설정하고, 요청 본문에 인증 코드 및 기타 필요한 정보를 포함
                        .queryParam("grant_type", "authorization_code")
                        .queryParam("client_id", KAKAO_CLIENT_ID)
                        .queryParam("client_secret", KAKAO_CLIENT_SECRET)
                        .queryParam("redirect_uri", KAKAO_REDIRECT_URL)
                        .queryParam("code", code)
                        .build(true))
                .header(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString())
                .retrieve()// HTTP 요청을 보냄
                //TODO : Custom Exception
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.error(new RuntimeException("Invalid Parameter")))
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> Mono.error(new RuntimeException("Internal Server Error")))
                .bodyToMono(KakaoTokenResponseDto.class)// 응답 본문을 KakaoTokenResponseDto로 변환
                .block(); // 비동기 처리를 동기적으로 변환 (결과가 완료될 때까지 기다린 후 단일 값을 반환)


        log.info(" [Kakao Service] Access Token ------> {}", kakaoTokenResponseDto.getAccessToken());
        log.info(" [Kakao Service] Refresh Token ------> {}", kakaoTokenResponseDto.getRefreshToken());
        //제공 조건: OpenID Connect가 활성화 된 앱의 토큰 발급 요청인 경우 또는 scope에 openid를 포함한 추가 항목 동의 받기 요청을 거친 토큰 발급 요청인 경우
        log.info(" [Kakao Service] Id Token ------> {}", kakaoTokenResponseDto.getIdToken());
        log.info(" [Kakao Service] Scope ------> {}", kakaoTokenResponseDto.getScope());

        return kakaoTokenResponseDto.getAccessToken();
    }


    public KakaoUserInfoResponseDto getUserInfo(String accessToken, HttpSession session) {

        KakaoUserInfoResponseDto userInfo = WebClient.create(KAUTH_USER_URL_HOST)
                .get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .path("/v2/user/me")
                        .build(true))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken) // access token 인가
                .header(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString())
                .retrieve()
                //TODO : Custom Exception
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.error(new RuntimeException("Invalid Parameter")))
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> Mono.error(new RuntimeException("Internal Server Error")))
                .bodyToMono(KakaoUserInfoResponseDto.class)
                .block();

        // DB에 사용자 정보 저장
        KakaoUser kakaoUser = kakaoUserRepository.findByKakaoId(userInfo.getId());
        if (kakaoUser == null) {
            kakaoUser = new KakaoUser();
            kakaoUser.setKakaoId(userInfo.getId());
        }
        kakaoUser.setNickname(userInfo.getKakaoAccount().getProfile().getNickName());
        kakaoUser.setProfileImageUrl(userInfo.getKakaoAccount().getProfile().getProfileImageUrl());
        kakaoUserRepository.save(kakaoUser);

        // 세션에 사용자 정보 저장
        session.setAttribute("userId", userInfo.getId());
        session.setAttribute("userInfo", userInfo);
        log.info("[ Session ID ] ---> {}", session.getId());

        log.info("[ Kakao Service ] Auth ID ---> {} ", userInfo.getId());
        log.info("[ Kakao Service ] NickName ---> {} ", userInfo.getKakaoAccount().getProfile().getNickName());
        log.info("[ Kakao Service ] ProfileImageUrl ---> {} ", userInfo.getKakaoAccount().getProfile().getProfileImageUrl());

        return userInfo;
    }


}