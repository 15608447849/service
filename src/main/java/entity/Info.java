package entity;

/**
 * Created by user on 2017/8/17.
 */
public class Info{
    private String ftpLaunch;
    private String resourceDirectory;
    private String editorInfo;
    private String translatePort;
    private String ftpUser;
    private String ftpPass;
    private String ftpHost;
    private String ftpPort;
    private String httpUploadAddress;
    private String backupAddress;
    private String uploadAndBackupAddress;
    private String backupServerPort;

    public String getFtpUser() {
        return ftpUser;
    }

    public String getFtpPass() {
        return ftpPass;
    }

    public String getFtpHost() {
        return ftpHost;
    }

    public String getFtpPort() {
        return ftpPort;
    }

    public String getHttpUploadAddress() {
        return httpUploadAddress;
    }

    public String getBackupAddress() {return backupAddress;}

    public String getFtpLaunch() {
        return ftpLaunch;
    }

    public String getResourceDirectory() {
        return resourceDirectory;
    }

    public String getEditorInfo() {
        return editorInfo;
    }

    public String getTranslatePort() {
        return translatePort;
    }

    public String getBackupServerPort() {
        return backupServerPort;
    }

    public String getUploadAndBackupAddress() {
        return uploadAndBackupAddress;
    }

    public Info setHttpUploadAddress(String httpUploadAddress) {
        this.httpUploadAddress = httpUploadAddress;
        return this;
    }

    public Info setFtpUser(String ftpUser) {
        this.ftpUser = ftpUser;
        return this;
    }

    public Info setFtpPass(String ftpPass) {
        this.ftpPass = ftpPass;
        return this;
    }

    public Info setFtpHost(String ftpHost) {
        this.ftpHost = ftpHost;
        return this;
    }

    public Info setFtpPort(String ftpPort) {
        this.ftpPort = ftpPort;
        return this;
    }

    public Info setBackupAddress(String backupAddress){
        this.backupAddress = backupAddress;
        return this;
    }

    public Info setFtpLaunch(String ftpLaunch) {
        this.ftpLaunch = ftpLaunch;
        return this;
    }

    public Info setResourceDirectory(String resourceDirectory) {
        this.resourceDirectory = resourceDirectory;
        return this;
    }

    public Info setEditorInfo(String editorInfo) {
        this.editorInfo = editorInfo;
        return this;
    }

    public Info setTranslatePort(String translatePort) {
        this.translatePort = translatePort;
        return this;
    }

    public Info setBackupServerPort(String backupServerPort) {
        this.backupServerPort = backupServerPort;
        return this;
    }
    public Info setUuploadAndBackupAddress(String uploadAndBackupAddress) {
        this.uploadAndBackupAddress = uploadAndBackupAddress;
        return this;
    }
}
