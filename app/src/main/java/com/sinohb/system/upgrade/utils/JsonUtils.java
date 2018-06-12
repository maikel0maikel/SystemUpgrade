package com.sinohb.system.upgrade.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.sinohb.logger.LogTools;

import java.util.ArrayList;
import java.util.List;

public class JsonUtils {
    private static final String TAG = "JsonUtils";
    private JsonUtils(){}
    public static <T> T parse(String jsonData, Class<T> type) {
        Gson gson = new Gson();
        T result = null;
        try{
            result = gson.fromJson(jsonData, type);
        }catch (Exception e){
            LogTools.e(TAG,e,"解析失败："+jsonData);
        }
        return result;
    }

    /**
     * 将Json数组解析成相应的映射对象列表
     * @param jsonData
     * @param cls
     * @param <T>
     * @return
     */
    public static <T> List<T> parseArray(String jsonData, Class<T> cls) {
        Gson gson = new Gson();
        List<T> list = new ArrayList<>();
        try {
            JsonArray array = new JsonParser().parse(jsonData).getAsJsonArray();
            for(final JsonElement elem : array){
                list.add(gson.fromJson(elem, cls));
            }
        }catch (Exception e){
            LogTools.e(TAG,e,"解析失败："+jsonData);
        }

        return list ;
    }

    /**
     * 将Json数组解析成相应的映射对象列表
     * @param jsonData
     * @param <T>
     * @return
     */
    public static <T> List<T> parseJsonList(String jsonData) {
        Gson gson = new Gson();
        List<T> result = gson.fromJson(jsonData, new TypeToken<List<T>>() {}.getType());
        return result;
    }

    public static String toJson(Object object) {
        Gson gson = new Gson();
        String json = gson.toJson(object);
        return json;
    }

}
