package nl.rotterdam.verbonden.core.config;

import org.apache.wicket.protocol.http.WicketFilter;
import org.apache.wicket.spring.SpringWebApplicationFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.apache.wicket.protocol.http.WicketFilter.APP_FACT_PARAM;
import static org.apache.wicket.protocol.http.WicketFilter.FILTER_MAPPING_PARAM;

@Configuration
public class WicketConfig {

    @Bean
    public WicketFilter wicketFilter() {
        return new WicketFilter();
    }

    @Bean
    public FilterRegistrationBean<WicketFilter> wicketFilterRegistration(WicketFilter wicketFilter) {
        FilterRegistrationBean<WicketFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(wicketFilter);
        registration.addInitParameter(FILTER_MAPPING_PARAM, "/*");
        registration.addInitParameter(APP_FACT_PARAM, SpringWebApplicationFactory.class.getName());
        registration.addUrlPatterns("/*");
        return registration;
    }
}
