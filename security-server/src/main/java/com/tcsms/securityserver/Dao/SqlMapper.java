package com.tcsms.securityserver.Dao;

import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface SqlMapper {
    /**
     * 这些是用来刷新每天的设备运行日志表的操作
     */
    @Update("CREATE TABLE operation_log_clone LIKE operation_log")
    void cloneTabOfOperationLog();

    @Update("RENAME TABLE operation_log_clone TO operation_log")
    void renameOperationLogCloneToOperationLog();

    @Update("RENAME TABLE operation_log TO operation_log_${date}")
    void renameOperationLogToOperationLogDate(@Param("date") String date);

    @Select("SELECT COUNT(table_name) FROM INFORMATION_SCHEMA.TABLES WHERE table_name='operation_log_${date}';")
    int existsOperationLogDate(@Param("date") String date);


    @Update("ALTER TABLE operation_log REMOVE PARTITIONING;")
    void removePartitioningOfOperationLog();

    @Update("ALTER TABLE operation_log PARTITION BY KEY(deviceId) PARTITIONS 127;")
    void createPartitioningOfOperationLog();


    @Delete("drop table if exists operation_log_clone;")
    void dropOperationLogClone();

    @Delete("drop table if exists operation_log_${date};")
    void dropOperationLogDate(@Param("date") String date);



}
