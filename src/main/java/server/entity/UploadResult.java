package server.entity;

/**
 * Created by user on 2017/7/11.
 */
public class UploadResult extends Result {
    public String ftpUrl;//ftp 下载的绝对路径
    public String httpUrl;//http 下载的绝对路径
    public String relativePath;//本地存储的相对路径
    public String fileMd5;//文件MD5值
    public String suffix; //文件后缀
    public String currentFileName;//现在的文件名
    public String md5FileRelativePath;//MD5文件相对路径
}
