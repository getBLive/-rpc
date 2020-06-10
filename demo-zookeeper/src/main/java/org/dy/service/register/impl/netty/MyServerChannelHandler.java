package org.dy.service.register.impl.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.lang.reflect.Method;

public class MyServerChannelHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        MyRequest request = (MyRequest)msg;
        Object serviceObj = NettyRegister.services.getOrDefault(request.getServiceClass().getName(), null);
        if(serviceObj==null){
            System.out.println("没有找到目标服务。。。。");
            return;
        }
        Method method = serviceObj.getClass().getMethod(request.getMethodName(),request.getParamTypes());
        Object result = method.invoke(serviceObj, request.getValues());
        System.out.println("已执行方法:"+method.getName()+", 返回:"+result);
        MyResponse response = new MyResponse(result);
        ctx.writeAndFlush(response);
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}
