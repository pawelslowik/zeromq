package pl.com.psl.zeromq.jeromq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(Config.class);

    @Autowired
    private ZeroMQServer server;
    @Autowired
    private ZeroMQClient client;
    @Autowired
    private ZContext zContext;

    @PostConstruct
    private void init(){
        LOGGER.info("Running post construct...");
        server.start();
        client.start();
        LOGGER.info("Finished post construct");
    }

    @PreDestroy
    private void shutdown(){
        LOGGER.info("Running pre destroy...");
        zContext.close();
        client.stop();
        server.stop();
        LOGGER.info("Finished pre destroy");
    }

    @Bean
    public ZContext zContext(){
        return new ZContext();
    }

}
