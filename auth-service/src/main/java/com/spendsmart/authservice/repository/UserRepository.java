package com.spendsmart.authservice.repository;

import com.spendsmart.authservice.domain.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByUserId(Long userId);

    boolean existsByEmail(String email);

    List<User> findByIsActive(boolean isActive);

    List<User> findByCurrency(String currency);

    long countByIsActive(boolean isActive);

    void deleteByUserId(Long userId);
}
