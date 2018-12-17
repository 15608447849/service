package server.servlet.imps;
import server.entity.BackupProperties;
import server.entity.Result;
import server.entity.UploadResult;
import server.servlet.beans.FileBackupOperation;
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
                        final UploadResult uploadResult = (UploadResult) it;
                        listItems.add(uploadResult.relativePath);
                        if (!uploadResult.md5FileRelativePath.equals("node")){
                            listItems.add(uploadResult.md5FileRelativePath); //同步MD5文件
                        }
                    }
                    new FileBackupOperation(listItems).execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
