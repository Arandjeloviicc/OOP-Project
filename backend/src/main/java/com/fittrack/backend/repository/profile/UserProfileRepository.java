package com.fittrack.backend.repository.profile;

import com.fittrack.backend.entity.profile.UserProfile;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<@NonNull UserProfile, @NonNull Integer> {

    boolean existsByUserId(Integer userId);
}