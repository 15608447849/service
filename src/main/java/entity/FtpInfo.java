package entity;

import com.winone.ftc.mtools.FileUtil;
import com.winone.ftc.mtools.Log;
import org.omg.PortableInterceptor.HOLDING;

import java.util.Properties;

/**
 * Created by lzp on 2017/5/8.
 * FTP信息
 */
public class FtpInfo{
    public final boolean isEmbedded;
    public String host;
    public int port;
    public String user;
    public String password;
    public String home;
    public StringBuilder launchInfo = new StringBuilder();

    public FtpInfo(Properties proper) {
        isEmbedded = Boolean.parseBoolean(proper.getProperty("ftp.embedded","true"));
        if (!isEmbedded){
            this.host  = proper.getProperty("ftp.external.host","127.0.0.1");
            this.port  = Integer.parseInt(proper.getProperty("ftp.external.port","21"));
            this.user  = proper.getProperty("ftp.external.user","admin");
            this.password  = proper.getProperty("ftp.external.password","admin");
            this.home  = proper.getProperty("ftp.external.home","./home");
        }
    }



    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("用户名: " + user).
                append(" ,密码: " + password).
                append(" ,主机地址: " + host).
                append(" ,端口号: " + port).
                append(" ,资源主目录: "+ home);
        return sb.toString();
    }

    public String getFtpSource(String sourcePath){
        return String.format("ftp://%s:%s@%s:%d/%s",user,password,host,port,sourcePath);
    }
}
