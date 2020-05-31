package com.tcsms.securityserver.Config;

import com.tcsms.securityserver.AOP.ReceiveOperationLogAop;
import com.tcsms.securityserver.Service.ServiceImp.RedisServiceImp;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Log4j2
@Component
@Order(value = 10)
public class WorkMapConfig implements CommandLineRunner {

    @Autowired
    RedisServiceImp redisServiceImp;


    @Override
    public void run(String... args) {
        log.info(">>>>>>>>>>>>初始化WorkMap>>>>>>>>>>>>>");
        try (Jedis jedis = redisServiceImp.getJedis()) {
            Map<String, String> map = jedis.hgetAll(ReceiveOperationLogAop.workMapKey);
            if (map.size() == 0) {
                return;
            }
            ConcurrentHashMap<String, Double> workMap = new ConcurrentHashMap<>();
            map.forEach((k, v) -> {
                workMap.put(k, Double.valueOf(v));
            });
            ReceiveOperationLogAop.setWorkMap(workMap);
        }
    }
}
