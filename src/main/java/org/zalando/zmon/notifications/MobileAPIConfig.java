package org.zalando.zmon.notifications;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Created by jmussler on 12/16/15.
 */
@Configuration
@ConfigurationProperties(prefix = "mobile")
public class MobileAPIConfig {

    /**
     * The url to the 'zmon-data-service'.
     * 
     */
    private String dataServiceUrl;

    private String readScope;

    public String getDataServiceUrl() {
        return dataServiceUrl;
    }

    public void setDataServiceUrl(String dataServiceUrl) {
        this.dataServiceUrl = dataServiceUrl;
    }

    public String getReadScope() {
        return readScope;
    }

    public void setReadScope(String readScope) {
        this.readScope = readScope;
    }

}
