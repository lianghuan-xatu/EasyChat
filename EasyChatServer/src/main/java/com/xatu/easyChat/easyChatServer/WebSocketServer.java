package com.xatu.easyChat.easyChatServer;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.springframework.stereotype.Component;

@Component
public class WebSocketServer {
    public static class SingletionWSServer{
         static final WebSocketServer instance = new WebSocketServer();
    }
    public static WebSocketServer getInstance() {
        return SingletionWSServer.instance;
    }

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ServerBootstrap server;
    private ChannelFuture channelFuture;

    public WebSocketServer() {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        server = new ServerBootstrap();
        server.group(bossGroup,workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new WSServerInitialzer());

    }
    public void startServer() throws InterruptedException {
        channelFuture = server.bind(8085).sync();
        if(channelFuture.isSuccess()) {
            System.out.println("server启动成功！");
        }
        //对关闭通道进行事件侦听
        channelFuture.channel().closeFuture().sync();
    }
}
