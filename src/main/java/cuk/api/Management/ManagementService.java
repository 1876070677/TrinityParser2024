package cuk.api.Management;

import cuk.api.Management.Request.ConfigRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ManagementService {
    private final RedisTemplate<String, Integer> counterRedisTemplate;
    private final RedisTemplate<String, String> configRedisTemplate;

    private final String REQUEST_CNT_KEY = "req_cnt";
    private final String SHTM = "shtm";
    private final String YYYY = "yyyy";

    public void incrementReqCount() throws Exception{
        counterRedisTemplate.opsForValue().increment(REQUEST_CNT_KEY, 1);
    }

    public int getReqCount() throws Exception{
        return counterRedisTemplate.opsForValue().get(REQUEST_CNT_KEY);
    }

    public void setConfig(ConfigRequest configRequest) throws Exception{
        configRedisTemplate.opsForValue().set(SHTM, configRequest.getShtm());
        configRedisTemplate.opsForValue().set(YYYY, configRequest.getYyyy());
    }

    public String getShtm() throws Exception{
        return configRedisTemplate.opsForValue().get(SHTM);
    }

    public String getYyyy() throws Exception{
        return configRedisTemplate.opsForValue().get(YYYY);
    }
}
