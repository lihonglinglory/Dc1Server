package server;

import bean.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.netty.channel.Channel;
import model.DataPool;

import java.lang.reflect.Type;
import java.util.UnknownFormatConversionException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * channel的管理
 */

public class Connection {
    /**
     * 设备上线
     */
    public static final String ACTIVATE = "activate=";
    /**
     * 查询设备状态
     */
    public static final String DATAPOINT = "datapoint";
    /**
     * 设置设备开关
     */
    public static final String SET_DATAPOINT = "datapoint=";

    public static final int CODE_SUCCESS = 200;

    //周期消息发送间隔时间（ms）
    private final static int DEFAULT_TIME = 100;
    private Channel channel;
    /**
     * dc1的mac，唯一标识
     */
    private String mac;
    private Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    /**
     * 查询状态正则
     */
    private Pattern pattern = Pattern.compile("\\{\"uuid\":\"\\w{0,14}\",\"status\":\\d{1,3},\"result\":\\{\"status\":[0|1]{1,4},\"I\":\\d{1,3},\"V\":\\d{1,3},\"P\":\\d{1,4}},\"msg\":\".+\"}[\r|\n]{0,2}");
    // 消息队列
    private final LinkedBlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
    private static ScheduledExecutorService sendMessageScheduleThread;

    public Connection() {
        sendMessageScheduleThread = Executors.newScheduledThreadPool(5);
        sendMessageScheduleThread.scheduleWithFixedDelay(new SendTask(), 0, DEFAULT_TIME, TimeUnit.MILLISECONDS);
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
        if (msg.contains("action")) {
            if (msg.contains(ACTIVATE)) {
                //收到dc1上线数据
                Type type = new TypeToken<AskBean<ActivateBean>>() {
                }.getType();
                AskBean<ActivateBean> askBean = gson.fromJson(msg, type);
                mac = askBean.getParams().getMac();
                sendMessageScheduleThread.scheduleWithFixedDelay(new QueryTask(), 0, 1, TimeUnit.MINUTES);
            }
        } else {
            if (mac == null) {
                return;
            }
            if (pattern.matcher(msg).matches()) {
                Type type = new TypeToken<AnswerBean<StatusBean>>() {
                }.getType();
                AnswerBean<StatusBean> answerBean = gson.fromJson(msg, type);
                if (answerBean.getStatus() == CODE_SUCCESS) {
                    DataPool.update(mac, answerBean.getResult());
                }
            } else {
                Type type = new TypeToken<AnswerBean<SwitchSetBean>>() {
                }.getType();
                AnswerBean<SwitchSetBean> answerBean = gson.fromJson(msg, type);
                if (answerBean.getStatus() == CODE_SUCCESS) {
                    DataPool.update(mac, answerBean.getResult());
                }
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
            } else {
                System.out.println("SendTask isActive() = false!!!!");
            }
        }
    }

    // 定时任务 发送请求dc1的状态，把请求数据添加到任务队列
    private class QueryTask implements Runnable {
        @Override
        public void run() {
            AskBean<String> askBean = new AskBean<>();
            String uuid = String.format("T%d", System.currentTimeMillis());
            askBean.setAction(DATAPOINT)
                    .setAuth("")
                    .setParams("")
                    .setUuid(uuid);
            String msg = gson.toJson(askBean);
            System.out.println("QueryTask appendMsgToQueue");
            appendMsgToQueue(msg);
        }
    }

    public void setStatus(String status) {
        AskBean<SwitchSetBean> askBean = new AskBean<>();
        String uuid = String.format("T%d", System.currentTimeMillis());
        askBean.setAction(SET_DATAPOINT)
                .setAuth("")
                .setParams(new SwitchSetBean().setStatus(status))
                .setUuid(uuid);
        String msg = gson.toJson(askBean);
        System.out.println("Switch set：" + status);
        appendMsgToQueue(msg);
    }

    @Override
    public String toString() {
        return this.hashCode() + channel.toString();
    }

    public String getMac() {
        return mac;
    }
}
