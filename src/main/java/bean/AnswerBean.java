package bean;

public class AnswerBean<T> {

    /**
     * uuid : 111111
     * status : 200
     * result : {"status":1011,"I":119,"V":219,"P":10}
     * msg : get datapoint success
     */

    private String uuid;
    private int status;
    private T result;
    private String msg;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public static class ResultBean {

    }
}
