package com.example.kakaologin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class KakaoService {

    @Value("${kakao.client.id}")
    private String KAKAO_CLINET_ID;

    @Value("${kakao.client.secret}")
    private String KAKAO_CLIENT_SECRET;

    @Value("${kakao.redirect.url}")
    private String KAKAO_REDIRECT_URL;

    private final static String KAKAO_AUTH_URL = "https://kauth.kakao.com";

    public String getKakaoLogin() {
        return KAKAO_AUTH_URL + "/oauth/authorize"
                + "?client_id=" + KAKAO_CLINET_ID
                + "&redirect_url" + KAKAO_REDIRECT_URL
                + "&response_type=code";
    }
}
