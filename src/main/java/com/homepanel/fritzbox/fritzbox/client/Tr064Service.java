/**
 * Copyright (c) 2010-2016, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.homepanel.fritzbox.fritzbox.client;

public class Tr064Service {

    private String serviceType;
    private String serviceId;
    private String controlUrl;
    private String eventSubUrl;
    private String scpdurl;

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getControlUrl() {
        return controlUrl;
    }

    public void setControlUrl(String controlUrl) {
        this.controlUrl = controlUrl;
    }

    public String getEventSubUrl() {
        return eventSubUrl;
    }

    public void setEventSubUrl(String eventSubUrl) {
        this.eventSubUrl = eventSubUrl;
    }

    public String getScpdurl() {
        return scpdurl;
    }

    public void setScpdurl(String scpdurl) {
        this.scpdurl = scpdurl;
    }
}