package com.uplus.ureka.repository.post;

import java.util.*;
import com.uplus.ureka.dto.post.CommentsResponseDTO;
import com.uplus.ureka.dto.post.CommentsRequestDTO;
import org.apache.ibatis.annotations.*;
import com.uplus.ureka.dto.participation.ParticipationResponseDTO;
import org.apache.ibatis.session.RowBounds;

@Mapper
public interface CommentsMapper {

    // 사용자 존재 확인
    @Select("SELECT EXISTS(SELECT 1 FROM USERS WHERE ID = #{userId})")
    boolean checkUserExists(@Param("userId") Long userId);

    // 신청 대상 게시글 존재 확인
    @Select("SELECT EXISTS (SELECT 1 FROM POSTS WHERE ID = #{postId})")
    boolean checkExistPost(@Param("postId") Long postId);

    //게시글의 댓글 작성
    @Insert("INSERT INTO COMMENTS (post_id, user_id, content, created_at, updated_at) " +
            "VALUES (#{postId}, #{userId}, #{content}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "commentId", keyColumn = "id")
    void createComment(CommentsRequestDTO requestDTO);

    // 댓글 수정하기
    @Update("UPDATE COMMENTS SET CONTENT = #{content}, UPDATED_AT = NOW(), IS_MODIFIED = 1 WHERE ID = #{commentId} AND POST_ID = #{postId} AND USER_ID = #{userId}")
    void updateComment(CommentsRequestDTO requestDTO);
    //댓글 삭제하기
    @Delete("DELETE FROM COMMENTS WHERE ID = #{commentId} AND USER_ID = #{userId} AND POST_ID = #{postId}")
    void deleteComment(@Param("commentId") Long commentId, @Param("postId") Long postId, @Param("userId") Long userId);

    // 댓글 목록 조회 (페이징)
    @Select("SELECT C.ID, C.POST_ID, C.USER_ID, U.MEMBER_NICKNAME AS USERNAME, C.CONTENT, C.CREATED_AT AS CREATEDAT, C.UPDATED_AT AS UPDATEDAT, C.IS_MODIFIED AS ISMODIFIED " +
            "FROM COMMENTS C " +
            "LEFT JOIN USERS U ON C.USER_ID = U.ID " +
            "WHERE C.POST_ID = #{postId} " +
            "ORDER BY C.CREATED_AT ${sort}")
    List<CommentsResponseDTO> findCommentsByPostId(
            @Param("postId") Long postId, @Param("sort") String sort, RowBounds rowBounds);


    //페이징 위해서 개수 세기
    @Select("SELECT COUNT(*) FROM COMMENTS WHERE POST_ID = #{postId}")
    long countCommentsByPostId(@Param("postId") Long postId);

    @Select("SELECT C.ID, C.POST_ID, C.USER_ID, U.MEMBER_NICKNAME AS USERNAME, C.CONTENT, C.CREATED_AT AS CREATEDAT, C.UPDATED_AT AS UPDATEDAT, C.IS_MODIFIED AS ISMODIFIED\n" +
            "FROM COMMENTS C\n" +
            "LEFT JOIN USERS U ON C.USER_ID = U.ID\n" +
            "WHERE C.ID = #{commentId}\n")
    CommentsResponseDTO findCommentwithCommentId(@Param("commentId") Long commentId);

    @Select("SELECT USER_ID AS POST_USER_ID FROM POSTS WHERE ID = #{postId}")
    long getPostOwnerId(@Param("postId") Long postId);

    // 내가 작성한 댓글


    @Select("SELECT C.ID, C.POST_ID, C.USER_ID, U.MEMBER_NICKNAME AS USERNAME, C.CONTENT, C.CREATED_AT AS CREATEDAT, C.UPDATED_AT AS UPDATEDAT, C.IS_MODIFIED AS ISMODIFIED " +
            "FROM COMMENTS C " +
            "LEFT JOIN USERS U ON C.USER_ID = U.ID " +
            "WHERE C.USER_ID = #{userId} " +
            "ORDER BY C.CREATED_AT ${sort}")
    List<CommentsResponseDTO> findCommentsByUserId(
            @Param("userId") Long userId,
            @Param("sort") String sort,
            RowBounds rowBounds);

    @Select("SELECT COUNT(*) FROM COMMENTS WHERE USER_ID = #{userId}")
    long countCommentsByUserId(@Param("userId") Long userId);
}
