package service.register;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.lang.reflect.Method;

public class PersonalInboundHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        RquestParam reqParam = (RquestParam) msg;
        if(!RpcServicesRegister.rpcServices.containsKey(reqParam.getServiceName())){
            System.out.println("没有找到与"+reqParam.getServiceName()+"对应的服务！");
            return;
        }
        Object service = RpcServicesRegister.rpcServices.get(reqParam.getServiceName());
        Method method = service.getClass().getMethod(reqParam.getMethod(),reqParam.getParamTypes());
        Object result = method.invoke(service, reqParam.getValues());
        ChannelFuture channelFuture = ctx.writeAndFlush(result);
        channelFuture.addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }

}
