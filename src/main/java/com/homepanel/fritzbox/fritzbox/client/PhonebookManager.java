/**
 * Copyright (c) 2010-2016, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.homepanel.fritzbox.fritzbox.client;

import java.util.ArrayList;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/***
 * Class managing all Phonebook related work
 *
 * @author gitbock
 * @since 1.8.0
 *
 */
public class PhonebookManager {

    private final static Logger logger = LoggerFactory.getLogger(PhonebookManager.class);

    private Tr064Client _tr064Client = null;
    private ArrayList<PhoneBookEntry> phoneBookEntries = null;

    private Tr064Client get_tr064Client() {
        return _tr064Client;
    }

    private void set_tr064Client(Tr064Client _tr064Client) {
        this._tr064Client = _tr064Client;
    }

    private ArrayList<PhoneBookEntry> getPhoneBookEntries() {
        return phoneBookEntries;
    }

    private void setPhoneBookEntries(ArrayList<PhoneBookEntry> phoneBookEntries) {
        this.phoneBookEntries = phoneBookEntries;
    }

    public PhonebookManager(Tr064Client tr064Client) {
        set_tr064Client(tr064Client);
        setPhoneBookEntries(new ArrayList<>());

    }

    /***
     * Looks up name in phone book entries and returns name and type if found
     * 
     * @param number number to look up name for
     * @param compareCount how many characters must match to accept a match
     * @return found name or null
     */
    public String getNameFromNumber(String number, int compareCount) {
        logger.info("Trying to resolve number {} to name comparing {} characters", number, compareCount);
        String name = null;
        Iterator<PhoneBookEntry> it = phoneBookEntries.iterator();
        while (it.hasNext()) {
            PhoneBookEntry pbe = it.next();
            StringBuilder sbAskNumber = new StringBuilder(number);
            sbAskNumber.reverse(); // to be able to compare numbers from the end
            String numberToCompare = "";

            // WORK number
            StringBuilder sbPhonebookNumber = new StringBuilder(pbe.getBusinessTel());
            sbPhonebookNumber.reverse();
            // check if comparing numbers are within entire string range
            if (compareCount <= sbAskNumber.length()) {
                numberToCompare = sbAskNumber.substring(0, compareCount);
            } else {
                numberToCompare = sbAskNumber.substring(0, sbAskNumber.length());
            }
            if (sbPhonebookNumber.toString().startsWith(numberToCompare)) {
                logger.info("found name match {} in phonebook by comparing {} with {} ", pbe.getName(),
                        sbPhonebookNumber.toString(), numberToCompare);
                name = pbe.getName() + " (Work)";
                break; // no need to cycle through rest of phonebook
            }

            // HOME number
            sbPhonebookNumber = new StringBuilder(pbe.getPrivateTel());
            sbPhonebookNumber.reverse();
            // check if comparing numbers are within entire string range
            if (compareCount <= sbAskNumber.length()) {
                numberToCompare = sbAskNumber.substring(0, compareCount);
            } else {
                numberToCompare = sbAskNumber.substring(0, sbAskNumber.length());
            }
            if (sbPhonebookNumber.toString().startsWith(numberToCompare)) {
                logger.info("found name match {} in phonebook by comparing {} with {} ", pbe.getName(),
                        sbPhonebookNumber.toString(), numberToCompare);
                name = pbe.getName() + " (Home)";
                break; // no need to cycle through rest of phonebook
            }

            // MOBILE number
            sbPhonebookNumber = new StringBuilder(pbe.getMobileTel());
            sbPhonebookNumber.reverse();
            // check if comparing numbers are within entire string range
            if (compareCount <= sbAskNumber.length()) {
                numberToCompare = sbAskNumber.substring(0, compareCount);
            } else {
                numberToCompare = sbAskNumber.substring(0, sbAskNumber.length());
            }
            if (sbPhonebookNumber.toString().startsWith(numberToCompare)) {
                logger.info("found name match {} in phonebook by comparing {} with {} ", pbe.getName(),
                        sbPhonebookNumber.toString(), numberToCompare);
                name = pbe.getName() + " (Mobile)";
                break; // no need to cycle through rest of phonebook
            }
        }

        return name;
    }

    /***
     * 
     * @param id Phonebook ID to download, can be determined using TR064 GetPhonebookList
     * @return XML Document downloaded
     */
    public Document downloadPhonebook(int id) {
        logger.info("Downloading phonebook ID {}", id);
        String phoneBookUrl = _tr064Client.getTr064Value("phonebook:" + id);
        Document phoneBook = _tr064Client.getFboxXmlResponse(phoneBookUrl);
        logger.debug("Downloaded Phonebook:");
        logger.trace(Helper.documentToString(phoneBook));

        return phoneBook;

    }

    /***
     * Downloads and parses phonebooks from fbox
     */
    public void downloadPhonebooks() {
        Document pb = downloadPhonebook(0);
        if (pb != null) {
            NodeList nlContacts = pb.getElementsByTagName("contact");
            for (int i = 0; i < nlContacts.getLength(); i++) {
                PhoneBookEntry pbe = new PhoneBookEntry();
                Node nContact = nlContacts.item(i);
                if (pbe.parseFromNode(nContact)) {
                    phoneBookEntries.add(pbe);
                } else {
                    logger.warn("could not parse phonebook entry: {}", nContact.toString());
                }
            }
        } else {
            logger.error("Could not download phonebook");
        }

    }

}
