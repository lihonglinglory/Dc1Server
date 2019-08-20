package model;

import bean.Dc1Bean;
import bean.StatusBean;
import bean.SwitchSetBean;

import java.util.ArrayList;
import java.util.HashMap;

public class DataPool {
    /**
     * key id
     * value dc1
     */
    public static HashMap<String, Dc1Bean> dc1Map = new HashMap<>();

    public static void update(String id, StatusBean statusBean) {
        Dc1Bean dc1Bean = dc1Map.get(id);
        if (dc1Bean == null) {
            dc1Bean = new Dc1Bean();
            dc1Bean.setId(id);
            dc1Map.put(id, dc1Bean);
        }
        dc1Bean.setStatus(statusBean.getStatus())
                .setI(statusBean.getI())
                .setV(statusBean.getV())
                .setP(statusBean.getP())
                .setUpdateTime(System.currentTimeMillis());
        System.out.println(dc1Bean.toString());
    }

    public static void update(String id, SwitchSetBean switchSetBean) {
        Dc1Bean dc1Bean = dc1Map.get(id);
        if (dc1Bean == null) {
            dc1Bean = new Dc1Bean();
            dc1Bean.setId(id);
            dc1Map.put(id, dc1Bean);
        }
        dc1Bean.setStatus(switchSetBean.getStatus())
                .setUpdateTime(System.currentTimeMillis());
        System.out.println(dc1Bean.toString());
    }

    public static void updateName(String id, ArrayList<String> nameList) {
        Dc1Bean dc1Bean = dc1Map.get(id);
        dc1Bean.setNames(nameList);
    }
}
