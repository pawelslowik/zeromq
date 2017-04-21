package pl.com.psl.zeromq.jeromq.routerdealer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import pl.com.psl.zeromq.jeromq.Profiles;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by psl on 17.04.17.
 */
@Profile(Profiles.ROUTER_DEALER)
@Component
public class RouterDealerZeroMQBroker {

    private static final Logger LOGGER = LoggerFactory.getLogger(RouterDealerZeroMQBroker.class);
    static final String ROUTER_ADDRESS = "tcp://localhost:8050";
    static final String DEALER_ADDRESS = "tcp://localhost:8051";
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private ZContext zContext;

    @Autowired
    public RouterDealerZeroMQBroker(ZContext zContext) {
        this.zContext = zContext;
    }

    protected void start() {
        executorService.submit(() -> {
            LOGGER.info("Creating and binding ROUTER socket with address={}", ROUTER_ADDRESS);
            ZMQ.Socket routerSocket = zContext.createSocket(ZMQ.ROUTER);
            routerSocket.bind(ROUTER_ADDRESS);

            LOGGER.info("Creating and binding DEALER socket with address={}", DEALER_ADDRESS);
            ZMQ.Socket dealerSocket = zContext.createSocket(ZMQ.DEALER);
            dealerSocket.bind(DEALER_ADDRESS);

            ZMQ.proxy(routerSocket, dealerSocket, null);
        });
    }

    protected void stop() {
        LOGGER.info("Stopping ZeroMQ broker...");
        executorService.shutdownNow();
        boolean terminated = false;
        try {
            terminated = executorService.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOGGER.error("Waiting for broker termination interrupted", e);
        }
        LOGGER.info("ZeroMQ broker stopped={}", terminated);
    }
}
