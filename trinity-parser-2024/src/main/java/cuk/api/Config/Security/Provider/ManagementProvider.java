package cuk.api.Config.Security.Provider;

import cuk.api.Management.Entities.User;
import cuk.api.Trinity.Entities.Role;
import cuk.api.Trinity.Entities.SecurityTrinityUser;
import cuk.api.Trinity.Entities.TrinityUser;
import cuk.api.Trinity.Request.LoginRequest;
import cuk.api.Trinity.TrinityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ManagementProvider implements AuthenticationProvider {

    private final RedisTemplate<String, String> configRedisTemplate;
    private final String ID = "id";
    private final String PASSWORD = "password";

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = (String) authentication.getPrincipal();
        String password = (String) authentication.getCredentials();

        User user = null;
        try {
            String adminId = configRedisTemplate.opsForValue().get(ID);
            String adminPassword = configRedisTemplate.opsForValue().get(PASSWORD);

            if (username.equals(adminId) && password.equals(adminPassword)) {
                user = new User(username, password, Role.ADMIN);
            } else {
                throw new AuthenticationServiceException("Invalid username or password");
            }

        } catch (Exception e) {
            throw new AuthenticationServiceException(e.getMessage());
        }

        return new UsernamePasswordAuthenticationToken(user, password, user.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
