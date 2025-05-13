package com.uplus.ureka.dto.post;

import lombok.Getter;
import lombok.Setter;
import java.util.*;
import java.time.LocalDateTime;

@Getter
@Setter
public class PostResponseDTO {
    private Long id;
    private String userName;
    private String title;
    private String content;
    private Long maxParticipants;
    private Long currentParticipants;
    private String categoryName;
    private String status;
    private LocalDateTime scheduleStart;
    private LocalDateTime scheduleEnd;
    private String location;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> postImage;
    private String postImageJson;

    public PostResponseDTO(Long id, String userName, String title, String content,
                           Long maxParticipants, Long currentParticipants, String categoryName,
                           String status, LocalDateTime scheduleStart, LocalDateTime scheduleEnd,
                           String location, LocalDateTime createdAt, LocalDateTime updatedAt, List<String> postImage) {
        this.id = id;
        this.userName = userName;
        this.title = title;
        this.content = content;
        this.maxParticipants = maxParticipants;
        this.currentParticipants = currentParticipants;
        this.categoryName = categoryName;
        this.status = status;
        this.scheduleStart = scheduleStart;
        this.scheduleEnd = scheduleEnd;
        this.location = location;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.postImage = postImage;
    }
}
