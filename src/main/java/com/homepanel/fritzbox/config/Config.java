package com.homepanel.fritzbox.config;

import com.homepanel.fritzbox.type.Connection;
import com.homepanel.fritzbox.type.Switch;

import jakarta.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement(name = "config")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class Config extends com.homepanel.core.config.ConfigTopic<Topic> {

    static {
        addTypes(new Switch(), new Connection());
    }

    private Fritzbox fritzbox;
    private List<Topic> topics;

    public Fritzbox getFritzbox() {
        return fritzbox;
    }

    private void setFritzbox(Fritzbox fritzbox) {
        this.fritzbox = fritzbox;
    }

    @XmlElementWrapper(name = "topics")
    @XmlElement(name = "topic")
    public List<Topic> getTopics() {
        return topics;
    }

    @Override
    public void setTopics(List<Topic> topics) {
        this.topics = topics;
    }
}