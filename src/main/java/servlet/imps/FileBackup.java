package servlet.imps;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.m.backup.client.FtcBackupClient;
import com.winone.ftc.mtools.FileUtil;
import com.winone.ftc.mtools.Log;
import com.winone.ftc.mtools.NetworkUtil;
import entity.*;
import servlet.iface.Mservlet;
import sun.security.krb5.Config;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;

/**
 * Created by lzp on 2017/11/29.
 * 文件同步
 */
public class FileBackup extends Mservlet {

    private final HashMap<InetSocketAddress,FtcBackupClient> hashMap = new HashMap<>();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req,resp);
        Result result = new Result();
        result.setResultInfo(401,"not found param key.");
        try {
            String json = req.getHeader("backup-param");
            if (json!=null){
                BackupParamBean bean = new Gson().fromJson(json,BackupParamBean.class);
                InetSocketAddress inetSocketAddress = new InetSocketAddress(bean.getIp(),bean.getPort());
                FtcBackupClient client = hashMap.get(inetSocketAddress);
                if (client==null){
                    if (NetworkUtil.ping(bean.getIp())){

                        client = new FtcBackupClient(ConfigManager.get().getFileDirectory(),inetSocketAddress,10,2000);
                        hashMap.put(inetSocketAddress,client);
                    }else{
                        result.setResultInfo(405,"server ip -  "+ bean.getIp()+" is fail.");
                    }
                }
                if (client!=null){
                    int type = bean.getType();
                    if (type==0){
                        client.ergodicDirectory();
                        result.setResultInfo(200,"success");
                    }else if (type==1){
                        List<String> filePaths = bean.getFileItem();
                        if (filePaths==null || filePaths.size()==0){
                            result.setResultInfo(407,"not found backup file item.");
                        }else{
                            File file;
                            StringBuilder stringBuilder = new StringBuilder();
                            for (String path : filePaths){

                                file = new File(ConfigManager.get().getFileDirectory()+ path);
                                if (file.exists()){
                                    client.addBackupFile(file);
                                }else{
                                    stringBuilder.append(path);
                                }
                            }
                            if (stringBuilder.length()>0){
                                stringBuilder.deleteCharAt(stringBuilder.length()-1);
                                result.setResultInfo(408,"local not found file list : ["+stringBuilder.toString()+"]");
                            }else{
                                result.setResultInfo(200,"success");
                            }
                        }
                    }else{
                        result.setResultInfo(406,"unknown type by "+type);

                    }
                }
            }
        } catch (JsonSyntaxException e) {
            result.setResultInfo(400,e.toString());
        }finally {
            writeJson(resp,result);
        }
    }
}
