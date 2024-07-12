package cuk.api.Config.Security.Provider;

import cuk.api.Trinity.Entities.SecurityTrinityUser;
import cuk.api.Trinity.Entities.TrinityUser;
import cuk.api.Trinity.Request.LoginRequest;
import cuk.api.Trinity.TrinityService;
import cuk.api.Trinity.Entities.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TrinityProvider implements AuthenticationProvider {

    private final TrinityService trinityService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = (String) authentication.getPrincipal();
        String password = (String) authentication.getCredentials();

        LoginRequest loginRequest = new LoginRequest(username, password);

        SecurityTrinityUser securityTrinityUser = new SecurityTrinityUser();
        try {
            TrinityUser trinityUser = trinityService.login(loginRequest);
            trinityUser.setRole(Role.MEMBER);
            securityTrinityUser.setUser(trinityUser);

        } catch (Exception e) {
            throw new AuthenticationServiceException(e.getMessage());
        }

        return new UsernamePasswordAuthenticationToken(securityTrinityUser, password, securityTrinityUser.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
