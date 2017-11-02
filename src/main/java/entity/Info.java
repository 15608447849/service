package entity;

/**
 * Created by user on 2017/8/17.
 */
public class Info{
    private String ftpUser;
    private String ftpPass;
    private String ftpHost;
    private String ftpPort;
    private String httpUploadAddress;

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
}
