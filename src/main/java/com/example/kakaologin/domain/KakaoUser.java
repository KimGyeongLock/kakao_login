package com.example.kakaologin.domain;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "kakao_user")
public class KakaoUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "kakao_id", unique = true, nullable = false)
    private Long kakaoId;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "profile_image_url")
    private String profileImageUrl;
}
