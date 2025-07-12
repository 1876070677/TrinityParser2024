package cuk.api;

import cuk.api.ResponseEntities.ResponseMessage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Api(tags="Redirection용도")
@Controller
@CrossOrigin(origins="*")
public class WelcomeController {
    @ApiOperation("Redirection 용도")
    @GetMapping("/")
    public String welcome() {
        return "redirect:/fe";
    }
}