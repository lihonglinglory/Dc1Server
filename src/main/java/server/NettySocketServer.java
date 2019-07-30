package server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;

public class NettySocketServer {
    private int port;

    public NettySocketServer(int port) {
        this.port = port;
    }

    public void start() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        try {
            bootstrap
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    // BACKLOG用于构造服务端套接字ServerSocket对象，标识当服务器请求处理线程全满时，
                    // 用于临时存放已完成三次握手的请求的队列的最大长度。如果未设置或所设置的值小于1，Java将使用默认值50。
                    .group(bossGroup, workerGroup) //绑定线程池
                    .channel(NioServerSocketChannel.class)// 指定使用的channel
                    .localAddress(port)
                    .childHandler(new SocketChannelChannelInitializer());
            ChannelFuture channelFuture = bootstrap.bind().sync(); //服务器异步创建绑定
            System.out.println("Server is listening：" + channelFuture.channel());
            channelFuture.channel().closeFuture().sync();//关闭服务器
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // 释放线程池资源
                workerGroup.shutdownGracefully().sync();
                bossGroup.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static class SocketChannelChannelInitializer extends ChannelInitializer<SocketChannel> {

        // 绑定客户端时触发的操作
        @Override
        protected void initChannel(SocketChannel socketChannel) throws Exception {
            ChannelPipeline pipeline = socketChannel.pipeline();
            pipeline.addLast(new StringDecoder(CharsetUtil.UTF_8));
            pipeline.addLast(new StringEncoder(CharsetUtil.UTF_8));
            pipeline.addLast(new LineBasedFrameDecoder(1024 * 1024));
            pipeline.addLast(new IdleStateHandler(15, 15, 15));
            pipeline.addLast("handler", new ServerHandler());//服务器处理客户端请求
        }
    }

    public static class ServerHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("One channel is ACTIVE." + ctx.channel());
            ConnectionManager.getInstance().addChannel(ctx.channel());
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("One channel is INACTIVE" + ctx.channel());
            ConnectionManager.getInstance().removeChannel(ctx.channel());
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            System.out.println("receive message:" + msg);
            System.out.println("Thread.currentThread().getId()=" + Thread.currentThread().getId());
            ConnectionManager.getInstance().dispatchMsg(ctx.channel(), (String) msg);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            super.channelReadComplete(ctx);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            super.exceptionCaught(ctx, cause);
        }
    }
}