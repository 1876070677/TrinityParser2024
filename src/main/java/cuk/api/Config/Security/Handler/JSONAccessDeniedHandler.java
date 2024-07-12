package cuk.api.Config.Security.Handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import cuk.api.ResponseEntities.ResponseMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RequiredArgsConstructor
public class JSONAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper objectMapper;
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e) throws IOException, ServletException {
        ResponseMessage resp = new ResponseMessage();
        resp.setMessage("접근 권한이 없습니다");
        resp.setStatus(HttpStatus.FORBIDDEN);

        response.addHeader("Content-Type", "application/json; charset=UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.getWriter().write(objectMapper.writeValueAsString(resp));
        response.getWriter().flush();
    }
}
