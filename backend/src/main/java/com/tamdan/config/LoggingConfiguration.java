package com.tamdan.config;

import static tech.jhipster.config.logging.LoggingUtils.*;

import ch.qos.logback.classic.LoggerContext;
import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import tech.jhipster.config.JHipsterProperties;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/*
 * Configures the console and Logstash log appenders from the app properties
 */
@Configuration
@RequiredArgsConstructor
public class LoggingConfiguration {

    private final RuntimeConfigurationProperties runtimeProperties;
    private final JHipsterProperties jHipsterProperties;
    private final ObjectMapper mapper;

    @PostConstruct
    void configureLogging() throws JacksonException {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        Map<String, String> map = new HashMap<>();
        map.put("app_name", runtimeProperties.applicationName());
        map.put("app_port", runtimeProperties.serverPort());
        var customFields = mapper.writeValueAsString(map);

        var loggingProperties = jHipsterProperties.getLogging();
        var logstashProperties = loggingProperties.getLogstash();

        if (loggingProperties.isUseJsonFormat()) {
            addJsonConsoleAppender(context, customFields);
        }
        if (logstashProperties.isEnabled()) {
            addLogstashTcpSocketAppender(context, customFields, logstashProperties);
        }
        if (loggingProperties.isUseJsonFormat() || logstashProperties.isEnabled()) {
            addContextListener(context, customFields, loggingProperties);
        }
    }
}
