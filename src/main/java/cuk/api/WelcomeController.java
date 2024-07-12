package cuk.api;

import cuk.api.ResponseEntities.ResponseMessage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Api(tags="테스트 기능")
@Controller
public class WelcomeController {
    @ApiOperation("서버 테스트")
    @GetMapping("/")
    @ResponseBody
    public ResponseEntity<ResponseMessage> welcome(HttpSession session) {
        ResponseMessage resp = new ResponseMessage();
        resp.setStatus(HttpStatus.OK);
        resp.setMessage("Welcome to gajang~");
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }
}