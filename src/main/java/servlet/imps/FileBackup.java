package servlet.imps;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.m.backup.client.FtcBackupClient;
import com.winone.ftc.mtools.FileUtil;
import com.winone.ftc.mtools.Log;
import com.winone.ftc.mtools.NetworkUtil;
import entity.*;
import servlet.beans.FileBackupOperation;
import servlet.iface.Mservlet;
import sun.security.krb5.Config;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by lzp on 2017/11/29.
 * 文件同步
 */
public class FileBackup extends Mservlet {



    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req,resp);
        Result result = new Result();
        result.setResultInfo(401,"not found param key.");
        List<Result> resultList = null;
        try {
                BackupParamBean bean = getJsonObject(req,"backup-json",BackupParamBean.class);
                resultList = new FileBackupOperation(bean,ConfigManager.get().getBackupClient()).execute();
        } catch (Exception e) {
            result.setResultInfo(400,e.toString());
        }finally {
            Object object = resultList==null?result:resultList;
            writeJson(resp,object);
        }
    }
}
