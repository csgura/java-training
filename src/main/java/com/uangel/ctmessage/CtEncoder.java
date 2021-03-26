package com.uangel.ctmessage;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class CtEncoder extends MessageToByteEncoder<CtxMessage> {
    @Override
    protected void encode(ChannelHandlerContext ctx, CtxMessage msg, ByteBuf out) throws Exception {

        var b = msg.getMsg().getBytes();

        out.writeLong(msg.getTrid());
        out.writeLong(b.length);

        out.writeBytes(b);
    }
}
