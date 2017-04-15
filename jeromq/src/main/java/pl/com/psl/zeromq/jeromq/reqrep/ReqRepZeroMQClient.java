package pl.com.psl.zeromq.jeromq.reqrep;

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
 *
 * REQ-REP pattern allows any number of clients be connected to any number of servers.
 * Requests from clients are distributed among servers, client blocks until getting a response
 * and server blocks until getting a request, requests and responses are automatically paired.
 */
@Profile(Profiles.REQ_REP)
@Component
public class ReqRepZeroMQClient extends ZeroMQClient {

    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private AtomicInteger atomicInteger = new AtomicInteger();

    @Autowired
    public ReqRepZeroMQClient(ZContext zContext) {
        super(zContext);
    }

    @Override
    protected void startInternal() {
        executorService.submit(() -> {
            LOGGER.info("Creating and connecting REQ socket...");
            ZMQ.Socket socket = zContext.createSocket(ZMQ.REQ);
            ReqRepZeroMQServer.ADDRESSES.stream().forEach(socket::connect);
            while(!Thread.interrupted()){
                String request = "Hello " + atomicInteger.getAndIncrement();
                LOGGER.info("Sending a request={}", request);
                socket.send(request);
                String response = socket.recvStr();
                LOGGER.info("Received response={}", response);
                try {
                    Thread.sleep(1000);
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
