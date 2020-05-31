package com.tcsms.securityserver.AOP;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tcsms.securityserver.Entity.OperationLog;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Aspect
@Component
@Log4j2
public class ReceiveOperationLogAop {


    public static final String workMapKey = "workMap";

    private static ConcurrentHashMap<String, Double> workMap = new ConcurrentHashMap<>();


    @Pointcut("execution(* com.tcsms.securityserver.Service.ServiceImp.OperationLogServiceImp.receiveOperationLog(..))")
    public void receiveOperationLog() {
    }


    @Around(value = "receiveOperationLog()")
    public void receiveOperationLogAround(ProceedingJoinPoint point) throws Throwable {
        Object[] args = point.getArgs();
        OperationLog operationLog = (OperationLog) args[0];
        String deviceId = operationLog.getDeviceId();
        double weight = operationLog.getWeight();
        double value = workMap.getOrDefault(deviceId, 0.0);
        workMap.put(deviceId, value + weight);
        point.proceed();
    }

    public static void clearWorkMap() {
        workMap.clear();
    }

    public static JsonArray getWorkLevel() {
        JsonArray jsonArray = new JsonArray();
        List<Map.Entry<String, Double>> hashList = new ArrayList<>(workMap.entrySet());
        // 降序排序
        hashList.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        int lv = 1;
        for (Map.Entry<String, Double> entry : hashList) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("deviceId", entry.getKey());
            jsonObject.addProperty("level", "第" + String.valueOf(lv) + "级");
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }

    public static ConcurrentHashMap<String, Double> getWorkMap() {
        return workMap;
    }

    public static void setWorkMap(ConcurrentHashMap<String, Double> map) {
        workMap = map;
    }

}
