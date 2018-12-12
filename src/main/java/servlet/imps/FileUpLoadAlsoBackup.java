package servlet.imps;
import com.winone.ftc.mtools.Log;
import entity.BackupProperties;
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
public class FileUpLoadAlsoBackup extends FileUpLoad {

    @Override
    protected void subHook(HttpServletRequest req, List<Result> resultList) {
        if (!BackupProperties.get().isAuto) return;
        if (resultList!=null){
            try {
                    ArrayList<String> listItems = new ArrayList<>();
                    for (Result it : resultList){
                        final UploadResult upit = (UploadResult) it;
                        listItems.add(upit.getRelativePath());
                        if (!upit.getMd5FileRelativePath().equals("node")){
                            listItems.add(upit.getMd5FileRelativePath());
                        }
                    }
                    new FileBackupOperation(listItems).execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
