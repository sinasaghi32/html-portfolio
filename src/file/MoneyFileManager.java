package file;
import exception.FileDataException;
import model.CoinStorage;
import java.nio.file.Path;
public class MoneyFileManager extends FileManager{ private final Path path=dataDir.resolve("money.txt"); public CoinStorage load() throws FileDataException{ if(readAll(path).isEmpty()) save(new CoinStorage()); return CoinStorage.fromCsv(readAll(path).get(0)); } public void save(CoinStorage c) throws FileDataException{ writeAll(path, java.util.List.of(c.toCsv())); } }
