package nl.rotterdam.huwelijk.config;

import nl.rotterdam.huwelijk.WicketApplication;
import org.apache.wicket.protocol.http.WicketFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
        registration.addInitParameter(WicketFilter.FILTER_MAPPING_PARAM, "/*");
        registration.addInitParameter("applicationClassName", WicketApplication.class.getName());
        registration.addUrlPatterns("/*");
        return registration;
    }
}
