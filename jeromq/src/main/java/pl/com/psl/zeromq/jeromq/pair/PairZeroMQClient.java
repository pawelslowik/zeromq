package pl.com.psl.zeromq.jeromq.pair;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import pl.com.psl.zeromq.jeromq.Profiles;
import pl.com.psl.zeromq.jeromq.ZeroMQClient;

import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by psl on 14.04.17.
 *
 * PAIR pattern allows client to connect directly to server.
 * After sending request, client doesn't have to wait for response,
 * it can send consecutive requests and after that, get all responses.
 * There can be only one connected peer.
 */
@Component
@Profile(Profiles.PAIR)
public class PairZeroMQClient extends ZeroMQClient {

    private AtomicInteger atomicInteger = new AtomicInteger();

    @Autowired
    public PairZeroMQClient(ZContext zContext) {
        super(zContext, Executors.newSingleThreadExecutor());
    }

    @Override
    protected void startInternal() {
        executorService.submit(() -> {
            LOGGER.info("Creating and connecting PAIR socket...");
            ZMQ.Socket socket = zContext.createSocket(ZMQ.PAIR);
            socket.connect(PairZeroMQServer.ADDRESS);
            while (!Thread.interrupted()) {
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
}
