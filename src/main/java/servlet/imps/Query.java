package servlet.imps;

import com.google.gson.Gson;
import entity.ConfigManager;
import entity.FtpInfo;
import entity.Info;
import servlet.iface.Mservlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by lzp on 2017/8/17.
 * 文件服务器信息查询
 */
public class Query extends Mservlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
        FtpInfo info = ConfigManager.get().getFtpInfo();
        resp.getWriter().println(new Gson().toJson(
                new Info().setFtpLaunch(ConfigManager.get().getFtpInfo().launchInfo.toString())
                .setFtpUser(info.user)
                .setFtpPass(info.password)
                .setFtpHost(info.host)
                .setFtpPort(info.port+"")
                .setHttpUploadAddress( ConfigManager.get().getHttpUrl(ConfigManager.get().getWebMainPath().substring(1)+"/upload"))
                .setBackupAddress(ConfigManager.get().getHttpUrl(ConfigManager.get().getWebMainPath().substring(1)+"/backup"))
                .setEditorInfo(ConfigManager.get().getHttpUrl(ConfigManager.get().getWebMainPath().substring(1)+"/editor"))
                .setTranslatePort(ConfigManager.get().getTransientServerPort()+"")
                .setResourceDirectory(ConfigManager.get().getFileDirectory())
                .setBackupServerPort(ConfigManager.get().getBackupPort()+"")
                .setUuploadAndBackupAddress(ConfigManager.get().getHttpUrl(ConfigManager.get().getWebMainPath().substring(1)+"/uploadAndBackup"))

        ));
    }
}
