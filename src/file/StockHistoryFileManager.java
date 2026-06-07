package file;
import exception.FileDataException;
import java.nio.file.Path;
import java.time.LocalDateTime;
public class StockHistoryFileManager extends FileManager{ private final Path path=dataDir.resolve("stock_history.txt"); public void add(String msg) throws FileDataException{ append(path, LocalDateTime.now()+","+msg); } public String readText() throws FileDataException{ return String.join(System.lineSeparator(), readAll(path)); } }
