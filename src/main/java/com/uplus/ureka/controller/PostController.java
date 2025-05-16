package com.uplus.ureka.controller;

import com.uplus.ureka.dto.CustomResponseDTO;
import com.uplus.ureka.dto.PageResponseDTO;
import com.uplus.ureka.dto.post.PostRequestDTO;
import com.uplus.ureka.dto.post.PostResponseDTO;
import com.uplus.ureka.service.post.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@RestController
@RequestMapping("/gooham/posts")
@RequiredArgsConstructor

public class PostController {

    private final PostService postService;

    // 모집 글 조회
    @GetMapping
    public ResponseEntity<CustomResponseDTO<PageResponseDTO<PostResponseDTO>>> findPostsWithFilters(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) LocalDateTime scheduleStartAfter,
            @RequestParam(required = false) LocalDateTime scheduleEndBefore,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder,
            Pageable pageable) {

        PageResponseDTO<PostResponseDTO> response = postService.findPostsWithFilters(
                userId, categoryId, status, location, scheduleStartAfter, scheduleEndBefore,
                sortField, sortOrder, pageable
        );
        return ResponseEntity.ok(new CustomResponseDTO<>("success", "모집 글 목록 조회 성공", response));
    }

    // 모집 글 상세 조회
    @GetMapping("/{postId}")
    public ResponseEntity<CustomResponseDTO<PostResponseDTO>> findPostById(@PathVariable Long postId) {
        PostResponseDTO response = postService.findPostById(postId);
        return ResponseEntity.ok(new CustomResponseDTO<>("success", "모집 글 상세 조회 성공", response));
    }

    // 모집 글 작성
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostResponseDTO> insertPost(
            @RequestPart("data") PostRequestDTO requestDTO,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        System.out.println("요청 DTO: " + requestDTO);
        System.out.println(">>> multipartFiles = " + files);
        return ResponseEntity.ok(postService.insertPost(requestDTO, files));
    }

    // 모집 글 수정
    @PatchMapping
    public ResponseEntity<CustomResponseDTO<PostResponseDTO>> updatePost(
            @RequestPart("data") PostRequestDTO requestDTO,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        PostResponseDTO response = postService.updatePost(requestDTO, files);

        if (requestDTO == null) {
            throw new IllegalArgumentException("RequestDTO가 null입니다. multipart 요청 형식을 확인해주세요.");
        }

        log.info("수정 요청 Post ID: {}", requestDTO.getPostId());
        if (files != null) log.info("업로드 파일 수: {}", files.size());
        return ResponseEntity.status(HttpStatus.OK)
                .body(new CustomResponseDTO<>("success", "모집 글 수정 성공", response));
    }

    // 모집 글 삭제
    @DeleteMapping("/{postId}/{userId}")
    public ResponseEntity<CustomResponseDTO<String>> removePost(
            @PathVariable Long postId,
            @PathVariable Long userId) {
        postService.removePost(postId, userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(new CustomResponseDTO<>("success", "모집 글 삭제 성공", "삭제 완료"));
    }
}