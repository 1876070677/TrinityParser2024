package cuk.api.Trinity.Entities;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    MEMBER ("ROLE_MEMBER"),
    ADMIN("ROLE_ADMIN");

    private final String role;
    private final String PREFIX = "ROLE_";

    public String getRoleWithoutPrefix() {
        return this.role.substring(PREFIX.length());
    }
}
