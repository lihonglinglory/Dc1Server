package server;

import bean.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.netty.channel.Channel;
import model.DataPool;
import sun.rmi.runtime.Log;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * channel的管理
 */

public class PhoneConnection {

    public static final int CODE_SUCCESS = 200;

    //周期消息发送间隔时间（ms）
    private final static int DEFAULT_TIME = 100;
    private Channel channel;

    private Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    // 消息队列
    private final LinkedBlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
    private static ScheduledExecutorService sendMessageScheduleThread;

    public PhoneConnection() {
        sendMessageScheduleThread = Executors.newScheduledThreadPool(5);
        sendMessageScheduleThread.scheduleWithFixedDelay(new SendTask(), 0, DEFAULT_TIME, TimeUnit.MILLISECONDS);
        sendMessageScheduleThread.scheduleWithFixedDelay(new QueryTask(), 0, 2, TimeUnit.MINUTES);
    }

    public void setChannel(Channel channel) {
        close();
        this.channel = channel;
    }


    private void appendMsgToQueue(String msg) {
        try {
            messageQueue.put(msg);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void processMessage(String msg) {
        msg = msg.replace("\n", "");
        String action = msg.split(" ", 2)[0];
        if (action == null || "".equals(action)) {
            return;
        }
        System.out.println("phone action = " + action);
        switch (action) {
            //查询
            case "query": {
                appendMsgToQueue(gson.toJson(DataPool.dc1Map.values()));
                break;
            }
            //设置
            case "set": {
                Pattern pattern = Pattern.compile("^set id=(?<id>[A-Fa-f0-9:|\\-]{17,18}) status=(?<status>[0|1]{4})$");
                Matcher matcher = pattern.matcher(msg);
                if (matcher.matches()) {
                    String id = matcher.group("id");
                    String status = matcher.group("status");
                    ConnectionManager.getInstance().setDc1Status(id, status);
                }
                break;
            }
        }
    }

    public void close() {
        if (channel != null) {
            channel.close();
            channel = null;
        }
    }

    public boolean isActive() {
        return channel != null && channel.isActive();
    }

    private class SendTask implements Runnable {
        @Override
        public void run() {
            if (isActive()) {
                try {
                    //阻塞线程
                    String message = messageQueue.take();
                    System.out.println("send:" + message + "\n");
                    channel.writeAndFlush(message + "\n");
                } catch (InterruptedException | NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 定时任务 心跳
    private class QueryTask implements Runnable {
        @Override
        public void run() {
            appendMsgToQueue("-");
        }
    }

    @Override
    public String toString() {
        return this.hashCode() + channel.toString();
    }
}
