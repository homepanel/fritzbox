/**
 * Copyright (c) 2010-2016, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.homepanel.fritzbox.fritzbox.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CallEvent {

    private static final Logger logger = LoggerFactory.getLogger(CallEvent.class);

    private String timestamp;
    private String callType;
    private String id;
    private String externalNo;
    private String internalNo;
    private String connectionType;
    private String raw;
    private String line;

    public CallEvent(String raw) {
        setRaw(raw);
    }

    public String getLine() {
        return line;
    }

    private void setLine(String line) {
        this.line = line;
    }

    public String getTimestamp() {
        return timestamp;
    }

    private void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getCallType() {
        return callType;
    }

    private void setCallType(String callType) {
        this.callType = callType;
    }

    public String getId() {
        return id;
    }

    private void setId(String id) {
        this.id = id;
    }

    public String getExternalNo() {
        return externalNo;
    }

    private void setExternalNo(String externalNo) {
        this.externalNo = externalNo;
    }

    public String getInternalNo() {
        return internalNo;
    }

    private void setInternalNo(String internalNo) {
        this.internalNo = internalNo;
    }

    public String getConnectionType() {
        return connectionType;
    }

    private void setConnectionType(String connectionType) {
        this.connectionType = connectionType;
    }

    public String getRaw() {
        return raw;
    }

    private void setRaw(String raw) {
        this.raw = raw;
    }

    public boolean parseRawEvent() {
        if (getRaw() == null) {
            logger.error("cannot parse call event. no input");
            return false;
        }
        String[] fields = getRaw().split(";");

        if (fields.length < 4) {
            logger.error("cannot parse call event. unexpected line received \"{}\"", getRaw());
            return false;
        }

        setTimestamp(fields[0]);
        setCallType(fields[1]);
        setId(fields[2]);

        if (getCallType().equals("RING")) {

            setExternalNo(fields[3]);
            setInternalNo(fields[4]);
            setConnectionType(fields[5]);

        } else if (getCallType().equals("CONNECT")) {

            setLine(fields[3]);
            setExternalNo(fields[4]);

        } else if (getCallType().equals("CALL")) {

            setLine(fields[3]);
            setInternalNo(fields[4]);
            setExternalNo(fields[5]);
            setConnectionType(fields[6]);
        }

        logger.debug("successfully parsed call event \"{}\"", this.toString());

        return true;
    }
}