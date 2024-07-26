package cuk.api.Trinity;

import cuk.api.Trinity.Entities.TrinityUser;
import cuk.api.Trinity.Request.LoginRequest;
import cuk.api.Trinity.Request.SubjtNoRequest;
import cuk.api.Trinity.Response.GradesResponse;
import cuk.api.Trinity.Response.SujtResponse;
import lombok.RequiredArgsConstructor;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.net.CookieManager;
import java.net.CookiePolicy;

@Service
public class TrinityService {
    private final TrinityRepository trinityRepository;
    private final RedisTemplate<String, Integer> counterRedisTemplate;
    private final String REQUEST_CNT_KEY = "req_cnt";

    @Autowired
    public TrinityService(TrinityRepository trinityRepository, RedisTemplate<String, Integer> counterRedisTemplate) {
        this.trinityRepository = trinityRepository;
        this.counterRedisTemplate = counterRedisTemplate;
    }
    public TrinityUser login(LoginRequest loginRequest) throws Exception{

        TrinityUser trinityUser = new TrinityUser(loginRequest);

        // Re-use OkHttpClient
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .cookieJar(new JavaNetCookieJar(cookieManager))
                .followRedirects(true)
                .build();

        trinityUser = trinityRepository.loginForm(trinityUser, cookieManager, httpClient);

        trinityUser = trinityRepository.auth(trinityUser, cookieManager, httpClient);

        trinityUser = trinityRepository.login(trinityUser, cookieManager, httpClient);

        trinityUser = trinityRepository.getUserInfo(trinityUser, cookieManager, httpClient);

        trinityUser = trinityRepository.getSchoolInfo(trinityUser, cookieManager, httpClient);

        counterRedisTemplate.opsForValue().increment(REQUEST_CNT_KEY, 1);
        return trinityUser;
    }

    public GradesResponse getGrades(TrinityUser trinityUser) throws Exception {
        return trinityRepository.getGrades(trinityUser);
    }

    public SujtResponse getSujtNo(TrinityUser trinityUser, SubjtNoRequest subjtNoRequest) throws Exception {
        return trinityRepository.getSujtNo(trinityUser, subjtNoRequest);
    }

    public void logout(TrinityUser trinityUser) throws Exception {
        trinityRepository.logout(trinityUser);
    }
}
