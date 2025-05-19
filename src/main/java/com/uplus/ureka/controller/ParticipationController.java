package com.uplus.ureka.controller;

import com.uplus.ureka.dto.CustomResponseDTO;
import com.uplus.ureka.dto.PageResponseDTO;
import com.uplus.ureka.dto.participation.ParticipationRequestDTO;
import com.uplus.ureka.dto.participation.ParticipationResponseDTO;
import com.uplus.ureka.service.participation.ParticipationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gooham/participants")
@RequiredArgsConstructor
public class ParticipationController {
    private final ParticipationService participationService;

    //  참여 신청 (RequestBody 사용)
    @PostMapping
    public ResponseEntity<CustomResponseDTO<ParticipationResponseDTO>>
    applyParticipation(@RequestBody ParticipationRequestDTO requestDTO) {
        ParticipationResponseDTO responseDTO = participationService.applyParticipation(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CustomResponseDTO<>("success", "참여 신청이 완료되었습니다.", responseDTO));
    }

    //  참여 취소 (DELETE는 RequestBody 지원 X → PathVariable 사용)
    @DeleteMapping("/{postId}/{userId}")
    public ResponseEntity<CustomResponseDTO<String>> cancelParticipation(@PathVariable Long userId, @PathVariable Long postId) {
        ParticipationRequestDTO requestDTO = new ParticipationRequestDTO();
        requestDTO.setUserId(userId);
        requestDTO.setPostId(postId);

        participationService.cancelParticipation(requestDTO);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(new CustomResponseDTO<>("success", "모집 글 삭제 성공", "삭제 완료"));
    }

    //  참여 승인 (RequestBody 사용)
    @PatchMapping("/approve")
    public ResponseEntity<CustomResponseDTO<ParticipationResponseDTO>>
    approveParticipation(@RequestBody ParticipationRequestDTO requestDTO) {
        ParticipationResponseDTO responseDTO =  participationService.approveParticipation(requestDTO);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new CustomResponseDTO<>("success","참여 신청이 승인되었습니다.", responseDTO));
    }

    //  참여 거부 (RequestBody 사용)
    @PatchMapping("/reject")
    public ResponseEntity<String> rejectParticipation(@RequestBody ParticipationRequestDTO requestDTO) {
        participationService.rejectParticipation(requestDTO);
        return ResponseEntity.ok("참여 신청이 거부되었습니다.");
    }

    // 게시물의 전체 참여 신청 조회
    @GetMapping("/{postId}")
    public ResponseEntity<CustomResponseDTO<PageResponseDTO<ParticipationResponseDTO>>> findAllParticipants(
            @PathVariable Long postId, Pageable pageable) {
        PageResponseDTO<ParticipationResponseDTO> response = participationService.findAllParticipantsByPostID(postId, pageable);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new CustomResponseDTO<PageResponseDTO<ParticipationResponseDTO>>("success", "조회되었습니다.", response));
    }

    @GetMapping("/accepted/{postId}")
    public ResponseEntity<CustomResponseDTO<PageResponseDTO<ParticipationResponseDTO>>> findAcceptedParticipants(
            @PathVariable Long postId, Pageable pageable) {
        PageResponseDTO<ParticipationResponseDTO> response = participationService.findAcceptedParticipantsByPostID(postId, pageable);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new CustomResponseDTO<PageResponseDTO<ParticipationResponseDTO>>("success", "조회되었습니다.", response));
    }

    //  특정 사용자의 참여 신청 상태 조회
    @GetMapping("/{postId}/{userId}/status")
    public ResponseEntity<CustomResponseDTO<ParticipationResponseDTO>> findUserParticipationStatus(
            @PathVariable Long userId, @PathVariable Long postId) {
        ParticipationRequestDTO requestDTO = new ParticipationRequestDTO();
        requestDTO.setUserId(userId);
        requestDTO.setPostId(postId);

        ParticipationResponseDTO responseDTO = participationService.findUserParticipationStatus(requestDTO);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new CustomResponseDTO<>("success","조회되었습니다.", responseDTO));
    }
}
