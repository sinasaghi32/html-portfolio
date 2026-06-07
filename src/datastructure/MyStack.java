package datastructure;
import java.util.ArrayList;
import java.util.List;
public class MyStack<T> { private Object[] data=new Object[100]; private int top=-1; public void push(T v){ if(top+1==data.length) grow(); data[++top]=v; } @SuppressWarnings("unchecked") public T pop(){ return top<0?null:(T)data[top--]; } public boolean isEmpty(){return top<0;} private void grow(){ Object[] n=new Object[data.length*2]; System.arraycopy(data,0,n,0,data.length); data=n; } @SuppressWarnings("unchecked") public List<T> recent(int max){ List<T> r=new ArrayList<>(); for(int i=top;i>=0&&r.size()<max;i--) r.add((T)data[i]); return r; } }
