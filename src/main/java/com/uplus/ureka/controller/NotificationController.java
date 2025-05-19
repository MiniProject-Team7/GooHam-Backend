package com.uplus.ureka.controller;

import com.uplus.ureka.domain.notification.Notification;
import com.uplus.ureka.dto.CustomResponseDTO;
import com.uplus.ureka.dto.notification.NotificationRequestDTO;
import com.uplus.ureka.dto.notification.NotificationResponseDTO;
import com.uplus.ureka.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gooham/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<CustomResponseDTO<NotificationResponseDTO>> createNotification(@RequestBody NotificationRequestDTO notificationRequestDTO) {
        NotificationResponseDTO responseDTO = notificationService.createNotification(notificationRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CustomResponseDTO<>("success", responseDTO.getType() + " 알림이 생성되었습니다.", responseDTO));
    }

    // 알림 조회
    @GetMapping("/{userId}")
    public ResponseEntity<CustomResponseDTO<List<NotificationResponseDTO>>> getNotification(@PathVariable Long userId) {
        List<NotificationResponseDTO> notifications = notificationService.getNotificationsByUserId(userId);

        if (notifications != null && !notifications.isEmpty()) {
            return ResponseEntity.ok(new CustomResponseDTO<>("success", "알림 목록을 불러왔습니다.", notifications));
        } else {
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(new CustomResponseDTO<>("fail", "알림이 없습니다.", "NO_CONTENT", "/gooham/notifications/" + userId));
        }
    }


    // 읽은 알림 삭제
    @DeleteMapping("/{userId}")
    public ResponseEntity<CustomResponseDTO<String>> deleteNotification(@PathVariable Long userId) {
        notificationService.deleteNotifications(userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(new CustomResponseDTO<>("success", "모집 글 삭제 성공", "삭제 완료"));
    }


    // 알림 읽음 처리
    @PatchMapping("/{id}")
    public ResponseEntity<CustomResponseDTO<String>> markAsRead(@PathVariable Long id, @RequestParam Long userId) {
        notificationService.markNotificationAsRead(id, userId);
        return ResponseEntity.ok(new CustomResponseDTO<>("success", "알림을 읽음처리했습니다.", "알림 읽음 처리 완료"));
    }
}