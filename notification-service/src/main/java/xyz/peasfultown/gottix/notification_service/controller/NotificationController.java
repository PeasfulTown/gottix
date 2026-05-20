package xyz.peasfultown.gottix.notification_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xyz.peasfultown.gottix.notifcation_service.NotificationApi;
import xyz.peasfultown.gottix.notification_service.model.BatchNotificationIdRequest;
import xyz.peasfultown.gottix.notification_service.model.NotificationType;
import xyz.peasfultown.gottix.notification_service.model.PagedNotificationResponse;
import xyz.peasfultown.gottix.notification_service.model.SortOrder;
import xyz.peasfultown.gottix.notification_service.service.NotificationService;

import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

@RestController
@RequiredArgsConstructor
public class NotificationController implements NotificationApi {
    private final NotificationService notifService;

    @Override
    public ResponseEntity<Void> markAllNotificationsAsRead(
            String xUserId,
            BatchNotificationIdRequest batchId) throws Exception {
        notifService.markAllAsRead(xUserId, batchId);
        return status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    public ResponseEntity<Void> deleteNotification(
            String xUserId,
            String notificationId) throws Exception {
        notifService.delete(notificationId);
        return status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    public ResponseEntity<PagedNotificationResponse> getNotifications(
            String xUserId,
            Boolean isRead,
            NotificationType type,
            @RequestParam(defaultValue = "DESC") SortOrder sortOrder,
            @RequestParam(defaultValue = "0") Integer pageNumber,
            @RequestParam(defaultValue = "10") Integer pageSize) throws Exception {
        return ok(notifService.getUserNotifications(xUserId, isRead, type, sortOrder, pageNumber, pageSize));
    }

    @Override
    public ResponseEntity<Void> markNotificationAsRead(
            String xUserId,
            @PathVariable("notifId") String notifId) throws Exception {
        notifService.markAsRead(notifId);
        return status(HttpStatus.NO_CONTENT).build();
    }

}
