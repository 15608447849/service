package entity;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by user on 2017/11/29.
 */
public class BackupParamBean {
    public static class BAddress{
        private String ip;
        private int port;
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
    }


    private ArrayList<BAddress> addressItems;
    private ArrayList<String> fileItems;
    private int type;
    private boolean isPing = false;
    public int getType() {
        return type;
    }
    public void setType(int type) {
        this.type = type;
    }

    public ArrayList<BAddress> getAddressItems() {return addressItems;}
    public void setAddressItems(ArrayList<BAddress> addressItems) {this.addressItems = addressItems;}

   public ArrayList<String> getFileItems() {return fileItems;}
   public void setFileItems(ArrayList<String> fileItems) {
        this.fileItems = fileItems;
    }
   public ArrayList<String> getFileItem() {
        return fileItems;
    }
   public void setFileItem(ArrayList<String> fileItems) {
        this.fileItems = fileItems;
    }

    public boolean isPing() {
        return isPing;
    }

    public void setPing(boolean ping) {
        isPing = ping;
    }
}
