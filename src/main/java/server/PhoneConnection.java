package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import io.netty.channel.Channel;
import model.DataPool;
import model.PlanPool;
import model.db.PlanBean;
import model.db.PlanDao;

import java.util.ArrayList;
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

    //周期消息发送间隔时间（ms）
    private final static int DEFAULT_TIME = 100;
    private Channel channel;

    private Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    // 消息队列
    private final LinkedBlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
    private static ScheduledExecutorService sendMessageScheduleThread;

    private Pattern setPattern = Pattern.compile("^set id=(?<id>[A-Fa-f0-9:|\\-]{17,18}) status=(?<status>[0|1]{4})$");
    private Pattern changeNamePattern = Pattern.compile("^changeName id=(?<id>[A-Fa-f0-9:|\\-]{17,18}) names=(?<names>.+)$");
    private Pattern resetPowerPattern = Pattern.compile("^resetPower id=(?<id>[A-Fa-f0-9:|\\-]{17,18})$");

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
        System.out.println("------phone action id=" + channel.id() + " message=" + msg);
        switch (action) {
            //查询
            case "queryDevice": {
                appendMsgToQueue("queryDevice " + gson.toJson(DataPool.dc1Map.values()));
                break;
            }
            case "queryPlan": {
                String deviceId = msg.split(" ", 2)[1];
                sendMessageScheduleThread.execute(() -> {
                    appendMsgToQueue("queryPlan " + gson.toJson(PlanDao.getInstance().queryAllByDeviceId(deviceId)));
                });
                break;
            }
            //设置
            case "set": {
                Matcher matcher = setPattern.matcher(msg);
                if (matcher.matches()) {
                    String id = matcher.group("id");
                    String status = matcher.group("status");
                    ConnectionManager.getInstance().setDc1Status(id, status);
                }
                break;
            }
            //改名字
            case "changeName": {
                Matcher matcher = changeNamePattern.matcher(msg);
                if (matcher.matches()) {
                    String id = matcher.group("id");
                    String names = matcher.group("names");
                    ArrayList<String> nameList = gson.fromJson(names, new TypeToken<ArrayList<String>>() {
                    }.getType());
                    DataPool.updateName(id, nameList);
                    ConnectionManager.getInstance().refreshPhoneData();
                }
                break;
            }
            //重置电量
            case "resetPower": {
                Matcher matcher = resetPowerPattern.matcher(msg);
                if (matcher.matches()) {
                    String id = matcher.group("id");
                    DataPool.resetPower(id);
                    ConnectionManager.getInstance().refreshPhoneData();
                }
                break;
            }
            case "addPlan": {
                String json = msg.split(" ", 2)[1];
                PlanBean plan = gson.fromJson(json, PlanBean.class);
                PlanPool.getInstance().addPlan(plan);
                break;
            }
            case "deletePlan": {
                String id = msg.split(" ", 2)[1];
                PlanPool.getInstance().deletePlan(id);
                break;
            }
            case "enablePlanById": {
                String id = msg.split(" ", 3)[1];
                boolean enable = Boolean.parseBoolean(msg.split(" ", 3)[2]);
                PlanPool.getInstance().enablePlan(id, enable);
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
                    System.out.println("------phone send to phone id=" + channel.id() + " message=" + message);
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
