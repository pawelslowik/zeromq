package pl.com.psl.zeromq.jeromq.reqrep;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import pl.com.psl.zeromq.jeromq.Profiles;
import pl.com.psl.zeromq.jeromq.ZeroMQServer;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Created by psl on 14.04.17.
 */
@Profile(Profiles.REQ_REP)
@Component
public class ReqRepZeroMQServer extends ZeroMQServer {

    static final List<String> ADDRESSES = Arrays.asList("tcp://localhost:8071", "tcp://localhost:8072", "tcp://localhost:8073");

    @Autowired
    public ReqRepZeroMQServer(ZContext zContext) {
        super(zContext, Executors.newFixedThreadPool(ADDRESSES.size()));
    }

    @Override
    protected void startInternal() {
        ADDRESSES.stream().forEach(address -> {
            executorService.submit(() -> {
                LOGGER.info("Creating and binding REP socket with address={}", address);
                ZMQ.Socket socket = zContext.createSocket(ZMQ.REP);
                socket.bind(address);
                while (!Thread.interrupted()) {
                    LOGGER.info("Listening for requests on address={}...", address);
                    String request = socket.recvStr();
                    LOGGER.info("Received request={} on address={}", request, address);
                    String response = "Response for request=" + request + " from address=" + address;
                    LOGGER.info("Sending back response={}", response);
                    socket.send(response);
                    LOGGER.info("Response sent!");
                }
            });
        });
    }
}
