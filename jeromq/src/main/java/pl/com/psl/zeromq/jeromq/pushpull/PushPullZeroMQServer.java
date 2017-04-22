package pl.com.psl.zeromq.jeromq.pushpull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import pl.com.psl.zeromq.jeromq.Profiles;
import pl.com.psl.zeromq.jeromq.ZeroMQServer;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

/**
 * Created by psl on 22.04.17.
 */
@Profile(Profiles.PUSH_PULL)
@Component
public class PushPullZeroMQServer extends ZeroMQServer {

    static final String PUSH_ADDRESS = "tcp://localhost:8040";
    static final String PULL_ADDRESS = "tcp://localhost:8041";
    private static final int NUMBER_OF_REQUESTS = 10;
    private Instant startInstant;

    @Autowired
    public PushPullZeroMQServer(ZContext zContext) {
        super(zContext, Executors.newFixedThreadPool(2));
    }

    @Override
    protected void startInternal() {
        startPushThread();
        startPullThread();
    }

    private void startPushThread() {
        executorService.submit(() -> {
            LOGGER.info("Creating and binding PUSH socket...");
            ZMQ.Socket pushSocket = zContext.createSocket(ZMQ.PUSH);
            pushSocket.bind(PUSH_ADDRESS);

            try {
                LOGGER.info("Putting server to sleep to let clients start and connect...");
                Thread.sleep(3000);
                LOGGER.info("Server sleep finished, starting to send requests...");
            } catch (InterruptedException e) {
                LOGGER.error("Server sleep interrupted!", e);
                return;
            }

            startInstant = Instant.now();
            IntStream.rangeClosed(1, NUMBER_OF_REQUESTS).forEach(i -> {
                String request = "Hello " + i;
                LOGGER.info("Sending a request={}", request);
                pushSocket.send(request);
            });
        });
    }

    private void startPullThread() {
        executorService.submit(() -> {
            int responseCounter = 0;
            Instant latestResponseInstant;
            LOGGER.info("Creating and binding PULL socket...");
            ZMQ.Socket pullSocket = zContext.createSocket(ZMQ.PULL);
            pullSocket.bind(PULL_ADDRESS);
            while (!Thread.interrupted()) {
                String response = pullSocket.recvStr();
                latestResponseInstant = Instant.now();
                responseCounter++;
                LOGGER.info("Received response={}", response);
                if (NUMBER_OF_REQUESTS == responseCounter) {
                    LOGGER.info("Received all responses in {} seconds", Duration.between(startInstant, latestResponseInstant).getSeconds());
                }
            }
        });
    }
}
