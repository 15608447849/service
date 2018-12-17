package server.entity;

import com.m.backup.server.FtcBackupServer;
import properties.abs.ApplicationPropertiesBase;
import properties.annotations.PropertiesFilePath;
import properties.annotations.PropertiesName;

import java.net.InetSocketAddress;

@PropertiesFilePath("/backup.properties")
public class BackupProperties extends ApplicationPropertiesBase {

    private BackupProperties(){}

    private static class Holder{
        private static BackupProperties INSTANCE = new BackupProperties();
    }

    public static BackupProperties get(){
        return Holder.INSTANCE;
    }

    @PropertiesName("ftc.backup.server.local.port")
    public int localPort;
    @PropertiesName("ftc.backup.server.remote.address")
    public String remoteListStr;
    @PropertiesName("ftc.backup.server.first.boot")
    public boolean isBoot;
    @PropertiesName("ftc.backup.server.upload.auto")
    public boolean isAuto;

    public InetSocketAddress[] remoteList;

    public FtcBackupServer ftcBackupServer;
    @Override
    protected void initialization() {
        String[] arr = remoteListStr.split(",");
        remoteList = new InetSocketAddress[arr.length];
        for (int i = 0; i < arr.length ;i++){
            String[] address = arr[i].split(":");
            remoteList[i] = new InetSocketAddress(address[0],Integer.parseInt(address[1]));
        }
    }


}
