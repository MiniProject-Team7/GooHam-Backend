package com.uplus.ureka.controller;

import com.uplus.ureka.dto.CustomResponseDTO;
import com.uplus.ureka.dto.PageResponseDTO;
import com.uplus.ureka.dto.post.CommentsRequestDTO;
import com.uplus.ureka.dto.post.CommentsResponseDTO;
import com.uplus.ureka.service.post.CommentsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/gooham/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentsService commentsService;

    @PostMapping
    public ResponseEntity<CustomResponseDTO<CommentsResponseDTO>>
    createComment(@RequestBody CommentsRequestDTO requestDTO){
        CommentsResponseDTO responseDTO = commentsService.createComment(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CustomResponseDTO<>("success", "댓글 작성이 완료되었습니다.", responseDTO));
    }

    @DeleteMapping("/{postId}/{commentId}/{userId}")
    public ResponseEntity<CustomResponseDTO<String>> deleteComment(@PathVariable Long postId, @PathVariable Long commentId, @PathVariable Long userId)
    {
        commentsService.deleteComment(postId, userId, commentId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(new CustomResponseDTO<>("success", "댓글 삭제 성공", "삭제 완료"));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CustomResponseDTO<CommentsResponseDTO>>
    updateComment(@RequestBody CommentsRequestDTO requestDTO){
        CommentsResponseDTO responseDTO = commentsService.updateComment(requestDTO);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new CustomResponseDTO<>("success", "댓글이 수정되었습니다.", responseDTO));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<CustomResponseDTO<PageResponseDTO<CommentsResponseDTO>>> getComments(
            @PathVariable Long postId, @RequestParam(value = "sort", required = false, defaultValue = "ASC") String sort, Pageable pageable) {
        PageResponseDTO<CommentsResponseDTO> response = commentsService.getCommentsByPostId(postId, sort, pageable);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new CustomResponseDTO<PageResponseDTO<CommentsResponseDTO>>("success", "참여자 목록 조회 성공", response));
    }


    // CommentController.java
    @GetMapping("/users/{userId}")
    public ResponseEntity<CustomResponseDTO<PageResponseDTO<CommentsResponseDTO>>> getUserComments(
            @PathVariable Long userId,
            @RequestParam(value = "sort", required = false, defaultValue = "ASC") String sort,
            Pageable pageable
    ) {
        PageResponseDTO<CommentsResponseDTO> response = commentsService.getCommentsByUserId(userId, sort, pageable);
        return ResponseEntity.ok(new CustomResponseDTO<>("success", "내 댓글 조회 성공", response));
    }


}
