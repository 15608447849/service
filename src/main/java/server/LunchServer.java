package server;

import com.winone.ftc.mtools.FileUtil;
import com.winone.ftc.mtools.Log;
import entity.Config;
import entity.FtpInfo;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.PathResourceManager;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentManager;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;

import org.apache.ftpserver.impl.DefaultFtpServer;
import org.apache.ftpserver.listener.Listener;

import org.apache.ftpserver.listener.nio.NioListener;

import org.apache.ftpserver.usermanager.*;


import org.springframework.context.support.FileSystemXmlApplicationContext;
import servlet.imps.FileUpLoad;
import io.undertow.servlet.api.DeploymentInfo;

import servlet.imps.Query;
import servlet.imps.TestEditor;
import udpconnect.FileQuery;
import udpconnect.UDPManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.*;


/**
 * Created by lzp on 2017/5/13.
 * 容器入口
 */
public class LunchServer {

    public static void main(String[] args) {

        Config.Init(null);
        if (Config.get().isUseEmbeddedFtp()){
            //开启FTP服务器
            startFTPServer();
        }else{
            if (Config.get().getFtpInfo()!=null){
                Log.e("使用外部FTP程序搭建FTP服务器:\n"+Config.get().getFtpInfo().toString()+"\n");
            }
        }
        //开启web文件服务器
        startWebServer();
        //开启udp服务器
//       startUdpServer();
    }

    /**
     * UDP SERVER
     */
    private static void startUdpServer() {
        //设置文件查询主目录
        FileQuery.home = Paths.get(Config.get().getFileDirectory());
        try {
            new UDPManager(Config.get().getIp(),10000);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.e("文件信息查询 : udp://"+Config.get().getIp()+":10000/文件相对路径");
    }

    private static void startWebServer() {

        try {
            //开启web服务器
            DeploymentInfo servletBuilder = io.undertow.servlet.Servlets.deployment()
                    .setClassLoader(LunchServer.class.getClassLoader())
                    .setContextPath(Config.get().getWebMainPath())
                    .setDeploymentName("file_server.war")
                    .addServlets(
                            io.undertow.servlet.Servlets.servlet("FileUpLoad", FileUpLoad.class).addMapping("/upload")
                    );
            //添加付文本编辑器
            if ( Config.get().isAddEditorSevlet()){
                servletBuilder.addServlet( io.undertow.servlet.Servlets.servlet("TestEditor", TestEditor.class).addMapping("/editor"));
            }
                servletBuilder.addServlet(io.undertow.servlet.Servlets.servlet("Query", Query.class).addMapping("/info"));
            DeploymentManager manager = Servlets.defaultContainer()
                    .addDeployment(servletBuilder);
            manager.deploy();
            HttpHandler servletHandler = manager.start();

            PathHandler path = Handlers.path()
                    .addPrefixPath(
                            Config.get().getWebMainPath(),
                            servletHandler
                    );
            path.addPrefixPath(
                    "/",   //下载地址- 请勿修改
                    io.undertow.Handlers.resource(

                            new PathResourceManager(
                                    Paths.get(Config.get().getFileDirectory()),2048L,true,false,true,(String[])null
                            )
                    )
            );
            Undertow server = Undertow
                    .builder()
                    .addHttpListener(
                            Config.get().getPort(),
                            Config.get().getIp())
                    .setHandler(path)
                    .build();
            server.start(); //开始运行
           StringBuffer stringBuffer = new StringBuffer();
           stringBuffer.append("服务器信息查询:"+Config.get().getHttpUrl(Config.get().getWebMainPath().substring(1)+"/info")+"\n");
            stringBuffer.append("资源目录:" +Config.get().getFileDirectory() +"\n"+
                    "文件访问url: "+Config.get().getHttpUrl("资源相对路径")+"\n"+
                    "文件上传url: " + Config.get().getHttpUrl(Config.get().getWebMainPath().substring(1)+"/upload") +" ;访问类型:POST"
            );
            if ( Config.get().isAddEditorSevlet()){
                stringBuffer.append("\n已启动附文本编辑器组件: "+Config.get().getHttpUrl(Config.get().getWebMainPath().substring(1)+"/editor"));
                stringBuffer.append("\n已启动附文本编辑器组件测试地址: "+Config.get().getHttpUrl(Config.get().getWebMainPath().substring(1)+"/editor?action=config"));
            }
            stringBuffer.append("\n本地文件转换服务端口号:"+ Config.get().getTransientServerPort());
            Log.e("WEB文件服务器信息:\n"+ stringBuffer.toString()+"\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static void startFTPServer() {
        try{
            FtpServer server = null;
            if (Config.get().isExistFtpConfig()){
                Log.e("存在FTP配置文件路径: "+ Config.get().getFtpConfigPath());
                try {
                    //配置文件存在
                    FileSystemXmlApplicationContext ctx = new FileSystemXmlApplicationContext(Config.get().getFtpConfigPath());
                    if(ctx.containsBean("server")) {
                        server = (FtpServer)ctx.getBean("server");
                    } else {
                        String[] beanNames = ctx.getBeanNamesForType(FtpServer.class);
                        if(beanNames.length >= 1) {
                            Log.e("使用配置中定义的第一个服务器 : " + beanNames[0]);
                            server = (FtpServer)ctx.getBean(beanNames[0]);
                        } else {
                            Log.e("XML配置文件不包含服务器配置,使用默认配置");
                            server = ftpApplicationDefault();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("FTP配置文件不正确: "+e.getMessage()+"\n使用默认配置.");
                    server = ftpApplicationDefault();
                }
            }else{
                server = ftpApplicationDefault();
            }
            server.start();

            if (server instanceof DefaultFtpServer){
                int port = 21;
                String host = Config.get().getIp();
                String user,pass,homedir;
                DefaultFtpServer dfs = (DefaultFtpServer)server;
                 Listener listener = dfs.getListener("default");
                if(listener!=null && listener instanceof NioListener){
                    NioListener nio =  (NioListener)listener;
                    port =nio.getPort();
                }
                user = dfs.getUserManager().getAdminName();
                homedir =  dfs.getUserManager().getUserByName(user).getHomeDirectory();
                InputStream is = null;
                try{
                    is = new FileInputStream(Config.get().FTP_USERS_PRPP);
                    Properties properties = new Properties();
                    properties.load(is);
                    pass = properties.getProperty("ftpserver.user.admin.userpassword");
                }catch (Exception e){
                    pass = user;
                }finally{
                    if(is!=null){
                        try{is.close();}catch(Exception es){}
                    }
                }
                Config.get().setFtpInfo(new FtpInfo(host,port,user,pass,homedir));
            }
            Log.e("FTP服务器启动成功:\n"+Config.get().getFtpInfo().toString()+"\n");
        } catch (Exception e) {
        e.printStackTrace();
        }
    }

    private static FtpServer ftpApplicationDefault() throws Exception{
        PropertiesUserManagerFactory propertiesUserManagerFactory = new PropertiesUserManagerFactory();
        propertiesUserManagerFactory.setFile(new File(Config.get().FTP_USERS_PRPP));
        propertiesUserManagerFactory.setPasswordEncryptor(new ClearTextPasswordEncryptor());
        FtpServerFactory ftpServerFactory = new FtpServerFactory();
        ftpServerFactory.setUserManager( propertiesUserManagerFactory.createUserManager());
        return ftpServerFactory.createServer();
    }
}




