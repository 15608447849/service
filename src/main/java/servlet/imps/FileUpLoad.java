package servlet.imps;

import com.google.gson.Gson;
import com.winone.ftc.mtools.FileUtil;
import com.winone.ftc.mtools.Log;
import com.winone.ftc.mtools.MD5Util;
import com.winone.ftc.mtools.StringUtil;
import entity.Config;
import entity.UploadResult;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import servlet.iface.Mservlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by lzp on 2017/5/13.
 * 文件上传接收
 */
public class FileUpLoad extends Mservlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req,resp);
        resp.setCharacterEncoding("UTF-8");
        req.setCharacterEncoding("UTF-8");
        ArrayList<UploadResult> resultList = new ArrayList<>();
        //根据判断是否指定保存路径
        ArrayList<String> pathList =filterData(req.getHeader("specify-path"));
        if (pathList.size()>0){
            String path;
            for(int i=0;i<pathList.size();i++){
                path = pathList.get(i);
                path = path.substring(path.indexOf(FileUtil.SEPARATOR)+1);// 去掉前面'/'
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
            ServletFileUpload uploader = Config.get().getServletFileUploader();
            ArrayList<FileItem> list = (ArrayList<FileItem>) uploader.parseRequest(req);
            Iterator<FileItem> iterator = list.iterator();
            while (iterator.hasNext()){
                if (iterator.next().isFormField()) iterator.remove();
            }

            FileItem fileItem;
            String specifyPath ;
            String specifyFileName ;
            String saveMD5Name;
            UploadResult uploadResult;
                for (int i = 0 ;i< list.size();i++) {
                    uploadResult = new UploadResult();
                    fileItem = list.get(i);
                    specifyPath = getData(pathList,i);
                    if (StringUtil.isEntry(specifyPath)) specifyPath = "defaults/";
                    specifyFileName = getData(fileNameList,i);
                    saveMD5Name = getData(fileSavaMD5,i);
                    if (!fileItem.isFormField()) {
                        // 如果是普通的formfield
                        String areaName = fileItem.getFieldName();//域名
                        String sName = fileItem.getName();// 域名中的文件名

                        String filename = StringUtil.isEntry(specifyFileName)? sName :specifyFileName;
                        //判断文件名存在
                        if (!StringUtil.isEntry(filename)){
                            Log.i("域名 :"+areaName+" ; 上传的文件: " + specifyPath+filename);
                            String dirPath = StringUtil.isEntry(specifyPath) ? Config.get().getFileDirectory() : Config.get().getFileDirectory() + specifyPath; //本地目录
                            if (FileUtil.checkDir(dirPath)){
                                try {

                                    File file = new File(dirPath + filename);
                                    fileItem.write(file); //流写入文件
                                    //尝试连接本地素材转换
                                    String attr = tryConnectLocalServerGetVideoLength(file.getCanonicalPath());
                                    String suffix = "";
                                    if (filename.contains(".")){
                                        suffix = filename.substring(filename.lastIndexOf("."));
                                    }
                                    String fileMd5 = MD5Util.getFileMD5String(file);//文件MD5
                                    String cFileName = filename;
                                    String path = StringUtil.isEntry(specifyPath)?filename:specifyPath+filename;
                                    String localRelativePath = String.format("/%s",path);//指定的文件路径 + 指定的文件名

                                    String md5FileReletivePath = "node";
                                    if (!StringUtil.isEntry(saveMD5Name)){
                                        cFileName = fileMd5+suffix;
                                        FileUtil.copyFile(file,new File(dirPath + cFileName));
                                        md5FileReletivePath = localRelativePath.replace(filename,cFileName);
//                                      FileUtil.deleteFile(dirPath + filename);
                                    }

                                    String httpUrl = Config.get().getHttpUrl(path);
                                    String ftpUrl = Config.get().getFtpUrl(path);



                                    uploadResult.setFileMd5(fileMd5);
                                    uploadResult.setFtpUrl(ftpUrl);
                                    uploadResult.setHttpUrl(httpUrl);
                                    uploadResult.setRelativePath(localRelativePath);

                                    uploadResult.setAttr(attr);
                                    uploadResult.setSourceFileName(filename);
                                    uploadResult.setCurrentFileName(cFileName);
                                    uploadResult.setSuffix(suffix);
                                    uploadResult.setMd5FileRelativePath(md5FileReletivePath);
                                    uploadResult.setCode(200);
                                    uploadResult.setMessage("success");
                                    resultList.add(uploadResult);
                                } catch (Exception e) {
//                                    e.printStackTrace();
                                    uploadResult.setCode(400);
                                    uploadResult.setMessage(e.toString());
                                }
                            }else{

                                uploadResult.setCode(3);
                                uploadResult.setMessage("directory does not exist or created fail.");
                            }
                        }else{
                            uploadResult.setCode(404);
                            uploadResult.setMessage("file name is entity.");
                        }
                    }else{
                        uploadResult.setCode(405);
                        uploadResult.setMessage("this is form field,no file stream.");
                    }

            }

        } catch (Exception e) {
//            e.printStackTrace();
            resultList.add(new UploadResult().setCode(400).setMessage(e.getCause()+" : "+e.getMessage()));
        }finally {
            // 向客户端返回结果
            PrintWriter out = resp.getWriter();
            out.write(new Gson().toJson(resultList));
            out.flush();
            out.close();
        }
        //
    }

}
