package server;

import io.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ConnectionManager {
    private static final ConnectionManager instance = new ConnectionManager();

    private ConnectionManager() {
    }

    public static ConnectionManager getInstance() {
        return instance;
    }

    private ConcurrentHashMap<String, Connection> mRemoteAddressConnectionMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, PhoneConnection> mPhoneConnectionMap = new ConcurrentHashMap<>();

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
            System.out.println("addChannel mPhoneConnectionMap:" + mRemoteAddressConnectionMap);
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
            System.out.println("removeChannel mRemoteAddressConnectionMap:" + mRemoteAddressConnectionMap);
        } else {
            if (mRemoteAddressConnectionMap.get(ip).isActive()) {
                return;
            }
            mRemoteAddressConnectionMap.remove(ip);
            System.out.println("removeChannel mRemoteAddressConnectionMap:" + mRemoteAddressConnectionMap);
        }
    }

    public void setDc1Status(String mac, String status) {
        Collection<Connection> values = mRemoteAddressConnectionMap.values();
        values.stream()
                .filter(connection -> connection.getMac().equals(mac))
                .findFirst()
                .ifPresent(connection -> connection.setStatus(status));
    }
}
