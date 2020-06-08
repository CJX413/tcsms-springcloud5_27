package com.tcsms.securityserver.ScheduTask;


import com.tcsms.securityserver.AOP.ReceiveOperationLogAop;
import com.tcsms.securityserver.Service.ServiceImp.RedisServiceImp;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
@Component
public class WorkMapTask {

    @Autowired
    RedisServiceImp redisServiceImp;

    @Scheduled(cron = "0 0 1 * * ?")
    public void refreshWorkMap() throws InterruptedException {
        ReceiveOperationLogAop.clearWorkMap();
        redisServiceImp.delete(ReceiveOperationLogAop.workMapKey);
    }

    @Scheduled(cron = "0 0 */1 * * ?")
    public void saveWorkMap() throws InterruptedException {
        try (Jedis jedis = redisServiceImp.getJedis()) {
            ConcurrentHashMap<String, Double> workMap = ReceiveOperationLogAop.getWorkMap();
            HashMap<String, String> map = new HashMap<>();
            workMap.forEach((k, v) -> {
                map.put(k, String.valueOf(v));
            });
            jedis.hmset(ReceiveOperationLogAop.workMapKey, map);
            log.info("保存WorkMap成功" + map.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
