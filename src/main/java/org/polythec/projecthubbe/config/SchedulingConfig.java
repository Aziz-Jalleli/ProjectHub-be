package org.polythec.projecthubbe.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration class to enable scheduling functionality for the application.
 * This allows the use of @Scheduled annotations for automatic task reminders.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
    // This empty configuration class enables Spring's scheduling capabilities
}