package com.tcsms.business.Controller;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tcsms.business.Entity.DeviceRegistry;
import com.tcsms.business.Entity.OperationLog;
import com.tcsms.business.Exception.CustomizeException;
import com.tcsms.business.JSON.SendJSON;
import com.tcsms.business.Service.ReceiveServiceImp.*;
import com.tcsms.business.Utils.JsonHelper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
@Component
@ServerEndpoint(value = "/webSocket/{name}")
public class WebSocket {

    /**
     * 与某个客户端的连接对话，需要通过它来给客户端发送消息
     */
    private Session session;

    /**
     * 标识当前连接客户端的用户名
     */
    private String name;
    public static final String TYPE_1 = "warning";
    public static final String TYPE_2 = "onlineLog";
    public static final String TYPE_3 = "monitorStatus";
    /**
     * 用于存所有的连接服务的客户端，这个对象存储是安全的
     */
    private static ConcurrentHashMap<String, WebSocket> webSocketSet = new ConcurrentHashMap<>();
    /**
     * 用于存储所有连接的用户发送设备运行状况的线程
     */
    private static ConcurrentHashMap<String, Thread> operationLogSendManager = new ConcurrentHashMap<>();

    @Autowired
    private RedisServiceImp redisServiceImp;
    @Autowired
    private UserServiceImp userServiceImp;
    @Autowired
    private OperationLogDateServiceImp operationLogDateServiceImp;
    @Autowired
    private OperationLogServiceImp operationLogServiceImp;
    @Autowired
    private DeviceRegistryServiceImp deviceRegistryServiceImp;

    public static final int REFRESH_DATA_COUNT = 100;
    public static final int LIMIT_DATA_COUNT = 600;
    public static final int SLEEP_TIME = 500;

    @OnOpen
    public void OnOpen(Session session, @PathParam(value = "name") String name) {
        this.session = session;
        this.name = name;
        // name是用来表示唯一客户端，如果需要指定发送，需要指定发送通过name来区分
        webSocketSet.put(name, this);
        log.info(name + "[WebSocket] 连接成功，当前连接人数为：={}", webSocketSet.size());
    }


    @OnClose
    public void OnClose() {
        webSocketSet.remove(this.name);
        log.info(this.name + "[WebSocket] 退出成功，当前连接人数为：={}", webSocketSet.size());
    }

    @OnMessage
    public void OnMessage(String message) {
        try {
            log.info("[WebSocket] 收到消息：{}", message);
            //判断是否需要指定发送，具体规则自定义
            if (message.indexOf("TOUSER") == 0) {
                String name = message.substring(message.indexOf("TOUSER") + 6, message.indexOf(";"));
                AppointSending(name, message.substring(message.indexOf(";") + 1, message.length()));
            } else {
                GroupSending(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 群发
     *
     * @param message
     */
    public void GroupSending(String message) {
        for (String name : webSocketSet.keySet()) {
            try {
                Session session = webSocketSet.get(name).session;
                synchronized (session) {
                    session.getBasicRemote().sendText(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 指定发送
     *
     * @param name
     * @param message
     */
    public void AppointSending(String name, String message) {
        try {
            Session session = webSocketSet.get(name).session;
            synchronized (session) {
                log.info(name + "发送消息成功" + "=>" + message);
                session.getBasicRemote().sendText(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void sendToAllUser(String message) {
        for (String key : webSocketSet.keySet()) {
            AppointSending(key, message);
        }
    }

    public void sendToMonitorAndAdmin(String type, String warningJson) {
        log.info(warningJson);
        String message = new SendJSON(200, type, warningJson).toString();
        for (String key : webSocketSet.keySet()) {
            if (userServiceImp.isRoleMonitorOrAdmin(key)) {
                AppointSending(key, message);
            }
        }
    }

    public void sendException(String name, String message, String type) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("error", JsonHelper.replaceIllegalChar(message));
        SendJSON sendJSON = new SendJSON(200, type, jsonObject.toString());
        AppointSending(name, sendJSON.toString());
    }

    public void sendMonitorStatusToAdmin(String monitorStatus) {
        String message = new SendJSON(200, "monitorStatus", monitorStatus).toString();
        for (String key : webSocketSet.keySet()) {
            if (userServiceImp.isRolAdmin(key)) {
                AppointSending(key, message);
            }
        }
    }

    public void closeAllKindOfOperationLogSendThread(String name) {
        Optional.ofNullable(operationLogSendManager.get(name)).ifPresent(thread -> {
            thread.interrupt();
            operationLogSendManager.remove(name);
        });
    }

    public void openOperationLogSendThread(String name, String deviceId) {
        Thread thread = new Thread(() -> {
            Jedis jedis = redisServiceImp.getJedis();
            try {
                while (!Thread.interrupted()) {
                    log.info("正在发" + deviceId + "消息给--" + name);
                    String value = jedis.get(deviceId);
                    if (value != null) {
                        JsonObject operationLog = new Gson().fromJson(value, JsonObject.class);
                        SendJSON sendJSON = new SendJSON(200, "operationLog", operationLog.toString());
                        AppointSending(name, sendJSON.toString());
                    }
                    Thread.sleep(SLEEP_TIME);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (RuntimeException e) {
                sendException(name, e.getMessage(), "operationLog");
                e.printStackTrace();
            } finally {
                if (jedis != null) {
                    jedis.close();
                }
            }
        });
        Optional.ofNullable(operationLogSendManager.getOrDefault(name, null))
                .ifPresent(oldThread -> {
                    oldThread.interrupt();
                    operationLogSendManager.remove(name);
                });
        operationLogSendManager.put(name, thread);
        thread.start();
    }

    public void openAllOperationLogSendThread(String name) {
        List<DeviceRegistry> devices = deviceRegistryServiceImp.getDao().findByIsRegistered(true);
        if (devices != null) {
            Thread thread = new Thread(() -> {
                Jedis jedis = redisServiceImp.getJedis();
                Gson gson = new Gson();
                try {
                    while (!Thread.interrupted()) {
                        log.info("正在发所有设备的" + "消息给--" + name);
                        JsonArray jsonArray = new JsonArray();
                        devices.forEach(device -> {
                            String value = jedis.get(device.getDeviceId());
                            if (value != null) {
                                jsonArray.add(gson.fromJson(value, JsonElement.class));
                            }
                        });
                        SendJSON sendJSON = new SendJSON(200, "allOperationLog", jsonArray.toString());
                        AppointSending(name, sendJSON.toString());
                        Thread.sleep(SLEEP_TIME);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (RuntimeException e) {
                    sendException(name, e.getMessage(), "allOperationLog");
                    e.printStackTrace();
                } finally {
                    if (jedis != null) {
                        jedis.close();
                    }
                }
            });
            Optional.ofNullable(operationLogSendManager.getOrDefault(name, null))
                    .ifPresent(oldThread -> {
                        oldThread.interrupt();
                        operationLogSendManager.remove(name);
                    });
            operationLogSendManager.put(name, thread);
            thread.start();
        }
    }


    public void openOperationLogDateSendThread(String name, String deviceId, String time) throws ParseException {
        OperationLogDateSend operationLogDateSend = new OperationLogDateSend(name, deviceId, time);
        Optional.ofNullable(operationLogSendManager.getOrDefault(name, null))
                .ifPresent(oldThread -> {
                    oldThread.interrupt();
                    operationLogSendManager.remove(name);
                });
        operationLogSendManager.put(name, operationLogDateSend);
        operationLogDateSend.start();
    }

    public void openAllOperationLogDateSendThread(String name, String time) throws ParseException {
        List<DeviceRegistry> devices = deviceRegistryServiceImp.getDao().findByIsRegistered(true);
        if (devices != null) {
            AllOperationLogDateSend allOperationLogDateSend = new AllOperationLogDateSend(name, time);
            Optional.ofNullable(operationLogSendManager.getOrDefault(name, null))
                    .ifPresent(oldThread -> {
                        oldThread.interrupt();
                        operationLogSendManager.remove(name);
                    });
            operationLogSendManager.put(name, allOperationLogDateSend);
            allOperationLogDateSend.start();
        }
    }

    class AllOperationLogDateSend extends Thread {
        private String name;
        private String time;
        private Date datetime;
        private boolean today;
        private String date;
        List<DeviceRegistry> devices;
        ConcurrentHashMap<String, ConcurrentHashMap<Long, OperationLog>> hashMap = new ConcurrentHashMap<>();

        public AllOperationLogDateSend(String name, String time) throws ParseException {
            this.time = time;
            this.name = name;
            this.devices = deviceRegistryServiceImp.getDao().findByIsRegistered(true);
            ConcurrentHashMap<String, ConcurrentHashMap<Long, OperationLog>> hashMap = new ConcurrentHashMap<>();
            Date datetime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(time);
            this.datetime = datetime;
            String date = new SimpleDateFormat("yyyy_MM_dd").format(datetime);
            this.date = date;
            if (new SimpleDateFormat("yyyy_MM_dd").format(new Date()).equals(date)) {
                //当天的
                today = true;
            } else {
                //往天的
                today = false;
            }
        }

        private int mapSize() {
            int size = 0;
            for (Map.Entry<String, ConcurrentHashMap<Long, OperationLog>> entry : hashMap.entrySet()) {
                size = Math.max(size, entry.getValue().size());
            }
            return size;
        }

        @Override
        public void run() {
            try {
                if (today) {
                    operationLogServiceImp.queryAllDeviceOperationLogDateByDeviceIdAndTime(hashMap, devices, time);
                } else {
                    operationLogDateServiceImp.queryAllDeviceOperationLogDateByDeviceIdAndTime(hashMap, devices, time, date);
                }
                int i = 0;
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Gson gson = new Gson();
                long newDatetime = datetime.getTime();
                while (!Thread.interrupted()) {
                    String newTime = simpleDateFormat.format(newDatetime);
                    log.info(mapSize()+"-----------------------------mapSize");
                    if (mapSize() == 0) {
                        throw new CustomizeException("之后都没有数据，请将时间往前调！");
                    }
                    if (mapSize() < REFRESH_DATA_COUNT) {
                        if (today) {
                            log.info("刷新今天的AllDeviceOperationLogDate");
                            operationLogServiceImp.refreshAllDeviceOperationLogDateHashMap(hashMap, devices, newTime);
                        } else {
                            log.info("刷新往日的AllDeviceOperationLogDate");
                            operationLogDateServiceImp.refreshAllDeviceOperationLogDateHashMap(hashMap, devices, newTime, date);
                        }
                    }
                    JsonArray jsonArray = new JsonArray();
                    for (Map.Entry<String, ConcurrentHashMap<Long, OperationLog>> entry : hashMap.entrySet()) {
                        OperationLog operationLog = entry.getValue().getOrDefault(newDatetime, null);
                        if (operationLog != null) {
                            jsonArray.add(gson.fromJson(operationLog.toString(), JsonElement.class));
                            entry.getValue().remove(newDatetime);
                        }
                    }
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.add("data", jsonArray);
                    jsonObject.addProperty("time", newTime);
                    SendJSON sendJSON = new SendJSON(200, "allOperationLogDate", jsonObject.toString());
                    AppointSending(name, sendJSON.toString());
                    newDatetime = newDatetime + 1000;
                    i = (i + 1) % LIMIT_DATA_COUNT;
                    Thread.sleep(SLEEP_TIME);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (Exception e) {
                sendException(name, e.getMessage(), "allOperationLogDate");
                e.printStackTrace();
            }
        }
    }


    class OperationLogDateSend extends Thread {
        private String name;
        private String deviceId;
        private String time;
        private boolean today;
        private String date;
        private Deque<OperationLog> deque = new ArrayDeque<>();

        public OperationLogDateSend(String name, String deviceId, String time) throws ParseException {
            this.time = time;
            this.deviceId = deviceId;
            this.name = name;
            Date datetime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(time);
            String date = new SimpleDateFormat("yyyy_MM_dd").format(datetime);
            this.date = date;
            if (new SimpleDateFormat("yyyy_MM_dd").format(new Date()).equals(date)) {
                //当天的
                today = true;
            } else {
                //往天的
                today = false;
            }
        }

        @Override
        public void run() {
            try {
                if (today) {
                    operationLogServiceImp.queryOperationLogByDeviceIdAndTime(deque, deviceId, time);
                } else {
                    operationLogDateServiceImp.queryOperationLogDateByDeviceIdAndTime(deque, deviceId, time, date);
                }
                String refreshTime;
                while (!Thread.interrupted()) {
                    log.info("消息队列的长度为：" + deque.size());
                    if (deque.isEmpty()) {
                        throw new CustomizeException("之后都没有数据，请将时间往前调！");
                    }
                    if (deque.size() < REFRESH_DATA_COUNT) {
                        refreshTime = deque.getLast().getTime();
                        if (today) {
                            log.info("刷新今天的OperationLogDate");
                            operationLogServiceImp.refreshOperationLogQueue(deque, deviceId, refreshTime);
                        } else {
                            log.info("刷新往天的OperationLogDate");
                            operationLogDateServiceImp.refreshOperationLogDateQueue(deque, deviceId, refreshTime, date);
                        }
                    }
                    Optional.ofNullable(deque.pollFirst()).ifPresent(operationLog -> {
                        SendJSON sendJSON = new SendJSON(200, "operationLogDate", operationLog.toString());
                        AppointSending(name, sendJSON.toString());
                    });
                    Thread.sleep(SLEEP_TIME);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
                sendException(name, e.getMessage(), "operationLogDate");
            } finally {
                deque.clear();
            }
        }

    }

}
