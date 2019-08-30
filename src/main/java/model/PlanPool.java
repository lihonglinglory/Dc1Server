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

    public static PlanPool getInstance() {
        return instance;
    }

    private ConcurrentHashMap<String, ScheduledFuture> mTaskMap = new ConcurrentHashMap<>();
    private ScheduledExecutorService mExecutorService = new ScheduledThreadPoolExecutor(5);

    public void init() {
        mExecutorService.execute(() -> {
            List<PlanBean> list = PlanDao.getInstance().queryAll();
            list.forEach(this::convert);
        });

        LocalTime now = LocalTime.now();
        LocalTime zero = LocalTime.of(0, 0);
        long diffTime = 24 * 60 * 60 * 1000 - Duration.between(zero, now).toMillis();
        mExecutorService.scheduleAtFixedRate(() -> {
            mTaskMap.values().parallelStream().forEach(future -> future.cancel(true));
            mTaskMap.clear();
            List<PlanBean> list = PlanDao.getInstance().queryAll();
            list.forEach(this::convert);
            System.out.println("---------------------------------------------------------------------------");
            System.out.println("---------------------------------------------------------------------------");
            System.out.println("---------------初始化定时------scheduleAtFixedRate--------------------------");
            System.out.println("---------------------------------------------------------------------------");
            System.out.println("---------------------------------------------------------------------------");
        }, diffTime + 2, 24, TimeUnit.HOURS);
    }

    private void convert(PlanBean bean) {
        if (!bean.isEnable()) {
            return;
        }
        LocalTime triggerTime = LocalTime.parse(bean.getTriggerTime());
        LocalTime now = LocalTime.now();
        long diffTime = Duration.between(now, triggerTime).toMillis() / 1000;
        if (diffTime > 0) {
            ScheduledFuture<?> future = mExecutorService.schedule(() -> {
                ConnectionManager.getInstance().setDc1Status(bean.getDeviceId(), bean.getStatus());
                System.out.println("---定时任务执行：deviceName=" + bean.getDeviceName() + " Status=" + bean.getStatus());
            }, diffTime, TimeUnit.SECONDS);
            mTaskMap.put(bean.getId(), future);
        }
    }

    public boolean canclePlan(String planId) {
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
