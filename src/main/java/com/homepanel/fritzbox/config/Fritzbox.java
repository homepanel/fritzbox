package com.homepanel.fritzbox.config;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "fritzbox")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class Fritzbox {

    private String host;
    private Integer port;
    private String username;
    private String password;
    private Boolean ssl;

    public String getHost() {
        return host;
    }

    private void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    private void setPort(Integer port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    private void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    private void setPassword(String password) {
        this.password = password;
    }

    public Boolean getSsl() {
        return ssl;
    }

    private void setSsl(Boolean ssl) {
        this.ssl = ssl;
    }
}