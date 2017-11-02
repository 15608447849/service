package servlet.imps;

import com.baidu.ueditor.ActionEnter;
import com.winone.ftc.mtools.FileUtil;
import com.winone.ftc.mtools.Log;
import entity.Config;
import servlet.iface.Mservlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

/**
 * Created by user on 2017/8/9.
 * 前端附文本编辑器
 */
public class TestEditor extends  Mservlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doGet(req,resp);
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Content-Type", "text/html");
        resp.getWriter().write(new ActionEnter(req).exec());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req,resp);
        doGet(req,resp);
    }
}
