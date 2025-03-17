package com.uplus.ureka.repository.participation;

import java.util.*;

import com.uplus.ureka.dto.participation.ParticipationRequestDTO;
import org.apache.ibatis.annotations.*;
import com.uplus.ureka.dto.participation.ParticipationResponseDTO;

@Mapper
public interface ParticipationMapper {

    // ✅ 신청 글 존재 확인
    @Select("SELECT EXISTS (SELECT 1 FROM POST_PARTICIPANTS WHERE USER_ID = #{userId} " +
            "AND POST_ID = #{postId} AND STATUS = '대기')")
    boolean checkExistParticipation(@Param("userId") Long userId, @Param("postId") Long postId);

    // ✅ 신청 대상 게시글 존재 확인
    @Select("SELECT EXISTS (SELECT 1 FROM POSTS WHERE ID = #{postId})")
    boolean checkExistPost(@Param("postId") Long postId);

    // ✅ 유효한 신청 글 여부 확인 (모집중 상태인지 체크)
    @Select("SELECT EXISTS (SELECT 1 FROM POSTS WHERE ID = #{postId} AND STATUS IN ('모집중'))")
    boolean checkPostStatusValid(@Param("postId") Long postId);

    // ✅ 승인 가능 여부 확인 (현재 참여 인원 < 최대 인원)
    @Select("SELECT EXISTS (SELECT 1 FROM POSTS WHERE ID = #{postId} " +
            "AND CURRENT_PARTICIPANTS < MAX_PARTICIPANTS)")
    boolean checkPersonAvailable(@Param("postId") Long postId);

    // ✅ 신청 추가 (INSERT) - participantId 포함
    @Insert("INSERT INTO POST_PARTICIPANTS (USER_ID, POST_ID, STATUS, JOINED_AT) " +
            "VALUES (#{userId}, #{postId}, '대기', NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "participantId")
    void applyParticipation(ParticipationRequestDTO requestDTO);


    // ✅ 신청 취소 (DELETE)
    @Delete("DELETE FROM POST_PARTICIPANTS WHERE USER_ID = #{userId} AND POST_ID = #{postId}")
    void cancelParticipation(@Param("userId") Long userId, @Param("postId") Long postId);

    // ✅ 참여 승인 (UPDATE)
    @Update("UPDATE POST_PARTICIPANTS SET STATUS = '승인' " +
            "WHERE USER_ID = #{userId} AND POST_ID = #{postId} AND STATUS = '대기'")
    void approveParticipation(@Param("userId") Long userId, @Param("postId") Long postId);

    // ✅ 승인 시 인원 증가 (UPDATE)
    @Update("UPDATE POSTS SET CURRENT_PARTICIPANTS = CURRENT_PARTICIPANTS + 1 " +
            "WHERE ID = #{postId} AND CURRENT_PARTICIPANTS < MAX_PARTICIPANTS")
    void increaseCurrentPerson(@Param("postId") Long postId);

    // ✅ 참여 거부 (UPDATE)
    @Update("UPDATE POST_PARTICIPANTS SET STATUS = '거절' " +
            "WHERE USER_ID = #{userId} AND POST_ID = #{postId} AND STATUS = '대기'")
    void rejectParticipation(@Param("userId") Long userId, @Param("postId") Long postId);

    // ✅ 승인/거부 후 참여 큐에서 삭제 (DELETE)
    @Delete("DELETE FROM POST_PARTICIPANTS " +
            "WHERE USER_ID = #{userId} " +
            "AND POST_ID = #{postId} " +
            "AND (STATUS = '승인' OR STATUS = '대기')")
    void deleteFromParticipationQueue(@Param("userId") Long userId, @Param("postId") Long postId);

    // ✅ 특정 게시글의 전체 참여 현황 조회 (SELECT)
    @Select("SELECT P.USER_ID AS userId, P.POST_ID AS postId, U.NICKNAME, PO.TITLE, " +
            "P.STATUS, P.JOINED_AT AS joinedAt " +
            "FROM POST_PARTICIPANTS P " +
            "JOIN USERS U ON P.USER_ID = U.ID " +
            "JOIN POSTS PO ON P.POST_ID = PO.ID " +
            "WHERE P.POST_ID = #{postId} " +
            "ORDER BY P.JOINED_AT ASC")
    List<ParticipationResponseDTO> findAllParticipantsByPostId(@Param("postId") Long postId);

    // ✅ 특정 사용자의 참여 신청 현황 조회 (SELECT)
    @Select("SELECT P.USER_ID AS userId, P.POST_ID AS postId, U.NICKNAME, PO.TITLE, " +
            "P.STATUS, P.JOINED_AT AS joinedAt " +
            "FROM POST_PARTICIPANTS P " +
            "JOIN USERS U ON P.USER_ID = U.ID " +
            "JOIN POSTS PO ON P.POST_ID = PO.ID " +
            "WHERE P.USER_ID = #{userId} AND P.POST_ID = #{postId}")
    ParticipationResponseDTO findUserParticipationStatus(@Param("userId") Long userId, @Param("postId") Long postId);
}
