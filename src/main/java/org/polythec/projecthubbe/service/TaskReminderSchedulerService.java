package org.polythec.projecthubbe.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.polythec.projecthubbe.entity.Task;
import org.polythec.projecthubbe.entity.User;
import org.polythec.projecthubbe.repository.TaskRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service responsible for scheduling and sending automated task reminders
 * based on AI-driven analysis of task progress and deadlines.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TaskReminderSchedulerService {

    private final TaskRepository taskRepository;
    private final NotificationService notificationService;

    /**
     * Scheduled method that runs daily to check for tasks at risk of delay
     * and send appropriate notifications to team members.
     */
    @Scheduled(cron = "0 0 9 * * ?") // Runs at 9:00 AM every day
    @Transactional
    public void checkTasksAndSendReminders() {
        log.info("Starting scheduled task reminder check");

        LocalDateTime today = LocalDateTime.now();
        LocalDateTime nearFuture = today.plusDays(7); // Look at tasks due within 7 days

        // Find all active tasks with deadlines approaching
        List<Task> approachingDeadlineTasks = taskRepository.findByEndDateBetweenAndStatusIdNot(
                today, nearFuture, 3L); // Status ID 3 is completed

        for (Task task : approachingDeadlineTasks) {
            analyzeTaskAndNotify(task);
        }

        // Find overdue tasks
        List<Task> overdueTasks = taskRepository.findByEndDateBeforeAndStatusIdNot(
                today, 3L); // Tasks with end date before today and not completed

        for (Task task : overdueTasks) {
            notifyOverdueTask(task);
        }

        log.info("Completed scheduled task reminder check");
    }

    /**
     * Analyzes a task to determine if it's at risk of being delayed based on current progress
     * and sends appropriate notifications.
     *
     * @param task The task to analyze
     */
    private void analyzeTaskAndNotify(Task task) {
        // Calculate days until deadline
        long daysUntilDeadline = ChronoUnit.DAYS.between(LocalDateTime.now(), task.getEndDate());

        // Determine risk level based on our AI heuristic
        TaskRiskLevel riskLevel = determineRiskLevel(task, daysUntilDeadline);

        if (riskLevel != TaskRiskLevel.LOW) {
            sendTaskRiskNotifications(task, riskLevel, daysUntilDeadline);
        }
    }

    /**
     * Determines the risk level of a task based on deadline proximity and task parameters.
     *
     * @param task The task to evaluate
     * @param daysUntilDeadline Number of days until the deadline
     * @return The calculated risk level
     */
    private TaskRiskLevel determineRiskLevel(Task task, long daysUntilDeadline) {
        // Basic risk assessment logic - can be enhanced with more sophisticated AI

        // High priority tasks with close deadlines are high risk
        if (task.getPriority() != null && task.getPriority().getId() == 1 && daysUntilDeadline <= 2) {
            return TaskRiskLevel.HIGH;
        }

        // Tasks with close deadlines
        if (daysUntilDeadline <= 1) {
            return TaskRiskLevel.HIGH;
        } else if (daysUntilDeadline <= 3) {
            return TaskRiskLevel.MEDIUM;
        }

        // Check if there are any assignees - unassigned tasks approaching deadline are medium risk
        if (task.getAssignees() == null || task.getAssignees().isEmpty()) {
            return TaskRiskLevel.MEDIUM;
        }

        return TaskRiskLevel.LOW;
    }

    /**
     * Sends notifications to all relevant team members based on the task's risk level.
     *
     * @param task The task at risk
     * @param riskLevel The calculated risk level
     * @param daysUntilDeadline Number of days until the deadline
     */
    private void sendTaskRiskNotifications(Task task, TaskRiskLevel riskLevel, long daysUntilDeadline) {
        // Base message depending on risk level
        String baseMessage = getBaseMessageForRiskLevel(task, riskLevel, daysUntilDeadline);

        // Send to all assignees
        for (User assignee : task.getAssignees()) {
            notificationService.sendNotification(assignee, baseMessage);
        }

        // If high risk, also notify project admins/owner
        if (riskLevel == TaskRiskLevel.HIGH) {
            // Get project owner and admins
            User projectOwner = task.getProject().getOwner();
            notificationService.sendNotification(
                    projectOwner,
                    "URGENT: Task \"" + task.getTitle() + "\" is at high risk of delay! Due in " + daysUntilDeadline + " days."
            );
        }
    }

    /**
     * Generates an appropriate message based on the task's risk level.
     *
     * @param task The task at risk
     * @param riskLevel The calculated risk level
     * @param daysUntilDeadline Number of days until the deadline
     * @return A formatted notification message
     */
    private String getBaseMessageForRiskLevel(Task task, TaskRiskLevel riskLevel, long daysUntilDeadline) {
        switch (riskLevel) {
            case HIGH:
                return "URGENT: Task \"" + task.getTitle() + "\" deadline is approaching fast! Due in " +
                        daysUntilDeadline + " days. Please prioritize this task.";
            case MEDIUM:
                return "Reminder: Task \"" + task.getTitle() + "\" is due in " + daysUntilDeadline +
                        " days. Make sure you're on track to complete it.";
            default:
                return "FYI: Task \"" + task.getTitle() + "\" is due in " + daysUntilDeadline + " days.";
        }
    }

    /**
     * Sends notifications for overdue tasks.
     *
     * @param task The overdue task
     */
    private void notifyOverdueTask(Task task) {
        String message = "OVERDUE: Task \"" + task.getTitle() + "\" has passed its deadline! Please update the status or request an extension.";

        // Notify all assignees
        for (User assignee : task.getAssignees()) {
            notificationService.sendNotification(assignee, message);
        }

        // Also notify project owner of overdue tasks
        User projectOwner = task.getProject().getOwner();
        notificationService.sendNotification(
                projectOwner,
                "OVERDUE TASK ALERT: Task \"" + task.getTitle() + "\" assigned to " +
                        task.getAssignees().stream().map(User::getUsername).collect(Collectors.joining(", ")) +
                        " has passed its deadline."
        );
    }

    /**
     * Enum representing the risk level of a task.
     */
    private enum TaskRiskLevel {
        LOW, MEDIUM, HIGH
    }
}