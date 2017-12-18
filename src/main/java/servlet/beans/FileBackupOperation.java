package servlet.beans;

import com.m.backup.client.FtcBackupClient;
import com.winone.ftc.mtools.NetworkUtil;
import entity.BackupParamBean;
import entity.ConfigManager;
import entity.Result;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by user on 2017/12/14.
 */
public class FileBackupOperation {

    final int type;
    final ArrayList<BackupParamBean.BAddress> addressItems ;
    final ArrayList<String> fileItems;
    final FtcBackupClient client;
    final boolean isPing;
    public FileBackupOperation(BackupParamBean backupParamBean,FtcBackupClient client) {
      this.addressItems = backupParamBean.getAddressItems();
      this.fileItems = backupParamBean.getFileItems();
      this.type = backupParamBean.getType();
        this.isPing = backupParamBean.isPing();
      this.client = client;

    }

    public List<Result> execute() throws Exception{
        if (addressItems==null || addressItems.size()==0) throw new IllegalArgumentException("not found valid backup server address.");
        List<Result> resultList = new ArrayList<>();

        Iterator<BackupParamBean.BAddress> iterator = addressItems.iterator();
        BackupParamBean.BAddress add;
        while(iterator.hasNext()){
            add = iterator.next();
            Result result = new Result();
            translate(add,result);
            resultList.add(result);
        }
        return resultList;
    }

    private void translate(BackupParamBean.BAddress add, Result result) {
        if (isPing && !NetworkUtil.ping(add.getIp())){
            result.setResultInfo(405,"fail by ping "+ add.getIp()+".");
            return;
        }
        if (client==null) throw new IllegalStateException("invalid backupFtcClient.");
            InetSocketAddress inetSocketAddress = new InetSocketAddress(add.getIp(),add.getPort());
            if (type==0){
                client.ergodicDirectory(inetSocketAddress);
                result.setResultInfo(200,"success");
            }else if (type==1){

                if (fileItems==null || fileItems.size()==0){
                    result.setResultInfo(407,"not found backup file item.");
                    return;
                }

                    File file;
                    StringBuilder stringBuilder = new StringBuilder();
                    for (String path : fileItems){

                        file = new File(ConfigManager.get().getFileDirectory()+ path);
                            try {
                                if (file.exists()){
                                    client.addBackupFile(file,inetSocketAddress);
                                }else{
                                    throw new FileNotFoundException();
                                }
                            } catch (Exception e) {
                                stringBuilder.append(path+",");
                            }
                    }
                    if (stringBuilder.length()>0){
                        stringBuilder.deleteCharAt(stringBuilder.length()-1);
                        result.setResultInfo(408,"local not found file list by ["+stringBuilder.toString()+"]");
                    }else{
                        result.setResultInfo(200,"success by "+ add.getIp()+":"+add.getPort()+ " backup file.");
                    }
        }
    }
}
