package com.fitnessops.modules.auth.repository;

import com.fitnessops.modules.auth.entity.UserSession;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {
}
