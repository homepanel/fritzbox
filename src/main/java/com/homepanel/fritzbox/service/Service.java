package com.homepanel.fritzbox.service;

import com.homepanel.fritzbox.config.Config;
import com.homepanel.core.config.ConfigTopic;
import com.homepanel.core.executor.PriorityThreadPoolExecutor;
import com.homepanel.core.service.PollingService;
import com.homepanel.core.state.Type;
import com.homepanel.fritzbox.config.Topic;
import com.homepanel.fritzbox.fritzbox.client.Tr064Client;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Service extends PollingService<Config, Topic> {

    private final static Logger LOGGER = LoggerFactory.getLogger(Service.class);

    private Tr064Client tr064Client;

    private Tr064Client getTr064Client() {
        return tr064Client;
    }

    private void setTr064Client(Tr064Client tr064Client) {
        this.tr064Client = tr064Client;
    }

    @Override
    public Config getConfig() {
        return (Config) super.getConfig();
    }

    @Override
    protected void startService() throws Exception {
        if (getConfig().getFritzbox() != null) {
            if (!StringUtils.isEmpty(getConfig().getFritzbox().getHost()) && !StringUtils.isEmpty(getConfig().getFritzbox().getUsername()) && !StringUtils.isEmpty(getConfig().getFritzbox().getPassword()) && getConfig().getFritzbox().getSsl() != null) {
                setTr064Client(new Tr064Client(getConfig().getFritzbox().getHost(), getConfig().getFritzbox().getPort() != null && !getConfig().getFritzbox().getPort().equals(0) ? getConfig().getFritzbox().getPort() : null, getConfig().getFritzbox().getUsername(), getConfig().getFritzbox().getPassword(), getConfig().getFritzbox().getSsl()));
            }


            for (Topic topic : getConfig().getTopics()) {
                Type type = null;

                if (topic.getGroup() != null && topic.getChannel() != null) {
                    switch (topic.getGroup()) {
                        case COMMON:
                            switch (topic.getChannel()) {
                                case MAC_ADDRESS_ONLINE:
                                    type = ConfigTopic.getType(Type.NAME.CONNECTION);
                                    break;
                            }
                            break;
                    }
                }

                topic.setType(type);
            }
        }
    }

    @Override
    protected void shutdownService() throws Exception {
    }

    @Override
    protected void onInit() {

        try {
            for (Topic topic : getConfig().getTopics()) {

                /*OwfsReadCallable owfsReadCallable = new OwfsReadCallable(PriorityThreadPoolExecutor.PRIORITY.LOWEST, getOwfsClient(), topic.getId(), topic.getProperty());
                Future<String> future = getExecutorService().submit(owfsReadCallable);
                String value = future.get();
                owfsReadCallable = null;

                writeData(topic, value);*/
            }
        } catch (Exception e) {
            LOGGER.error("error reading directory listing from ow server", e);
        }
    }

    @Override
    protected Integer getPollingExecutorServicePoolSize() {
        return 1;
    }

    @Override
    public String getTopicNameByTopic(Topic topic) {
        return getTopicNameByParameter(topic.getGroup(), topic.getChannel());
    }

    @Override
    public void pollData(Topic topic, Long jobRunningTimeInMilliseconds, Long refreshIntervalInMilliseconds) {

        try {
            if (getTr064Client() != null) {
                if (topic.getGroup() != null && topic.getChannel() != null) {
                    String request = null;

                    switch (topic.getGroup()) {
                        case COMMON:
                            switch (topic.getChannel()) {
                                case MAC_ADDRESS_ONLINE:
                                    if (topic.getMacAddress() != null) {
                                        request = topic.getChannel().getKey() + ":" + topic.getMacAddress().replace(":" , "-");
                                    }
                                    break;
                            }
                            break;
                    }

                    if (request != null) {
                        Object value = getTr064Client().getTr064Value(request);

                        publishData(topic, value);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("error reading data from ow server", e);
        }
    }

    @Override
    protected void onData(Topic topic, Object value, PriorityThreadPoolExecutor.PRIORITY priority) {

        /*
        logger.trace("internalReceiveCommand({},{}) is called!", itemName, command);
        if (_fboxComm == null) {
            _fboxComm = new Tr064Comm(_url, _user, _pw);
        }
        // Search Item Binding config for this itemName
        for (FritzboxTr064BindingProvider provider : providers) {
            FritzboxTr064BindingConfig conf = provider.getBindingConfigByItemName(itemName);
            if (conf != null) {
                _fboxComm.setTr064Value(conf.getConfigString(), command); // pass config String because config string
                // needed for finding item map
            }
        }*/
        try {
            /*OwfsWriteCallable owfsWriteCallable = new OwfsWriteCallable(priority, getOwfsClient(), topic.getId(), topic.getProperty(), value.toString());
            Future<Void> future = getExecutorService().submit(owfsWriteCallable);
            future.get();
            owfsWriteCallable = null;

            writeData(topic, value);*/

        } catch (Exception e) {
            LOGGER.error("error writing data to ow server", e);
        }
    }

    @Override
    protected void updateData(Topic topic) {
        // unused
    }

    public static void main(String[] arguments) throws Exception {
        new Service().start(arguments, Config.class);
    }
}