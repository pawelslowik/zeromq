package pl.com.psl.zeromq.jeromq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by psl on 14.04.17.
 */
public abstract class ZeroMQServer {

    protected static final Logger LOGGER = LoggerFactory.getLogger(ZeroMQServer.class);
    protected ZContext zContext;
    protected ExecutorService executorService;

    public ZeroMQServer(ZContext zContext, ExecutorService executorService) {
        this.zContext = zContext;
        this.executorService = executorService;
    }

    protected void start(){
        LOGGER.info("Starting ZeroMQ server...");
        startInternal();
        LOGGER.info("ZeroMQ server started!");
    }

    protected void stop(){
        LOGGER.info("Stopping ZeroMQ server...");
        executorService.shutdownNow();
        boolean terminated = false;
        try {
            terminated = executorService.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOGGER.error("Waiting for server termination interrupted", e);
        }
        LOGGER.info("ZeroMQ server stopped={}", terminated);
    }

    protected abstract void startInternal();
}
