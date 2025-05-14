package com.biit.appointment.google;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;

@SpringBootApplication()
@ConfigurationPropertiesScan({"com.biit.appointment"})
@EntityScan({"com.biit.appointment"})
@ComponentScan({"com.biit.appointment", "com.biit.server.client", "com.biit.usermanager"} )
@EnableJpaRepositories({"com.biit.appointment"})
public class Server {
    public static void main(String[] args) {
        SpringApplication.run(Server.class, args);
    }
}
