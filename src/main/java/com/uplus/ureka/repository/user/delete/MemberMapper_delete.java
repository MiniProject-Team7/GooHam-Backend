package com.uplus.ureka.repository.user.delete;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MemberMapper_delete {

    void deleteByEmail(@Param("email") String id);

    String findUserById(@Param("email")String email);

    String getPasswordById(@Param("email")String email);
}