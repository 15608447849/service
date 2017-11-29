package entity;

/**
 * Created by user on 2017/11/29.
 */
public class Result<T extends Result> {
    private int code;
    private String message;
    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public void setCode(int code) {
        this.code = code;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public T setResultInfo(int code,String message){
        setCode(code);
        setMessage(message);
        return (T)this;
    }
}
