package org.example;

import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ServerChannel;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

public class Server {

    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);

        Logger log = Logger.getLogger(Server.class.getName());
        EpollEventLoopGroup boss = new EpollEventLoopGroup(1);
        EpollEventLoopGroup worker = new EpollEventLoopGroup(1);

        ServerBootstrap b = new ServerBootstrap();
        b.group(boss, worker).channel(EpollServerSocketChannel.class).option(ChannelOption.SO_BACKLOG, 1)
                .handler(new ChannelInitializer<ServerChannel>() {
                    @Override
                    protected void initChannel(ServerChannel ch) throws Exception {
                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {

                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                log.info("[SERVER] accept: " + msg);
                                ctx.channel().config().setAutoRead(false);
                                super.channelRead(ctx, msg);
                            }
                                
                        });
                    }
            })
            .childHandler(new ChannelInitializer<Channel>(){
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline()
                            .addLast(new LengthFieldBasedFrameDecoder(100, 0, 1, 0, 1))
                            .addLast(new LengthFieldPrepender(1))
                            .addLast(new SimpleChannelInboundHandler<ByteBuf>() {
								@Override
								protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
                                    log.info("[SERVER] receive: " + msg.toString(StandardCharsets.UTF_8));
                                    ctx.channel().writeAndFlush(Unpooled.copiedBuffer("PONG", StandardCharsets.UTF_8));
								}
                            });

                    }
                
            });

        b.bind(port).syncUninterruptibly();

        log.info("[SERVER] started...");
    }
    
}
