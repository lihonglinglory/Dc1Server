import server.NettySocketServer;
import server.Dc1ServerHandler;
import server.PhoneServerHandler;

class main {
    public static void main(String[] args) {
        //dc1
        new NettySocketServer(8000, new Dc1ServerHandler()).start();
        //phone
        new NettySocketServer(8800, new PhoneServerHandler()).start();
    }
}
