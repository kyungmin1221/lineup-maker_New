package org.example.lineupmaker_be.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// FE(Vite 개발 서버 localhost:5173, 배포된 Vercel 도메인)에서 브라우저로 이 API를 호출하려면
// CORS 허용이 필요하다. 이게 없으면 FE의 fetch 요청이 전부 브라우저에서 차단된다.
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(
                        "http://localhost:5173",
                        "https://lineup-maker-tau.vercel.app"
                )
                .allowedMethods("GET", "POST", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
