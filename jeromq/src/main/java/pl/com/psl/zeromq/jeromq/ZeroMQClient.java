package pl.com.psl.zeromq.jeromq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by psl on 14.04.17.
 */
public abstract class ZeroMQClient {

    protected static final Logger LOGGER = LoggerFactory.getLogger(ZeroMQClient.class);
    protected ZContext zContext;
    protected ExecutorService executorService;

    public ZeroMQClient(ZContext zContext, ExecutorService executorService) {
        this.zContext = zContext;
        this.executorService = executorService;
    }

    protected void start(){
        LOGGER.info("Starting ZeroMQ client...");
        startInternal();
        LOGGER.info("ZeroMQ client started!");
    }

    protected void stop(){
        LOGGER.info("Stopping ZeroMQ client...");
        executorService.shutdownNow();
        boolean terminated = false;
        try {
            terminated = executorService.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOGGER.error("Waiting for client termination interrupted", e);
        }
        LOGGER.info("ZeroMQ client stopped={}", terminated);
    }

    protected abstract void startInternal();
}
