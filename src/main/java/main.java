import server.NettySocketServer;
import server.ServerHandler;

class main {
    public static void main(String[] args) {
        new NettySocketServer(8000, new ServerHandler()).start();
    }
}
