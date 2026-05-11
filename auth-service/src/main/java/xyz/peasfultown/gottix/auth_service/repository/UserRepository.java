package xyz.peasfultown.gottix.auth_service.repository;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import xyz.peasfultown.gottix.auth_service.entity.UserEntity;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    @Query("""
            SELECT u FROM UserEntity u WHERE u.email = :email
            """)
    Optional<UserEntity> findUserByEmail(@NotNull String email);
}
