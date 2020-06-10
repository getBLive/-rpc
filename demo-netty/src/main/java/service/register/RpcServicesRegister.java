package service.register;

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
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import service.anno.RpcService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RpcServicesRegister implements ApplicationContextAware {
    private int port;
    public static final Map<String,Object> rpcServices = new ConcurrentHashMap(64);

    public RpcServicesRegister(int port) {
        this.port = port;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(RpcService.class);
        for (Object bean:beansWithAnnotation.values()){
            RpcService annotation = bean.getClass().getAnnotation(RpcService.class);
            rpcServices.put(annotation.value().getName()+"_"+annotation.version(),bean);
            System.out.println("rpcServices(): "+rpcServices);
        }
    }

    public void start(){
        EventLoopGroup boosGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        try{
            serverBootstrap.group(boosGroup,workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>(){
                    public void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(
                                new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0,4,0,4),
                                new LengthFieldPrepender(4)
                                );
                        pipeline.addLast("decoder",new ObjectEncoder());
                        pipeline.addLast("encoder",new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));
                        pipeline.addLast(new PersonalInboundHandler());
                    }
                })
                    .option(ChannelOption.SO_BACKLOG,128)
                    .childOption(ChannelOption.SO_KEEPALIVE,true);
        ChannelFuture sync = serverBootstrap.bind("localhost",this.port).sync();
        System.out.println("server had started on localhost"+": "+this.port);
        sync.channel().closeFuture().sync();
    } catch (Exception e){
            e.printStackTrace();
        }finally {
            boosGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
    }
    }
}
