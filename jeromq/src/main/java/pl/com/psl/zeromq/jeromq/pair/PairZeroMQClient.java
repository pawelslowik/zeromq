package pl.com.psl.zeromq.jeromq.pair;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * Created by psl on 14.04.17.
 */
@Component
@Profile(Profiles.PAIR)
public class PairZeroMQClient extends ZeroMQClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(PairZeroMQClient.class);
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private AtomicInteger atomicInteger = new AtomicInteger();

    @Autowired
    public PairZeroMQClient(ZContext zContext) {
        super(zContext);
    }

    @Override
    protected void startInternal() {
        executorService.submit(() -> {
            LOGGER.info("Creating and connecting PAIR socket...");
            ZMQ.Socket socket = zContext.createSocket(ZMQ.PAIR);
            socket.connect(PairZeroMQServer.ADDRESS);
            while(!Thread.interrupted()){
                String request = "Hello " + atomicInteger.getAndIncrement();
                LOGGER.info("Sending a request={}", request);
                socket.send(request);
                String response = socket.recvStr();
                LOGGER.info("Received response={}", response);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    LOGGER.error("Interrupted while sleeping", e);
                    return;
                }
            }
        });
    }

    @Override
    protected void stopInternal() {
        executorService.shutdown();
    }
}
