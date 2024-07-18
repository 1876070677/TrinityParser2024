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
    public JSONParser jsonParser() {
        return new JSONParser();
    }
}
