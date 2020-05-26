package com.tcsms.business.Service.ReceiveServiceImp;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tcsms.business.Dao.DeviceRegistryDao;
import com.tcsms.business.Dao.WarningRankingDao;
import com.tcsms.business.Entity.DeviceRegistry;
import com.tcsms.business.Service.DeviceRegistryService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Log4j2
@Service
public class DeviceRegistryServiceImp implements DeviceRegistryService {
    @Autowired
    DeviceRegistryDao deviceRegistryDao;
    @Autowired
    RedisServiceImp redisServiceImp;
    @Autowired
    private WarningRankingDao warningRankingDao;

    public DeviceRegistryDao getDao() {
        return deviceRegistryDao;
    }


    public void updateDeviceRegistry(DeviceRegistry deviceRegistry) throws RuntimeException {
        deviceRegistryDao.update(deviceRegistry);
    }

    public JsonArray getAllDeviceInfo() {
        JsonArray jsonArray = new JsonArray();
        deviceRegistryDao.findAll().forEach(deviceRegistry -> {
            jsonArray.add(new Gson().fromJson(deviceRegistry.toString(), JsonObject.class));
        });
        return jsonArray;
    }

    public JsonArray getAllRegisteredDeviceInfo() {
        JsonArray jsonArray = new JsonArray();
        deviceRegistryDao.findByIsRegistered(true).forEach(deviceRegistry -> {
            jsonArray.add(new Gson().fromJson(deviceRegistry.toString(), JsonObject.class));
        });
        return jsonArray;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteDeviceById(String deviceId) {
        try {
            warningRankingDao.deleteById(deviceId);
            deviceRegistryDao.deleteById(deviceId);
        } catch (Exception e) {
            throw e;
        }
    }
}
