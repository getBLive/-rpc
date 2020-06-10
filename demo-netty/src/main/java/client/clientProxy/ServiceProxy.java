package client.clientProxy;

import client.rpcconfig.PersonalOutboundHandler;
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
import service.register.RquestParam;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ServiceProxy {
    public static<T> T getServices(RquestParam requestParam){
        T service = (T) Proxy.newProxyInstance(requestParam.getServiceClass().getClassLoader(),
                new Class[]{requestParam.getServiceClass()},new ServicesHandler(requestParam));
        return service;
    }

    static class ServicesHandler implements InvocationHandler{
        private RquestParam requestParam;

        public ServicesHandler(RquestParam requestParam) {
            this.requestParam = requestParam;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            requestParam.setMethod(method.getName());
            requestParam.setParamTypes(method.getParameterTypes());
            requestParam.setValues(args);
            final PersonalOutboundHandler result = new PersonalOutboundHandler();
            EventLoopGroup group = new NioEventLoopGroup();
            Bootstrap b = new Bootstrap();
            try {
                b.group(group)
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
                                pipeline.addLast("handler",result);
                            }
                        });

                ChannelFuture future = b.connect(requestParam.getHost(), requestParam.getPort()).sync();
                future.channel().writeAndFlush(requestParam).sync();
                future.channel().closeFuture().sync();
            } catch(Exception e){
                e.printStackTrace();
            }finally {
                group.shutdownGracefully();
            }
            return result.getObject();
        }
    }
}
