package model;
import datastructure.MyQueue;
import exception.InvalidMoneyException;

/**
 * 요구사항 B: MoneyInput moneyInput = new MoneyInput()처럼 동적 할당하여 사용한다.
 * Java는 C의 free() 대신 Garbage Collector가 사용하지 않는 객체를 정리하므로,
 * 환불/구매 후 moneyInput = null 로 참조를 끊는 방식으로 해제 의도를 표현한다.
 */
public class MoneyInput {
    private int total;
    private int billTotal;
    private final MyQueue<Integer> inputHistory = new MyQueue<>(); // custom queue 사용
    public void insert(int unit) throws InvalidMoneyException {
        if(!(unit==10||unit==50||unit==100||unit==500||unit==1000)) throw new InvalidMoneyException("사용할 수 없는 화폐 단위: "+unit);
        if(unit==1000 && billTotal + unit > 5000) throw new InvalidMoneyException("지폐 투입 한도 5000원 초과");
        if(total + unit > 7000) throw new InvalidMoneyException("총 투입 한도 7000원 초과");
        total += unit; if(unit==1000) billTotal += unit; inputHistory.enqueue(unit);
    }
    public int getTotal(){ return total; }
    public MyQueue<Integer> getInputHistory(){ return inputHistory; }
}
