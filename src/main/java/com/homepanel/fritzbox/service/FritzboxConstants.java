package com.homepanel.fritzbox.service;

public class FritzboxConstants {

    public enum GROUP {
        COMMON,
        NETWORK,
        DSL,
        CALLMONITOR
    }

    public enum CHANNEL {
        MODEL_NAME("modelName"),
        MANUFACTURER_NAME("manufacturerName"),
        SERIAL_NUMBER("serialNumber"),
        SOFTWARE_VERSION("softwareVersion"),
        UP_TIME("upTime"),
        WAN_IP("wanip"),
        EXTERNAL_WAN_IP("externalWanip"),
        WIFI_24("wifi24Switch"),
        WIFI_50("wifi50Switch"),
        WIFI_GUEST("wifiGuestSwitch"),
        MAC_ADDRESS_ONLINE("maconline"),
        REBOOT("reboot");

/*
// WAN statistics

        String  fboxWanAccessType "FBox WAN access type [%s]" {fritzboxtr064="wanWANAccessType"}
        Number  fboxWanLayer1UpstreamMaxBitRate "FBox WAN us max bit rate [%s]" {fritzboxtr064="wanLayer1UpstreamMaxBitRate"}
        Number  fboxWanLayer1DownstreamMaxBitRate "FBox WAN ds max bit rate [%s]" {fritzboxtr064="wanLayer1DownstreamMaxBitRate"}
        String  fboxWanPhysicalLinkStatus "FBox WAN physical link status [%s]" {fritzboxtr064="wanPhysicalLinkStatus"}
        Number  fboxWanTotalBytesSent "WAN total bytes sent [%s]" {fritzboxtr064="wanTotalBytesSent"}
        Number  fboxWanTotalBytesReceived "WAN total bytes received [%s]" {fritzboxtr064="wanTotalBytesReceived"}

// DSL statistics

        Contact fboxDslEnable       "FBox DSL Enable [%s]"      {fritzboxtr064="dslEnable"}
        String  fboxDslStatus       "FBox DSL Status [%s]"      {fritzboxtr064="dslStatus"}
        Number  fboxDslUpstreamCurrRate "DSL Upstream Current [%s mbit/s]" {fritzboxtr064="dslUpstreamCurrRate"}
        Number  fboxDslDownstreamCurrRate "DSL Downstream Current [%s mbit/s]" {fritzboxtr064="dslDownstreamCurrRate"}
        Number  fboxDslUpstreamMaxRate "DSL Upstream Max [%s mbit/s]" {fritzboxtr064="dslUpstreamMaxRate"}
        Number  fboxDslDownstreamMaxRate "DSL Downstream Max [%s mbit/s]" {fritzboxtr064="dslDownstreamMaxRate"}
        Number  fboxDslUpstreamNoiseMargin "DSL Upstream Noise Margin [%s dB*10]" {fritzboxtr064="dslUpstreamNoiseMargin"}
        Number  fboxDslDownstreamNoiseMargin "DSL Downstream Noise Margin [%s dB*10]" {fritzboxtr064="dslDownstreamNoiseMargin"}
        Number  fboxDslUpstreamAttenuation "DSL Upstream Attenuation [%s dB*10]" {fritzboxtr064="dslUpstreamAttenuation"}
        Number  fboxDslDownstreamAttenuation "DSL Downstream Attenuation [%s dB*10]" {fritzboxtr064="dslDownstreamAttenuation"}
        Number  fboxDslFECErrors "DSL FEC Errors [%s]" {fritzboxtr064="dslFECErrors"}
        Number  fboxDslHECErrors "DSL HEC Errors [%s]" {fritzboxtr064="dslHECErrors"}
        Number  fboxDslCRCErrors "DSL CRC Errors [%s]" {fritzboxtr064="dslCRCErrors"}

        // only when using call monitor
        Switch  fboxRinging         "Phone ringing [%s]"                {fritzboxtr064="callmonitor_ringing" }
        Switch  fboxRinging_Out     "Phone ringing [%s]"                {fritzboxtr064="callmonitor_outgoing" }
        Switch  fboxCallConnecting  "Call established [%s]"             {fritzboxtr064="callmonitor_active" }
        Call    fboxIncomingCall    "Incoming call: [%1$s to %2$s]"     {fritzboxtr064="callmonitor_ringing" }
        Call    fboxOutgoingCall    "Outgoing call: [%1$s to %2$s]"     {fritzboxtr064="callmonitor_outgoing" }
        Call    fboxConnectedCall   "Call established [%1$s to %2$s]"   {fritzboxtr064="callmonitor_active" }

        // resolve numbers to names based on phonebook
        Call    fboxIncomingCallResolved    "Incoming call: [%1$s to %2$s]"     {fritzboxtr064="callmonitor_ringing:resolveName" }

        // Telephone answering machine (TAM) items
// Number after tamSwitch is ID of configured TAM, start with 0
        Switch  fboxTAM0Switch   "Answering machine ID 0"       {fritzboxtr064="tamSwitch:0"}
        Number  fboxTAM0NewMsg   "New Messages TAM 0 [%s]"      {fritzboxtr064="tamNewMessages:0"}

        // Missed calls: specify the number of last days which should be searched for missed calls
        Number  fboxMissedCalls  "Missed Calls [%s]"            {fritzboxtr064="missedCallsInDays:5"}

        // Call deflection items
// Number after callDeflectionSwitch is ID of configured call deflection
// The ID count includes the entries from the "Call Blocks" page.
// If you have no "Call Blocks", the first entry on the "Call Diversions" page has ID 0.
// If you have 3 "Call Blocks", the first entry on the "Call Diversions" page has ID 3.
        Switch  fboxCD0Switch    "Call Deflection ID 0"         {fritzboxtr064="callDeflectionSwitch:0"}
        */
        private String key;

        public String getKey() {
            return key;
        }

        private void setKey(String key) {
            this.key = key;
        }

        CHANNEL(String key) {
            setKey(key);
        }
    }
}