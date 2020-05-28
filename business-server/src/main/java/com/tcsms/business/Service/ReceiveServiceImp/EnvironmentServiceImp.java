package com.tcsms.business.Service.ReceiveServiceImp;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.tcsms.business.Dao.EnvironmentDao;
import com.tcsms.business.Entity.Environment;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class EnvironmentServiceImp {
    private final static int environmentId = 1;
    @Autowired
    private EnvironmentDao environmentDao;

    public JsonObject getEnvironment() throws Exception {
        JsonObject jsonObject;
        Environment environment = environmentDao.findById(environmentId).orElse(null);
        if (environment == null) {
            jsonObject = new Gson().fromJson(new Environment().toString(), JsonObject.class);
        } else {
            jsonObject = new Gson().fromJson(environment.toString(), JsonObject.class);
        }
        return jsonObject;
    }

    public void updateEnvironment(Environment newEnvironment) throws Exception {
        newEnvironment.setId(environmentId);
        environmentDao.save(newEnvironment);
    }

    public boolean checkInvitationCode(String invitationCode) throws Exception {
        Environment environment = environmentDao.findById(environmentId).orElse(null);
        if (environment == null) {
            return false;
        }
        return invitationCode.equals(environment.getInvitationCode());
    }

    public JsonObject getCenter() {
        JsonObject center = new JsonObject();
        Environment environment = environmentDao.findById(environmentId).orElse(null);
        if (environment == null) {
            center.addProperty("lat", 0);
            center.addProperty("lng", 0);
            return center;
        }
        if (environment.getCenterLat() == null || environment.getCenterLng() == null) {
            center.addProperty("lat", 0);
            center.addProperty("lng", 0);
        } else {
            center.addProperty("lat", environment.getCenterLat());
            center.addProperty("lng", environment.getCenterLng());
        }
        return center;
    }
}

