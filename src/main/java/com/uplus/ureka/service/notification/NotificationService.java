package com.uplus.ureka.service.notification;

import com.uplus.ureka.domain.notification.Notification;
import com.uplus.ureka.dto.notification.NotificationRequestDTO;
import com.uplus.ureka.dto.notification.NotificationResponseDTO;
import com.uplus.ureka.exception.*;
import com.uplus.ureka.repository.notification.NotificationMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {
    private final NotificationMapper notificationMapper;

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);


    // ✅ 게시글 존재 여부 체크
    public boolean checkPostExists(Long postId) {
        return notificationMapper.checkPostExists(postId);
    }

    // ✅ 댓글 존재 여부 확인
    public boolean checkCommentExists(Long postId) {
        return notificationMapper.checkCommentExists(postId);
    }


    public NotificationResponseDTO createNotification(NotificationRequestDTO notificationRequestDTO) {
        Long postId = notificationRequestDTO.getPostId();
        if (!checkPostExists(postId)) {
            throw new ResourceExceptions("해당 게시글이 존재하지 않습니다.");
        }

        if ("댓글".equals(notificationRequestDTO.getType()) && !checkCommentExists(postId)) {
            throw new ResourceExceptions("해당 댓글이 존재하지 않습니다.");
        }

        Notification notification = new Notification();
        notification.setUserId(notificationRequestDTO.getUserId());
        notification.setPostId(notificationRequestDTO.getPostId());
        notification.setPostUserId(notificationRequestDTO.getPostUserId());
        notification.setParticipantId(notificationRequestDTO.getParticipantId());
        notification.setType(notificationRequestDTO.getType());

        switch (notificationRequestDTO.getType()) {
            case 신청:
                notificationMapper.createNotificationForRequest(notification);
                break;
            case 승인:
                notificationMapper.createNotificationForApproval(notification);
                break;
            case 거절:
                notificationMapper.createNotificationForRejection(notification);
                break;
            case 댓글:
                notificationMapper.createNotificationForComment(notification);
                break;
            default:
                throw new CustomExceptions("잘못된 알림 타입입니다.");
        }
        //  저장된 `notification`을 기반으로 `NotificationResponseDTO` 생성하여 반환
        return notificationMapper.getsimpleNotifications(notification.getId());
    }

    // ✅ 사용자 ID로 알림 목록 조회
    public List<NotificationResponseDTO> getNotificationsByUserId(Long userId) {
        return notificationMapper.getNotifications(userId);
    }

    // ✅ 읽은 알림 삭제
    public void deleteNotifications(Long userId) {
        notificationMapper.deleteNotifications(userId);
    }

    // ✅ 알림 상태 업데이트
    public void markNotificationAsRead(Long notificationId, Long userId) {
        notificationMapper.updateNotification(notificationId, userId);
    }

    @Scheduled(cron = "0 0 0 * * ?") // TEST
    // ✅ 일정 시간이 지난 알림 삭제
    public void deleteOldNotifications() {
        // 기한이 지난 스케줄 조회
        List<Long> pastNotifications = notificationMapper.findPastNotifications();

        // 기한이 지난 스케줄이 없을 경우 예외 처리
        if (pastNotifications == null || pastNotifications.isEmpty()) {
            logger.warn("기한이 지난 참여 신청 내역이 없습니다.");
            throw new ResourceExceptions("기한이 지난 참여 신청 내역이 없습니다.");
            // return   // 기한이 지난 스케줄이 없으면 작업 종료
        }

        // 기한이 지난 신청 내역 삭제
        try {
            notificationMapper.deleteOldNotifications(pastNotifications);
            logger.info("기한이 지난 알림이 삭제되었습니다.");
        } catch (Exception e) {
            throw new CustomExceptions("알림 삭제 중 오류 발생");
        }
    }
}