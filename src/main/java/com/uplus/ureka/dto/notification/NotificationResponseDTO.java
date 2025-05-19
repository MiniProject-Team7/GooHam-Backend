package com.uplus.ureka.dto.notification;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponseDTO implements Serializable {
    private Long id;
    private Long userId;
    private Long postId;
    private String postTitle;
    private Long postUserId;
    private Long participantId;
    private String participantName;
    private NotificationType type;
    private boolean isRead;
    private LocalDateTime createdAt;

}
