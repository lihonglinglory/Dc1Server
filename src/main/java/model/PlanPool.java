package model;

import util.LogUtil;
import model.db.PlanBean;
import model.db.PlanDao;
import server.ConnectionManager;

import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;

public class PlanPool {

    private static PlanPool instance = new PlanPool();

    /**
     * 一天的秒
     */
    private static final long ONE_DAY_SECOND = 24 * 60 * 60;

    public static PlanPool getInstance() {
        return instance;
    }

    /**
     * KEY : planId<br/>
     * VALUE : ScheduledFuture
     */
    private ConcurrentHashMap<String, ScheduledFuture> mTaskMap = new ConcurrentHashMap<>();
    private ScheduledExecutorService mExecutorService = new ScheduledThreadPoolExecutor(5);

    public void init() {
        mExecutorService.execute(() -> {
            List<PlanBean> list = PlanDao.getInstance().queryAll();
            list.forEach(this::convert);
        });
    }

    /**
     * 将任务转换成计划任务
     *
     * @param bean
     */
    private void convert(PlanBean bean) {
        if (!bean.isEnable()) {
            return;
        }
        long diffSecond = getDiffSecond(bean);
        if (!bean.isEnable()) {
            return;
        }
        ScheduledFuture<?> future = mExecutorService.schedule(() -> {
            mTaskMap.remove(bean.getId());
            ConnectionManager.getInstance().setDc1Status(bean.getDeviceId(), bean.getStatus());
            if (PlanBean.REPEAT_ONCE.equals(bean.getRepeat())) {
                bean.setEnable(false);
                PlanDao.getInstance().updateOne(bean);
                ConnectionManager.getInstance().pushPlanDataChanged(bean.getId());
            }
            //执行完成后添加下一次触发的任务
            convert(bean);
        }, diffSecond, TimeUnit.SECONDS);
        mTaskMap.put(bean.getId(), future);
    }

    /**
     * @param bean
     * @return 计算下一次执行时间差，单位：秒
     */
    private long getDiffSecond(PlanBean bean) {
        long diffSecond;

        //当天或者第二天执行
        LocalTime triggerTime = LocalTime.parse(bean.getTriggerTime());
        LocalTime now = LocalTime.now();
        final long diff = Duration.between(now, triggerTime).toMillis() / 1000;

        switch (bean.getRepeat()) {
            case PlanBean.REPEAT_ONCE:
            case PlanBean.REPEAT_EVERYDAY: {
                if (diff > 1) {
                    diffSecond = diff;
                } else {
                    diffSecond = ONE_DAY_SECOND + diff;
                }
                LogUtil.info("下次运行时间 :" + diff / 3600f + "小时");
                return diffSecond;
            }
            default: {
                if (bean.getRepeat() == null || bean.getRepeat().equals("")) {
                    LogUtil.warning("周期设置异常！！");
                    setPlanDisable(bean);
                    return 0;
                }
                //选星期执行
                String repeat = bean.getRepeat();
                int[] array = Arrays.stream(repeat.split(","))
                        .mapToInt(value -> {
                            try {
                                return Integer.parseInt(value);
                            } catch (Exception e) {
                                e.printStackTrace();
                                return 0;
                            }
                        })
                        .filter(integer -> integer > 0)
                        .toArray();
                if (array.length == 0) {
                    setPlanDisable(bean);
                    return 0;
                }
                if (array.length == 7) {
                    bean.setRepeat(PlanBean.REPEAT_EVERYDAY);
                    PlanDao.getInstance().updateOne(bean);
                    ConnectionManager.getInstance().pushPlanDataChanged(bean.getId());
                    if (diff > 1) {
                        diffSecond = diff;
                    } else {
                        diffSecond = ONE_DAY_SECOND + diff;
                    }
                    return diffSecond;
                }

                LocalDateTime localDateTime = LocalDateTime.now();
                int dayOfWeekNow = localDateTime.getDayOfWeek().getValue();

                IntPredicate predicate;
                if (diff > 1) {
                    //当天可执行
                    predicate = i -> i >= dayOfWeekNow;
                } else {
                    predicate = i -> i > dayOfWeekNow;
                }
                Integer triggerDay = IntStream.of(array)
                        .filter(predicate)
                        .findFirst()
                        .orElseGet(() -> array[0]);

                int i = triggerDay - dayOfWeekNow;
                if (diff <= 1 && i <= 0) {
                    i = i + 7;
                }
                diffSecond = i * ONE_DAY_SECOND + diff;
                LogUtil.info("下次运行时间 i:" + i + "天  偏移小时数:" + diff / 3600f);
                return diffSecond;
            }
        }
    }

    /**
     * 异常状态纠正
     *
     * @param bean
     */
    private void setPlanDisable(PlanBean bean) {
        bean.setEnable(false)
                .setRepeat(PlanBean.REPEAT_ONCE);
        PlanDao.getInstance().updateOne(bean);
        ConnectionManager.getInstance().pushPlanDataChanged(bean.getId());
    }

    private boolean canclePlan(String planId) {
        ScheduledFuture future = mTaskMap.get(planId);
        if (future == null || future.isCancelled()) {
            return true;
        }
        future.cancel(true);
        mTaskMap.remove(planId);
        return future.isCancelled();
    }

    public void addPlan(PlanBean plan) {
        mExecutorService.execute(() -> {
            boolean add = PlanDao.getInstance().add(plan);
            if (add) {
                convert(plan);
            }
        });
    }

    public void deletePlan(String planId) {
        canclePlan(planId);
        try {
            PlanDao.getInstance().deleteById(planId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void enablePlan(String id, boolean enable) {
        mExecutorService.execute(() -> {
            PlanBean planBean = PlanDao.getInstance().enablePlanById(id, enable);
            if (enable) {
                convert(planBean);
            } else {
                canclePlan(id);
            }
        });
    }
}
