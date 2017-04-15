package pl.com.psl.zeromq.jeromq.pubsub;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import pl.com.psl.zeromq.jeromq.Profiles;
import pl.com.psl.zeromq.jeromq.ZeroMQClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by psl on 15.04.17.
 *
 * PUB-SUB pattern allows client (subscriber) to connect to multiple servers (publishers)
 * and filter out only messages with given prefix (messages from server beginning with prefix).
 * Also, multiple clients can subscribe messages from single server.
 */
@Profile(Profiles.PUB_SUB)
@Component
public class PubSubZeroMQClient extends ZeroMQClient {

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Autowired
    public PubSubZeroMQClient(ZContext zContext) {
        super(zContext);
    }

    @Override
    protected void startInternal() {
        executorService.submit(() -> {
            LOGGER.info("Creating and connecting SUB socket...");
            ZMQ.Socket socket = zContext.createSocket(ZMQ.SUB);
            PubSubZeroMQServer.ADDRESSES.forEach(socket::connect);
            socket.subscribe(PubSubZeroMQServer.PREFIX_B.getBytes());
            while (!Thread.interrupted()){
                LOGGER.info("Listening for messages...");
                String message = socket.recvStr();
                LOGGER.info("Received message={}", message);
            }
        });
    }

    @Override
    protected void stopInternal() {
        executorService.shutdown();
    }
}
