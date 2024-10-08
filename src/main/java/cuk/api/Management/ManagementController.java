package cuk.api.Management;

import cuk.api.Management.Request.ConfigRequest;
import cuk.api.ResponseEntities.ResponseMessage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;

@Controller
@RequestMapping("/manage")
@RequiredArgsConstructor
@Api(tags = "관리 기능 (Admin만 접근 가능)")
public class ManagementController {
    private final ManagementService managementService;

    @GetMapping("/requestCnt")
    @ApiOperation("누적 로그인 횟수")
    public ResponseEntity<ResponseMessage> getRequestCnt() throws Exception {
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setStatus(HttpStatus.OK);
        responseMessage.setMessage("Success");
        responseMessage.setData(managementService.getReqCount());
        return new ResponseEntity<>(responseMessage, HttpStatus.OK);
    }

    @PostMapping("/auth/configure")
    @ApiOperation(value = "학기, 연도 등록")
    public ResponseEntity<ResponseMessage> setConfigure(@RequestBody @Valid ConfigRequest configRequest) throws Exception {
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setStatus(HttpStatus.OK);
        responseMessage.setMessage("Success");

        managementService.setConfig(configRequest);
        return new ResponseEntity<>(responseMessage, HttpStatus.OK);
    }
}
