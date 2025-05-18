package com.uplus.ureka.service.post;

import com.uplus.ureka.dto.PageResponseDTO;
import com.uplus.ureka.exception.CustomExceptions;
import com.uplus.ureka.dto.post.CommentsRequestDTO;
import com.uplus.ureka.dto.post.CommentsResponseDTO;
import com.uplus.ureka.dto.notification.NotificationRequestDTO;
import com.uplus.ureka.dto.notification.NotificationType;
import com.uplus.ureka.repository.post.CommentsMapper;
import com.uplus.ureka.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.RowBounds;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional

public class CommentsService {
    private final CommentsMapper commentsMapper;
    private final NotificationService notificationService;

    //댓글 작성
    public CommentsResponseDTO createComment(CommentsRequestDTO requestDTO){
        Long postId = requestDTO.getPostId();
        Long userId = requestDTO.getUserId();
        String content = requestDTO.getContent();

        commentsMapper.createComment(requestDTO);

        //댓글 추가 알림
        NotificationRequestDTO RequestDTO = new NotificationRequestDTO(
                null,
                commentsMapper.getPostOwnerId(postId),
                postId,
                commentsMapper.getPostOwnerId(postId),
                userId,
                NotificationType.댓글
        );

        notificationService.createNotification(RequestDTO);

        CommentsResponseDTO responseDTO = commentsMapper.findCommentwithCommentId(requestDTO.getCommentId());
        return responseDTO;
    }

    //댓글 수정
    public CommentsResponseDTO updateComment(CommentsRequestDTO requestDTO) {
        Long postId = requestDTO.getPostId();
        Long userId = requestDTO.getUserId();
        Long commentId = requestDTO.getCommentId();
        String content = requestDTO.getContent();


        commentsMapper.updateComment(requestDTO);
        CommentsResponseDTO responseDTO = commentsMapper.findCommentwithCommentId(commentId);

        return responseDTO;
    }

    // 댓글 삭제
    public void deleteComment(Long postId, Long userId, Long commentId){

        commentsMapper.deleteComment(commentId, postId, userId);
    }

    //댓글 목록 조회
    public PageResponseDTO<CommentsResponseDTO> getCommentsByPostId(Long postId, String sort, Pageable pageable) {
        int offset = pageable.getPageNumber()* pageable.getPageSize();
        int limit = pageable.getPageSize();
        if (sort == null || (!sort.equalsIgnoreCase("ASC") && !sort.equalsIgnoreCase("DESC"))) {
            sort = "ASC";
        }
        List<CommentsResponseDTO> comments =
                commentsMapper.findCommentsByPostId(postId, sort, new RowBounds(offset, limit));

        // 2. 총 댓글 개수 조회
        long totalElements = commentsMapper.countCommentsByPostId(postId);

        // 3. 페이징 응답 객체 생성 및 반환
        Page<CommentsResponseDTO> page = new PageImpl<>(comments, pageable, totalElements);
        return new PageResponseDTO<>(page);
    }

    public PageResponseDTO<CommentsResponseDTO> getCommentsByUserId(Long userId, String sort, Pageable pageable) {
        int offset = (int) pageable.getOffset();
        int limit = pageable.getPageSize();

        if (sort == null || (!sort.equalsIgnoreCase("ASC") && !sort.equalsIgnoreCase("DESC"))) {
            sort = "ASC";
        }

        List<CommentsResponseDTO> list = commentsMapper.findCommentsByUserId(userId, sort, new RowBounds(offset, limit));
        long total = commentsMapper.countCommentsByUserId(userId);

        Page<CommentsResponseDTO> page = new PageImpl<>(list, pageable, total);
        return new PageResponseDTO<>(page);
    }
}
