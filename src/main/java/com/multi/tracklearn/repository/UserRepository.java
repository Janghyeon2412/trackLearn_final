package com.multi.tracklearn.repository;

import com.multi.tracklearn.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    User findByEmail(String email);
    boolean existsByNickname(String nickname);
    Optional<User> findOptionalByEmail(String email);
    Optional<User> findByNickname(String nickname);



}