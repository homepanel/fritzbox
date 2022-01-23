package com.homepanel.fritzbox.type;

import com.homepanel.core.state.State;
import com.homepanel.core.type.DefaultConnection;
import com.homepanel.core.type.DefaultSwitch;

public class Connection extends DefaultConnection {

    public Connection() {
        super(
                new State("1", CONNECTION.ONLINE.name()),
                new State("0", CONNECTION.OFFLINE.name())
        );
    }
}