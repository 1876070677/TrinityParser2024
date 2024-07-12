package cuk.api.Config.Security.Filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import cuk.api.ResponseEntities.ResponseMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RequiredArgsConstructor
public class EntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
        ResponseMessage resp = new ResponseMessage();
        resp.setStatus(HttpStatus.UNAUTHORIZED);
        resp.setMessage("인증되지 않은 사용자입니다.");

        // JSON 응답 출력
        response.addHeader("Content-Type", "application/json; charset=UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.getWriter().write(objectMapper.writeValueAsString(resp));
        response.getWriter().flush();
    }
}
