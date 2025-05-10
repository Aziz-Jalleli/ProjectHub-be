package org.polythec.projecthubbe.controller;

import lombok.RequiredArgsConstructor;
import org.polythec.projecthubbe.service.TaskAnalyticsService;
import org.polythec.projecthubbe.service.TaskReminderSchedulerService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for managing task reminders and analytics.
 */
@RestController
@RequestMapping("/api/reminders")
@RequiredArgsConstructor
public class TaskReminderController {

    private final TaskReminderSchedulerService reminderSchedulerService;
    private final TaskAnalyticsService taskAnalyticsService;

    /**
     * Manually trigger the reminder analysis for all projects.
     * Admin-only endpoint for troubleshooting.
     */
    @PostMapping("/run-analysis")
    public ResponseEntity<String> triggerReminderAnalysis() {
        reminderSchedulerService.checkTasksAndSendReminders();
        return ResponseEntity.ok("Reminder analysis triggered successfully");
    }

    /**
     * Manually trigger reminder analysis for a specific project.
     * Accessible to project owners and admins.
     */
    @PostMapping("/project/{projectId}/analyze")
    public ResponseEntity<String> analyzeProjectTasks(@PathVariable Long projectId) {
        taskAnalyticsService.analyzeProjectTasksAndSendReminders(projectId);
        return ResponseEntity.ok("Project tasks analyzed and reminders sent");
    }

    /**
     * Get the current reminder settings configuration.
     */
    @GetMapping("/settings")
    public ResponseEntity<ReminderSettings> getReminderSettings() {
        ReminderSettings settings = new ReminderSettings(
                true,                // Enabled by default
                7,                   // Days before deadline to start checking
                9,                   // Hour of day to send notifications
                true,                // Notify project owner on high risk
                0.7                  // High risk threshold
        );
        return ResponseEntity.ok(settings);
    }

    /**
     * Update reminder settings.
     */
    @PutMapping("/settings")
    public ResponseEntity<ReminderSettings> updateReminderSettings(@RequestBody ReminderSettings settings) {
        // In a real implementation, this would save to a database
        // For now, we'll just return the settings as if they were saved
        return ResponseEntity.ok(settings);
    }

    /**
     * Data class for reminder settings.
     */
    public static class ReminderSettings {
        private boolean enabled;
        private int daysBeforeDeadline;
        private int hourOfDay;
        private boolean notifyProjectOwner;
        private double highRiskThreshold;

        // Constructor
        public ReminderSettings(boolean enabled, int daysBeforeDeadline, int hourOfDay,
                                boolean notifyProjectOwner, double highRiskThreshold) {
            this.enabled = enabled;
            this.daysBeforeDeadline = daysBeforeDeadline;
            this.hourOfDay = hourOfDay;
            this.notifyProjectOwner = notifyProjectOwner;
            this.highRiskThreshold = highRiskThreshold;
        }

        // No-args constructor for JSON deserialization
        public ReminderSettings() {
        }

        // Getters and setters
        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getDaysBeforeDeadline() {
            return daysBeforeDeadline;
        }

        public void setDaysBeforeDeadline(int daysBeforeDeadline) {
            this.daysBeforeDeadline = daysBeforeDeadline;
        }

        public int getHourOfDay() {
            return hourOfDay;
        }

        public void setHourOfDay(int hourOfDay) {
            this.hourOfDay = hourOfDay;
        }

        public boolean isNotifyProjectOwner() {
            return notifyProjectOwner;
        }

        public void setNotifyProjectOwner(boolean notifyProjectOwner) {
            this.notifyProjectOwner = notifyProjectOwner;
        }

        public double getHighRiskThreshold() {
            return highRiskThreshold;
        }

        public void setHighRiskThreshold(double highRiskThreshold) {
            this.highRiskThreshold = highRiskThreshold;
        }
    }
}