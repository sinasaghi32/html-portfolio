package model;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** 판매 기록: 파일 저장, 스택 최근 내역, 서버 전송에 같이 사용한다. */
public class SaleRecord {
    private final LocalDateTime dateTime;
    private final String drinkName;
    private final int price;
    private final int quantity;
    public SaleRecord(LocalDateTime dateTime, String drinkName, int price, int quantity){ this.dateTime=dateTime; this.drinkName=drinkName; this.price=price; this.quantity=quantity; }
    public LocalDateTime getDateTime(){ return dateTime; }
    public String getDrinkName(){ return drinkName; }
    public int getPrice(){ return price; }
    public int getQuantity(){ return quantity; }
    public int getTotal(){ return price * quantity; }
    public String day(){ return dateTime.toLocalDate().toString(); }
    public String month(){ return dateTime.getYear()+"-"+String.format("%02d", dateTime.getMonthValue()); }
    public String toCsv(){ return dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)+","+drinkName+","+price+","+quantity; }
    public static SaleRecord fromCsv(String line){ String[] p=line.split(",",-1); return new SaleRecord(LocalDateTime.parse(p[0]),p[1],Integer.parseInt(p[2]),Integer.parseInt(p[3])); }
    @Override public String toString(){ return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))+" | "+drinkName+" | "+getTotal()+"원"; }
}
