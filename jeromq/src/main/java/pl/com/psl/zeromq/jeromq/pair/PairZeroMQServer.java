package pl.com.psl.zeromq.jeromq.pair;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import pl.com.psl.zeromq.jeromq.Profiles;
import pl.com.psl.zeromq.jeromq.ZeroMQServer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by psl on 14.04.17.
 */
@Component
@Profile(Profiles.PAIR)
public class PairZeroMQServer extends ZeroMQServer {

    static final String ADDRESS = "tcp://localhost:8090";
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Autowired
    public PairZeroMQServer(ZContext zContext) {
        super(zContext);
    }

    @Override
    protected void startInternal() {
        executorService.submit(() -> {
            LOGGER.info("Creating and binding PAIR socket...");
            ZMQ.Socket socket = zContext.createSocket(ZMQ.PAIR);
            socket.bind(ADDRESS);
            while(!Thread.interrupted()){
                LOGGER.info("Listening for requests...");
                String request = socket.recvStr();
                LOGGER.info("Received request={}", request);
                String response = "Response for request=" + request;
                LOGGER.info("Sending back response={}", response);
                socket.send(response);
                LOGGER.info("Response sent!");
            }
        });
    }

    @Override
    protected void stopInternal() {
        executorService.shutdown();
    }
}
