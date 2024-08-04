package cuk.api.Trinity;

import cuk.api.Management.ManagementService;
import cuk.api.Trinity.Entities.TrinityInfo;
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
    private final ManagementService managementService;

    @Autowired
    public TrinityService(TrinityRepository trinityRepository, ManagementService managementService) {
        this.trinityRepository = trinityRepository;
        this.managementService = managementService;
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

        // increment of login count
        managementService.incrementReqCount();

        // set shtm and yyyy
        TrinityInfo info = trinityUser.getTrinityInfo();
        info.setShtm(managementService.getShtm());
        info.setYyyy(managementService.getYyyy());

        trinityUser.setTrinityInfo(info);

        trinityRepository.clientClear(httpClient);
        httpClient = null;

        return trinityUser;
    }

    public GradesResponse getGrades(TrinityUser trinityUser) throws Exception {
        return trinityRepository.getGrades(trinityUser);
    }

    public SujtResponse getSujtNo(TrinityUser trinityUser, SubjtNoRequest subjtNoRequest) throws Exception {
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

        JavaNetCookieJar javaNetCookieJar = trinityRepository.getCookieJar(cookieManager, trinityUser);

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .cookieJar(javaNetCookieJar)
                .followRedirects(true)
                .build();

        SujtResponse sujtResponse = trinityRepository.getSujtNo(trinityUser, subjtNoRequest, cookieManager, httpClient);
        sujtResponse = trinityRepository.getRemainNo(trinityUser, sujtResponse, cookieManager, httpClient);

        trinityRepository.clientClear(httpClient);
        httpClient = null;

        return sujtResponse;
    }

    public void logout(TrinityUser trinityUser) throws Exception {
        trinityRepository.logout(trinityUser);
    }
}
