package com.uplus.ureka.repository.post;

import com.uplus.ureka.dto.post.PostRequestDTO;
import com.uplus.ureka.dto.post.PostResponseDTO;
import java.time.LocalDateTime;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.session.RowBounds;

import java.util.*;


@Mapper
public interface PostMapper {



    //사용자 존재 확인
    @Select("SELECT EXISTS(SELECT 1 FROM USERS WHERE ID = #{userId})")
    boolean checkUserExists(@Param("userId") Long userId);

    //모집 글 존재 확인_id로 식별
    @Select("SELECT EXISTS(SELECT 1 FROM POSTS WHERE ID = #{postId} AND STATUS = '모집 중')")
    boolean checkExistPost(@Param("postId") Long postId);


    //모집 글 작성_id 자동 증가, userId는 자동 입력
    @Insert("INSERT INTO POSTS (USER_ID, TITLE, CONTENT, MAX_PARTICIPANTS, CURRENT_PARTICIPANTS, CATEGORY_ID, STATUS, SCHEDULE_START, SCHEDULE_END, LOCATION, POST_IMAGE, CREATED_AT, UPDATED_AT, EVENT_START) " +
            "VALUES (#{userId},  #{title}, #{content}, #{maxParticipants}, 1, #{categoryId}, '모집 중', #{scheduleStart}, #{scheduleEnd}, #{location}, #{postImage}, NOW(), NOW(), #{eventStart})")
    @Options(useGeneratedKeys = true, keyProperty = "postId", keyColumn = "id")
    void insertPost(PostRequestDTO requestDTO);

    //모집 글 삭제_id 자동 처리
    @Delete("DELETE FROM POSTS WHERE ID = #{postId}")
    void removePost(@Param("postId") Long postId);

    //모집 글 수정_id 자동 처리, 수정할 내용은 DTO에서 가져옴.
    @Update("""
        UPDATE POSTS 
        SET 
            TITLE = COALESCE(#{title}, TITLE), 
            CONTENT = COALESCE(#{content}, CONTENT), 
            MAX_PARTICIPANTS = COALESCE(#{maxParticipants}, MAX_PARTICIPANTS),
            CURRENT_PARTICIPANTS = COALESCE(#{curParticipants}, CURRENT_PARTICIPANTS), 
            CATEGORY_ID = COALESCE(#{categoryId}, CATEGORY_ID), 
            STATUS = COALESCE(#{status}, STATUS), 
            SCHEDULE_START = COALESCE(#{scheduleStart}, SCHEDULE_START), 
            SCHEDULE_END = COALESCE(#{scheduleEnd}, SCHEDULE_END), 
            LOCATION = COALESCE(#{location}, LOCATION), 
            POST_IMAGE = COALESCE(#{postImage}, POST_IMAGE),
            UPDATED_AT = NOW(),
            EVENT_START = COALESCE(#{eventStart}, EVENT_START)
        WHERE ID = #{postId}
        """)
    void updatePost(PostRequestDTO requestDTO);

    //모집 글 조회
    List<PostResponseDTO> findPostsWithFilters(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId,
            @Param("status") String status,
            @Param("location") String location,
            @Param("scheduleStartAfter") LocalDateTime scheduleStartAfter,
            @Param("scheduleEndBefore") LocalDateTime scheduleEndBefore,
            @Param("sortField") String sortField,
            @Param("sortOrder") String sortOrder,
            RowBounds rowBounds
    );

    long countPostsWithFilters(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId,
            @Param("status") String status,
            @Param("location") String location,
            @Param("scheduleStartAfter") LocalDateTime scheduleStartAfter,
            @Param("scheduleEndBefore") LocalDateTime scheduleEndBefore
    );


    @Results(id = "PostResult", value = {
            @Result(column = "id",             property = "id",           id = true),
            @Result(column = "userName",       property = "userName"),
            @Result(column = "title",          property = "title"),
            @Result(column = "content",        property = "content"),
            @Result(column = "maxParticipants",property = "maxParticipants"),
            @Result(column = "currentParticipants", property = "currentParticipants"),
            @Result(column = "categoryName",   property = "categoryName"),
            @Result(column = "status",         property = "status"),
            @Result(column = "scheduleStart",  property = "scheduleStart"),
            @Result(column = "scheduleEnd",    property = "scheduleEnd"),
            @Result(column = "location",       property = "location"),
            @Result(column = "createdAt",      property = "createdAt"),
            @Result(column = "updatedAt",      property = "updatedAt"),
            @Result(column = "postImage",      property = "postImageJson"),
            // JSON 문자열 → List<String> 변환
            @Result(column = "postImage",      property = "postImage",
                    typeHandler = com.uplus.ureka.config.JsonListTypeHandler.class),
            @Result(column = "eventStart",     property = "eventStart")
    })
    //모집 글 상세 조회
    @Select("""
        SELECT 
            P.ID AS id,
            U.MEMBER_NICKNAME AS userName,  -- AS 뒤에 별칭 수정
            P.TITLE AS title,
            P.CONTENT AS content,
            P.MAX_PARTICIPANTS AS maxParticipants,
            P.CURRENT_PARTICIPANTS AS currentParticipants,
            C.NAME AS categoryName,
            P.STATUS AS status,
            P.SCHEDULE_START AS scheduleStart,
            P.SCHEDULE_END AS scheduleEnd,
            P.LOCATION AS location,
            P.CREATED_AT AS createdAt,
            P.UPDATED_AT AS updatedAt,
            P.POST_IMAGE AS postImage,
            P.EVENT_START AS eventStart
        FROM 
            POSTS P
        LEFT JOIN 
            USERS U ON P.USER_ID = U.ID
        LEFT JOIN 
            CATEGORIES C ON C.ID = P.CATEGORY_ID
        WHERE 
            P.ID = #{postId}
        """)
    PostResponseDTO findPostById(@Param("postId") Long postId);


    @Select("""
    SELECT
        p.id,
        u.member_nickname AS userName,
        p.title,
        p.content,
        p.max_participants AS maxParticipants,
        p.current_participants AS currentParticipants,
        c.name AS categoryName,
        p.status,
        p.schedule_start AS scheduleStart,
        p.schedule_end AS scheduleEnd,
        p.location,
        p.created_at AS createdAt,
        p.updated_at AS updatedAt,
        p.post_image AS postImage
    FROM POSTS p
    LEFT JOIN USERS u ON p.user_id = u.id
    LEFT JOIN CATEGORIES c ON c.id = p.category_id
    WHERE p.user_id = #{userId}
    ORDER BY p.created_at DESC
""")
    @ResultMap("PostResult")
    List<PostResponseDTO> findPostsByUserId(@Param("userId") Long userId);



}