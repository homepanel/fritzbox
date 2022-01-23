/**
 * Copyright (c) 2010-2016, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.homepanel.fritzbox.fritzbox.client;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.DigestScheme;
import org.apache.http.impl.client.*;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;

public class Tr064Client {

    private final static Logger LOGGER = LoggerFactory.getLogger(Tr064Client.class);

    private final static String DEFAULT_USERNAME = "dslf-config"; // is used when no username is provided.
    private final static String TR064_DOWNLOAD_FILENAME = "tr64desc.xml"; // filename of all available TR064 on fbox

    private String host;
    private Integer port;
    private String username;
    private String password;
    private Boolean ssl;
    private String fritzboxUrl;

    private CloseableHttpClient httpClient;
    private HttpClientContext httpClientContext;

    private String getHost() {
        return host;
    }

    private void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    private String getUsername() {
        return username;
    }

    private void setUsername(String username) {
        this.username = username;
    }

    private String getPassword() {
        return password;
    }

    private void setPassword(String password) {
        this.password = password;
    }

    private Boolean getSsl() {
        return ssl;
    }

    private void setSsl(Boolean ssl) {
        this.ssl = ssl;
    }

    private String getFritzboxUrl() {
        return fritzboxUrl;
    }

    private void setFritzboxUrl(String fritzboxUrl) {
        this.fritzboxUrl = fritzboxUrl;
    }

    private CloseableHttpClient getHttpClient() {
        return httpClient;
    }

    private void setHttpClient(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    private HttpClientContext getHttpClientContext() {
        return httpClientContext;
    }

    private void setHttpClientContext(HttpClientContext httpClientContext) {
        this.httpClientContext = httpClientContext;
    }

    private ArrayList<Tr064Service> services = null;

    // mappig table for mapping item command to tr064 parameters
    private ArrayList<ItemMap> alItemMap = null;

    public Tr064Client(String host, Integer port, String username, String password, Boolean ssl) {
        setHost(host);
        setPort(port);
        setUsername(username);
        setPassword(password);
        setSsl(ssl);

        services = new ArrayList<Tr064Service>();
        alItemMap = new ArrayList<ItemMap>();
        init();
    }

    /***
     * makes sure all values are set properly
     * before starting communications
     */
    private void init() {

        if (getPort() == null) {
            if (getSsl()) {
                setPort(49443);
            } else {
                setPort(49000);
            }
        }

        setFritzboxUrl(ssl ? "https" : "http" + "://" + getHost() + ":" + getPort());

        if (getUsername() == null) {
            setUsername(DEFAULT_USERNAME);
        }

        if (getHttpClient() == null) {
            setHttpClient(createTr064HttpClient());
        }

        if (services.isEmpty()) { // no services are known yet?
            readAllServices(); // can be done w/out item mappings and w/out auth
        }

        if (alItemMap.isEmpty()) { // no mappings present yet?
            generateItemMappings();
        }
    }

    /***
     * Fetches a specific value from FritzBox
     *
     *
     * @param request string from config including the command and optional parameters
     * @return parsed value
     */
    public String getTr064Value(String request) {
        String value = null;

        // extract itemCommand from request
        String[] itemConfig = request.split(":");
        String itemCommand = itemConfig[0]; // command is always first

        // search for proper item Mapping
        ItemMap itemMap = determineItemMappingByItemCommand(itemCommand);

        if (itemMap == null) {
            LOGGER.warn("No item mapping found for {}. Function not implemented by your FritzBox (?)", request);
            return "";
        }

        // determine which url etc. to connect to for accessing required value
        Tr064Service tr064service = determineServiceByItemMapping(itemMap);

        Document document = null;

        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            document = documentBuilder.newDocument();

            Element envelope = document.createElement("s:Envelope");
            envelope.setAttribute("xmlns:s", "http://schemas.xmlsoap.org/soap/envelope/");
            envelope.setAttribute("s:encodingStyle", "http://schemas.xmlsoap.org/soap/encoding/");

            Element header = document.createElement("s:Header");
            envelope.appendChild(header);

            Element body = document.createElement("s:Body");

            Element type = document.createElement("u:" + itemMap.getReadServiceCommand());
            type.setAttribute("xmlns:u", tr064service.getServiceType());

            // only if input parameter is present
            if (itemConfig.length > 1) {

                String dataInValue = itemConfig[1];

                if (itemMap.getItemCommand().equals("maconline")) {
                    dataInValue = dataInValue.replaceAll("-", ":");
                }

                Element item = document.createElement(itemMap.getReadDataInName());
                item.appendChild(document.createTextNode(dataInValue));

                type.appendChild(item);
            }

            body.appendChild(type);

            envelope.appendChild(body);
            document.appendChild(envelope);

            LOGGER.debug("Raw SOAP Request to be sent to FritzBox: {}", soapToString(document));

        } catch (Exception e) {
            LOGGER.error("Error constructing request SOAP msg for getting parameter. {}", e.getMessage());
            LOGGER.debug("Request was: {}", request);
        }

        if (document == null) {
            LOGGER.error("Could not determine data to be sent to FritzBox!");
            return null;
        }

        Document response = readSoapResponse(tr064service.getServiceType() + "#" + itemMap.getReadServiceCommand(), document, getFritzboxUrl() + tr064service.getControlUrl());

        if (response == null) {
            LOGGER.error("Error retrieving SOAP response from FritzBox");
            return null;
        }

        LOGGER.debug("Raw SOAP Response from FritzBox: {}", soapToString(response));
        // check if special "soap value parser" handler for extracting SOAP value is defined. If yes, use svp
        if (itemMap.getSoapValueParser() == null) { // extract dataOutName1 as default, no handler used
            Element element = response.getDocumentElement();
            NodeList nlDataOutNodes = element.getElementsByTagName(itemMap.getReadDataOutName());


            //NodeList nlDataOutNodes = response.getSOAPPart().getElementsByTagName(itemMap.getReadDataOutName());
            if (nlDataOutNodes != null && nlDataOutNodes.getLength() > 0) {
                // extract value from soap response
                value = nlDataOutNodes.item(0).getTextContent();
            } else {
                LOGGER.error(
                        "FritzBox returned unexpected response. Could not find expected datavalue {} in response {}",
                        itemMap.getReadDataOutName(), soapToString(response));
            }

        } else {
            LOGGER.debug("Parsing response using SOAP value parser in Item map");
            value = itemMap.getSoapValueParser().parseValueFromSoapMessage(response, itemMap, request); // itemMap is
            // passed for
            // accessing
            // mapping in
            // anonymous
            // method
            // (better way
            // to do??)
        }

        return value;
    }

    /***
     * Sets a parameter in fbox. Called from event bus
     *
     * @param request config string from itemconfig
     */

    public void setTr064Value(String request, String value) {
        // extract itemCommand from request
        String[] itemConfig = request.split(":");
        String itemCommand = itemConfig[0]; // command is always first

        // search for proper item Mapping
        ItemMap itemMap = determineItemMappingByItemCommand(itemCommand);

        // determine which url etc. to connect to for accessing required value
        Tr064Service tr064service = determineServiceByItemMapping(itemMap);
        // construct soap Body which is added to soap msg later
        Document document = null;

        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            document = documentBuilder.newDocument();

            Element envelope = document.createElement("s:Envelope");
            envelope.setAttribute("xmlns:s", "http://schemas.xmlsoap.org/soap/envelope/");
            envelope.setAttribute("s:encodingStyle", "http://schemas.xmlsoap.org/soap/encoding/");

            Element header = document.createElement("s:Header");
            envelope.appendChild(header);

            Element body = document.createElement("s:Body");

            Element type = document.createElement("u:" + itemMap.getWriteServiceCommand());
            type.setAttribute("xmlns:u", tr064service.getServiceType());


            // only if input parameter is present
            if (itemConfig.length > 1) {

                String dataInValue = itemConfig[1];

                Element item = document.createElement(itemMap.getWriteDataInNameAdditional());
                item.appendChild(document.createTextNode(dataInValue));

                type.appendChild(item);
            }

            body.appendChild(type);

            envelope.appendChild(body);
            document.appendChild(envelope);

            // convert String command into numeric
            Element item = document.createElement(itemMap.getWriteDataInName());
            item.appendChild(document.createTextNode(value));
            type.appendChild(item);

            LOGGER.debug("SOAP Msg to send to FritzBox for setting data: {}", soapToString(document));

        } catch (Exception e) {
            LOGGER.error("Error constructing request SOAP msg for setting parameter. {}", e.getMessage());
            LOGGER.debug("Request was: {}. value was: {}.", request, value);
        }

        if (document == null) {
            LOGGER.error("Could not determine data to be sent to FritzBox!");
            return;
        }

        Document response = readSoapResponse(tr064service.getServiceType() + "#" + itemMap.getWriteServiceCommand(), document, getFritzboxUrl() + tr064service.getControlUrl());
        if (response == null) {
            LOGGER.error("Error retrieving SOAP response from FritzBox");
            return;
        }
        LOGGER.debug("SOAP response from FritzBox: {}", soapToString(response));

        // Check if error received
        try {
            Element element = response.getDocumentElement();
            NodeList faults = element.getElementsByTagName("s:Fault");
            if (faults.getLength() > 0) {
                LOGGER.error("Error received from FritzBox while trying to set parameter");
                LOGGER.debug("Soap Response was: {}", soapToString(response));
            }
        } catch (Exception e) {
            LOGGER.error("Could not parse soap response from FritzBox while setting parameter. {}", e.getMessage());
            LOGGER.debug("Soap Response was: {}", soapToString(response));
        }

    }

    /***
     * Creates a apache HTTP Client object, ignoring SSL Exceptions like self signed certificates
     * and sets Auth. Scheme to Digest Auth
     *
     * @return the ready-to-use httpclient for tr064 requests
     */
    private CloseableHttpClient createTr064HttpClient() {
        CloseableHttpClient httpClientBuilder = null;
        // Convert URL String from config in easy explotable URI object
        URIBuilder uriFbox = null;
        try {
            uriFbox = new URIBuilder(getFritzboxUrl());
        } catch (URISyntaxException e) {
            LOGGER.error("Invalid FritzBox URL! {}", e.getMessage());
            return null;
        }
        // Create context of the http client
        setHttpClientContext(HttpClientContext.create());
        CookieStore cookieStore = new BasicCookieStore();
        getHttpClientContext().setCookieStore(cookieStore);

        // SETUP AUTH
        // Auth is specific for this target
        HttpHost target = new HttpHost(uriFbox.getHost(), uriFbox.getPort(), uriFbox.getScheme());
        // Add digest authentication with username/pw from global config
        CredentialsProvider credp = new BasicCredentialsProvider();
        credp.setCredentials(new AuthScope(target.getHostName(), target.getPort()),
                new UsernamePasswordCredentials(username, password));
        // Create AuthCache instance. Manages authentication based on server response
        AuthCache authCache = new BasicAuthCache();
        // Generate DIGEST scheme object, initialize it and add it to the local auth cache. Digeste is standard for fbox
        // auth SOAP
        DigestScheme digestAuth = new DigestScheme();
        digestAuth.overrideParamter("realm", "HTTPS Access"); // known from fbox specification
        digestAuth.overrideParamter("nonce", ""); // never known at first request
        authCache.put(target, digestAuth);
        // Add AuthCache to the execution context
        getHttpClientContext().setAuthCache(authCache);

        // SETUP SSL TRUST
        SSLContextBuilder sslContextBuilder = new SSLContextBuilder();
        SSLConnectionSocketFactory sslsf = null;
        try {
            sslContextBuilder.loadTrustMaterial(null, new TrustSelfSignedStrategy()); // accept self signed certs
            sslsf = new SSLConnectionSocketFactory(sslContextBuilder.build(), null, null, new NoopHostnameVerifier()); // dont             // CN
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
        }

        // Set timeout values
        RequestConfig rc = RequestConfig.copy(RequestConfig.DEFAULT).setSocketTimeout(4000).setConnectTimeout(4000).setConnectionRequestTimeout(4000).build();

        // BUILDER
        // setup builder with parameters defined before
        httpClientBuilder = HttpClientBuilder.create()
                .setSSLSocketFactory(sslsf) // set the SSL options which trust every self signed
                .setDefaultCredentialsProvider(credp) // set auth options using digest
                .setDefaultRequestConfig(rc) // set the request config specifying timeout
                .build();

        return httpClientBuilder;
    }

    /***
     * converts SOAP msg into string
     *
     * @param document
     * @return
     */
    private String soapToString(Document document) {

        String strMsg = "";

        try {
            StringWriter stringWriter = new StringWriter();
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            transformer.transform(new DOMSource(document), new StreamResult(stringWriter));
            strMsg = stringWriter.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return strMsg;
    }

    private Document readSoapResponse(String soapActionHeader, Document request, String serviceUrl) {

        Document response = null;

        // Soap Body to post
        HttpPost postSoap = new HttpPost(serviceUrl); // url is service specific
        postSoap.addHeader("SOAPAction", soapActionHeader); // add the Header specific for this request
        HttpEntity entBody = null;
        HttpResponse resp = null; // stores raw response from fbox
        boolean exceptionOccurred = false;

        try {
            entBody = new StringEntity(soapToString(request), ContentType.create("text/xml", "UTF-8")); // add body
            postSoap.setEntity(entBody);
            resp = getHttpClient().execute(postSoap, getHttpClientContext());

            // Fetch content data
            StatusLine slResponse = resp.getStatusLine();
            HttpEntity heResponse = resp.getEntity();

            // Check for (auth-) error
            if (slResponse.getStatusCode() == 401) {
                LOGGER.error(
                        "Could not read response from FritzBox. Unauthorized! Check User/PW in config. Create user for tr064 requests");
                return null;
            }

            // Parse response into SOAP Message
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            response = documentBuilder.parse(heResponse.getContent());

        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Encoding not supported: {}", e.getMessage().toString());
            response = null;
            exceptionOccurred = true;
        } catch (ClientProtocolException e) {
            LOGGER.error("Client Protocol not supported: {}", e.getMessage().toString());
            response = null;
            exceptionOccurred = true;
        } catch (IOException e) {
            LOGGER.error("Cannot send/receive: {}", e.getMessage().toString());
            response = null;
            exceptionOccurred = true;
        } catch (UnsupportedOperationException e) {
            LOGGER.error("Operation unsupported: {}", e.getMessage().toString());
            response = null;
            exceptionOccurred = true;
        } catch (Exception e) {
            LOGGER.error("SOAP Error: {}", e.getMessage().toString());
            response = null;
            exceptionOccurred = true;
        } finally {
            // Make sure connection is released. If error occurred make sure to print in log
            if (exceptionOccurred) {
                LOGGER.error("Releasing connection to FritzBox because of error!");
            } else {
                LOGGER.debug("Releasing connection");
            }
            postSoap.releaseConnection();
        }

        return response;
    }

    /***
     * looks for the proper item mapping for the item command given from item file
     *
     * @param itemCommand String item command
     * @return found itemMap object if found, or null
     */
    private ItemMap determineItemMappingByItemCommand(String itemCommand) {
        ItemMap foundMapping = null;

        // iterate over all itemMappings to find proper mapping for requested item command
        Iterator<ItemMap> itMap = alItemMap.iterator();
        while (itMap.hasNext()) {
            ItemMap currentMap = itMap.next();
            if (itemCommand.equals(currentMap.getItemCommand())) {
                foundMapping = currentMap;
                break;
            }
        }
        if (foundMapping == null) {
            LOGGER.error("No mapping found for item command {}", itemCommand);
        }
        return foundMapping;
    }

    private Tr064Service determineServiceByItemMapping(ItemMap mapping) {
        Tr064Service foundService = null;

        // search which service matches the item mapping
        Iterator<Tr064Service> it = services.iterator();
        while (it.hasNext()) {
            Tr064Service currentService = it.next();
            if (currentService.getServiceId().contains(mapping.getServiceId())) {
                foundService = currentService;
                break;
            }
        }
        if (foundService == null) {
            LOGGER.warn("No tr064 service found for service id {}", mapping.getServiceId());
        }
        return foundService;
    }

    /***
     * Connects to fbox service xml to get a list of all services
     * which are offered by TR064. Saves it into local list
     */
    private void readAllServices() {
        Document xml = getFboxXmlResponse(getFritzboxUrl() + "/" + TR064_DOWNLOAD_FILENAME);
        if (xml == null) {
            LOGGER.error("Could not read xml response services");
            return;
        }
        NodeList nlServices = xml.getElementsByTagName("service"); // get all service nodes
        Node currentNode = null;
        XPath xPath = XPathFactory.newInstance().newXPath();
        for (int i = 0; i < nlServices.getLength(); i++) { // iterate over all services fbox offered us
            currentNode = nlServices.item(i);
            Tr064Service trS = new Tr064Service();
            try {
                trS.setControlUrl((String) xPath.evaluate("controlURL", currentNode, XPathConstants.STRING));
                trS.setEventSubUrl((String) xPath.evaluate("eventSubURL", currentNode, XPathConstants.STRING));
                trS.setScpdurl((String) xPath.evaluate("SCPDURL", currentNode, XPathConstants.STRING));
                trS.setServiceId((String) xPath.evaluate("serviceId", currentNode, XPathConstants.STRING));
                trS.setServiceType((String) xPath.evaluate("serviceType", currentNode, XPathConstants.STRING));
            } catch (XPathExpressionException e) {
                LOGGER.debug("Could not parse service {}", currentNode.getTextContent());
                e.printStackTrace();
            }
            services.add(trS);
        }
    }

    /***
     * populates local static mapping table
     * todo: refactore to read from config file later?
     * sets the parser based on the itemcommand -> soap value parser "svp" anonymous method
     * for each mapping
     *
     */
    private void generateItemMappings() {
        // services available from fbox. Needed for e.g. wifi select 5GHz/Guest Wifi
        if (services.isEmpty()) { // no services are known yet?
            readAllServices();
        }

        // Mac Online Checker
        ItemMap imMacOnline = new ItemMap("maconline", "GetSpecificHostEntry", "LanDeviceHosts-com:serviceId:Hosts1",
                "NewMACAddress", "NewActive");
        imMacOnline.setSoapValueParser(new SoapValueParser() {

            @Override
            public String parseValueFromSoapMessage(Document document, ItemMap mapping, String request) {
                LOGGER.debug("Parsing FritzBox response for maconline");
                String value = "";
                // maconline: if fault is present could also indicate not a fault but MAC is not known
                try {
                    Element element = document.getDocumentElement();
                    NodeList faults = element.getElementsByTagName("s:Fault");
                    if (faults.getLength() > 0) {
                        Element fault = (Element) faults.item(0);

                        NodeList details = element.getElementsByTagName("detail");

                        if (details.getLength() > 0) {
                            Element detail = (Element) details.item(0);

                            NodeList nlErrorCode = detail.getElementsByTagName("errorCode");
                            Node nErrorCode = nlErrorCode.item(0);
                            String errorCode = nErrorCode.getTextContent();
                            if (errorCode.equals("714")) {
                                value = "MAC not known to FritzBox!";
                                LOGGER.debug(value);
                            } else {
                                LOGGER.error("Error received from FritzBox: {}. SOAP request was: {}", soapToString(document),
                                        request);
                                value = "ERROR";
                            }
                        }
                    } else {
                        // parameter name to extract is taken from mapping
                        NodeList nlActive = element.getElementsByTagName(mapping.getReadDataOutName());
                        if (nlActive.getLength() > 0) {
                            Node nActive = nlActive.item(0);
                            value = nActive.getTextContent();
                            LOGGER.debug("parsed as {}", value);
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("Error parsing SOAP response from FritzBox: {}", e.getMessage());
                }
                return value;
            }
        });
        alItemMap.add(imMacOnline);

        alItemMap.add(new ItemMap("modelName", "GetInfo", "DeviceInfo-com:serviceId:DeviceInfo1", "", "NewModelName"));
        alItemMap.add(new ItemMap("wanip", "GetExternalIPAddress",
                "urn:WANPPPConnection-com:serviceId:WANPPPConnection1", "", "NewExternalIPAddress"));

        // Wifi 2,4GHz
        ItemMap imWifi24Switch = new ItemMap("wifi24Switch", "GetInfo",
                "urn:WLANConfiguration-com:serviceId:WLANConfiguration1", "", "NewEnable");
        imWifi24Switch.setWriteServiceCommand("SetEnable");
        imWifi24Switch.setWriteDataInName("NewEnable");
        alItemMap.add(imWifi24Switch);

        // wifi 5GHz
        ItemMap imWifi50Switch = new ItemMap("wifi50Switch", "GetInfo",
                "urn:WLANConfiguration-com:serviceId:WLANConfiguration2", "", "NewEnable");
        imWifi50Switch.setWriteServiceCommand("SetEnable");
        imWifi50Switch.setWriteDataInName("NewEnable");

        // guest wifi
        ItemMap imWifiGuestSwitch = new ItemMap("wifiGuestSwitch", "GetInfo",
                "urn:WLANConfiguration-com:serviceId:WLANConfiguration3", "", "NewEnable");
        imWifiGuestSwitch.setWriteServiceCommand("SetEnable");
        imWifiGuestSwitch.setWriteDataInName("NewEnable");

        // check if 5GHz wifi and/or guest wifi is available.
        Tr064Service svc5GHzWifi = determineServiceByItemMapping(imWifi50Switch);
        Tr064Service svcGuestWifi = determineServiceByItemMapping(imWifiGuestSwitch);

        if (svc5GHzWifi != null && svcGuestWifi != null) { // WLANConfiguration3+2 present -> guest wifi + 5Ghz present
            // prepared properly, only needs to be added
            alItemMap.add(imWifi50Switch);
            alItemMap.add(imWifiGuestSwitch);
            LOGGER.debug("Found 2,4 Ghz, 5Ghz and Guest Wifi");
        }

        if (svc5GHzWifi != null && svcGuestWifi == null) { // WLANConfiguration3 not present but 2 -> no 5Ghz Wifi
                                                           // available but Guest Wifi
            // remap itemMap for Guest Wifi from 3 to 2
            imWifiGuestSwitch.setServiceId("urn:WLANConfiguration-com:serviceId:WLANConfiguration2");
            alItemMap.add(imWifiGuestSwitch);// only add guest wifi, no 5Ghz
            LOGGER.debug("Found 2,4 Ghz and Guest Wifi");
        }
        if (svc5GHzWifi == null && svcGuestWifi == null) { // WLANConfiguration3+2 not present > no 5Ghz Wifi or Guest
                                                           // Wifi
            LOGGER.debug("Found 2,4 Ghz Wifi");
        }

        // Phonebook Download
        // itemcommand is dummy: not a real item
        ItemMap imPhonebook = new ItemMap("phonebook", "GetPhonebook",
                "urn:X_AVM-DE_OnTel-com:serviceId:X_AVM-DE_OnTel1", "NewPhonebookID", "NewPhonebookURL");
        alItemMap.add(imPhonebook);

        // TAM (telephone answering machine) Switch
        ItemMap imTamSwitch = new ItemMap("tamSwitch", "GetInfo", "urn:X_AVM-DE_TAM-com:serviceId:X_AVM-DE_TAM1",
                "NewIndex", "NewEnable");
        imTamSwitch.setWriteServiceCommand("SetEnable");
        imTamSwitch.setWriteDataInName("NewEnable");
        imTamSwitch.setWriteDataInNameAdditional("NewIndex"); // additional Parameter to set
        alItemMap.add(imTamSwitch);

        // New Messages per TAM ID
        // two requests needed: First gets URL to download tam info from, 2nd contains info of messages
        ItemMap imTamNewMessages = new ItemMap("tamNewMessages", "GetMessageList",
                "urn:X_AVM-DE_TAM-com:serviceId:X_AVM-DE_TAM1", "NewIndex", "NewURL");
        // SVP fetches desired infos
        imTamNewMessages.setSoapValueParser(new SoapValueParser() {

            @Override
            public String parseValueFromSoapMessage(Document document, ItemMap mapping, String request) {
                String value = "";
                LOGGER.debug("Parsing FritzBox response for TAM messages: {}", soapToString(document));
                try {
                    Element element = document.getDocumentElement();

                    NodeList faults = element.getElementsByTagName("s:Fault");
                    if (faults.getLength() > 0) {
                        Element fault = (Element) faults.item(0);

                        NodeList details = element.getElementsByTagName("detail");

                        if (details.getLength() > 0) {
                            LOGGER.error("Error received from fbox while parsing TAM message info: {}. ",
                                    soapToString(document));
                            value = "ERROR";
                        }
                    } else {
                        NodeList nlDataOutNodes = element.getElementsByTagName(mapping.getReadDataOutName()); // URL
                        if (nlDataOutNodes != null && nlDataOutNodes.getLength() > 0) {
                            // extract URL from soap response
                            String url = nlDataOutNodes.item(0).getTextContent();
                            Document xmlTamInfo = getFboxXmlResponse(url);
                            LOGGER.debug("Parsing xml message TAM info {}", Helper.documentToString(xmlTamInfo));
                            NodeList nlNews = xmlTamInfo.getElementsByTagName("New"); // get all Nodes containing "new",
                            // indicating message was not
                            // listened to

                            // When <new> contains 1 -> message is new, when 0, message not new -> Counting 1s
                            int newMessages = 0;
                            for (int i = 0; i < nlNews.getLength(); i++) {
                                if (nlNews.item(i).getTextContent().equals("1")) {
                                    newMessages++;
                                }
                            }
                            value = Integer.toString(newMessages);
                            LOGGER.debug("Parsed new messages as: {}", value);
                        } else {
                            LOGGER.error(
                                    "FritzBox returned unexpected response. Could not find expected datavalue {} in response {}",
                                    mapping.getReadDataOutName(), soapToString(document));
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("Error parsing SOAP response from FritzBox");
                    e.printStackTrace();
                }

                return value;
            }
        });

        alItemMap.add(imTamNewMessages);

        // Missed calls
        // two requests: 1st fetches URL to download call list, 2nd fetches xml call list
        ItemMap imMissedCalls = new ItemMap("missedCallsInDays", "GetCallList",
                "urn:X_AVM-DE_OnTel-com:serviceId:X_AVM-DE_OnTel1", "NewDays", "NewCallListURL");
        // svp for downloading call list from received URL
        imMissedCalls.setSoapValueParser(new SoapValueParser() {

            @Override
            public String parseValueFromSoapMessage(Document document, ItemMap mapping, String request) {
                String value = "";
                LOGGER.debug("Parsing FritzBox response for call list: {}", soapToString(document));

                // extract how many days of call list should be examined for missed calls
                String days = "3"; // default
                String[] itemConfig = request.split(":");
                if (itemConfig.length == 2) {
                    days = itemConfig[1]; // set the days as defined in item config. Otherwise default value of 3 is
                    // used
                }

                try {
                    Element element = document.getDocumentElement();

                    NodeList faults = element.getElementsByTagName("s:Fault");
                    if (faults.getLength() > 0) {
                        Element fault = (Element) faults.item(0);

                        NodeList details = element.getElementsByTagName("detail");

                        if (details.getLength() > 0) {

                            LOGGER.error("Error received from FritzBox while parsing call list: {}", soapToString(document));
                            value = "ERROR";
                        }
                    } else {
                        NodeList nlDataOutNodes = element.getElementsByTagName(mapping.getReadDataOutName()); // URL
                        if (nlDataOutNodes != null && nlDataOutNodes.getLength() > 0) {
                            // extract URL from soap response
                            String url = nlDataOutNodes.item(0).getTextContent();
                            // only get missed calls of the last x days
                            url = url + "&days=" + days;
                            LOGGER.debug("Downloading call list using url {}", url);
                            Document xmlTamInfo = getFboxXmlResponse(url); // download call list
                            LOGGER.debug("Parsing xml message call list info {}", Helper.documentToString(xmlTamInfo));
                            NodeList nlTypes = xmlTamInfo.getElementsByTagName("Type"); // get all Nodes containing
                            // "Type". Type 2 => missed

                            // When <type> contains 2 -> call was missed -> Counting only 2 entries
                            int missedCalls = 0;
                            for (int i = 0; i < nlTypes.getLength(); i++) {
                                if (nlTypes.item(i).getTextContent().equals("2")) {
                                    missedCalls++;
                                }
                            }
                            value = Integer.toString(missedCalls);
                            LOGGER.debug("Parsed new messages as: {}", value);
                        } else {
                            LOGGER.error(
                                    "FritzBox returned unexpected response. Could not find expected datavalue {} in response {}",
                                    mapping.getReadDataOutName(), soapToString(document));
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("Error parsing SOAP response from FritzBox: {}", e.getMessage());
                }

                return value;
            }
        });
        alItemMap.add(imMissedCalls);

    }

    /***
     * sets up a raw http(s) connection to Fbox and gets xml response
     * as XML Document, ready for parsing
     *
     * @return
     */
    public Document getFboxXmlResponse(String url) {
        Document tr064response = null;
        HttpGet httpGet = new HttpGet(url);
        boolean exceptionOccurred = false;
        try {
            CloseableHttpResponse resp = getHttpClient().execute(httpGet, getHttpClientContext());
            int responseCode = resp.getStatusLine().getStatusCode();
            if (responseCode == 200) {
                HttpEntity entity = resp.getEntity();
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                tr064response = db.parse(entity.getContent());
                EntityUtils.consume(entity);
            } else {
                LOGGER.error("Failed to receive valid response from httpGet");
            }

        } catch (Exception e) {
            exceptionOccurred = true;
            LOGGER.error("Failed to receive valid response from httpGet: {}", e.getMessage());
        } finally {
            // Make sure connection is released. If error occurred make sure to print in log
            if (exceptionOccurred) {
                LOGGER.error("Releasing connection to FritzBox because of error!");
            } else {
                LOGGER.debug("Releasing connection");
            }
            httpGet.releaseConnection();
        }
        return tr064response;
    }
}
