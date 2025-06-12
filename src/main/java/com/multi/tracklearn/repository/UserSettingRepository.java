package com.multi.tracklearn.repository;

import com.multi.tracklearn.domain.UserSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSettingRepository extends JpaRepository<UserSetting, Long> {

}