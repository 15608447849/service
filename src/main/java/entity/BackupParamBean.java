package entity;

import java.util.ArrayList;

/**
 * Created by user on 2017/11/29.
 */
public class BackupParamBean {
   private String ip;
   private int port;
   private int type;
   private ArrayList<String> fileItem;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public ArrayList<String> getFileItem() {
        return fileItem;
    }

    public void setFileItem(ArrayList<String> fileItem) {
        this.fileItem = fileItem;
    }
}
