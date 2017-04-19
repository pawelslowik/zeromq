package pl.com.psl.zeromq.jeromq.routerdealer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import pl.com.psl.zeromq.jeromq.Profiles;
import pl.com.psl.zeromq.jeromq.ZeroMQClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by psl on 17.04.17.
 */
@Profile(Profiles.ROUTER_DEALER)
@Component
public class RouterDealerZeroMQClient extends ZeroMQClient {

    private int THREAD_POOL_SIZE = 2;
    private ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    private AtomicInteger atomicInteger = new AtomicInteger();
    private RouterDealerZeroMQBroker broker;

    @Autowired
    public RouterDealerZeroMQClient(ZContext zContext, RouterDealerZeroMQBroker broker) {
        super(zContext);
        this.broker = broker;
    }

    @Override
    protected void startInternal() {
        broker.start();
        try {
            LOGGER.info("Putting client to sleep to let broker start...");
            Thread.sleep(3000);
            LOGGER.info("Client sleep finished, starting client threads");
        } catch (InterruptedException e) {
            LOGGER.error("Client sleep interrupted!", e);
            return;
        }
        for (int i = 0; i < THREAD_POOL_SIZE; i++) {
            executorService.submit(() -> {
                LOGGER.info("Creating and connecting REQ socket on Router address={}...", RouterDealerZeroMQBroker.ROUTER_ADDRESS);
                ZMQ.Socket routerSocket = zContext.createSocket(ZMQ.REQ);
                routerSocket.connect(RouterDealerZeroMQBroker.ROUTER_ADDRESS);
                while (!Thread.interrupted()) {
                    String request = "Hello " + atomicInteger.getAndIncrement() + " from " + Thread.currentThread().getName();
                    LOGGER.info("Sending a request={}", request);
                    routerSocket.send(request);
                    LOGGER.info("Listening for response...");
                    String response = routerSocket.recvStr();
                    LOGGER.info("Received response={}", response);
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        LOGGER.error("Interrupted while sleeping", e);
                        return;
                    }
                }
            });
        }
    }

    @Override
    protected void stopInternal() {
        broker.stop();
        executorService.shutdown();
    }
}
