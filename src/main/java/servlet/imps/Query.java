package servlet.imps;

import com.google.gson.Gson;
import entity.Config;
import entity.FtpInfo;
import entity.Info;
import servlet.iface.Mservlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by user on 2017/8/17.
 */
public class Query extends Mservlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
        resp.setCharacterEncoding("UTF-8");
        req.setCharacterEncoding("UTF-8");
        FtpInfo info = Config.get().getFtpInfo();
        resp.getWriter().println(new Gson().toJson( new Info()
                .setFtpUser(info.userName)
                .setFtpPass(info.password)
                .setFtpHost(info.host)
                .setFtpPort(info.port+"")
                .setHttpUploadAddress( Config.get().getHttpUrl(Config.get().getWebMainPath().substring(1)+"/upload"))));
    }
}
