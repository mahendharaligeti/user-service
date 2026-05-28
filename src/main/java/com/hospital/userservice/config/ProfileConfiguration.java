package com.hospital.userservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.Map;

@Configuration(proxyBeanMethods = false)
public class ProfileConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileConfiguration.class);

    public static final String DEVELOPMENT = "dev";
    public static final String TEST = "test";
    public static final String PRODUCTION = "prod";

    private static final String DEFAULT_PROFILE_PROPERTY = "spring.profiles.default";

    public static SpringApplication withDefaultProfile(SpringApplication application) {
        application.setDefaultProperties(Map.of(DEFAULT_PROFILE_PROPERTY, DEVELOPMENT));
        return application;
    }

    @Bean
    @Profile({DEVELOPMENT, TEST, PRODUCTION})
    ApplicationRunner profileRunner(Environment environment) {
        return args -> LOGGER.info("Active Spring profile: {}", resolveProfiles(environment));
    }

    private String resolveProfiles(Environment environment) {
        String[] activeProfiles = environment.getActiveProfiles();
        if (activeProfiles.length > 0) {
            return String.join(", ", activeProfiles);
        }
        return String.join(", ", Arrays.asList(environment.getDefaultProfiles()));
    }
}
