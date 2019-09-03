package model;

import model.db.PlanBean;
import model.db.PlanDao;
import server.ConnectionManager;

import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;

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
        LocalTime triggerTime = LocalTime.parse(bean.getTriggerTime());
        LocalTime now = LocalTime.now();
        long diff = Duration.between(now, triggerTime).toMillis() / 1000;
        long diffSecond;
        if (diff > 1) {
            diffSecond = diff;
        } else {
            diffSecond = ONE_DAY_SECOND + diff;
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
