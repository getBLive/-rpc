package org.dy.service.register.impl.netty;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Configuration
@ComponentScan("org.dy.service")
public class ServicesScanConf {

    @Bean
    public NettyRegister myRegister(){
        String[] registrCenterAddr = new String[1];
        registrCenterAddr[0]="192.168.252.101:2181";
        try {
            return new NettyRegister(InetAddress.getLocalHost().getHostAddress(),8080,registrCenterAddr);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }
}
