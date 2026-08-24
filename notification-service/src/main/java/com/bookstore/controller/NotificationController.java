package com.bookstore.controller;

import com.bookstore.entity.Notification;
import com.bookstore.repository.NotificationRepository;
import com.bookstore.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final SecurityUtil securityUtil;

    // GET notifications for the logged-in member with optional pagination
    @GetMapping("/me")
    public ResponseEntity<List<Notification>> getMyNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Long memberId = securityUtil.getCurrentMemberId();
        if (memberId == null) {
            return ResponseEntity.status(401).build();
        }

        Pageable pageable = PageRequest.of(page, size);
        List<Notification> notifications = notificationRepository.findByMemberIdOrderByCreatedAtDesc(memberId, pageable);
        return ResponseEntity.ok(notifications);
    }

    // Endpoint for member ID path variable with optional pagination
    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<Notification>> getNotificationsByMember(
            @PathVariable Long memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        List<Notification> notifications = notificationRepository.findByMemberIdOrderByCreatedAtDesc(memberId, pageable);
        return ResponseEntity.ok(notifications);
    }
}