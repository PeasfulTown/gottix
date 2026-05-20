package xyz.peasfultown.gottix.notification_service.service;

import xyz.peasfultown.gottix.notification_service.dto.TicketChangeNotificationEvent;
import xyz.peasfultown.gottix.notification_service.model.BatchNotificationIdRequest;
import xyz.peasfultown.gottix.notification_service.model.NotificationType;
import xyz.peasfultown.gottix.notification_service.model.PagedNotificationResponse;
import xyz.peasfultown.gottix.notification_service.model.SortOrder;

public interface NotificationService {

    PagedNotificationResponse getUserNotifications(String xUserId, Boolean isRead, NotificationType type, SortOrder sortOrder, Integer pageNumber, Integer pageSize);

    void markAllAsRead(String xUserId, BatchNotificationIdRequest batchId);

    void markAsRead(String notificationId);

    void delete(String notificationId);

    void createNotification(
            TicketChangeNotificationEvent event);
}
