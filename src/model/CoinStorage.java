package model;
import exception.ChangeNotAvailableException;
import java.util.HashMap;
import java.util.Map;

/** 거스름돈 저장소. 생성자에서 기본 동전 10개씩 초기화한다. */
public class CoinStorage {
    private int c10, c50, c100, c500;
    public CoinStorage(){ this(10,10,10,10); } // 요구사항 C: constructor initialization
    public CoinStorage(int c10,int c50,int c100,int c500){ this.c10=c10; this.c50=c50; this.c100=c100; this.c500=c500; }
    public synchronized void addCoin(int unit){ if(unit==10)c10++; else if(unit==50)c50++; else if(unit==100)c100++; else if(unit==500)c500++; }
    public synchronized boolean canMakeChange(int amount){ return calculateChange(amount)!=null; }
    public synchronized Map<Integer,Integer> returnChange(int amount) throws ChangeNotAvailableException {
        Map<Integer,Integer> r=calculateChange(amount); if(r==null) throw new ChangeNotAvailableException("거스름돈 없음: " + amount + "원");
        c500-=r.getOrDefault(500,0); c100-=r.getOrDefault(100,0); c50-=r.getOrDefault(50,0); c10-=r.getOrDefault(10,0); return r;
    }
    private Map<Integer,Integer> calculateChange(int amount){
        int[] units={500,100,50,10}; int[] counts={c500,c100,c50,c10}; Map<Integer,Integer> r=new HashMap<>(); int remain=amount;
        for(int i=0;i<units.length;i++){ int n=Math.min(remain/units[i], counts[i]); if(n>0){ r.put(units[i],n); remain-=n*units[i]; } }
        return remain==0 ? r : null;
    }
    public synchronized int collectKeepMinimum(){ int sum=0; int k10=Math.max(0,c10-5), k50=Math.max(0,c50-5), k100=Math.max(0,c100-5), k500=Math.max(0,c500-5); c10-=k10; c50-=k50; c100-=k100; c500-=k500; return k10*10+k50*50+k100*100+k500*500; }
    public int total(){ return c10*10+c50*50+c100*100+c500*500; }
    public String toCsv(){ return c10+","+c50+","+c100+","+c500; }
    public static CoinStorage fromCsv(String line){ String[] p=line.split(","); return new CoinStorage(Integer.parseInt(p[0]),Integer.parseInt(p[1]),Integer.parseInt(p[2]),Integer.parseInt(p[3])); }
    @Override public String toString(){ return "10원="+c10+", 50원="+c50+", 100원="+c100+", 500원="+c500+", 합계="+total()+"원"; }
}
