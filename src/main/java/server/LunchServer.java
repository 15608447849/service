package server;

import com.m.backup.client.FtcBackupClient;
import com.m.backup.server.FtcBackupServer;
import com.winone.ftc.mtools.Log;
import entity.ConfigManager;
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
import servlet.imps.FileBackup;
import servlet.imps.FileUpLoad;
import io.undertow.servlet.api.DeploymentInfo;
import servlet.imps.Query;
import servlet.imps.TestEditor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.file.Paths;
import java.util.*;


/**
 * Created by lzp on 2017/5/13.
 * 容器入口
 */
public class LunchServer {

    public static void main(String[] args) {

        //开启文件备份服务
        startFileBackupServer();
        //开启FTP服务器
        startFTPServer();
        //开启web文件服务器
        startWebServer();

    }



    private static void startWebServer() {
        try {
            //开启web服务器
            DeploymentInfo servletBuilder = io.undertow.servlet.Servlets.deployment()
                    .setClassLoader(LunchServer.class.getClassLoader())
                    .setContextPath(ConfigManager.get().getWebMainPath())
                    .setDeploymentName("file_server.war")
                    .addServlets(
                            io.undertow.servlet.Servlets.servlet("FileUpLoad", FileUpLoad.class).addMapping("/upload")
                    );
            //添加付文本编辑器
            if ( ConfigManager.get().isAddEditorServlet()){
                servletBuilder.addServlet( io.undertow.servlet.Servlets.servlet("TestEditor", TestEditor.class).addMapping("/editor"));
            }
            if (ConfigManager.get().isAddBackupServlet()){
                servletBuilder.addServlet(io.undertow.servlet.Servlets.servlet("FileBackup", FileBackup.class).addMapping("/backup"));
            }
                servletBuilder.addServlet(io.undertow.servlet.Servlets.servlet("Query", Query.class).addMapping("/info"));
            DeploymentManager manager = Servlets.defaultContainer()
                    .addDeployment(servletBuilder);
            manager.deploy();
            HttpHandler servletHandler = manager.start();

            PathHandler path = Handlers.path()
                    .addPrefixPath(
                            ConfigManager.get().getWebMainPath(),
                            servletHandler
                    );
            path.addPrefixPath(
                    "/",   //下载地址- 请勿修改
                    io.undertow.Handlers.resource(

                            new PathResourceManager(
                                    Paths.get(ConfigManager.get().getFileDirectory()),2048L,true,false,true,(String[])null
                            )
                    )
            );
            Undertow server = Undertow
                    .builder()
                    .addHttpListener(
                            ConfigManager.get().getPort(),
                            ConfigManager.get().getIp())
                    .setHandler(path)
                    .build();
            server.start(); //开始运行
            Log.println("文件服务启动成功 , 信息查询:"+ ConfigManager.get().getHttpUrl(ConfigManager.get().getWebMainPath().substring(1)+"/info")+"\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static void startFTPServer() {

        FtpInfo info = ConfigManager.get().getFtpInfo();
        if (!ConfigManager.get().isUseEmbeddedFtp()){
            info.launchInfo.append("[ The outreach program builds the FTP server ]");
            return;
        }
        try{
            FtpServer server = null;
            if (ConfigManager.get().isExistFtpConfig()){
                info.launchInfo.append("[ Ftp config file local path by "+ ConfigManager.get().getFtpConfigPath()+" ]");
                try {
                    //配置文件存在
                    FileSystemXmlApplicationContext ctx = new FileSystemXmlApplicationContext(ConfigManager.get().getFtpConfigPath());
                    if(ctx.containsBean("server")) {
                        server = (FtpServer)ctx.getBean("server");
                    } else {
                        String[] beanNames = ctx.getBeanNamesForType(FtpServer.class);
                        if(beanNames.length >= 1) {
                            info.launchInfo.append("[ Using the first server defined in the configuration by " + beanNames[0]+"]");
                            server = (FtpServer)ctx.getBean(beanNames[0]);
                        } else {
                           throw new IllegalArgumentException(" 'The available server information was not found' ");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    info.launchInfo.append("[ Using the default configuration, the configuration file is incorrect : "+e.getMessage()+" ]");
                    server = ftpApplicationDefault();
                }
            }else{
                server = ftpApplicationDefault();
            }
            server.start();

            if (server instanceof DefaultFtpServer){
                int port = 21;
                String host = ConfigManager.get().getIp();
                String user,pass,home;
                DefaultFtpServer dfs = (DefaultFtpServer)server;
                 Listener listener = dfs.getListener("default");
                if(listener!=null && listener instanceof NioListener){
                    NioListener nio =  (NioListener)listener;
                    port =nio.getPort();
                }
                user = dfs.getUserManager().getAdminName();
                home =  dfs.getUserManager().getUserByName(user).getHomeDirectory();
                InputStream is = null;
                try{
                    is = new FileInputStream(ConfigManager.get().FTP_USERS_PRPP);
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
                    info.host = host;
                    info.port = port;
                    info.user = user;
                    info.password = pass;
                    info.home = home;
            }
            //Log.println();
            info.launchInfo.append("[ FTP Server startup success. ]");
        } catch (Exception e) {
        e.printStackTrace();
        }
    }

    private static FtpServer ftpApplicationDefault() throws Exception{
        PropertiesUserManagerFactory propertiesUserManagerFactory = new PropertiesUserManagerFactory();
        propertiesUserManagerFactory.setFile(new File(ConfigManager.get().FTP_USERS_PRPP));
        propertiesUserManagerFactory.setPasswordEncryptor(new ClearTextPasswordEncryptor());
        FtpServerFactory ftpServerFactory = new FtpServerFactory();
        ftpServerFactory.setUserManager( propertiesUserManagerFactory.createUserManager());
        return ftpServerFactory.createServer();
    }


    private static void startFileBackupServer() {
        try {
            new FtcBackupServer(ConfigManager.get().getFileDirectory(),new InetSocketAddress(ConfigManager.get().getIp(), ConfigManager.get().getBackupPort()));
            ConfigManager.get().setBackupSuccess(true);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



}




