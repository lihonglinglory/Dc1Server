package server;

import io.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.util.Collection;
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
    private HashMap<String, PhoneConnection> mPhoneConnectionMap = new HashMap<>();

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    public void dispatchMsg(Channel channel, String msg) {
        InetSocketAddress remoteAddress = (InetSocketAddress) (channel.remoteAddress());
        InetSocketAddress localAddress = (InetSocketAddress) (channel.localAddress());
        String ip = remoteAddress.getAddress().getHostAddress();
        int localPort = localAddress.getPort();
        if (localPort == 8800) {
            executorService.execute(() -> mPhoneConnectionMap.get(ip).processMessage(msg));
        } else {
            executorService.execute(() -> mRemoteAddressConnectionMap.get(ip).processMessage(msg));
        }
    }

    public void addChannel(Channel channel) {
        InetSocketAddress remoteAddress = (InetSocketAddress) (channel.remoteAddress());
        InetSocketAddress localAddress = (InetSocketAddress) (channel.localAddress());
        String ip = remoteAddress.getAddress().getHostAddress();
        int localPort = localAddress.getPort();
        System.out.println("remoteAddress ip:" + ip);
        System.out.println("localAddress port:" + localPort);
        if (localPort == 8800) {
            //手机连接
            PhoneConnection connection = mPhoneConnectionMap.get(ip);
            if (connection == null) {
                connection = new PhoneConnection();
                mPhoneConnectionMap.put(ip, connection);
            }
            connection.setChannel(channel);
            System.out.println("addChannel mPhoneConnectionMap:" + mPhoneConnectionMap);
        } else {
            Connection connection = mRemoteAddressConnectionMap.get(ip);
            if (connection == null) {
                connection = new Connection();
                mRemoteAddressConnectionMap.put(ip, connection);
            }
            connection.setChannel(channel);
            System.out.println("addChannel mRemoteAddressConnectionMap:" + mRemoteAddressConnectionMap);
        }
    }

    public void removeChannel(Channel channel) {
        InetSocketAddress remoteAddress = (InetSocketAddress) (channel.remoteAddress());
        InetSocketAddress localAddress = (InetSocketAddress) (channel.localAddress());
        String ip = remoteAddress.getAddress().getHostAddress();
        int localPort = localAddress.getPort();
        if (localPort == 8800) {
            //手机连接
            if (mPhoneConnectionMap.get(ip).isActive()) {
                return;
            }
            mPhoneConnectionMap.remove(ip);
            System.out.println("removeChannel mPhoneConnectionMap:" + mPhoneConnectionMap);
        } else {
            if (mRemoteAddressConnectionMap.get(ip).isActive()) {
                return;
            }
            mRemoteAddressConnectionMap.remove(ip);
            System.out.println("removeChannel mRemoteAddressConnectionMap:" + mRemoteAddressConnectionMap);
        }
    }

    public void setDc1Status(String id, String status) {
        Collection<Connection> values = mRemoteAddressConnectionMap.values();
        for (Connection conn : values) {
            if (conn.getId().equals(id)) {
                conn.setStatus(status);
                return;
            }
        }
    }
}
