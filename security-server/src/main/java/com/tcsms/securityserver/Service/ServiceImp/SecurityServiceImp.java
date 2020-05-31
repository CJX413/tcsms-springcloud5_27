package com.tcsms.securityserver.Service.ServiceImp;

import com.tcsms.securityserver.Dao.BuildingRegistryDao;
import com.tcsms.securityserver.Dao.DeviceRegistryDao;
import com.tcsms.securityserver.Dao.OperatorDao;
import com.tcsms.securityserver.Entity.BuildingRegistry;
import com.tcsms.securityserver.Entity.DeviceRegistry;
import com.tcsms.securityserver.Monitor.*;
import com.tcsms.securityserver.Service.SecurityService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;


@Log4j2
@Service
@Component
public class SecurityServiceImp implements SecurityService {
    @Autowired
    DeviceRegistryDao deviceRegistryDao;
    @Autowired
    BuildingRegistryDao buildingRegistryDao;
    @Autowired
    OperatorDao operatorDao;
    @Autowired
    RedisServiceImp redisServiceImp;

    public void openDeviceCollisionMonitor() throws RuntimeException {
        List<DeviceRegistry> deviceRegistryList = deviceRegistryDao.findByIsRegistered(true);
        for (int i = 0; i < deviceRegistryList.size(); i++) {
            for (int j = i + 1; j < deviceRegistryList.size(); j++) {
                if (!TcsmsMonitor.isCompleteSafe(deviceRegistryList.get(i), deviceRegistryList.get(j))) {
                    DeviceCollisionMonitor monitor = new DeviceCollisionMonitor(deviceRegistryList.get(i),
                            deviceRegistryList.get(j));
                    monitor.start();
                    MonitorManager.addMonitor(monitor);
                }
            }
        }
    }

    public void openBuildingCollisionMonitor() throws RuntimeException {
        List<DeviceRegistry> deviceRegistryList = deviceRegistryDao.findByIsRegistered(true);
        List<BuildingRegistry> buildingRegistryList = buildingRegistryDao.findAll();
        for (int i = 0; i < deviceRegistryList.size(); i++) {
            for (int j = 0; j < buildingRegistryList.size(); j++) {
                if (!TcsmsMonitor.isCompleteSafe(deviceRegistryList.get(i), buildingRegistryList.get(j))) {
                    BuildingCollisionMonitor monitor = new BuildingCollisionMonitor(deviceRegistryList.get(i),
                            buildingRegistryList.get(j));
                    monitor.start();
                    MonitorManager.addMonitor(monitor);
                }
            }
        }
    }

    public void openOtherMonitor() throws RuntimeException {
        List<DeviceRegistry> deviceRegistryList = deviceRegistryDao.findByIsRegistered(true);
        HashMap<String, String> operatorMap = new HashMap<>();
        operatorDao.findAll().forEach(operator -> {
            operatorMap.put(operator.getWorkerId(), operator.getName());
        });
        for (DeviceRegistry deviceRegistry : deviceRegistryList) {
            OtherMonitor monitor = new OtherMonitor(deviceRegistry, operatorMap);
            monitor.start();
            MonitorManager.addMonitor(monitor);
        }
    }

    public void openManagerMonitor() throws RuntimeException {
        ManagerMonitor monitor = new ManagerMonitor();
        monitor.start();
        MonitorManager.addMonitor(monitor);
    }
}
