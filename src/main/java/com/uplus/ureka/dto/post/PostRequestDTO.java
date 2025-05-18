package com.uplus.ureka.dto.post;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostRequestDTO {
    private Long postId;
    private Long userId;
    private String title;
    private String content;
    private Long maxParticipants;
    private Long curParticipants;
    private Long categoryId;
    private String status;
    private LocalDateTime scheduleStart;
    private LocalDateTime scheduleEnd;
    private String location;
    private String postImage;
    private LocalDateTime eventStart;
}