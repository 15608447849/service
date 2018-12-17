package server.servlet.beans;

import com.m.backup.client.FtcBackupClient;
import com.winone.ftc.mtools.NetworkUtil;
import server.entity.BackupProperties;
import server.entity.Result;
import server.entity.WebProperties;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 2017/12/14.
 */
public class FileBackupOperation {

    private ArrayList<String> fileItems;
    public FileBackupOperation(ArrayList<String> fileItems) {
      this.fileItems = fileItems;
    }

    public List<Result> execute() throws Exception{
        List<Result> resultList = new ArrayList<>();
        for (InetSocketAddress remoteAddress :  BackupProperties.get().remoteList){
            Result result = new Result();
            translate(remoteAddress,result);
            resultList.add(result);
        }

        return resultList;
    }

    private void translate(InetSocketAddress add, Result result) {
        if (!NetworkUtil.ping(add.getAddress().getHostAddress())){
            result.Info(405,"fail by ping "+ add.getAddress().getHostAddress() +".");
            return;
        }
        FtcBackupClient client =  BackupProperties.get().ftcBackupServer.getClient();

        if (fileItems==null || fileItems.size()==0){
            result.Info(407,"not found backup file item.");
            return;
        }

        File file;
        StringBuilder stringBuilder = new StringBuilder();
        for (String path : fileItems){
            file = new File(WebProperties.get().rootPath + path);
            try {
                if (file.exists()){
                    client.addBackupFile(file,add);
                }else{
                    throw new FileNotFoundException();
                }
            } catch (Exception e) {
                stringBuilder.append(path+",");
            }
        }
        if (stringBuilder.length() > 0){
            stringBuilder.deleteCharAt(stringBuilder.length()-1);
            result.Info(408,"local not found file list by ["+stringBuilder.toString()+"]");
        }else{
            result.Info(200,"success by "+ add.getAddress().getHostAddress() +":"+add.getPort()+ " backup file.");
        }

    }
}
