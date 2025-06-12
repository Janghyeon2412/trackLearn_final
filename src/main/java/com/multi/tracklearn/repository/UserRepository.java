package com.multi.tracklearn.repository;

import com.multi.tracklearn.domain.User;
import com.multi.tracklearn.domain.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);

    Optional<User> findOptionalByEmail(String email);


    boolean existsByEmailAndStatus(String email, UserStatus status);
    Optional<User> findByEmailAndStatus(String email, UserStatus status);
    Optional<User> findByNicknameAndStatus(String nickname, UserStatus status);

}