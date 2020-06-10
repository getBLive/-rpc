package service.springConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import service.register.RpcServicesRegister;

@Configuration
@ComponentScan("service")
public class SpringConfig {
    @Bean
    public RpcServicesRegister rpcServicesRegister(){
        return new RpcServicesRegister(8848);
    }
}
