package org.dy.service.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.dy.service.register.impl.netty.MyResponse;

public class MyClientChannelHandler extends ChannelInboundHandlerAdapter {
    private MyResponse myResponse;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        myResponse= (MyResponse) msg;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.channel().close();
        /*try {
            throw cause;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }*/
    }

    public MyResponse getMyResponse() {
        return myResponse;
    }
}
