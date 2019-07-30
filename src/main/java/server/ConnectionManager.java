package server;

import io.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConnectionManager {
    private static final ConnectionManager instance = new ConnectionManager();

    private ConnectionManager() {
    }

    public static ConnectionManager getInstance() {
        return instance;
    }

    private HashMap<String, Connection> mRemoteAddressConnectionMap = new HashMap<>();
    private HashMap<String, Connection> mMacConnectionMap = new HashMap<>();
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    public void dispatchMsg(Channel channel, String msg) {
        InetSocketAddress remoteAddress = (InetSocketAddress) (channel.remoteAddress());
        String ip = remoteAddress.getAddress().getHostAddress();
        executorService.execute(() -> mRemoteAddressConnectionMap.get(ip).processMessage(msg));
    }

    public void addChannel(Channel channel) {
        InetSocketAddress remoteAddress = (InetSocketAddress) (channel.remoteAddress());
        String ip = remoteAddress.getAddress().getHostAddress();
        System.out.println("ip:" + ip);
        Connection connection = mRemoteAddressConnectionMap.get(ip);
        if (connection == null) {
            connection = new Connection();
            mRemoteAddressConnectionMap.put(ip, connection);
        }
        connection.setChannel(channel);
        System.out.println("addChannel mRemoteAddressConnectionMap:" + mRemoteAddressConnectionMap);
    }

    public void removeChannel(Channel channel) {
        InetSocketAddress remoteAddress = (InetSocketAddress) (channel.remoteAddress());
        String ip = remoteAddress.getAddress().getHostAddress();
        if (mRemoteAddressConnectionMap.get(ip).isActive()) {
            return;
        }
        mRemoteAddressConnectionMap.remove(ip);
        System.out.println("removeChannel mRemoteAddressConnectionMap:" + mRemoteAddressConnectionMap);
    }
}
