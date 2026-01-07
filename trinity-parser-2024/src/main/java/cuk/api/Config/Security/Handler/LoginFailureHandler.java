package cuk.api.Config.Security.Handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import cuk.api.ResponseEntities.ResponseMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class LoginFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    public LoginFailureHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
        ResponseMessage resp = new ResponseMessage();
        resp.setStatus(HttpStatus.BAD_REQUEST);
        resp.setMessage(getExceptionMessage(e));

        // JSON 응답 출력
        response.addHeader("Content-Type", "application/json; charset=UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.getWriter().write(objectMapper.writeValueAsString(resp));
        response.getWriter().flush();
    }

    private String getExceptionMessage(AuthenticationException exception) {
        if (exception instanceof BadCredentialsException) {
            return "존재하지 않는 아이디를 입력했거나 아이디와 비밀번호가 일치하지 않습니다";
        } else if (exception instanceof AccountExpiredException) {
            return "만료된 계정입니다.";
        } else if (exception instanceof CredentialsExpiredException) {
            return "비밀번호가 만료된 상태입니다.";
        } else if (exception instanceof DisabledException) {
            return "비활성화된 계정입니다.";
        } else if (exception instanceof LockedException) {
            return "계정이 잠금처리 되어 있습니다.";
        } else {
            return exception.getMessage();
        }
    }
}
