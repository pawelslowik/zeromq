package pl.com.psl.zeromq.jeromq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;

/**
 * Created by psl on 14.04.17.
 */
public abstract class ZeroMQServer {

    protected static final Logger LOGGER = LoggerFactory.getLogger(ZeroMQServer.class);
    protected ZContext zContext;

    public ZeroMQServer(ZContext zContext) {
        this.zContext = zContext;
    }

    void start(){
        LOGGER.info("Starting ZeroMQ server...");
        startInternal();
        LOGGER.info("ZeroMQ server started!");
    }

    void stop(){
        LOGGER.info("Stopping ZeroMQ server...");
        stopInternal();
        zContext.close();
        LOGGER.info("ZeroMQ server stopped!");
    }

    protected abstract void startInternal();
    protected abstract void stopInternal();
}
