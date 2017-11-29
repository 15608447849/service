package entity;

import com.winone.ftc.mtools.FileUtil;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;

import java.io.*;
import java.util.Properties;

/**
 * Created by user on 2017/11/29.
 */
public class WebInfo {
    private final int _200M = 1024 * 1024 * 1024 * 200;
    private final String WEB_MAIN_PATH = "/ftc";
    private final String fileDirectory; //文件存储根目录
    private final String tempDirectory;//临时文件存储目录
    private final String ip;//web ip
    private final int port; //web端口号
    private final DiskFileItemFactory diskFileItemFactory;

    public WebInfo(Properties proper){

        this.ip = proper.getProperty("web.ip","127.0.0.1");
        this.port = Integer.parseInt(proper.getProperty("web.port","8080"));
        this.fileDirectory = FileUtil.replaceFileSeparatorAndCheck(proper.getProperty("web.file.directory","./home"),null,FileUtil.SEPARATOR);
        this.tempDirectory = fileDirectory+FileUtil.SEPARATOR+"temporary";
        this.diskFileItemFactory = new DiskFileItemFactory();
        this.diskFileItemFactory.setRepository(new File(this.tempDirectory));
        // 设定上传文件的值，如果上传文件大于200M，就可能在repository所代表的文件夹中产生临时文件，否则直接在内存中进行处理
        this.diskFileItemFactory.setSizeThreshold(_200M);
    }

    public String getFileDirectory() {
        return fileDirectory;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public DiskFileItemFactory getDiskFileItemFactory() {
        return diskFileItemFactory;
    }

    public String getWebMainPath() {
        return WEB_MAIN_PATH;
    }
}
