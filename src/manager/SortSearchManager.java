package manager;
import datastructure.DrinkPriceTree;
import model.Drink;
import java.util.*;
public class SortSearchManager{
    public List<Drink> sortByPrice(List<Drink> src){ List<Drink>a=new ArrayList<>(src); for(int i=0;i<a.size()-1;i++) for(int j=0;j<a.size()-1-i;j++) if(a.get(j).getPrice()>a.get(j+1).getPrice()) swap(a,j,j+1); return a; } // bubble sort
    public List<Drink> sortByStock(List<Drink> src){ List<Drink>a=new ArrayList<>(src); for(int i=0;i<a.size();i++){ int m=i; for(int j=i+1;j<a.size();j++) if(a.get(j).getStock()<a.get(m).getStock()) m=j; swap(a,i,m);} return a; } // selection sort
    public List<Drink> sortBySoldCount(List<Drink> src){ List<Drink>a=new ArrayList<>(src); for(int i=1;i<a.size();i++){ Drink key=a.get(i); int j=i-1; while(j>=0&&a.get(j).getSoldCount()<key.getSoldCount()){ a.set(j+1,a.get(j)); j--; } a.set(j+1,key);} return a; } // insertion sort
    private void swap(List<Drink>a,int i,int j){ Drink t=a.get(i); a.set(i,a.get(j)); a.set(j,t); }
    public Drink linearSearchByName(List<Drink> drinks,String name){ for(Drink d:drinks) if(d.getName().contains(name)) return d; return null; }
    public Drink binarySearchByPrice(List<Drink> sorted,int price){ int l=0,r=sorted.size()-1; while(l<=r){ int m=(l+r)/2; if(sorted.get(m).getPrice()==price)return sorted.get(m); if(sorted.get(m).getPrice()<price)l=m+1; else r=m-1;} return null; }
    public List<Drink> treeSearchByPrice(List<Drink> drinks,int price){ DrinkPriceTree tree=new DrinkPriceTree(); for(Drink d:drinks) tree.insert(d); return tree.searchPrice(price); }
}
