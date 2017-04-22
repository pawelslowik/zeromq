package pl.com.psl.zeromq.jeromq.pushpull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import pl.com.psl.zeromq.jeromq.Profiles;
import pl.com.psl.zeromq.jeromq.ZeroMQClient;

import java.util.concurrent.Executors;
import java.util.stream.IntStream;

/**
 * Created by psl on 22.04.17.
 *
 * PUSH-PULL pattern provides a processing pipeline, in which ventilator sends requests evenly to all connected workers
 * (working in parallel), but responses from workers are not sent back to ventilator, but rather to the sink
 * - all messages go through sockets in one direction - down the pipeline.
 * All workers have to be connected to PUSH/PULL sockets before ventilator sends first request, otherwise one of workers might
 * get all requests.
 * In this example, to keep the code structure simple, server is the ventilator, client threads are workers,
 * and the role of sink is played by a separate thread in server, listening on different socket than ventilator.
 */
@Profile(Profiles.PUSH_PULL)
@Component
public class PushPullZeroMQClient extends ZeroMQClient {

    private static final int THREAD_POOL_SIZE = 5;

    @Autowired
    public PushPullZeroMQClient(ZContext zContext) {
        super(zContext, Executors.newFixedThreadPool(THREAD_POOL_SIZE));
    }

    @Override
    protected void startInternal() {
        IntStream.rangeClosed(1, THREAD_POOL_SIZE).forEach(i -> executorService.submit(() -> {
            LOGGER.info("Creating and connecting PULL socket...");
            ZMQ.Socket pullSocket = zContext.createSocket(ZMQ.PULL);
            pullSocket.connect(PushPullZeroMQServer.PUSH_ADDRESS);

            LOGGER.info("Creating and connecting PUSH socket...");
            ZMQ.Socket pushSocket = zContext.createSocket(ZMQ.PUSH);
            pushSocket.connect(PushPullZeroMQServer.PULL_ADDRESS);

            while (!Thread.interrupted()) {
                String request = pullSocket.recvStr();
                LOGGER.info("Received request={}", request);
                try {
                    LOGGER.info("Processing request...");
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    LOGGER.error("Interrupted while sleeping", e);
                    return;
                }
                String response = "Response from client " + i + " (" + Thread.currentThread().getName()
                        + ") for request=" + request;
                LOGGER.info("Sending back response={}", response);
                pushSocket.send(response);
                LOGGER.info("Response sent!");
            }
        }));
    }
}
