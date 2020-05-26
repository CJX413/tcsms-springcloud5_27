package com.tcsms.securityserver.Dao;

import com.tcsms.securityserver.Entity.OnlineLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.io.Serializable;

public interface OnlineLogDao extends JpaRepository<OnlineLog, Integer>, JpaSpecificationExecutor<OnlineLog>, Serializable {

    @Query(value = "select * from online_log  where online_log.deviceId=:deviceId order by version desc limit 1;", nativeQuery = true)
    OnlineLog findByLatestVersionAndDeviceId(@Param("deviceId") String deviceId);
}
