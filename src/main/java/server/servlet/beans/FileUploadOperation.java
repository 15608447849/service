package server.servlet.beans;

import com.winone.ftc.mtools.FileUtil;
import com.winone.ftc.mtools.Log;
import com.winone.ftc.mtools.MD5Util;
import com.winone.ftc.mtools.StringUtil;
import server.entity.FtpInfo;
import server.entity.Result;
import server.entity.UploadResult;
import server.entity.WebProperties;
import org.apache.commons.fileupload.FileItem;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Created by user on 2017/12/14.
 */
public class FileUploadOperation {


    final ArrayList<String> specifyPaths; //指定的文件保存相对路径
    final ArrayList<String> specifyNames;//指定的文件名
    final ArrayList<String> specifyMd5;//保存MD5文件名
    final List<FileItem> fileItems;

    public FileUploadOperation(ArrayList<String> specifyPaths, ArrayList<String> specifyNames, ArrayList<String> specifyMd5, List<FileItem> fileItems) {
        this.specifyPaths = specifyPaths;
        this.specifyNames = specifyNames;
        this.specifyMd5 = specifyMd5;
        this.fileItems = fileItems;
    }
    public  List<Result>  execute() throws Exception{
        List<Result> resultList = new ArrayList<>();

        Iterator<FileItem> iterator = fileItems.iterator();
        while (iterator.hasNext()){
            if (iterator.next().isFormField()) iterator.remove();
        }
        if (fileItems==null || fileItems.size()==0) throw new NullPointerException("the file item list is null.");

        FileItem fileItem;
        String areaName;//域名
        String areaFileName;//域名中的文件名
        String specifyPath ;
        String specifyFileName ;
        String saveMD5Name;
        UploadResult uploadResult;

        for (int i = 0 ;i< fileItems.size();i++) {
            uploadResult = new UploadResult();
            fileItem = fileItems.get(i);
            areaName = fileItem.getFieldName();
            areaFileName = fileItem.getName();
            specifyPath = getIndexValue(specifyPaths,i,"/defaults/"+areaName+"/");
            specifyFileName = getIndexValue(specifyNames,i,areaFileName);
            saveMD5Name = getIndexValue(specifyMd5,i,"false");
            Log.i("表单域名 :"+areaName+" ; 表单名 :"+areaFileName+" ; 上传的文件: " + specifyPath+specifyFileName);
            saveFile(fileItem,specifyPath,specifyFileName,saveMD5Name,uploadResult);
            resultList.add(uploadResult);//添加结果集合
        }
        return resultList;
    }

    private void saveFile(FileItem fileItem, String specifyPath, String specifyFileName, String saveMD5Name, UploadResult uploadResult) {

        final String dirPath = WebProperties.get().rootPath; //本地绝对目录
        //创建目录
        if (!FileUtil.checkDir(dirPath+specifyPath)){
            uploadResult.Info(600,"directory does not exist or created fail.");
            return;
        }
        //获取后缀
        String suffix = "";
        if (specifyFileName.contains(".")){
            suffix = specifyFileName.substring(specifyFileName.lastIndexOf(".")+1); //不包含 '.'
        }
        //相对完成路径
        String localRelativePath = specifyPath + specifyFileName;
        String md5FileRelativePath = "node";

        try {
            File file = new File(dirPath + localRelativePath);

            fileItem.write(file); //流写入文件

            String fileMd5 = MD5Util.getFileMd5ByString(file);//文件MD5

            if (!StringUtil.isEntry(saveMD5Name) && saveMD5Name.equals("true")){
                //创建目录
                if (FileUtil.checkDir(dirPath + "/md5s" + specifyPath)){
                    md5FileRelativePath = "/md5s" + specifyPath + fileMd5 + "." +suffix;
                    FileUtil.copyFile(file,new File(dirPath + md5FileRelativePath)); //文件复制
                }
            }
            String httpUrl = String.format(Locale.CANADA,"http://%s:%d%s",
                    WebProperties.get().webIp,
                    WebProperties.get().webPort,
                    localRelativePath
            );
            String ftpUrl = String.format(Locale.CANADA,"ftp://%s:%s@%s:%d%s",
                    FtpInfo.get().user,
                    FtpInfo.get().password,
                    FtpInfo.get().host,
                    FtpInfo.get().port,
                    localRelativePath
                    );

            uploadResult.ftpUrl = ftpUrl;
            uploadResult.httpUrl = httpUrl;
            uploadResult.relativePath = localRelativePath;
            uploadResult.fileMd5 = fileMd5;
            uploadResult.currentFileName = specifyFileName;
            uploadResult.suffix = suffix;
            uploadResult.md5FileRelativePath = md5FileRelativePath;
            uploadResult.Info(200,"success");

        } catch (Exception e) {
            e.printStackTrace();
            uploadResult.Info(601,e.toString());
        }
    }

    private String getIndexValue(List<String> dataList,int index,String def){
        if (dataList.size()>index) return dataList.get(index);
        return StringUtil.isEntry(def)?null:def;
    }

}
