package org.dy.service.register.impl.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.dy.service.register.api.AbstractRegisterCenter;
import org.dy.service.spring.MyService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.concurrent.ConcurrentHashMap;

public class NettyRegister extends AbstractRegisterCenter implements ApplicationContextAware {

    public static String SERVICE_PATH="/service";
    public static final ConcurrentHashMap<String,Object> services = new ConcurrentHashMap<>(64);
    private CuratorFramework curatorFramework ;
    private StringBuilder addrsb = new StringBuilder();
    private String addrstr;
    private String hostName;
    private int port;

    public NettyRegister(String hostName, int port,String[] addrs){
        this.hostName = hostName;
        this.port = port;
        addrstr=hostName+":"+port;
        for(String addr:addrs){
            addrsb.append(",");
            addrsb.append(addr);
        }
        String addr= addrsb.toString().replaceFirst(",","");
        curatorFramework = CuratorFrameworkFactory.builder().connectString(addr).sessionTimeoutMs(5000)
                .retryPolicy(new ExponentialBackoffRetry(1000,3)).build();
        curatorFramework.start();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        String[] beanNames = applicationContext.getBeanNamesForAnnotation(MyService.class);
        for (String beanName:beanNames) {
            Object bean = applicationContext.getBean(beanName);
            MyService annotation = bean.getClass().getAnnotation(MyService.class);
            String id = annotation.id().getName();
            System.out.println("已发现服务："+annotation.name());
            services.put(id,bean);
            doRegister(id,hostName+":"+port);
        }
    }

    @Override
    public void doRegister(String serviceName, Object service) {
        try {
            curatorFramework.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(SERVICE_PATH+"/"+serviceName+"/"+addrstr);
            System.out.println("已注册服务："+serviceName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onstart() {
        {
            EventLoopGroup boss = new NioEventLoopGroup();
            EventLoopGroup worker = new NioEventLoopGroup();
            ServerBootstrap server = new ServerBootstrap();
            server.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast(
                                    new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4),
                                    new LengthFieldPrepender(4)
                            );
                            pipeline.addLast("decoder", new ObjectEncoder());
                            pipeline.addLast("encoder", new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));
                            pipeline.addLast(new MyServerChannelHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            try {
                ChannelFuture sync = server.bind(hostName, port).sync();
                System.out.println("server已启动，监听端口：" + port);
                sync.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                try {
                    worker.shutdownGracefully().sync();
                    boss.shutdownGracefully().sync();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
