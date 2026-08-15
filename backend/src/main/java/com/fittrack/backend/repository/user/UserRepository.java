package com.fittrack.backend.repository.user;

import org.jspecify.annotations.NonNull;
import com.fittrack.backend.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<@NonNull User, @NonNull Integer> {

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    Optional<User> findByEmail(String email);
}