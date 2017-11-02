package entity;


import com.winone.ftc.mtools.FileUtil;
import com.winone.ftc.mtools.Log;
import com.winone.ftc.mtools.StringUtil;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import java.io.*;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Properties;

/**
 * Created by lzp on 2017/5/13.
 *
 */
public class Config {
    public static String APP_DIR = FileUtil.PROGRESS_HOME_PATH + FileUtil.SEPARATOR + "res";
    public final String WEB_PRPP = APP_DIR + FileUtil.SEPARATOR+"webserver.properties";
    public final String FTP_USERS_PRPP = APP_DIR  +FileUtil.SEPARATOR +"ftpusers.properties";
    public final String EXTERNAL_FTP = APP_DIR  +FileUtil.SEPARATOR +"ftpexternal.properties"; //外部FTP信息
    public final String FTP_CONFIG_XML = APP_DIR  +FileUtil.SEPARATOR + "ftpconfig.xml";
    public final String EDITER_PATH = APP_DIR+FileUtil.SEPARATOR+"config.json";

    private String webMainPath;
    private String fileDirectory; //文件存储根目录
    private String tempDirectory;//临时文件存储目录
    private String ip;//web ip
    private int port; //web端口号
    private boolean useEmbeddedFtp = true;
    private String baiduConfigJsonPath;//百度附文本配置文件路径
    private boolean isAddEditorSevlet;
    private String ftpConfigPath;//ftp配置文件
    private boolean existFtpConfig;//存在ftp配置文件
    private int transientServerPort;//文件转换程序端口号

    private DiskFileItemFactory diskFileItemFactory;
    private FtpInfo ftpInfo;

    private static class InstantHolder{
        private static Config config = new Config();
    }
    public static Config get(){
        return InstantHolder.config;
    }
    //初始化
    public static void Init(String path){
        if (!StringUtil.isEntry(path)){
            APP_DIR = path;
        }
        if (!FileUtil.checkDir(APP_DIR)){
            throw new IllegalStateException("启动失败,错误的执行路径:"+APP_DIR);
        }

    }

    //构造函数
    private Config(){
        //判断文件
        if (!FileUtil.checkFileNotCreate(WEB_PRPP)) {
            StringBuilder sb = new StringBuilder();
            sb.append("WEB服务器配置文件不存在,已创建并写入默认配置 : "+ WEB_PRPP+"\n");
            sb.append(
                    "ip=127.0.0.1\n" +
                    "port=8080\n" +
                    "webMainPath=/ftc\n" +
                    "fileDirectory=./res/home\n"+
                     "useEmbeddedFtp=true\n"+
                     "transientServerPort=12345"
                );
            LinkedHashMap<String,String> map = new LinkedHashMap<>();
                map.put("ip","127.0.0.1");
                map.put("port","8080");
                map.put("webMainPath","/ftc");
                map.put("fileDirectory","./res/home");
                map.put("useEmbeddedFtp","true");
                map.put("transientServerPort","12345");

            FileUtil.writeMapToFile(map,WEB_PRPP);
            Log.e(sb.toString());
        }
        //初始化值
        initParam();

        checkFtpConfig();
        checkFtpSetting();
        checkEditorPath();
    }

    private void checkFtpConfig() {
        Object[] objects = checkFileByBool(FTP_CONFIG_XML);
        ftpConfigPath = (String)objects[0];
        existFtpConfig = (Boolean)objects[1];
    }

    private void checkEditorPath() {
        Object[] objects = checkFileByBool(EDITER_PATH);
        baiduConfigJsonPath = (String)objects[0];
        isAddEditorSevlet = (Boolean)objects[1];
    }

    private Object[] checkFileByBool(String path){
        Object[] objects = {null,false};
        String val;
        if (FileUtil.checkFileNotCreate(path)){
            try {
                val = new File(path) .getCanonicalPath();
                objects[0] = val.replaceAll("\\\\",FileUtil.SEPARATOR);
                objects[1] = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return objects;
    }

    private void checkFtpSetting() {
        if (useEmbeddedFtp){
            //检查FTP配置文件
            if (!FileUtil.checkFileNotCreate(FTP_USERS_PRPP)){
                StringBuilder sb = new StringBuilder();
                sb.append("\nFTP服务器用户配置文件不存在,已创建并写入默认配置 : "+ FTP_USERS_PRPP+"\n");
                sb.append(
                                "ftpserver.user.admin.userpassword=admin\n" +
                                "ftpserver.user.admin.homedirectory=./res/home\n" +
                                "ftpserver.user.admin.enableflag=true\n" +
                                "ftpserver.user.admin.writepermission=true\n" +
                                "ftpserver.user.admin.maxloginnumber=最大登录数量,不限制设置0\n" +
                                "ftpserver.user.admin.maxloginperip=最大并发登录数量,不限制设置0\n" +
                                "ftpserver.user.admin.idletime=客户端闲置时间,不限制设置0\n" +
                                "ftpserver.user.admin.uploadrate=上传限速,不限制设置0\n" +
                                "ftpserver.user.admin.downloadrate=下载限速,不限制设置0\n"
                );
                String key = "ftpserver.user.admin.";
                LinkedHashMap<String,String> map = new LinkedHashMap<>();
                map.put(key+"userpassword","admin");
                map.put(key+"homedirectory","./res/home");
                map.put(key+"enableflag","true");
                map.put(key+"writepermission","true");
                map.put(key+"maxloginnumber","0");
                map.put(key+"maxloginperip","0");
                map.put(key+"idletime","0");
                map.put(key+"uploadrate","0");
                map.put(key+"downloadrate","0");
                FileUtil.writeMapToFile(map,FTP_USERS_PRPP);
               Log.e( sb.toString());
            }
        }else{
            //检测外部ftp信息文件
            if (!FileUtil.checkFileNotCreate(EXTERNAL_FTP)){
                Log.e("警告","未找到外部FTP服务器信息,请配置文件:"+EXTERNAL_FTP+"\n"+
                        "host=主机ip地址\n" +
                        "port=端口号\n" +
                        "user=用户名\n" +
                        "password=密码\n" +
                        "home=资源主目录\n" +
                        ")");
            }else{
                //存在外部FTP信息文件
                setFtpInfo(new File(EXTERNAL_FTP));
            }
        }
    }

    //初始化值
    private void initParam() {

        InputStream in = null;
        try {
            in =  new FileInputStream(WEB_PRPP);
            Properties proper = new Properties();
            proper.load(in);
            in.close();
            String ip = proper.getProperty("ip");
            String port = proper.getProperty("port");
            String webMainPath = proper.getProperty("webMainPath");
            String fileDirectory = proper.getProperty("fileDirectory");
            String useEmbeddedFtp = proper.getProperty("useEmbeddedFtp","true");
            String transientServerPort = proper.getProperty("transientServerPort","12345");
            setIp(StringUtil.isEntry(ip)? InetAddress.getLocalHost().getHostAddress():ip);
            try{
            if (!StringUtil.isEntry(port)){
                    int portInt = Integer.parseInt(port);
                    setPort(portInt);
            }else{
                new NullPointerException("null");
            }
            }catch (Exception e){
                Log.e("参数不正确 port="+port+"  ,设置默认端口8080");
                setPort(8080);
            }

            try{
                if (!StringUtil.isEntry(transientServerPort)){
                    int portInt = Integer.parseInt(transientServerPort);
                    setTransientServerPort(portInt);
                }else{
                    new NullPointerException("null");
                }
            }catch (Exception e){
                Log.e("参数不正确 transientServerPort="+transientServerPort+"  ,设置默认端口12345");
                setTransientServerPort(12345);
            }

            setWebMainPath(StringUtil.isEntry(webMainPath)?"ftc":webMainPath);
            setFileDirectory(StringUtil.isEntry(fileDirectory)?APP_DIR+FileUtil.SEPARATOR+"resource":fileDirectory);//资源目录

            try{
                boolean useEmbeddedFtpBoolean = Boolean.parseBoolean(useEmbeddedFtp);
                setUseEmbeddedFtp(useEmbeddedFtpBoolean);
            }catch (Exception e){
                Log.e("参数不正确 useEmbeddedFtp="+useEmbeddedFtp+"  ,默认打开内嵌FTP服务器");
                setUseEmbeddedFtp(true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (in!=null){
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
    }


    public String getWebMainPath() {
        return webMainPath;
    }

    public void setWebMainPath(String webMainPath) {
        this.webMainPath = webMainPath.startsWith("/")?webMainPath:("/"+webMainPath);
    }

    public String getFileDirectory() {
        return fileDirectory;
    }

    public void setFileDirectory(String fileDirectory) {
        fileDirectory = fileDirectory.replaceAll("\\\\",FileUtil.SEPARATOR);
        this.fileDirectory = fileDirectory.endsWith(FileUtil.SEPARATOR)?fileDirectory:fileDirectory+FileUtil.SEPARATOR;
        if (!FileUtil.checkDir(this.fileDirectory)) throw new IllegalStateException("文件夹不存在:"+this.fileDirectory);
        setTempDirectory(fileDirectory+FileUtil.SEPARATOR+"temporary"+FileUtil.SEPARATOR);//临时目录
    }

    public String getTempDirectory() {
        return tempDirectory;
    }

    public void setTempDirectory(String tempDirectory) {
        tempDirectory = tempDirectory.replaceAll("\\\\",FileUtil.SEPARATOR);
        this.tempDirectory = tempDirectory.endsWith(FileUtil.SEPARATOR)?tempDirectory:tempDirectory+FileUtil.SEPARATOR;
        if (!FileUtil.checkDir(this.tempDirectory)) throw new IllegalStateException("文件夹不存在:"+this.tempDirectory);
    }

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

    public DiskFileItemFactory getDiskFileItemFactory() {
        if (diskFileItemFactory==null){
            diskFileItemFactory = new DiskFileItemFactory();
            // 设定临时文件夹为repositoryPath
            diskFileItemFactory.setRepository(new File(getTempDirectory()));
            // 设定上传文件的值，如果上传文件大于200M，就可能在repository
            // 所代表的文件夹中产生临时文件，否则直接在内存中进行处理
            diskFileItemFactory.setSizeThreshold(1024 * 1024 * 1024 * 200);
        }
        return diskFileItemFactory;
    }

    public int getTransientServerPort() {
        return transientServerPort;
    }

    public void setTransientServerPort(int transientServerPort) {
        this.transientServerPort = transientServerPort;
    }

    public String getBaiduConfigJsonPath() {
        return baiduConfigJsonPath;
    }

    public boolean isAddEditorSevlet() {
        return isAddEditorSevlet;
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
        return String.format("http://%s:%d/%s",ip,port,path);
    }
    //获取 ftp 文件url
    public String getFtpUrl(String path){
        return ftpInfo.getFtpSource(path);
    }

    public boolean isUseEmbeddedFtp() {
        return useEmbeddedFtp;
    }

    public void setUseEmbeddedFtp(boolean useEmbeddedFtp) {
        this.useEmbeddedFtp = useEmbeddedFtp;
    }

    public FtpInfo getFtpInfo(){
        return this.ftpInfo;
    }
    public void setFtpInfo(FtpInfo ftpInfo) {
        this.ftpInfo = ftpInfo;
    }
    public void setFtpInfo(File extFile) {

        InputStream is = null;
        try {
            is = new FileInputStream(extFile);
            Properties properties = new Properties();
            properties.load(is);
            String host = properties.getProperty("host");
            String port = properties.getProperty("port");
            String user = properties.getProperty("user");
            String password = properties.getProperty("password");
            String home = properties.getProperty("home");
            int portInt = Integer.valueOf(port);
            setFtpInfo(new FtpInfo(host,portInt,user,password,home));
        } catch (Exception e) {
           Log.e("警告","外部FTP服务器信息文件配置错误."+e.getMessage()+"\n 配置参数文件如下形式:\n"+"host=127.0.0.1\nport=21\nuser=admin\npassword=admin\nhome=/local");
        }finally {
            try {
                is.close();
            } catch (IOException e) {
            }
        }
    }
}
