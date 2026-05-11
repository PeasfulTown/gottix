package xyz.peasfultown.gottix.auth_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import xyz.peasfultown.gottix.auth_service.entity.RefreshTokenEntity;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {
    @Query("""
            select t from RefreshTokenEntity t where t.token = :token
            """)
    Optional<RefreshTokenEntity> findByToken(UUID token);
}
