package model.db;

import com.j256.ormlite.field.DatabaseField;

public class PlanBean {
    public static final String ATTR_EXPIRE = "expire";
    public static final String ATTR_TRIGGER_TIME = "triggerTime";
    public static final String ATTR_DEVICE_ID = "deviceId";

    public static final String REPEAT_ONCE = "repeat_once";
    public static final String REPEAT_EVERYDAY = "repeat_everyday";
    /**
     * uuid
     */
    @DatabaseField(id = true, canBeNull = false, unique = true)
    private String id;

    /**
     * 触发的设备Id
     */
    @DatabaseField(canBeNull = false)
    private String deviceId;

    /**
     * 触发的设备名称
     */
    @DatabaseField
    private String deviceName;

    /**
     * 任务最新修改或添加时间
     */
    @DatabaseField
    private long updateTime;

    /**
     * 设备开关指令
     */
    @DatabaseField
    private String status;

    /**
     * 触发时间,
     * 格式 05:43:22
     */
    @DatabaseField
    private String triggerTime;

    /**
     * 重复状态，一次，每天，
     */
    @DatabaseField
    private String repeat;

    /**
     * 是否开启,开启/关闭控制
     */
    @DatabaseField
    private boolean enable;


    public String getId() {
        return id;
    }

    public PlanBean setId(String id) {
        this.id = id;
        return this;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public PlanBean setDeviceId(String deviceId) {
        this.deviceId = deviceId;
        return this;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public PlanBean setDeviceName(String deviceName) {
        this.deviceName = deviceName;
        return this;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public PlanBean setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public PlanBean setStatus(String status) {
        this.status = status;
        return this;
    }

    public String getTriggerTime() {
        return triggerTime;
    }

    public PlanBean setTriggerTime(String triggerTime) {
        this.triggerTime = triggerTime;
        return this;
    }

    public String getRepeat() {
        return repeat;
    }

    public PlanBean setRepeat(String repeat) {
        this.repeat = repeat;
        return this;
    }

    public boolean isEnable() {
        return enable;
    }

    public PlanBean setEnable(boolean enable) {
        this.enable = enable;
        return this;
    }
}
