package wonderland.webauthn.webauthnserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

import java.util.List;

@SpringBootApplication
public class WebauthnServerApplication {//implements WebFluxConfigurer {

    public static void main(String[] args) {
        SpringApplication.run(WebauthnServerApplication.class, args);
    }

//    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("https://localhost.localdomain:8080, https://localhost.localdomain")
                .allowedMethods("POST", "GET", "OPTIONS")
                .allowCredentials(true);
    }

    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowCredentials(false);
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.setAllowedOriginPatterns(List.of("https://localhost.localdomain:[0-9]*"));
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.addAllowedOrigin("https://localhost.localdomain:8080, https://localhost.localdomain");
//        corsConfiguration.addExposedHeader(HttpHeaders.SET_COOKIE);
        UrlBasedCorsConfigurationSource corsConfigurationSource = new UrlBasedCorsConfigurationSource();
        corsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsWebFilter(corsConfigurationSource);
    }
}
