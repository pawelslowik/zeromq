package pl.com.psl.zeromq.jeromq.routerdealer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import pl.com.psl.zeromq.jeromq.Profiles;
import pl.com.psl.zeromq.jeromq.ZeroMQServer;

import java.util.concurrent.Executors;

/**
 * Created by psl on 17.04.17.
 *
 * ROUTER-DEALER pattern is similar to REQ-REP, but additionally it introduces a broker that works between the client and the server.
 * That way client knows only the broker, which receives the request, forwards it one of available servers, and when server delivers the response,
 * broker sends it back to the respective client. Client knows nothing about the server(s), broker hides all connectivity details.
 */
@Profile(Profiles.ROUTER_DEALER)
@Component
public class RouterDealerZeroMQServer extends ZeroMQServer {

    private static final int THREAD_POOL_SIZE = 3;

    @Autowired
    public RouterDealerZeroMQServer(ZContext zContext) {
        super(zContext, Executors.newFixedThreadPool(THREAD_POOL_SIZE));
    }

    @Override
    protected void startInternal() {
        for (int i = 0; i < THREAD_POOL_SIZE; i++) {
            executorService.submit(() -> {
                LOGGER.info("Creating and connecting REP socket on Dealer address={}...", RouterDealerZeroMQBroker.DEALER_ADDRESS);
                ZMQ.Socket socket = zContext.createSocket(ZMQ.REP);
                socket.connect(RouterDealerZeroMQBroker.DEALER_ADDRESS);
                while (!Thread.interrupted()) {
                    LOGGER.info("Listening for requests from Dealer address={}...", RouterDealerZeroMQBroker.DEALER_ADDRESS);
                    String request = socket.recvStr();
                    LOGGER.info("Received request={}", request);
                    String response = "Response from " + Thread.currentThread().getName() + " for request=" + request;
                    LOGGER.info("Sending back response={}", response);
                    socket.send(response);
                    LOGGER.info("Response sent!");
                }
            });
        }
    }
}
