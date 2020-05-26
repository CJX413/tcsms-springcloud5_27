package com.tcsms.business.Service.ReceiveServiceImp;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tcsms.business.Dao.WarningRankingDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class WarningRankingServiceImp {
    @Autowired
    private WarningRankingDao warningRankingDao;

    public JsonArray getWarningRanking() throws Exception {
        JsonArray jsonArray = new JsonArray();
        List<Map<String, Object>> list = warningRankingDao.orderByWarningCount();
        for (Map<String, Object> map : list) {
            JsonObject jsonObject = new JsonObject();
            map.forEach((k, v) -> {
                if (v instanceof String) {
                    jsonObject.addProperty(k, (String) v);
                } else if (v instanceof Boolean) {
                    jsonObject.addProperty(k, (Boolean) v);
                } else if (v instanceof Integer) {
                    jsonObject.addProperty(k, (Integer) v);
                }
            });
            jsonArray.add(jsonObject);

        }
        return jsonArray;
    }
}
