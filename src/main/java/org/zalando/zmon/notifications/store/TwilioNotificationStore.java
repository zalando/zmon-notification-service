package org.zalando.zmon.notifications.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.zalando.zmon.notifications.TwilioAlert;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.UUID;

/**
 * Created by jmussler on 11.10.16.
 */
public class TwilioNotificationStore {

    private final ObjectMapper mapper;

    private final JedisPool pool;

    public TwilioNotificationStore(JedisPool pool, ObjectMapper mapper) {
        this.pool = pool;
        this.mapper = mapper;
    }

    public TwilioAlert getAlert(String uuid) {
        if (null == uuid || "".equals(uuid)) {
            return null;
        }

        try(Jedis jedis = pool.getResource()) {
            String data = jedis.get(uuid);
            if (null == data) {
                return null;
            }
            return mapper.readValue(data, TwilioAlert.class);
        }
        catch(Exception ex) {
            return null;
        }
    }

    public String storeAlert(TwilioAlert data) {
        String uuid = UUID.randomUUID().toString();

        try(Jedis jedis = pool.getResource()) {
            jedis.set(uuid, mapper.writeValueAsString(data));
            jedis.expire(uuid, 60*60);
        }
        catch(Exception ex) {

        }

        return uuid;
    }
}