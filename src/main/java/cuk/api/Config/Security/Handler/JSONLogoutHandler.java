package cuk.api.Config.Security.Handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import cuk.api.Config.Security.TrinitySecurityConfig;
import cuk.api.ResponseEntities.ResponseMessage;
import cuk.api.Trinity.Entities.SecurityTrinityUser;
import cuk.api.Trinity.Entities.TrinityUser;
import cuk.api.Trinity.TrinityService;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JSONLogoutHandler extends SimpleUrlLogoutSuccessHandler {
    private final ObjectMapper objectMapper;
    private final TrinityService trinityService;

    public JSONLogoutHandler(ObjectMapper objectMapper, TrinityService trinityService) {
        this.objectMapper = objectMapper;
        this.trinityService = trinityService;
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        ResponseMessage resp = new ResponseMessage();
        resp.setMessage("Success");
        resp.setStatus(HttpStatus.OK);

        SecurityTrinityUser securityTrinityUser = (SecurityTrinityUser) authentication.getPrincipal();
        TrinityUser trinityUser = securityTrinityUser.getUser();
        // Trinity에 로그아웃 요청
        try {
            trinityService.logout(trinityUser);
            response.addHeader("Content-Type", "application/json; charset=UTF-8");
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpStatus.OK.value());
            response.getWriter().write(objectMapper.writeValueAsString(resp));
            response.getWriter().flush();
        } catch (Exception e) {
            throw new ServletException("Failed to logout user");
        }
    }
}
