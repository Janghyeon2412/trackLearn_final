package com.multi.tracklearn.repository;

import com.multi.tracklearn.domain.UserSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSettingRepository extends JpaRepository<UserSetting, Long> {
    // userId 기준으로 조회 (기본 메서드로 제공됨: findById)
}