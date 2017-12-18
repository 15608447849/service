package servlet.imps;

import entity.BackupParamBean;
import entity.ConfigManager;
import entity.Result;
import entity.UploadResult;
import servlet.beans.FileBackupOperation;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 2017/12/14.
 * 上传完文件并且同步文件
 */
public class FileUpLoadAndFileBackup extends FileUpLoad {

    @Override
    protected void subHook(HttpServletRequest req, List<Result> resultList) {
        if (resultList!=null){

            try {
                BackupParamBean bean = getJsonObject(req,"backup-json",BackupParamBean.class);
                if (bean!=null){
                    ArrayList<String> listItems = new ArrayList<>();
                    for (Result it : resultList){
                        final UploadResult upit = (UploadResult) it;
                        listItems.add(upit.getRelativePath());
                        if (!upit.getMd5FileRelativePath().equals("node")){
                            listItems.add(upit.getMd5FileRelativePath());
                        }
                    }

                    bean.setFileItems(listItems);
                    new FileBackupOperation(bean, ConfigManager.get().getBackupClient()).execute();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
