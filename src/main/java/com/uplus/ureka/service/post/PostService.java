package com.uplus.ureka.service.post;


import java.util.*;
import com.uplus.ureka.dto.post.PostRequestDTO;
import com.uplus.ureka.dto.post.PostResponseDTO;
import com.uplus.ureka.dto.PageResponseDTO;
import com.uplus.ureka.exception.*;
import com.uplus.ureka.repository.post.PostMapper;
import com.uplus.ureka.service.s3.S3Service; // ✅ S3Service import

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.apache.ibatis.session.RowBounds;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {
    private final PostMapper postMapper;
    private final S3Service s3Service;
    private final ObjectMapper objectMapper = new ObjectMapper();


    // 모집 글 작성
    public PostResponseDTO insertPost(PostRequestDTO requestDTO, List<MultipartFile> multipartFiles) {
        Long userId = requestDTO.getUserId();

        if (!postMapper.checkUserExists(userId)) {
            throw new ForbiddenException("존재하지 않는 회원입니다.");
        }
        if (multipartFiles != null && !multipartFiles.isEmpty()) {
            String imageJson = s3Service.uploadFile(multipartFiles, "posts");
            requestDTO.setPostImage(imageJson);
        }

        postMapper.insertPost(requestDTO);

        return findPostById(requestDTO.getPostId());
    }

    //모집 글 삭제
    public void removePost(Long postId, Long userId) {
        //작성한 유저가 맞는 지 확인
        //모집 글 존재 확인
        if (!postMapper.checkExistPost(postId)) {
            throw new ResourceExceptions("해당 모집 글이 존재하지 않습니다.");
        }

        postMapper.removePost(postId);
    }

    // 모집 글 수정
    public PostResponseDTO updatePost(PostRequestDTO requestDTO,
                                      List<MultipartFile> multipartFiles) {
        Long postId = requestDTO.getPostId();
        if (!postMapper.checkExistPost(postId)) {
            throw new ResourceExceptions("해당 모집 글이 존재하지 않습니다.");
        }

        // 1) DB에서 기존 이미지 키 목록 꺼내오기
        String existingJson = postMapper.findPostById(postId).getPostImageJson();
        List<String> existingKeys = Collections.emptyList();
        if (existingJson != null && !existingJson.isBlank()) {
            try {
                existingKeys = objectMapper.readValue(
                        existingJson, new TypeReference<List<String>>() {}
                );
            } catch (Exception ignored) {}
        }

        // 2) 요청 DTO 에서 유지할 키 목록 파싱
        String requestedJson = requestDTO.getPostImage();  // 클라이언트가 보낸 JSON
        List<String> requestedKeys = Collections.emptyList();
        if (requestedJson != null && !requestedJson.isBlank()) {
            try {
                requestedKeys = objectMapper.readValue(
                        requestedJson, new TypeReference<List<String>>() {}
                );
            } catch (Exception ignored) {}
        }

        // 3) existingKeys 중 requestedKeys 에 없는 것들만 S3에서 삭제
        for (String key : existingKeys) {
            if (!requestedKeys.contains(key)) {
                s3Service.deleteFile(key);
            }
        }

        // 4) multipartFiles 가 있으면 새 업로드하고, requestDTO.postImage 에 JSON 덮어쓰기
        if (multipartFiles != null && !multipartFiles.isEmpty()) {
            String newJson = s3Service.uploadFile(multipartFiles, "posts");
            requestDTO.setPostImage(newJson);
        }
        //    없으면 클라이언트가 보낸 requestDTO.postImage 그대로 사용

        // 5) DB 업데이트
        postMapper.updatePost(requestDTO);

        // 6) 변경 후 다시 조회해 반환
        return findPostById(postId);
    }



    //모집 글 조회
    public PageResponseDTO<PostResponseDTO> findPostsWithFilters(
            Long categoryId, String status, String location,
            LocalDateTime scheduleStartAfter, LocalDateTime scheduleEndBefore,
            String sortField, String sortOrder, Pageable pageable) {

        int offset = pageable.getPageNumber() * pageable.getPageSize();
        int limit = pageable.getPageSize();
        RowBounds rowBounds = new RowBounds(offset, limit);

        List<PostResponseDTO> posts = postMapper.findPostsWithFilters(
                categoryId, status, location, scheduleStartAfter, scheduleEndBefore,
                sortField, sortOrder, rowBounds
        );

        long totalElements = postMapper.countPostsWithFilters(
                categoryId, status, location, scheduleStartAfter, scheduleEndBefore
        );
        Page<PostResponseDTO> page = new PageImpl<>(posts, pageable, totalElements);
        return new PageResponseDTO<>(page);
    }

    //모집 글 상세 조회
    public PostResponseDTO findPostById(Long postId) {
        if (!postMapper.checkExistPost(postId)) {
            throw new ResourceExceptions("해당 모집 글이 존재하지 않습니다.");
        }

        PostResponseDTO dto = postMapper.findPostById(postId);

        // JSON 파싱 수행
        try {
            List<String> images = objectMapper.readValue(dto.getPostImageJson(), new TypeReference<List<String>>() {});
            dto.setPostImage(images);
        } catch (Exception e) {
            dto.setPostImage(Collections.emptyList()); // 실패하면 빈 리스트
        }

        return dto;
    }

    public List<PostResponseDTO> findPostsByUserId(Long userId) {
        return postMapper.findPostsByUserId(userId);
    }

}