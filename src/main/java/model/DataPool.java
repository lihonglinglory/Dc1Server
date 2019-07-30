package model;

import bean.Dc1Bean;
import bean.StatusBean;
import bean.SwitchSetBean;

import java.util.HashMap;

public class DataPool {
    public static HashMap<String, Dc1Bean> dc1Map = new HashMap<>();

    public static void update(String mac, StatusBean statusBean) {
        Dc1Bean dc1Bean = dc1Map.get(mac);
        if (dc1Bean == null) {
            dc1Bean = new Dc1Bean();
            dc1Bean.setMac(mac);
            dc1Map.put(mac, dc1Bean);
        }
        dc1Bean.setStatus(statusBean.getStatus())
                .setI(statusBean.getI())
                .setV(statusBean.getV())
                .setP(statusBean.getP())
                .setUpdateTime(System.currentTimeMillis());
        System.out.println(dc1Bean.toString());
    }

    public static void update(String mac, SwitchSetBean switchSetBean) {
        Dc1Bean dc1Bean = dc1Map.get(mac);
        if (dc1Bean == null) {
            dc1Bean = new Dc1Bean();
            dc1Bean.setMac(mac);
            dc1Map.put(mac, dc1Bean);
        }
        dc1Bean.setStatus(switchSetBean.getStatus())
                .setUpdateTime(System.currentTimeMillis());
        System.out.println(dc1Bean.toString());
    }
}
