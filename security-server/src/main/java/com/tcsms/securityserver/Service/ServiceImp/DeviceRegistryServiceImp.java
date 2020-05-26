package com.tcsms.securityserver.Service.ServiceImp;


import com.tcsms.securityserver.Dao.DeviceRegistryDao;
import com.tcsms.securityserver.Dao.WarningRankingDao;
import com.tcsms.securityserver.Entity.DeviceRegistry;
import com.tcsms.securityserver.Entity.WarningRanking;
import com.tcsms.securityserver.Service.DeviceRegistryService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Log4j2
@Service
public class DeviceRegistryServiceImp implements DeviceRegistryService {
    @Autowired
    private DeviceRegistryDao deviceRegistryDao;
    @Autowired
    private WarningRankingDao warningRankingDao;

    public DeviceRegistryDao getDao() {
        return deviceRegistryDao;
    }
    @Transactional(rollbackFor = Exception.class)
    public void insertNewDevice(DeviceRegistry device) throws Exception{
        deviceRegistryDao.save(device);
        WarningRanking warningRanking = new WarningRanking();
        warningRanking.setDeviceId(device.getDeviceId());
        warningRankingDao.save(warningRanking);
    }


}
