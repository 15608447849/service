package servlet.iface;

import com.winone.ftc.mtools.StringUtil;
import entity.Config;
import sun.rmi.runtime.Log;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.ArrayList;

/**
 * Created by Administrator on 2017/5/31.
 */
public class Mservlet extends javax.servlet.http.HttpServlet {
    protected static final String  SPLIT =";";
    protected static final String regexVideoSuffix = ".*\\.(?:avi|rm|rmvb|mpeg|mpg|mpg|dat|mov|oq|asf|wmv|mp4)";

    protected void filter(HttpServletResponse resp){
        resp.setHeader("Access-Control-Allow-Origin","*");
        resp.setHeader("Access-Control-Allow-Methods","*");
        resp.setHeader("Access-Control-Allow-Headers","X_Requested_With,content-type,X-Requested-With,specify-path,specify-filename,save-md5");//
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        filter(resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        filter(resp);
    }

    protected void doOptions(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
        filter(resp);
        super.doOptions(request, resp);
    }

    protected ArrayList<String> filterData(String data){
        ArrayList<String> dataList = new ArrayList<>();
        try {

            if (!StringUtil.isEntry(data)){
                data = URLDecoder.decode(data,"UTF-8");
                if (data.contains(SPLIT)){
                    String [] pathArray = data.split(SPLIT);
                    for (String path :pathArray){
                        dataList.add(path);
                    }
                }else{
                    dataList.add(data);
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return dataList;
    }

    protected String getData(ArrayList<String> dataList,int index){
        if (dataList.size()>index) return dataList.get(index);
        return null;
    }

    protected String tryConnectLocalServerGetVideoLength(String filePath){
        String result = "node";
        if (!(StringUtil.isEntry(filePath))){
            if (!filePath.matches(regexVideoSuffix)){
                return result;
            }
            com.winone.ftc.mtools.Log.i("转换视频并获取时长: "+ filePath);
            Socket socket = null;
            try {
                String tmp = "video*"+filePath;
                socket = new Socket("127.0.0.1", Config.get().getTransientServerPort());
                socket.getOutputStream().write(tmp.getBytes("utf-8"));
                long currentTime = System.currentTimeMillis();
                while(  (System.currentTimeMillis() - currentTime ) < 1000){

                    if (socket.getInputStream().available() > 0){
                        byte[] buffer  = new byte[socket.getInputStream().available()];
                        socket.getInputStream().read(buffer);
                        result = new String(buffer,"utf-8");
                        break;
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                    }
                }
            } catch (Exception e) {
//                e.printStackTrace();
            }finally {
                if ( socket!=null){
                    try {
                        socket.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
        if (!result.equals("node")){
            return String.valueOf(getTimelen(result));
        }
        return result;
    }

    //格式:"00:00:10.68"
    private int getTimelen(String timelen){
        int min=0;
        String strs[] = timelen.split(":");
        if (strs[0].compareTo("0") > 0) {
            min+=Integer.valueOf(strs[0])*60*60;//秒
        }
        if(strs[1].compareTo("0")>0){
            min+=Integer.valueOf(strs[1])*60;
        }
        if(strs[2].compareTo("0")>0){
            min+=Math.round(Float.valueOf(strs[2]));
        }
        return min;
    }

}
