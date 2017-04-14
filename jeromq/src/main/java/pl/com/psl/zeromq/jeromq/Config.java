package pl.com.psl.zeromq.jeromq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.zeromq.ZContext;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Created by psl on 14.04.17.
 */
@Configuration
@ComponentScan(basePackageClasses = Config.class)
public class Config{

    @Autowired
    private ZeroMQServer server;
    @Autowired
    private ZeroMQClient client;

    @PostConstruct
    private void init(){
        server.start();
        client.start();
    }

    @PreDestroy
    private void shutdown(){
        client.stop();
        server.stop();
    }

    @Bean
    public ZContext zContext(){
        return new ZContext();
    }

}
