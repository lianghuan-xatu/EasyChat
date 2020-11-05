package com.xatu.easyChat.entity;

import io.netty.channel.Channel;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户Channel关联
 */
public class UserChannelRel {
    public static HashMap<String, Channel> map = new HashMap();

    public static void put(String userId, Channel channel) {
        map.put(userId,channel);
    }
    public static Channel get(String userId) {
        Channel channel = map.get(userId);
        return channel;
    }

    public static void output() {
        for(Map.Entry<String,Channel> entry: map.entrySet()) {
            System.out.println("userId：" +entry.getKey() +"||" +
                    "ChannelId：" +entry.getValue().id().asLongText());
        }
    }

}
