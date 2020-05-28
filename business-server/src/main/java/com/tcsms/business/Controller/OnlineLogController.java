package com.tcsms.business.Controller;

import com.google.gson.JsonArray;
import com.tcsms.business.JSON.ResultJSON;
import com.tcsms.business.Service.ReceiveServiceImp.OnlineLogServiceImp;
import com.tcsms.business.Utils.JsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OnlineLogController {
    @Autowired
    OnlineLogServiceImp runningLogServiceImp;

    @PreAuthorize(value = "hasAnyAuthority('MONITOR','ADMIN')")
    @RequestMapping(value = "/runningLog", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String runningLog() {
        JsonArray data;
        try {
            data = runningLogServiceImp.onlineLog();
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultJSON(200, false, JsonHelper.replaceIllegalChar(e.getMessage()), null).toString();
        }
        return new ResultJSON(200, true, "获取设备上线日志成功！", data).toString();
    }
}
