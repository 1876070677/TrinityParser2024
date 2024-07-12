package cuk.api.Trinity;

import cuk.api.ResponseEntities.ResponseMessage;
import cuk.api.Trinity.Entities.SecurityTrinityUser;
import cuk.api.Trinity.Entities.TrinityUser;
import cuk.api.Trinity.Request.SubjtNoRequest;
import cuk.api.Trinity.Response.GradesResponse;
import cuk.api.Trinity.Response.SujtResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Controller
@RequestMapping("/trinity/auth")
@Api(tags = "트리니티 관련 기능")
public class TrinityController {
    private final TrinityService trinityService;

    @Autowired
    public TrinityController(TrinityService trinityService) {
        this.trinityService = trinityService;
    }

    @GetMapping("/grade")
    @ApiOperation("세션 정보 토대로 금학기 성적 조회, 비공개여도 받아올 수 있음")
    public ResponseEntity<ResponseMessage> getGrades() throws Exception{
        ResponseMessage resp = new ResponseMessage();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityTrinityUser securityTrinityUser = (SecurityTrinityUser) authentication.getPrincipal();
        TrinityUser trinityUser = securityTrinityUser.getUser();

        GradesResponse gradesResponse = trinityService.getGrades(trinityUser);
        resp.setStatus(HttpStatus.OK);
        resp.setMessage("Success");
        resp.setData(gradesResponse);
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    @PostMapping("/sujtNo")
    public ResponseEntity<ResponseMessage> getSujtNo(@RequestBody @Valid SubjtNoRequest subjtNoRequest) throws Exception{
        ResponseMessage resp = new ResponseMessage();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityTrinityUser securityTrinityUser = (SecurityTrinityUser) authentication.getPrincipal();
        TrinityUser trinityUser = securityTrinityUser.getUser();

        System.out.println(trinityUser.toString());


        SujtResponse sujtResponse = trinityService.getSujtNo(trinityUser, subjtNoRequest);

        resp.setStatus(HttpStatus.OK);
        resp.setMessage("Success");
        resp.setData(sujtResponse);
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }
}
