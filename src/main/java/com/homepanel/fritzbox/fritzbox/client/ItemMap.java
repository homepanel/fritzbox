/**
 * Copyright (c) 2010-2016, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.homepanel.fritzbox.fritzbox.client;

public class ItemMap {

    private String itemCommand; // matches itemconfig
    private String serviceId; // SOAP service ID
    private String readServiceCommand; // command to execute on fbox if value should be read
    private String readDataInName; // name of parameter to put in soap request to read value
    private String readDataOutName; // name of parameter to extract from fbox soap response when reading value (is
    private String writeServiceCommand; // command to execute on fbox if value should be set
    private String writeDataInName; // name of parameter which is put in soap request when setting an option on fbox
    private String writeDataInNameAdditional; // additional Parameter to add to write request. e.g. id of TAM to set

    private SoapValueParser soapValueParser;

    public String getItemCommand() {
        return itemCommand;
    }

    private void setItemCommand(String itemCommand) {
        this.itemCommand = itemCommand;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getReadServiceCommand() {
        return readServiceCommand;
    }

    private void setReadServiceCommand(String readServiceCommand) {
        this.readServiceCommand = readServiceCommand;
    }

    public String getReadDataInName() {
        return readDataInName;
    }

    private void setReadDataInName(String readDataInName) {
        this.readDataInName = readDataInName;
    }

    public String getReadDataOutName() {
        return readDataOutName;
    }

    private void setReadDataOutName(String readDataOutName) {
        this.readDataOutName = readDataOutName;
    }

    public String getWriteServiceCommand() {
        return writeServiceCommand;
    }

    public void setWriteServiceCommand(String writeServiceCommand) {
        this.writeServiceCommand = writeServiceCommand;
    }

    public String getWriteDataInName() {
        return writeDataInName;
    }

    public void setWriteDataInName(String writeDataInName) {
        this.writeDataInName = writeDataInName;
    }

    public String getWriteDataInNameAdditional() {
        return writeDataInNameAdditional;
    }

    public void setWriteDataInNameAdditional(String writeDataInNameAdditional) {
        this.writeDataInNameAdditional = writeDataInNameAdditional;
    }

    public SoapValueParser getSoapValueParser() {
        return soapValueParser;
    }

    public void setSoapValueParser(SoapValueParser soapValueParser) {
        this.soapValueParser = soapValueParser;
    }

    public ItemMap(String itemCommand, String getServiceCommand, String serviceId, String getDataInName, String getDataOutName) {
        setItemCommand(itemCommand);
        setReadServiceCommand(getServiceCommand);
        setServiceId(serviceId);
        setReadDataInName(getDataInName);
        setReadDataOutName(getDataOutName);
    }

    @Override
    public String toString() {
        return "ItemMap [itemCommand=" + itemCommand + ", serviceId=" + serviceId + ", readServiceCommand="
                + readServiceCommand + ", readDataInName=" + readDataInName + ", readDataOutName="
                + readDataOutName + ", svp=" + soapValueParser + ", writeDataInName=" + writeDataInName + "]";
    }
}