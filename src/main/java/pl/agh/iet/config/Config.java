package pl.agh.iet.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class Config {

    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of("Europe/Warsaw"));
    }

    @Bean
    public FilterRegistrationBean<CommonsRequestLoggingFilter> registrationFilter() {
        FilterRegistrationBean<CommonsRequestLoggingFilter> registrationFilter = new FilterRegistrationBean<>();

        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();

        filter.setIncludeQueryString(true);
        filter.setIncludePayload(true);
        filter.setIncludeHeaders(true);
        filter.setMaxPayloadLength(2048);
        filter.setAfterMessagePrefix("REQUEST DATA: ");
        filter.setAfterMessageSuffix("");

        registrationFilter.setFilter(filter);
        registrationFilter.addUrlPatterns("/streaming-api/*", "/video-details");

        return registrationFilter;
    }

    @Bean
    public MultipartResolver multipartResolver() {
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
        multipartResolver.setMaxUploadSize(209715200);
        return multipartResolver;
    }
}
