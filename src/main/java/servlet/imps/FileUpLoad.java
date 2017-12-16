package servlet.imps;

import com.winone.ftc.mtools.FileUtil;
import com.winone.ftc.mtools.StringUtil;
import entity.ConfigManager;
import entity.Result;
import entity.UploadResult;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import servlet.beans.FileUploadOperation;
import servlet.iface.Mservlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lzp on 2017/5/13.
 * 文件上传接收
 */
public class FileUpLoad extends Mservlet {

    protected ArrayList<String> filterData(String data){
        ArrayList<String> dataList = new ArrayList<>();
        try {
            final String  SPLIT =";";
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

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req,resp);

        List<Result> resultList = null;
        Result result = new Result<UploadResult>().setResultInfo(199,"unknown error.");
        //根据判断是否指定保存路径
        ArrayList<String> pathList = filterData(req.getHeader("specify-path"));
        if (pathList.size()>0){
            String path;
            for(int i=0;i<pathList.size();i++){
                path = pathList.get(i);
                if (!path.startsWith(FileUtil.SEPARATOR)) path = FileUtil.SEPARATOR+ path;//保证前面有 '/'
                if (!path.endsWith(FileUtil.SEPARATOR)) path+= FileUtil.SEPARATOR; //后面保证 '/'
                pathList.set(i,path);
            }
        }
        //根据判断是否指定保存文件名
        ArrayList<String> fileNameList = filterData( req.getHeader("specify-filename"));

        //判断是否保存成md5文件名
        ArrayList<String> fileSavaMD5 = filterData(req.getHeader("save-md5"));

        try {
            // 创建一个ServletFileUpload对象
            ServletFileUpload uploader = ConfigManager.get().getServletFileUploader();
            if (!uploader.isMultipartContent(req)){
                throw new IllegalArgumentException("content-type is not 'multipart/form-data'");
            }
            List<FileItem> listItems = uploader.parseRequest(req);
            resultList = new FileUploadOperation(pathList,fileNameList,fileSavaMD5,listItems).execute();
            subHook(req,resultList);
        } catch (Exception e) {
            result.setResultInfo(400,e.toString());
        }finally {
            // 向客户端返回结果
            Object object = resultList==null?result:resultList;
          writeJson(resp,object);
        }
    }
    protected void subHook(HttpServletRequest req,List<Result> resultList){}
}
