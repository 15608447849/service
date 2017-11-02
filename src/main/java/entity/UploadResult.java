package entity;

/**
 * Created by user on 2017/7/11.
 */
public class UploadResult {
    private String ftpUrl;
    private String httpUrl;
    private String relativePath;
    private String fileMd5;
    private String attr;
    private int code;
    private String message;
    private String suffix; //后缀
    private String sourceFileName;//源文件名
    private String currentFileName;//现在的文件名
    private String md5FileRelativePath;
    public String getFtpUrl() {
        return ftpUrl;
    }

    public void setFtpUrl(String ftpUrl) {
        this.ftpUrl = ftpUrl;
    }

    public String getHttpUrl() {
        return httpUrl;
    }

    public void setHttpUrl(String httpUrl) {
        this.httpUrl = httpUrl;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public String getFileMd5() {
        return fileMd5;
    }

    public void setFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
    }

    public int getCode() {
        return code;
    }

    public UploadResult setCode(int code) {
        this.code = code;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public UploadResult setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getAttr() {
        return attr;
    }

    public void setAttr(String attr) {
        this.attr = attr;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getSourceFileName() {
        return sourceFileName;
    }

    public void setSourceFileName(String sourceFileName) {
        this.sourceFileName = sourceFileName;
    }

    public String getCurrentFileName() {
        return currentFileName;
    }

    public void setCurrentFileName(String currentFileName) {
        this.currentFileName = currentFileName;
    }

    public String getMd5FileRelativePath() {
        return md5FileRelativePath;
    }

    public void setMd5FileRelativePath(String md5FileRelativePath) {
        this.md5FileRelativePath = md5FileRelativePath;
    }
}
