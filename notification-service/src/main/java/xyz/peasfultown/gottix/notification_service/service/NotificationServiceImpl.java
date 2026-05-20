package xyz.peasfultown.gottix.notification_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.peasfultown.gottix.notification_service.dto.TicketChangeNotificationEvent;
import xyz.peasfultown.gottix.notification_service.entity.NotificationEntity;
import xyz.peasfultown.gottix.notification_service.mapper.NotificationMapper;
import xyz.peasfultown.gottix.notification_service.model.*;
import xyz.peasfultown.gottix.notification_service.repository.NotificationRepository;

import java.util.UUID;

import static xyz.peasfultown.gottix.notification_service.repository.specification.NotificationSpecification.hasIsRead;
import static xyz.peasfultown.gottix.notification_service.repository.specification.NotificationSpecification.hasType;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notifRepo;
    private final NotificationMapper notifMapper;

    @Override
    public PagedNotificationResponse getUserNotifications(
            String xUserId,
            Boolean isRead,
            NotificationType type,
            SortOrder sortOrder,
            Integer pageNumber,
            Integer pageSize) {
        Pageable pageable = PageRequest.of(
                pageNumber,
                pageSize,
                sortOrder == SortOrder.ASC
                        ? Sort.by("createdAt").ascending()
                        : Sort.by("createdAt").descending()
        );

        Page<NotificationEntity> page = notifRepo.findAll(
                hasIsRead(isRead)
                        .and(hasType(type == null ? null : NotificationEntity.NotificationType.valueOf(type.getValue()))),
                pageable);

        return PagedNotificationResponse.builder()
                .content(page.getContent()
                        .stream()
                        .map(notifMapper::toModel)
                        .toList())
                .page(ResponsePage.builder()
                        .number(page.getNumber())
                        .size(page.getSize())
                        .totalElements(page.getTotalElements())
                        .totalPages(page.getTotalPages())
                        .build())
                .build();
    }

    @Override
    public void markAllAsRead(
            String xUserId,
            BatchNotificationIdRequest batchId) {
        notifRepo.markAllAsRead(batchId
                .getContent()
                .stream()
                .map(UUID::fromString)
                .toList());
    }

    @Override
    public void markAsRead(
            String notificationId) {
        notifRepo.markAsRead(UUID.fromString(notificationId));
    }

    @Override
    public void delete(
            String notificationId) {
        notifRepo.deleteById(UUID.fromString(notificationId));
    }

    @Override
    public void createNotification(
            TicketChangeNotificationEvent event) {
        NotificationEntity ne = NotificationEntity.builder()
                .ticketId(UUID.fromString(event.getTicketId()))
                .userId(UUID.fromString(event.getReceiverId()))
                .message(event.getMessage())
                .type(NotificationEntity.NotificationType.valueOf(event.getType().toString()))
                .build();
        ne = notifRepo.save(ne);
        log.debug("saved notification {}", ne);
    }
}
