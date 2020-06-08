package com.tcsms.business.Dao;

import com.tcsms.business.Entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;

public interface UserInfoDao extends JpaRepository<UserInfo, String>, JpaSpecificationExecutor<UserInfo>, Serializable {
    UserInfo findByPhoneNumber(String phone);

    UserInfo findByWorkerId(String workerId);

    boolean existsByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    @Transactional //注解用于提交事务，若没有带上这句，会报事务异常提示。
    @Modifying(clearAutomatically = true)
    @Query(value = "update user_info set phoneNumber=:phoneNumber where username=:username;", nativeQuery = true)
    void updatePhoneNimberByUsername(@Param("username") String username, @Param("phoneNumber") String phoneNumber);
}
