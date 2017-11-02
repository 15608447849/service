package entity;

import com.winone.ftc.mtools.FileUtil;
import com.winone.ftc.mtools.Log;
import org.omg.PortableInterceptor.HOLDING;

/**
 * Created by lzp on 2017/5/8.
 * FTP信息
 */
public class FtpInfo{
    public final String host;
    public final int port;
    public final String userName;
    public final String password;
    public final String homeDir;

    public FtpInfo(String host,int port, String userName, String password, String homedir) {
        this.port = port;
        this.host = host;
        this.userName = userName;
        this.password = password;
        this.homeDir = homedir;

    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("用户名: " + userName).append(" ,密码: " + password).append(" ,主机地址: " + host).append(" ,端口号: " + port).append(" ,资源主目录: "+ homeDir);
        return sb.toString();
    }
    public String getFtpSource(String sourcePath){
        return String.format("ftp://%s:%s@%s:%d/%s",userName,password,host,port,sourcePath);
    }
}
