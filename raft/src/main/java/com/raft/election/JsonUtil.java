package com.raft.election;


import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtil {
    public static <T> T decode(String data, Class<T> theClass) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(data.getBytes(), theClass);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
