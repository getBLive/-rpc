package org.dy.service.client.proxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.BoundedExponentialBackoffRetry;
import org.dy.service.client.MyClientChannelHandler;
import org.dy.service.register.impl.netty.MyRequest;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ClientProxy{

    public static Object getService(MyRequest request){
        Class serviceClass = request.getServiceClass();
        Object o = Proxy.newProxyInstance(serviceClass.getClassLoader(), new Class[]{serviceClass}, new MyInvoker(request));
        return o;
    }

    public static class MyInvoker implements InvocationHandler {
        private MyRequest request;

        public MyInvoker(MyRequest request) {
            this.request = request;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args){
            request.setMethodName(method.getName());
            request.setParamTypes(method.getParameterTypes());
            request.setValues(args);
            final MyClientChannelHandler clientChannelHandler = new MyClientChannelHandler();
            Bootstrap client = new Bootstrap();
            EventLoopGroup group = new NioEventLoopGroup();
            try {
                client.group(group)
                        .channel(NioSocketChannel.class)
                        .option(ChannelOption.TCP_NODELAY, true)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            public void initChannel(SocketChannel ch) throws Exception {
                                ChannelPipeline pipeline = ch.pipeline();
                                //自定义协议解码器
                                /** 入参有5个，分别解释如下
                                 maxFrameLength：框架的最大长度。如果帧的长度大于此值，则将抛出TooLongFrameException。
                                 lengthFieldOffset：长度字段的偏移量：即对应的长度字段在整个消息数据中得位置
                                 lengthFieldLength：长度字段的长度：如：长度字段是int型表示，那么这个值就是4（long型就是8）
                                 lengthAdjustment：要添加到长度字段值的补偿值
                                 initialBytesToStrip：从解码帧中去除的第一个字节数
                                 */
                                pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                                //自定义协议编码器
                                pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
                                //对象参数类型编码器
                                pipeline.addLast("encoder", new ObjectEncoder());
                                //对象参数类型解码器
                                pipeline.addLast("decoder", new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));
                                pipeline.addLast("handler", clientChannelHandler);
                            }
                        });
                ChannelFuture sync = client.connect(request.getHost(),request.getProt()).sync();
                sync.channel().writeAndFlush(request).sync();
                sync.channel().closeFuture().sync();
            }catch (Exception e){
                System.out.println(e.getCause());
            }finally {
                group.shutdownGracefully();
            }
            return clientChannelHandler.getMyResponse().getResult();
        }

        private String getAddrFromRegister(MyRequest request) throws Exception {
            String host = request.getHost();
            int prot = request.getProt();
            CuratorFramework curatorFramework = CuratorFrameworkFactory.builder()
                    .connectString(host+":"+prot).sessionTimeoutMs(5000)
                    .retryPolicy(new BoundedExponentialBackoffRetry(1000,2000,3))
                    .build();
            curatorFramework.start();
            byte[] bytes = curatorFramework.getData()
                    .forPath("/services/" + request.getServiceClass().getName());
            return new String(bytes);
        }
    }
}
