package com.tcsms.business.Dao;


import com.tcsms.business.Entity.DeviceRegistry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;

public interface DeviceRegistryDao extends JpaRepository<DeviceRegistry, String>, JpaSpecificationExecutor<DeviceRegistry>, Serializable {

    @Modifying
    @Transactional
    @Query("update DeviceRegistry dr set " +
            "dr.bigHeight = CASE WHEN :#{#deviceRegistry.bigHeight} IS NULL THEN dr.bigHeight ELSE :#{#deviceRegistry.bigHeight} END ," +
            "dr.bigLength = CASE WHEN :#{#deviceRegistry.bigLength} IS NULL THEN dr.bigLength ELSE :#{#deviceRegistry.bigLength} END ," +
            "dr.smallHeight =  CASE WHEN :#{#deviceRegistry.smallHeight} IS NULL THEN dr.smallHeight ELSE :#{#deviceRegistry.smallHeight} END ," +
            "dr.smallLength =  CASE WHEN :#{#deviceRegistry.smallLength} IS NULL THEN dr.smallLength ELSE :#{#deviceRegistry.smallLength} END ," +
            "dr.rlt =  CASE WHEN :#{#deviceRegistry.rlt} IS NULL THEN dr.rlt ELSE :#{#deviceRegistry.rlt} END ," +
            "dr.isRegistered =  CASE WHEN :#{#deviceRegistry.isRegistered} IS NULL THEN dr.isRegistered ELSE :#{#deviceRegistry.isRegistered} END " +
            "where dr.deviceId = :#{#deviceRegistry.deviceId}")
    void update(@Param("deviceRegistry") DeviceRegistry deviceRegistry);

    List<DeviceRegistry> findByIsRegistered(Boolean isRegistered);
}
