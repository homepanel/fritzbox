/**
 * Copyright (c) 2010-2016, openHAB.org and others.
 * <p>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.homepanel.fritzbox.fritzbox.client;

import org.w3c.dom.Document;


/***
 *
 * @author gitbock
 * @since 1.8.0
 *
 */

public interface SoapValueParser {
    /***
     *
     * @param document soap message to parse
     * @param mapping itemmap with information about all TR064 parameters
     * @param request the raw original request which was used in itemconfig
     * @return the value which was parsed from soap message
     */

    String parseValueFromSoapMessage(Document document, ItemMap mapping, String request);

}
