package server.entity;

/**
 * Created by lzp on 2017/5/8.
 * FTP信息
 */
public class FtpInfo{
    private FtpInfo(){}

    private static class Holder{
        private static FtpInfo INSTANCE = new FtpInfo();
    }

    public static FtpInfo get(){
        return FtpInfo.Holder.INSTANCE;
    }

    public String host;
    public int port;
    public String user;
    public String password;

    public void setInfo(String host, int port, String user, String password) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
    }

}
