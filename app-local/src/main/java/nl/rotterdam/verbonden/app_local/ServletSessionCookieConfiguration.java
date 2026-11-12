package nl.rotterdam.verbonden.app_local;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
class ServletSessionCookieConfiguration {

    @Bean
    ServletContextInitializer sessionCookieInitializer(
            @Value("${server.servlet.session.cookie.secure:true}") boolean secure,
            @Value("${server.servlet.session.cookie.http-only:true}") boolean httpOnly) {
        return servletContext -> {
            var cookieConfig = servletContext.getSessionCookieConfig();
            cookieConfig.setSecure(secure);
            cookieConfig.setHttpOnly(httpOnly);
        };
    }
}
