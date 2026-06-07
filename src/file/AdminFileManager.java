package file;
import exception.FileDataException;
import java.nio.file.Path;
public class AdminFileManager extends FileManager{ private final Path path=dataDir.resolve("admin.txt"); public String loadPassword() throws FileDataException{ if(readAll(path).isEmpty()) savePassword("admin123!"); return readAll(path).get(0).trim(); } public void savePassword(String p) throws FileDataException{ writeAll(path, java.util.List.of(p)); } }
