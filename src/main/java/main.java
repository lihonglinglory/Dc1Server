import server.NettySocketServer;

class main {
    public static void main(String[] args) {
        new NettySocketServer(8000).start();
    }
}
