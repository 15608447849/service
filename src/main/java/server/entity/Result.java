package server.entity;

/**
 * Created by user on 2017/11/29.
 */
public class Result<T extends Result> {

    public int code;

    public String message;

    public Result Info(int code, String message){
        this.code = code;
        this.message = message;
        return this;
    }
}
