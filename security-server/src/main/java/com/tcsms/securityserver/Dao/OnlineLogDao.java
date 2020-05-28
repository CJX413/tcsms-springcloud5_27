package com.tcsms.securityserver.Dao;

import com.tcsms.securityserver.Entity.OnlineLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface OnlineLogDao extends JpaRepository<OnlineLog, Integer>, JpaSpecificationExecutor<OnlineLog>, Serializable {

    @Query(value = "select * from online_log  where online_log.deviceId=:deviceId order by version desc limit 1;", nativeQuery = true)
    OnlineLog findByLatestVersionAndDeviceId(@Param("deviceId") String deviceId);

    @Query(value = "select * from online_log where version in (select max(version) from online_log group by deviceId)", nativeQuery = true)
    List<OnlineLog> findAllLatestVersion();

    @Query(value = "select online_log.deviceId,online_log.startTime,online_log.endTime,device_registry.deviceModel," +
            "device_registry.isRegistered,device_registry.latitude,device_registry.longitude " +
            "from online_log INNER JOIN device_registry ON online_log.deviceId=device_registry.deviceId" +
            " where version in (select max(version) from online_log group by deviceId)", nativeQuery = true)
    List<Map<String, Object>> findAllLatestVersionJoinDeviceRegistry();
}
