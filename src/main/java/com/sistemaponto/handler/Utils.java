package com.sistemaponto.handler;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Utils {
    public static Map<String,String> queryToMap(String query) {
        Map<String,String> result = new HashMap<>();
        if (query == null || query.isBlank()) return result;
        for (String param : query.split("&")) {
            String[] pair = param.split("=");
            String key = URLDecoder.decode(pair[0], StandardCharsets.UTF_8);
            String value = pair.length>1 ? URLDecoder.decode(pair[1], StandardCharsets.UTF_8) : "";
            result.put(key, value);
        }
        return result;
    }
}
