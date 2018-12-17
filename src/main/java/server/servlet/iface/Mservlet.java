package server.servlet.iface;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by Administrator on 2017/5/31.
 */
public class Mservlet extends javax.servlet.http.HttpServlet {

    //跨域
    protected void filter(HttpServletResponse resp){
        resp.setHeader("Access-Control-Allow-Origin","*");
        resp.setHeader("Access-Control-Allow-Methods","*");
        resp.setHeader("Access-Control-Allow-Headers",
                "X_Requested_With,content-type,X-Requested-With," +
                "specify-path,specify-filename,save-md5");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        filter(resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        filter(resp);
        resp.setCharacterEncoding("UTF-8");
        req.setCharacterEncoding("UTF-8");
    }

    protected void doOptions(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
        filter(resp);
        super.doOptions(request, resp);
    }





    protected <T> T getJsonObject(HttpServletRequest req,String headerKey,Class<T> clazzType) throws JsonSyntaxException{

        final String json = req.getHeader(headerKey);
        if (json!=null){
           T t = new Gson().fromJson(json,clazzType);
           return t;
        }
        return null;
    }


    protected void writeString(HttpServletResponse resp,String str,boolean isClose){
        try {
            PrintWriter out = resp.getWriter();
            out.write(str);
            out.flush();
            if (isClose) out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void writeJson(HttpServletResponse resp,Object o) {
        writeString(resp,new Gson().toJson(o),true);
    }

}
