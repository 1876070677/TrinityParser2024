package cuk.api.Trinity.Response;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;

@Data
public class LoginResponse {
    private String name;
    private ArrayList<String> roles;

    public LoginResponse(String name, Collection<? extends GrantedAuthority> authorities) {
        this.name = name;
        this.roles = new ArrayList<>();
        for (GrantedAuthority authority : authorities ) {
            roles.add(authority.getAuthority());
        }
    }
}
