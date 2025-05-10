package org.polythec.projecthubbe.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.polythec.projecthubbe.entity.Comment;
import org.polythec.projecthubbe.entity.Task;
import org.polythec.projecthubbe.entity.User;
import org.polythec.projecthubbe.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for analyzing tasks and predicting potential delays
 * using AI-inspired heuristics based on historical data and task patterns.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TaskAnalyticsService {

    private final TaskRepository taskRepository;
    private final NotificationService notificationService;

    /**
     * Analyzes a specific task to predict if it's at risk of being delayed.
     * Uses various factors like task size, past user performance, and complexity.
     *
     * @param task The task to analyze
     * @return A risk score from 0.0 (low risk) to 1.0 (high risk)
     */
    public double calculateDelayRiskScore(Task task) {
        if (task.getEndDate() == null || task.getStartDate() == null) {
            return 0.5; // Moderate risk if dates aren't set
        }

        double riskScore = 0.0;

        // Factor 1: Days until deadline (closer deadline = higher risk)
        long daysUntilDeadline = ChronoUnit.DAYS.between(LocalDate.now(), task.getEndDate());
        if (daysUntilDeadline <= 0) {
            riskScore += 0.4; // Already overdue
        } else if (daysUntilDeadline <= 2) {
            riskScore += 0.3; // Very close to deadline
        } else if (daysUntilDeadline <= 5) {
            riskScore += 0.2; // Approaching deadline
        }

        // Factor 2: Task priority (higher priority = higher risk attention needed)
        if (task.getPriority() != null) {
            Long priorityId = task.getPriority().getId();
            if (priorityId == 1) { // High priority
                riskScore += 0.2;
            } else if (priorityId == 2) { // Medium priority
                riskScore += 0.1;
            }
        }

        // Factor 3: Task complexity (estimated by description length and type)
        if (task.getDescription() != null && task.getDescription().length() > 500) {
            riskScore += 0.1; // Complex task based on description length
        }

        // Factor 4: Number of assignees (too many or too few is risky)
        int assigneeCount = task.getAssignees().size();
        if (assigneeCount == 0) {
            riskScore += 0.15; // No assignees is risky
        } else if (assigneeCount > 3) {
            riskScore += 0.05; // Too many assignees can cause coordination issues
        }

        // Factor 5: Recent activity on the task (lack of activity increases risk)
        double activityScore = calculateRecentActivityScore(task);
        riskScore += (0.15 * (1 - activityScore)); // Lower activity = higher risk

        // Cap the risk score at 1.0
        return Math.min(1.0, riskScore);
    }

    /**
     * Calculate a score based on recent activity on the task.
     * More recent activity = lower risk of delay.
     *
     * @param task The task to analyze
     * @return Activity score from 0.0 (no activity) to 1.0 (very active)
     */
    private double calculateRecentActivityScore(Task task) {
        // Check for recent comments as a proxy for activity
        double activityScore = 0.0;

        if (task.getComments() != null && !task.getComments().isEmpty()) {
            // Get the most recent comment
            Optional<Comment> mostRecentComment = task.getComments().stream()
                    .max(Comparator.comparing(Comment::getCreatedAt));

            if (mostRecentComment.isPresent()) {
                long daysSinceLastComment = ChronoUnit.DAYS.between(
                        mostRecentComment.get().getCreatedAt().toLocalDate(),
                        LocalDate.now());

                if (daysSinceLastComment <= 1) {
                    activityScore = 1.0; // Very recent activity
                } else if (daysSinceLastComment <= 3) {
                    activityScore = 0.7; // Recent activity
                } else if (daysSinceLastComment <= 7) {
                    activityScore = 0.4; // Some activity
                } else {
                    activityScore = 0.1; // Old activity
                }
            }
        }

        // Also consider the last update time of the task itself
        if (task.getUpdatedAt() != null) {
            long daysSinceUpdate = ChronoUnit.DAYS.between(
                    task.getUpdatedAt().toLocalDate(),
                    LocalDate.now());

            double updateScore;
            if (daysSinceUpdate <= 1) {
                updateScore = 1.0;
            } else if (daysSinceUpdate <= 3) {
                updateScore = 0.7;
            } else if (daysSinceUpdate <= 7) {
                updateScore = 0.4;
            } else {
                updateScore = 0.1;
            }

            // Combine comment activity with update activity
            activityScore = Math.max(activityScore, updateScore);
        }

        return activityScore;
    }

    /**
     * Analyze a user's task completion history to predict their performance
     * on future similar tasks.
     *
     * @param user The user to analyze
     * @param taskType The type of task to predict performance for
     * @return Estimated completion efficiency (1.0 = on time, >1.0 = faster, <1.0 = slower)
     */
    public double predictUserTaskPerformance(User user, String taskType) {
        // For a real AI system, this would use machine learning on historical data
        // This is a simplified heuristic approach

        // Get all completed tasks by this user
        List<Task> completedTasks = taskRepository.findByAssigneesIdAndStatusIdNot(user.getId(), 3L)
                .stream()
                .filter(task -> task.getType() != null && task.getType().equals(taskType))
                .collect(Collectors.toList());

        if (completedTasks.isEmpty()) {
            return 1.0; // No history, assume average performance
        }

        // Calculate average performance based on planned vs. actual duration
        double totalEfficiencyRatio = 0.0;
        int countValidTasks = 0;

        for (Task task : completedTasks) {
            // Only consider tasks with valid start/end dates and update times
            if (task.getStartDate() != null && task.getEndDate() != null && task.getUpdatedAt() != null) {
                // Planned duration in days
                long plannedDuration = ChronoUnit.DAYS.between(task.getStartDate(), task.getEndDate());

                // Actual duration (from start to last update, assuming that's completion)
                long actualDuration = ChronoUnit.DAYS.between(
                        task.getStartDate(),
                        task.getUpdatedAt().toLocalDate());

                // Avoid division by zero
                if (plannedDuration > 0 && actualDuration > 0) {
                    // Efficiency ratio: planned/actual (>1 means faster than planned)
                    double efficiencyRatio = (double) plannedDuration / actualDuration;
                    totalEfficiencyRatio += efficiencyRatio;
                    countValidTasks++;
                }
            }
        }

        // Calculate average efficiency
        return countValidTasks > 0 ? totalEfficiencyRatio / countValidTasks : 1.0;
    }

    /**
     * Analyze all ongoing tasks for a project and send smart reminders
     * based on predicted risk of delay.
     *
     * @param projectId The project to analyze
     */
    public void analyzeProjectTasksAndSendReminders(Long projectId) {
        List<Task> activeTasks = taskRepository.findByProjectIdprojetAndStatusIdNot(projectId, 3L);

        for (Task task : activeTasks) {
            double riskScore = calculateDelayRiskScore(task);

            // Send reminders based on risk score
            if (riskScore >= 0.7) {
                // High risk
                sendHighRiskReminders(task, riskScore);
            } else if (riskScore >= 0.4) {
                // Medium risk
                sendMediumRiskReminders(task, riskScore);
            }
        }
    }

    private void sendHighRiskReminders(Task task, double riskScore) {
        // Format a percentage
        int riskPercentage = (int) (riskScore * 100);

        // For assignees
        for (User assignee : task.getAssignees()) {
            String message = "URGENT: Task \"" + task.getTitle() + "\" has a " + riskPercentage +
                    "% risk of delay based on our analysis. Please prioritize this task or request assistance.";
            notificationService.sendNotification(assignee, message);
        }

        // For project owner
        User projectOwner = task.getProject().getOwner();
        String ownerMessage = "HIGH RISK ALERT: Task \"" + task.getTitle() + "\" has a " + riskPercentage +
                "% risk of not meeting its deadline. Consider reallocating resources or adjusting the timeline.";
        notificationService.sendNotification(projectOwner, ownerMessage);
    }

    private void sendMediumRiskReminders(Task task, double riskScore) {
        int riskPercentage = (int) (riskScore * 100);

        // Just notify assignees for medium risk
        for (User assignee : task.getAssignees()) {
            String message = "ATTENTION: Task \"" + task.getTitle() + "\" has a " + riskPercentage +
                    "% risk of delay based on our analysis. Please review your progress.";
            notificationService.sendNotification(assignee, message);
        }
    }
}