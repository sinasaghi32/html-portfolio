package datastructure;
import model.Drink;
import java.util.ArrayList;
import java.util.List;

/** 요구사항 H: Java LinkedList를 쓰지 않고 직접 만든 단순 연결 리스트로 음료/재고를 관리한다. */
public class DrinkLinkedList {
    private DrinkNode head;
    public void add(Drink d){ if(head==null){head=new DrinkNode(d);return;} DrinkNode cur=head; while(cur.next!=null)cur=cur.next; cur.next=new DrinkNode(d); }
    public Drink findById(int id){ for(DrinkNode c=head;c!=null;c=c.next) if(c.data.getId()==id) return c.data; return null; }
    public Drink findByName(String name){ for(DrinkNode c=head;c!=null;c=c.next) if(c.data.getName().equalsIgnoreCase(name)) return c.data; return null; }
    public List<Drink> toList(){ List<Drink> list=new ArrayList<>(); for(DrinkNode c=head;c!=null;c=c.next) list.add(c.data); return list; }
    public void clear(){ head=null; }
}
