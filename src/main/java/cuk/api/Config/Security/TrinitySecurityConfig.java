package cuk.api.Config.Security;

import com.fasterxml.jackson.databind.ObjectMapper;
import cuk.api.Config.Security.Filter.EntryPoint;
import cuk.api.Config.Security.Filter.TrinityLoginFilter;
import cuk.api.Config.Security.Handler.JSONAccessDeniedHandler;
import cuk.api.Config.Security.Handler.JSONLogoutHandler;
import cuk.api.Config.Security.Handler.LoginFailureHandler;
import cuk.api.Config.Security.Handler.LoginSuccessHandler;
import cuk.api.Config.Security.Provider.TrinityProvider;
import cuk.api.Trinity.Entities.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Order(2)
public class TrinitySecurityConfig {
    private final ObjectMapper objectMapper;
    private final TrinityProvider trinityProvider;

    @Bean
    public SecurityFilterChain filterChain2(HttpSecurity http) throws Exception {
        http
                .antMatcher("/trinity/**")
                .authorizeRequests()
                .antMatchers("/auth/**").hasRole(Role.MEMBER.getRoleWithoutPrefix())
                .antMatchers("/login", "/logout").permitAll()
                .anyRequest().authenticated()
                .and()
                .csrf().disable()
                .headers().disable()
                .httpBasic().disable()
                .formLogin().disable()
                .rememberMe().disable()
                .addFilterBefore(trinityLoginFilter(), UsernamePasswordAuthenticationFilter.class)
                .logout().logoutUrl("/trinity/logout")
                .logoutSuccessHandler(jsonLogoutHandler())
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(entryPoint())
                .accessDeniedHandler(jsonAccessDeniedHandler());
        return http.build();
    }

    @Bean
    public TrinityLoginFilter trinityLoginFilter() throws Exception {
        TrinityLoginFilter trinityLoginFilter = new TrinityLoginFilter(objectMapper);
        trinityLoginFilter.setAuthenticationManager(trinityAuthenticationManager());
        trinityLoginFilter.setAuthenticationSuccessHandler(loginSuccessHandler());
        trinityLoginFilter.setAuthenticationFailureHandler(loginFailureHandler());
        return trinityLoginFilter;
    }


    @Bean
    public AuthenticationManager trinityAuthenticationManager() throws Exception {//AuthenticationManager 등록
        return new ProviderManager(trinityProvider);
    }

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public LoginSuccessHandler loginSuccessHandler() {
        return new LoginSuccessHandler(objectMapper);
    }

    @Bean
    public LoginFailureHandler loginFailureHandler() {
        return new LoginFailureHandler(objectMapper);
    }

    @Bean
    public JSONLogoutHandler jsonLogoutHandler() {
        return new JSONLogoutHandler(objectMapper);
    }

    @Bean
    public EntryPoint entryPoint() {
        return new EntryPoint(objectMapper);
    }

    @Bean
    public JSONAccessDeniedHandler jsonAccessDeniedHandler() {
        return new JSONAccessDeniedHandler(objectMapper);
    }
}
