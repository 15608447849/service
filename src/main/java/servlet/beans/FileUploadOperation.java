package servlet.beans;

import com.winone.ftc.mtools.FileUtil;
import com.winone.ftc.mtools.Log;
import com.winone.ftc.mtools.MD5Util;
import com.winone.ftc.mtools.StringUtil;
import entity.ConfigManager;
import entity.Result;
import entity.UploadResult;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
            saveMD5Name = getIndexValue(specifyMd5,i,null);
            Log.i("域名 :"+areaName+" ; 上传的文件: " + specifyPath+specifyFileName);
            saveFile(fileItem,specifyPath,specifyFileName,saveMD5Name,uploadResult);
            resultList.add(uploadResult);//添加结果集合
        }
        return resultList;
    }

    private void saveFile(FileItem fileItem, String specifyPath, String specifyFileName, String saveMD5Name, UploadResult uploadResult) {

        final String dirPath = ConfigManager.get().getFileDirectory(); //本地绝对目录
        //创建目录
        if (!FileUtil.checkDir(dirPath+specifyPath)){
            uploadResult.setResultInfo(600,"directory does not exist or created fail.");
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
            if (!StringUtil.isEntry(saveMD5Name)){
                //创建目录
                if (FileUtil.checkDir(dirPath + "/md5s" + specifyPath)){
                    md5FileRelativePath = "/md5s" + specifyPath + fileMd5 + "." +suffix;
                    FileUtil.copyFile(file,new File(dirPath + md5FileRelativePath)); //文件复制
                }
            }
            String attr = tryConnectLocalServerGetVideoLength(file.getCanonicalPath());
            String httpUrl = ConfigManager.get().getHttpUrl(localRelativePath.substring(1));//去除路径前的 '/'
            String ftpUrl = ConfigManager.get().getFtpUrl(localRelativePath.substring(1));


            uploadResult.setFtpUrl(ftpUrl);
            uploadResult.setHttpUrl(httpUrl);
            uploadResult.setAttr(attr);
            uploadResult.setRelativePath(localRelativePath);
            uploadResult.setFileMd5(fileMd5);
            uploadResult.setCurrentFileName(specifyFileName);
            uploadResult.setSuffix(suffix);
            uploadResult.setMd5FileRelativePath(md5FileRelativePath);
            uploadResult.setResultInfo(200,"success");

        } catch (Exception e) {
            uploadResult.setResultInfo(601,e.toString());
        }
    }


    private String getIndexValue(List<String> dataList,int index,String def){
        if (dataList.size()>index) return dataList.get(index);
        return StringUtil.isEntry(def)?null:def;
    }

    protected String tryConnectLocalServerGetVideoLength(String filePath){
        final String regexVideoSuffix = ".*\\.(?:avi|rm|rmvb|mpeg|mpg|mpg|dat|mov|oq|asf|wmv|mp4)";
        String result = "node";
        if (!(StringUtil.isEntry(filePath))){
            if (!filePath.matches(regexVideoSuffix)){
                return result;
            }

            Socket socket = null;
            try {
                String tmp = "video*"+filePath;
                socket = new Socket("127.0.0.1", ConfigManager.get().getTransientServerPort());
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
            int len = getTimelen(result);
            com.winone.ftc.mtools.Log.i("转换视频并获取时长: "+ filePath+" - "+ len);
            return String.valueOf(len);
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
