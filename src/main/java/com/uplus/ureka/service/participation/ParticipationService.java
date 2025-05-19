package com.uplus.ureka.service.participation;

import com.uplus.ureka.dto.PageResponseDTO;
import com.uplus.ureka.dto.notification.NotificationType;
import com.uplus.ureka.dto.notification.NotificationRequestDTO;
import com.uplus.ureka.service.notification.NotificationService;
import com.uplus.ureka.exception.*;
import com.uplus.ureka.dto.participation.ParticipationRequestDTO;
import com.uplus.ureka.dto.participation.ParticipationResponseDTO;
import com.uplus.ureka.repository.participation.ParticipationMapper;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ParticipationService {
    private final ParticipationMapper participationMapper;
    private final NotificationService notificationService;

    private static final Logger logger = LoggerFactory.getLogger(ParticipationService.class);

    // 참여 신청
    public ParticipationResponseDTO applyParticipation(ParticipationRequestDTO requestDTO) {
        Long userId = requestDTO.getUserId();
        Long postId = requestDTO.getPostId();
        // 사용자 확인
        if(!participationMapper.checkUserExists(userId)){
            throw new ResourceExceptions("존재하지 않는 회원입니다.");
        }
        // 게시물 확인
        if (!participationMapper.checkExistPost(postId)) {
            throw new ResourceExceptions("해당 게시글이 존재하지 않습니다.");
        }
        // 중복 신청 확인
        if (participationMapper.checkExistParticipation(userId, postId)) {
            throw new CustomExceptions("이미 신청한 게시글입니다.");
        }
        // 모집 가능 확인
        if (!participationMapper.checkPostStatusValid(postId) || !participationMapper.checkPersonAvailable(postId)) {
            throw new CustomExceptions("현재 모집이 불가능한 상태입니다.");
        }

        // DTO를 전달하여 applyParticipation 호출
        participationMapper.applyParticipation(requestDTO);

        // 추가로 게시물 주인한테 알림 필요함.
        NotificationRequestDTO RequestDTO = new NotificationRequestDTO(
                null,
                participationMapper.getPostOwnerId(postId),
                postId,
                participationMapper.getPostOwnerId(postId),
                userId,
                NotificationType.신청
        );

        notificationService.createNotification(RequestDTO);
        // 생성된 participantId를 확인
        ParticipationResponseDTO responseDTO = participationMapper.findUserParticipationStatus(userId, postId);

        return responseDTO;
    }

    public void cancelParticipation(ParticipationRequestDTO requestDTO) {
        Long userId = requestDTO.getUserId();
        Long postId = requestDTO.getPostId();
        // 신청 기록 확인
        if (!participationMapper.checkExistParticipation(userId, postId)) {
            throw new ResourceExceptions("신청 기록이 없습니다.");
        }
        // 사용자 확인
        if(!participationMapper.checkUserExists(userId)){
            throw new ForbiddenException("존재하지 않는 회원입니다.");
        }
        // 게시물 확인
        if (!participationMapper.checkExistPost(postId)) {
            throw new ResourceExceptions("해당 게시글이 존재하지 않습니다.");
        }
        participationMapper.cancelParticipation(requestDTO.getUserId(), requestDTO.getPostId());
    }

    public ParticipationResponseDTO approveParticipation(ParticipationRequestDTO requestDTO) {
        Long userId = requestDTO.getUserId();
        Long postId = requestDTO.getPostId();

        // 사용자 확인
        if(!participationMapper.checkUserExists(userId)){
            throw new ResourceExceptions("존재하지 않는 회원입니다.");
        }
        // 게시물 확인
        if (!participationMapper.checkExistPost(postId)) {
            throw new ResourceExceptions("해당 게시글이 존재하지 않습니다.");
        }

        // 신청 기록 확인
        if (!participationMapper.checkExistParticipation(userId, postId)) {
            throw new ResourceExceptions("신청한 기록이 없습니다.");
        }
        // 최대 정원 확인
        if (!participationMapper.checkPersonAvailable(postId)) {
            throw new CustomExceptions("최대 인원을 초과하여 승인할 수 없습니다.");
        }
        ParticipationResponseDTO responseDTO = participationMapper.findUserParticipationStatus(userId, postId);

        participationMapper.approveParticipation(userId, postId);
        //알림 추가 필요
        NotificationRequestDTO RequestDTO = new NotificationRequestDTO(
                null,
                userId,
                postId,
                participationMapper.getPostOwnerId(postId),
                userId,
                NotificationType.승인
        );

        notificationService.createNotification(RequestDTO);

        participationMapper.increaseCurrentPerson(postId);
        return responseDTO;
    }

    public void rejectParticipation(ParticipationRequestDTO requestDTO) {
        Long userId = requestDTO.getUserId();
        Long postId = requestDTO.getPostId();
        // 사용자 확인
        if(!participationMapper.checkUserExists(userId)){
            throw new ResourceExceptions("존재하지 않는 회원입니다.");
        }
        // 게시물 확인
        if (!participationMapper.checkExistPost(postId)) {
            throw new ResourceExceptions("해당 게시글이 존재하지 않습니다.");
        }
        // 신청 기록 확인
        if (!participationMapper.checkExistParticipation(userId, postId)) {
            throw new ResourceExceptions("신청한 기록이 없습니다.");
        }
        participationMapper.rejectParticipation(requestDTO.getUserId(), requestDTO.getPostId());
        //알림 추가 필요
        NotificationRequestDTO RequestDTO = new NotificationRequestDTO(
                null,
                userId,
                postId,
                participationMapper.getPostOwnerId(postId),
                userId,
                NotificationType.거절
        );

        notificationService.createNotification(RequestDTO);

        participationMapper.deleteFromParticipationQueue(requestDTO.getUserId(), requestDTO.getPostId());
    }

    public PageResponseDTO<ParticipationResponseDTO> findAllParticipantsByPostID(Long postId, Pageable pageable){
        int offset = pageable.getPageNumber() * pageable.getPageSize();
        int limit = pageable.getPageSize();

        //MyBatis에서 페이징 적용
        List<ParticipationResponseDTO> participants =
                participationMapper.findAllParticipantsByPostId(postId, new RowBounds(offset, limit));
        long totalElements = participationMapper.countParticipantsByPostId(postId); // 전체 개수 조회

        // Page 객체로 변환 후, PageResponseDTO로 감싸서 반환
        Page<ParticipationResponseDTO> page = new PageImpl<>(participants, pageable, totalElements);
        return new PageResponseDTO<>(page);
    }

    public PageResponseDTO<ParticipationResponseDTO> findAcceptedParticipantsByPostID(Long postId, Pageable pageable){
        int offset = pageable.getPageNumber() * pageable.getPageSize();
        int limit = pageable.getPageSize();

        //MyBatis에서 페이징 적용
        List<ParticipationResponseDTO> participants =
                participationMapper.findAcceptedParticipantsByPostId(postId, new RowBounds(offset, limit));
        long totalElements = participationMapper.countParticipantsByPostId(postId); // 전체 개수 조회

        // Page 객체로 변환 후, PageResponseDTO로 감싸서 반환
        Page<ParticipationResponseDTO> page = new PageImpl<>(participants, pageable, totalElements);
        return new PageResponseDTO<>(page);
    }

    public ParticipationResponseDTO findUserParticipationStatus(ParticipationRequestDTO requestDTO) {
        Long userId = requestDTO.getUserId();
        Long postId = requestDTO.getPostId();
        // 사용자 확인
        if(!participationMapper.checkUserExists(userId)){
            throw new ResourceExceptions("존재하지 않는 회원입니다.");
        }
        // 게시물 확인
        if (!participationMapper.checkExistPost(postId)) {
            throw new ResourceExceptions("해당 게시글이 존재하지 않습니다.");
        }
        return participationMapper.findUserParticipationStatus(requestDTO.getUserId(), requestDTO.getPostId());
    }

    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정에 실행
    public void deleteEndedSchedules() {
        List<Long> pastSchedules = participationMapper.findPastSchedules();

        // 기한이 지난 스케줄이 없을 경우 예외 처리
        if (pastSchedules == null || pastSchedules.isEmpty()) {
//            logger.warn("기한이 지난 참여 신청 내역이 없습니다.");
            throw new ResourceExceptions("기한이 지난 참여 신청 내역이 없습니다.");
        }

        // 기한이 지난 신청 내역 삭제
        participationMapper.deleteParticipantsList();
        logger.info("기한이 지난 참여 신청 내역이 삭제되었습니다.");
    }
}
