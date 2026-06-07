package file;
import exception.FileDataException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

/** 파일 공통 처리. UTF-8 CSV 스타일 저장을 사용한다. */
public class FileManager {
    protected final Path dataDir = Paths.get("data");
    protected void ensureDir() throws FileDataException { try{ Files.createDirectories(dataDir); }catch(IOException e){ throw new FileDataException("data 폴더 생성 실패", e);} }
    protected List<String> readAll(Path p) throws FileDataException { try{ ensureDir(); return Files.exists(p)?Files.readAllLines(p, StandardCharsets.UTF_8):List.of(); }catch(IOException e){ throw new FileDataException("파일 읽기 실패: "+p,e);} }
    protected void writeAll(Path p,List<String> lines) throws FileDataException { try{ ensureDir(); Files.write(p, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING); }catch(IOException e){ throw new FileDataException("파일 쓰기 실패: "+p,e);} }
    protected void append(Path p,String line) throws FileDataException { try{ ensureDir(); Files.writeString(p,line+System.lineSeparator(),StandardCharsets.UTF_8,StandardOpenOption.CREATE,StandardOpenOption.APPEND); }catch(IOException e){ throw new FileDataException("파일 추가 실패: "+p,e);} }
}
