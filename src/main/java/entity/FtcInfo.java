package entity;

import java.util.Properties;

/**
 * Created by user on 2017/11/29.
 */
public class FtcInfo {
    private final int translatePort;
    private final int backupPort;

    public FtcInfo(Properties proper) {
        this.translatePort = Integer.parseInt(proper.getProperty("ftc.transient.local.port","10000"));
        this.backupPort = Integer.parseInt(proper.getProperty("ftc.backup.server.port","10001"));
    }

    public int getTranslatePort() {
        return translatePort;
    }

    public int getBackupPort() {
        return backupPort;
    }
}
