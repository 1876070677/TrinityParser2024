package cuk.api.Management;

import cuk.api.ResponseEntities.ResponseMessage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/manage")
@RequiredArgsConstructor
@Api(tags = "API 관련 정보 제공, API 명세를 아는 분들에게 특별 기능")
public class ManagementController {
    private final RedisTemplate<String, Integer> counterRedisTemplate;
    private final String REQUEST_CNT_KEY = "req_cnt";

    @GetMapping("/requestCnt")
    @ApiOperation("누적 로그인 횟수")
    public ResponseEntity<ResponseMessage> getRequestCnt() throws Exception {
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setStatus(HttpStatus.OK);
        responseMessage.setMessage("Success");
        responseMessage.setData(counterRedisTemplate.opsForValue().get(REQUEST_CNT_KEY));
        return new ResponseEntity<>(responseMessage, HttpStatus.OK);
    }
}
