package com.tcsms.securityserver.Controller;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.tcsms.securityserver.Entity.OperationLog;
import com.tcsms.securityserver.JSON.ResultJSON;
import com.tcsms.securityserver.Service.ServiceImp.OnlineLogServiceImp;
import com.tcsms.securityserver.Service.ServiceImp.OperationLogServiceImp;
import com.tcsms.securityserver.Service.ServiceImp.RestTemplateServiceImp;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;


@CrossOrigin
@RestController
@Log4j2
public class ReceiveController {
    @Autowired
    OperationLogServiceImp operationLogServiceImp;
    @Autowired
    OnlineLogServiceImp onlineLogServiceImp;
    @Autowired
    RestTemplateServiceImp restTemplateServiceImp;
    private static final double G = 10;

    /**
     * 接收设备运行信息的接口
     *
     * @param json；格式为OperationLog
     */
    @RequestMapping(value = "/operationLog", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public void receiveOperationLog(@RequestBody String json) {
        OperationLog operationLog = new Gson().fromJson(json, OperationLog.class);
        BigDecimal radius = new BigDecimal(operationLog.getRadius());
        BigDecimal weight = new BigDecimal(operationLog.getWeight());
        BigDecimal torque = radius.multiply(weight).multiply(new BigDecimal(G));
        double Torque = torque.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        operationLog.setTorque(Torque);
        operationLogServiceImp.receiveOperationLog(operationLog);
    }


    @RequestMapping(value = "/startRunning", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String startRunning(@RequestBody String json) {
        try {
            String deviceId = new Gson().fromJson(json, JsonObject.class).get("deviceId").getAsString();
            log.info(deviceId + "上线了------------------");
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String date = simpleDateFormat.format(new Date());
            onlineLogServiceImp.connectOne(deviceId, date);
            restTemplateServiceImp.sendDeviceConnect(deviceId, date);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultJSON(200, false, "连接失败", null).toString();
        }
        return new ResultJSON(200, true, "连接成功", null).toString();
    }

    @RequestMapping(value = "/stopRunning", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String stopRunning(@RequestBody String json) {
        try {
            String deviceId = new Gson().fromJson(json, JsonObject.class).get("deviceId").getAsString();
            log.info(deviceId + "下线了------------------");
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String date = simpleDateFormat.format(new Date());
            onlineLogServiceImp.disconnectOne(deviceId, date);
            restTemplateServiceImp.sendDeviceDisconnect(deviceId, date);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultJSON(200, false, "断开连接失败", null).toString();
        }
        return new ResultJSON(200, true, "断开连接成功", null).toString();
    }


}
