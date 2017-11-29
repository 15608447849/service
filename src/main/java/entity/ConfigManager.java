package entity;


import com.winone.ftc.mtools.FileUtil;
import com.winone.ftc.mtools.Log;
import com.winone.ftc.mtools.StringUtil;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Properties;

/**
 * Created by lzp on 2017/5/13.
 *
 */
public class ConfigManager {
    public static String APP_DIR = FileUtil.PROGRESS_HOME_PATH + FileUtil.SEPARATOR + "res";
    public final String SERVICE_PROP = APP_DIR + FileUtil.SEPARATOR+"service.properties";
    public final String FTP_USERS_PRPP = APP_DIR  +FileUtil.SEPARATOR +"ftpusers.properties";
    public final String FTP_CONFIG_XML = APP_DIR  +FileUtil.SEPARATOR + "ftpconfig.xml";
    public final String EDITOR_PATH = APP_DIR+FileUtil.SEPARATOR+"config.json";


    private String baidu_ConfigJsonPath;//百度附文本配置文件真实路径
    private boolean isAddEditorServlet;
    private String ftpConfigPath;//ftp配置文件真实路径
    private boolean existFtpConfig;//是否存在ftp配置文件
    private boolean backupSuccess; //是否成功启动 备份文件socket
    private WebInfo webInfo;
    private FtpInfo ftpInfo;
    private FtcInfo ftcInfo;



    public String getFileDirectory() {
        return webInfo.getFileDirectory();
    }

    public int getPort() {
        return webInfo.getPort();
    }

    public String getIp() {
        return webInfo.getIp();
    }

    public int getBackupPort() {
        return ftcInfo.getBackupPort();
    }

    public void setBackupSuccess(boolean backupSuccess) {
        this.backupSuccess = backupSuccess;
    }

    public boolean isAddBackupServlet() {
        return backupSuccess;
    }

    private static class InstantHolder{
        private static ConfigManager config = new ConfigManager();
    }
    public static ConfigManager get(){
        return InstantHolder.config;
    }

    //构造函数
    private ConfigManager(){
        try{
            if (!FileUtil.checkDir(APP_DIR)){
                throw new IllegalStateException("启动失败,错误的执行路径:"+APP_DIR);
            }
            InputStream in = new FileInputStream(SERVICE_PROP);
            Properties proper = new Properties();
            proper.load(in);
            in.close();
            webInfo = new WebInfo(proper);
            ftpInfo = new FtpInfo(proper);
            ftcInfo = new FtcInfo(proper);
            checkPath();
        }catch (Exception e){
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private void checkPath() throws FileNotFoundException {
        if (ftpInfo.isEmbedded){
            if (!FileUtil.checkFileNotCreate(FTP_USERS_PRPP)){
                throw new FileNotFoundException("找不到内嵌FTP服务用户信息配置文件, "+FTP_USERS_PRPP);
            }
            Object[] objects = checkFileByBool(FTP_CONFIG_XML);
            ftpConfigPath = (String)objects[0];
            existFtpConfig = (Boolean)objects[1];
        }
        //百度附文本
        Object[] objects = checkFileByBool(EDITOR_PATH);
        baidu_ConfigJsonPath = (String)objects[0];
        isAddEditorServlet = (Boolean)objects[1];
    }

    private Object[] checkFileByBool(String path){
        Object[] objects = {null,false};
        String val;
        if (FileUtil.checkFileNotCreate(path)){
            try {
                val = new File(path) .getCanonicalPath();
                objects[0] = FileUtil.replaceFileSeparatorAndCheck(val,null,null);
                objects[1] = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return objects;
    }

    public String getWebMainPath() {
        return webInfo.getWebMainPath();
    }
    public DiskFileItemFactory getDiskFileItemFactory() {
        return webInfo.getDiskFileItemFactory();
    }

    public int getTransientServerPort() {
        return ftcInfo.getTranslatePort();
    }
    public String get_baidu_ConfigJsonPath() {
        return baidu_ConfigJsonPath;
    }

    public boolean isAddEditorServlet() {
        return isAddEditorServlet;
    }

    public String getFtpConfigPath() {
        return ftpConfigPath;
    }

    public boolean isExistFtpConfig() {
        return existFtpConfig;
    }

    public ServletFileUpload getServletFileUploader(){
        return new ServletFileUpload(getDiskFileItemFactory());
    }
    //获取 资源 文件url
    public String getHttpUrl(String path){
        if (StringUtil.isEntry(path)) return null;
        return String.format("http://%s:%d/%s",webInfo.getIp(),webInfo.getPort(),path);
    }
    //获取 ftp 文件url
    public String getFtpUrl(String path){
        return ftpInfo.getFtpSource(path);
    }

    public boolean isUseEmbeddedFtp() {
        return ftpInfo.isEmbedded;
    }
    public FtpInfo getFtpInfo(){
        return this.ftpInfo;
    }


}
