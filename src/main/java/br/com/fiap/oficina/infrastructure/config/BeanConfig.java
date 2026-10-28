package br.com.fiap.oficina.infrastructure.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean
    Clock clock() {
        return Clock.systemDefaultZone();
    }
}
