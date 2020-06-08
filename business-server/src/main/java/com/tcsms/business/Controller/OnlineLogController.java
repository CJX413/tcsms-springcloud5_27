package com.tcsms.business.Controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tcsms.business.JSON.ResultJSON;
import com.tcsms.business.Service.ReceiveServiceImp.OnlineLogServiceImp;
import com.tcsms.business.Utils.JsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OnlineLogController {
    @Autowired
    OnlineLogServiceImp onlineLogServiceImp;

    @PreAuthorize(value = "hasAnyAuthority('MONITOR','ADMIN')")
    @RequestMapping(value = "/onlineLog", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String onlineLog() {
        JsonArray data;
        try {
            data = onlineLogServiceImp.onlineLog();
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultJSON(200, false, JsonHelper.replaceIllegalChar(e.getMessage()), null).toString();
        }
        return new ResultJSON(200, true, "获取设备上线日志成功！", data).toString();
    }

    @PreAuthorize(value = "hasAnyAuthority('MONITOR','ADMIN')")
    @RequestMapping(value = "/onlineLogBar", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String onlineLogBar(@RequestBody String json) {
        JsonObject data;
        try {
            JsonObject jsonObject = new Gson().fromJson(json, JsonObject.class);
            String date = jsonObject.get("date").getAsString();
            data = onlineLogServiceImp.runningLogBar(date);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultJSON(200, false, JsonHelper.replaceIllegalChar(e.getMessage()), null).toString();
        }
        return new ResultJSON(200, true, "获取设备上线日志成功！", data).toString();
    }
}
