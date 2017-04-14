package pl.com.psl.zeromq.jeromq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;

/**
 * Created by psl on 14.04.17.
 */
public abstract class ZeroMQClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZeroMQClient.class);
    protected ZContext zContext;

    public ZeroMQClient(ZContext zContext) {
        this.zContext = zContext;
    }

    void start(){
        LOGGER.info("Starting ZeroMQ client...");
        startInternal();
        LOGGER.info("ZeroMQ client started!");
    }

    void stop(){
        LOGGER.info("Stopping ZeroMQ client...");
        stopInternal();
        LOGGER.info("ZeroMQ client stopped!");
    }

    protected abstract void startInternal();
    protected abstract void stopInternal();
}
