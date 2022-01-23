package com.homepanel.fritzbox.config;

import com.homepanel.core.config.InterfaceTopic;
import com.homepanel.core.config.InterfaceTopicPolling;
import com.homepanel.core.config.InterfaceTopicValue;
import com.homepanel.core.state.Type;
import com.homepanel.fritzbox.service.FritzboxConstants;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlValue;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

public class Topic implements InterfaceTopic, InterfaceTopicValue, InterfaceTopicPolling {

    private String path;
    private Type type;
    private FritzboxConstants.GROUP group;
    private FritzboxConstants.CHANNEL channel;
    private String macAddress;
    private Object lastValue;
    private LocalDateTime lastDateTime;
    private Integer refreshIntervalValue;
    private TimeUnit refreshIntervalUnit;

    @XmlValue
    @Override
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @XmlTransient
    @Override
    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @XmlAttribute
    public FritzboxConstants.GROUP getGroup() {
        return group;
    }

    private void setGroup(FritzboxConstants.GROUP group) {
        this.group = group;
    }

    @XmlAttribute
    public FritzboxConstants.CHANNEL getChannel() {
        return channel;
    }

    private void setChannel(FritzboxConstants.CHANNEL channel) {
        this.channel = channel;
    }

    @XmlAttribute
    public String getMacAddress() {
        return macAddress;
    }

    private void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    @XmlTransient
    @Override
    public Object getLastValue() {
        return lastValue;
    }

    public void setLastValue(Object lastValue) {
        this.lastValue = lastValue;
    }

    @XmlTransient
    @Override
    public LocalDateTime getLastDateTime() {
        return lastDateTime;
    }

    @Override
    public void setLastDateTime(LocalDateTime lastDateTime) {
        this.lastDateTime = lastDateTime;
    }

    @XmlAttribute
    @Override
    public Integer getRefreshIntervalValue() {
        return refreshIntervalValue;
    }

    @Override
    public void setRefreshIntervalValue(Integer refreshIntervalValue) {
        this.refreshIntervalValue = refreshIntervalValue;
    }

    @XmlAttribute
    @Override
    public TimeUnit getRefreshIntervalUnit() {
        return refreshIntervalUnit;
    }

    @Override
    public void setRefreshIntervalUnit(TimeUnit refreshIntervalUnit) {
        this.refreshIntervalUnit = refreshIntervalUnit;
    }
}