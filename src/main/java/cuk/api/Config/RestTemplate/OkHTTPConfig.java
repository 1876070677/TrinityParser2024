package cuk.api.Config.RestTemplate;

import okhttp3.*;
import org.json.simple.parser.JSONParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.net.CookieManager;
import java.net.CookiePolicy;

@Configuration
public class OkHTTPConfig {
    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder()
                .cookieJar(new JavaNetCookieJar(cookieManager()))
                .followRedirects(true)
                .build();
    }
    @Bean
    public CookieManager cookieManager() {
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        return new CookieManager();
    }
    @Bean
    public JSONParser jsonParser() {
        return new JSONParser();
    }
}
