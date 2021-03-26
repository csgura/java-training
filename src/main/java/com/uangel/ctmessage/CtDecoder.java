package com.uangel.ctmessage;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class CtDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list) throws Exception {

        var index = byteBuf.readerIndex();

       if (byteBuf.readableBytes() >= 16) {
           var trid = byteBuf.readLong();
           var length = byteBuf.readLong();

           if (byteBuf.readableBytes() >= length) {
               byte[] b = new byte[(int)length];

               byteBuf.readBytes(b);
               list.add(new CtxMessage(trid , new String(b)));
               return;
           }
       }

        byteBuf.readerIndex(index);
    }
}
