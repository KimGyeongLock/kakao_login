package com.example.kakaologin.repository;

import com.example.kakaologin.domain.KakaoUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KakaoUserRepository extends JpaRepository<KakaoUser, Long> {
    KakaoUser findByKakaoId(Long KakaoId);
}
