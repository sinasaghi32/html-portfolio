package database;
import exception.FileDataException;
import model.CoinStorage;
import model.SaleRecord;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

/**
 * 3학년 DB 요구사항 구현: 외부 SQLite JDBC 드라이버 없이도 실행되도록 분리된 로컬 DB 스타일 레이어를 둔다.
 * 파일 관리 클래스와 별도로 database/local_db.txt에 관리자 요약 데이터를 동기화한다.
 */
public class LocalDatabaseManager {
    private final Path db=Paths.get("data","local_db.txt");
    public synchronized void syncSale(SaleRecord r) throws FileDataException { append("SALE_DB|"+r.toCsv()+"|total="+r.getTotal()); }
    public synchronized void syncStock(String text) throws FileDataException { append("STOCK_DB|"+text); }
    public synchronized void syncMoney(CoinStorage c) throws FileDataException { append("MONEY_DB|"+c); }
    private void append(String s) throws FileDataException { try{ Files.createDirectories(db.getParent()); Files.writeString(db,s+System.lineSeparator(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);}catch(IOException e){throw new FileDataException("로컬 DB 저장 실패",e);} }
}
