package server;

import com.m.backup.client.FtcBackupClient;
import com.m.backup.server.FtcBackupServer;
import com.winone.ftc.mtools.Log;
import com.winone.ftc.mtools.NetworkUtil;
import entity.BackupProperties;
import entity.FtpInfo;
import entity.WebProperties;
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
import servlet.imps.*;
import io.undertow.servlet.api.DeploymentInfo;

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
                    .setContextPath(WebProperties.get().pathPrefix)
                    .setDeploymentName("file_server.war");
            //添加文件上传
            servletBuilder.addServlets(io.undertow.servlet.Servlets.servlet("FileUpLoad", FileUpLoad.class).addMapping("/upload"));
            //文件上传并同步
            servletBuilder.addServlet(io.undertow.servlet.Servlets.servlet("FileUpLoadAlsoBackup", FileUpLoadAlsoBackup.class).addMapping("/backup"));

            DeploymentManager manager = Servlets.defaultContainer().addDeployment(servletBuilder);

            manager.deploy();
            
            HttpHandler servletHandler = manager.start();

            PathHandler path = Handlers.path();

            path.addPrefixPath(
                           WebProperties.get().pathPrefix,// web访问路径前缀
                            servletHandler
                    );



            PathResourceManager pathResourceManager =  new PathResourceManager(
                    Paths.get(WebProperties.get().rootPath),
                    4096L,
                    false,
                    false,
                    false,
                    null
            );
            path.addPrefixPath(
                    "/",   //下载地址- 请勿修改
                    io.undertow.Handlers.resource(pathResourceManager)
            );
            Undertow server = Undertow
                    .builder()
                    .addHttpListener(
                            WebProperties.get().webPort,
                            WebProperties.get().webIp)
                    .setHandler(path)
                    .build();
            server.start(); //开始运行
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static void startFTPServer() {
        try{
            FtpServer server;
            try {
                //配置文件存在
                FileSystemXmlApplicationContext ctx = new FileSystemXmlApplicationContext(String.valueOf(LunchServer.class.getResource("/ftpconfig.xml")));
                if(ctx.containsBean("server")) {
                    server = (FtpServer)ctx.getBean("server");
                } else {
                    String[] beanNames = ctx.getBeanNamesForType(FtpServer.class);
                    if(beanNames.length >= 1) {
                        server = (FtpServer)ctx.getBean(beanNames[0]);
                    } else {
                        throw new IllegalArgumentException("无效的ftp配置信息");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                server = ftpApplicationDefault();
            }
            server.start();

            if (server instanceof DefaultFtpServer){
                int port = 21;
                String host = WebProperties.get().webIp;
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
                    is = LunchServer.class.getResourceAsStream("/ftpusers.properties");
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
                FtpInfo.get().setInfo(host,port,user,pass);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static FtpServer ftpApplicationDefault() throws Exception{
        PropertiesUserManagerFactory propertiesUserManagerFactory = new PropertiesUserManagerFactory();
        propertiesUserManagerFactory.setFile(new File(String.valueOf(LunchServer.class.getResource("/ftpusers.properties"))));
        propertiesUserManagerFactory.setPasswordEncryptor(new ClearTextPasswordEncryptor());
        FtpServerFactory ftpServerFactory = new FtpServerFactory();
        ftpServerFactory.setUserManager( propertiesUserManagerFactory.createUserManager());
        return ftpServerFactory.createServer();
    }


    private static void startFileBackupServer() {
        try {
            BackupProperties.get().ftcBackupServer = new FtcBackupServer(WebProperties.get().rootPath,new InetSocketAddress(WebProperties.get().webIp, BackupProperties.get().localPort));

            if (BackupProperties.get().isBoot){

                for (InetSocketAddress remoteAddress : BackupProperties.get().remoteList){
                    if (NetworkUtil.ping(remoteAddress.getAddress().getHostAddress())){
                        FtcBackupClient client =  BackupProperties.get().ftcBackupServer.getClient();
                        client.ergodicDirectory(remoteAddress);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



}




