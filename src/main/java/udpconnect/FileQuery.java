package udpconnect;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;


/**
 * Created by user on 2017/6/19.
 */
public class FileQuery extends SimpleFileVisitor<Path> {
    private static final String TAG = "文件查询";
    public static Path home;
    private String queryPathStr;
    private Path queryPath;
    private Path result = null;

    public FileQuery(String queryPathStr) {
        this.queryPathStr = queryPathStr;
        this.queryPath = Paths.get(home+queryPathStr.trim());

    }

    /**
     * 查询文件
     * @return
     */
    public Path queryFile() throws IOException {
        if (home==null) new IOException("home is not setting.");
        Files.walkFileTree(home, this);
        if (result == null) throw new IOException("file not fount : "+queryPath);
        return result;
    }
    @Override
    public FileVisitResult visitFileFailed(Path path, IOException e) throws IOException {
        return FileVisitResult.SKIP_SUBTREE;
    }
    @Override
    public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) {
            if (filePath.equals(queryPath)){
                this.result = filePath;
                return FileVisitResult.TERMINATE;
            }
        return FileVisitResult.CONTINUE;
    }

}
