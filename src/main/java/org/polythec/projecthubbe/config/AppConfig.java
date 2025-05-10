package org.polythec.projecthubbe.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration class that enables scheduled tasks in the application.
 * This allows for automated task reminders and notifications.
 */
@Configuration
@EnableScheduling
public class AppConfig {
    // Additional application configuration can be added here
}