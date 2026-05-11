package xyz.peasfultown.gottix.auth_service.entity;

import org.springframework.security.core.GrantedAuthority;

public enum UserRole implements GrantedAuthority {
    ADMIN, AGENT, CUSTOMER;

    public UserRole fromValue(String value) {
        for (UserRole r : UserRole.values())
            if (value.equals(r.name()))
                return r;
        throw new IllegalArgumentException(String.format(
                "Unknown role: %s", value
        ));
    }

    @Override
    public String getAuthority() {
        return this.name();
    }
}
