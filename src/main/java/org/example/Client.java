package org.example;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

public class Client {

    public static void main(String[] args) throws IOException {
        String ip = args[0];
        int port = Integer.parseInt(args[1]);

        Logger log = Logger.getLogger(Server.class.getName());
        EpollEventLoopGroup worker = new EpollEventLoopGroup(1);
        Bootstrap b = new Bootstrap();
        b.group(worker).channel(EpollSocketChannel.class).handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(100, 0, 1, 0, 1))
                        .addLast(new LengthFieldPrepender(1)).addLast(new SimpleChannelInboundHandler<ByteBuf>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
                                log.info("[CLIENT] receive: " + msg.toString(StandardCharsets.UTF_8));
                            }

                            @Override
                            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                log.info("[CLIENT] exception: " + cause);
                                super.exceptionCaught(ctx, cause);
                            }

                            
                        });
				}
            });
        ChannelFuture cf = b.connect(ip, port).syncUninterruptibly();
        Channel channel = cf.channel();
        log.info("[CLIENT] connected - " + channel);

        System.in.read();
        log.info("[CLIENT] send ping");

        channel.writeAndFlush(Unpooled.copiedBuffer("PING", StandardCharsets.UTF_8));

        
    }
    
}
