package pl.com.psl.zeromq.jeromq.pubsub;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import pl.com.psl.zeromq.jeromq.Profiles;
import pl.com.psl.zeromq.jeromq.ZeroMQServer;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by psl on 15.04.17.
 */
@Profile(Profiles.PUB_SUB)
@Component
public class PubSubZeroMQServer extends ZeroMQServer {

    static final List<String> ADDRESSES = Arrays.asList("tcp://localhost:8061", "tcp://localhost:8062");
    static final String PREFIX_A = "PREFIX-A";
    static final String PREFIX_B = "PREFIX-B";
    private ExecutorService executorService = Executors.newFixedThreadPool(ADDRESSES.size());
    private AtomicInteger atomicInteger = new AtomicInteger();

    @Autowired
    public PubSubZeroMQServer(ZContext zContext) {
        super(zContext);
    }

    @Override
    protected void startInternal() {
        List<String> prefixes = Arrays.asList(PREFIX_A, PREFIX_B);
        ADDRESSES.stream().forEach( address -> {
            executorService.submit(() -> {
                LOGGER.info("Creating and binding PUB socket with address={}", address);
                ZMQ.Socket socket = zContext.createSocket(ZMQ.PUB);
                socket.bind(address);
                while(!Thread.interrupted()){
                    prefixes.stream().forEach( prefix -> {
                        String message = prefix + " Message " + atomicInteger.getAndIncrement() +" from address=" + address;
                        LOGGER.info("Publishing message={}...", message);
                        socket.send(message);
                        LOGGER.info("Message sent!");
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            LOGGER.error("Interrupted while sleeping", e);
                            Thread.currentThread().interrupt();
                        }
                    });
                }
            });
        });
    }

    @Override
    protected void stopInternal() {
        executorService.shutdown();
    }
}
