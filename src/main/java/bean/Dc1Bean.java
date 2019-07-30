package bean;

public class Dc1Bean {
    public String mac;
    public String status;
    public int I;
    public int V;
    public int P;
    private long updateTime;

    public String getMac() {
        return mac;
    }

    public Dc1Bean setMac(String mac) {
        this.mac = mac;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public Dc1Bean setStatus(String status) {
        this.status = status;
        return this;
    }

    public int getI() {
        return I;
    }

    public Dc1Bean setI(int i) {
        I = i;
        return this;
    }

    public int getV() {
        return V;
    }

    public Dc1Bean setV(int v) {
        V = v;
        return this;
    }

    public int getP() {
        return P;
    }

    public Dc1Bean setP(int p) {
        P = p;
        return this;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public Dc1Bean setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
        return this;
    }

    @Override
    public String toString() {
        return "Dc1Bean{" +
                "mac='" + mac + '\'' +
                ", status='" + status + '\'' +
                ", I=" + I +
                ", V=" + V +
                ", P=" + P +
                ", updateTime=" + updateTime +
                '}';
    }
}
