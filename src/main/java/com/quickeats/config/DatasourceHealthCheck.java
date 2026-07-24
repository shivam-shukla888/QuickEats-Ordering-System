package com.quickeats.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

@Component
public class DatasourceHealthCheck {

    private static final Logger logger = LoggerFactory.getLogger(DatasourceHealthCheck.class);

    @Value("${spring.datasource.url:}")
    private String datasourceUrl;

    @Autowired(required = false)
    private Environment environment;

    @PostConstruct
    public void checkDatasourceUrl() {
        boolean isDev = environment != null && environment.acceptsProfiles(Profiles.of("dev"));
        if (datasourceUrl != null && datasourceUrl.contains("h2:mem") && !isDev) {
            logger.warn("WARNING: Running on in-memory H2 database in a non-dev environment. ALL DATA WILL BE LOST ON RESTART. Set SPRING_DATASOURCE_URL to persist data.");
        }
    }
}
