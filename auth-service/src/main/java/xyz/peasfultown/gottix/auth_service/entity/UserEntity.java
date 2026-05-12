package xyz.peasfultown.gottix.auth_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "`user`")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "role", nullable = false)
    @Enumerated(value = EnumType.STRING)
    @Builder.Default
    private UserRole role = UserRole.CUSTOMER;

    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL
    )
    @Builder.Default
    private List<RefreshTokenEntity> refreshTokens = new ArrayList<>();

}
