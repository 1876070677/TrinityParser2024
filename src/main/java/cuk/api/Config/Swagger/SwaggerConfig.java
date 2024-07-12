package cuk.api.Config.Swagger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Server;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import javax.servlet.http.HttpSession;
import java.util.Collections;

@Configuration
@EnableWebMvc
@EnableOpenApi
public class SwaggerConfig implements WebMvcConfigurer{
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.
                addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/")
                .resourceChain(false);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/swagger-ui/")
                .setViewName("forward:" + "/swagger-ui/index.html");
    }

    @Bean
    public Docket api() {
        Server publicServer = new Server("Public", "https://trinity.dobby.kr", "실제 api url", Collections.emptyList(), Collections.emptyList());
        Server localServer = new Server("Test", "http://trinitydev.dobby.kr", "테스트", Collections.emptyList(), Collections.emptyList());
        return new Docket(DocumentationType.OAS_30)
                .servers(publicServer, localServer)
                .ignoredParameterTypes(HttpSession.class)
                .select()
                .apis(RequestHandlerSelectors.basePackage("cuk.api"))
                .paths(PathSelectors.any())
                .build()
                .groupName("API 1.0.0")
                .useDefaultResponseMessages(false)
                .apiInfo(apiInfo());
    }
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Gajang API Document")
                .description("오류 있으면 연락주세요")
                .version("v1")
                .build();
    }
}
