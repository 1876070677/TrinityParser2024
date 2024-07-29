package cuk.api.Config.Security;

import com.fasterxml.jackson.databind.ObjectMapper;
import cuk.api.Config.Security.Filter.EntryPoint;
import cuk.api.Config.Security.Filter.ManagementLoginFilter;
import cuk.api.Config.Security.Handler.JSONAccessDeniedHandler;
import cuk.api.Config.Security.Handler.LoginFailureHandler;
import cuk.api.Config.Security.Handler.LoginSuccessHandler;
import cuk.api.Config.Security.Handler.ManagementLogoutHandler;
import cuk.api.Config.Security.Provider.ManagementProvider;
import cuk.api.Trinity.Entities.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Order(2)
public class ManagementSecurityConfig {
    private final ObjectMapper objectMapper;
    private final ManagementProvider managementProvider;

    private final JSONAccessDeniedHandler jsonAccessDeniedHandler;
    private final LoginFailureHandler loginFailureHandler;
    private final LoginSuccessHandler loginSuccessHandler;
    private final EntryPoint entryPoint;

    @Bean
    public SecurityFilterChain filterChain2(HttpSecurity http) throws Exception {
        http
                .antMatcher("/manage/**")
                .authorizeRequests()
                .antMatchers("/**").hasRole(Role.ADMIN.getRoleWithoutPrefix())
                .antMatchers("/login", "/logout").permitAll()
                .anyRequest().authenticated()
                .and()
                .csrf().disable()
                .cors().configurationSource(corsConfigurationSourceInManagement()).and()
                .headers().disable()
                .httpBasic().disable()
                .formLogin().disable()
                .rememberMe().disable()
                .addFilterBefore(managementLoginFilter(), UsernamePasswordAuthenticationFilter.class)
                .logout().logoutUrl("/manage/logout")
                .logoutSuccessHandler(managementLogoutHandler())
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(entryPoint)
                .accessDeniedHandler(jsonAccessDeniedHandler);
        return http.build();
    }

    @Bean
    public ManagementLoginFilter managementLoginFilter() throws Exception {
        ManagementLoginFilter managementLoginFilter = new ManagementLoginFilter(objectMapper);
        managementLoginFilter.setAuthenticationManager(managementAuthenticationManager());
        managementLoginFilter.setAuthenticationSuccessHandler(loginSuccessHandler);
        managementLoginFilter.setAuthenticationFailureHandler(loginFailureHandler);
        return managementLoginFilter;
    }

    @Bean
    public ManagementLogoutHandler managementLogoutHandler() throws Exception {
        return new ManagementLogoutHandler(objectMapper);
    }


    public AuthenticationManager managementAuthenticationManager() throws Exception {//AuthenticationManager 등록
        return new ProviderManager(managementProvider);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSourceInManagement() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("https://admin.dobby.kr");
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
