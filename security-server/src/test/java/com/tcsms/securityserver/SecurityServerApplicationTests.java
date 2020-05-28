package com.tcsms.securityserver;

import com.tcsms.securityserver.Dao.*;
import com.tcsms.securityserver.Entity.OperationLog;
import com.tcsms.securityserver.ScheduTask.RefreshOperationLogTab;
import com.tcsms.securityserver.Service.ServiceImp.DeviceRegistryServiceImp;
import com.tcsms.securityserver.Service.ServiceImp.OnlineLogServiceImp;
import com.tcsms.securityserver.Service.ServiceImp.OperationLogDateServiceImp;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
class SecurityServerApplicationTests {

    @Autowired
    RefreshOperationLogTab task;
    @Autowired
    SqlMapper sqlMapper;
    @Autowired
    OperationLogDateServiceImp operationLogDateServiceImp;
    @Autowired
    WarningDetailDao warningDetailDao;
    @Autowired
    WarningLogDao warningLogDao;
    @Autowired
    OperationLogDao operationLogDao;
    @Autowired
    OnlineLogDao runningLogDao;

    @Autowired
    WarningRankingDao warningRankingDao;

    @Test
    void contextLoads() throws InterruptedException {
        int i = 1;
        while (true) {
            OperationLog operationLog = new OperationLog(i, "1", "D1", "1", "1", "1", 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1);
            operationLogDao.save(operationLog);
            OperationLog operationLog1 = new OperationLog(i + 1, "1", "D3", "1", "1", "1", 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1);
            operationLogDao.save(operationLog1);
            i = i + 2;
            Thread.sleep(500);
        }
    }

    @Autowired
    DeviceRegistryServiceImp deviceRegistryServiceImp;

    @Test
    void test1() {
//        DeviceRegistry deviceRegistry = new DeviceRegistry();
//        deviceRegistry.setIsRegistered(false);
//        deviceRegistry.setDeviceId("D5");
//        deviceRegistry.setDeviceModel("XXX");
//        deviceRegistry.setLatitude(10.00);
//        deviceRegistry.setLongitude(10.0);
//        deviceRegistryServiceImp.insertNewDevice(deviceRegistry);
    }

    @Test
    void test2() {
//        RunningLog runningLog = new RunningLog();
//        runningLog.setStartTime("1");
//        runningLog.setEndTime("1");
//        runningLog.setDeviceId("D3");
//        runningLogDao.save(runningLog);
        //System.out.println(runningLogDao.findById(8).toString());
    }

    @Autowired
    OnlineLogDao onlineLogDao;
    @Autowired
    OnlineLogServiceImp onlineLogServiceImp;
    @Autowired
    RefreshOperationLogTab refreshOperationLogTab;

    @Test
    void test3() throws InterruptedException {
        System.out.println("---------------------------------");
        System.out.println(sqlMapper.existsOperationLogDate("2020_05_13"));
    }


}
