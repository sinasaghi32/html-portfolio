package datastructure;
import model.Drink;
import java.util.ArrayList;
import java.util.List;
public class DrinkPriceTree { private TreeNode root; public void insert(Drink d){ root=insert(root,d); } private TreeNode insert(TreeNode n,Drink d){ if(n==null)return new TreeNode(d); if(d.getPrice()<n.price)n.left=insert(n.left,d); else n.right=insert(n.right,d); return n; } public List<Drink> searchPrice(int price){ List<Drink> r=new ArrayList<>(); search(root,price,r); return r; } private void search(TreeNode n,int price,List<Drink> r){ if(n==null)return; if(price<n.price)search(n.left,price,r); else if(price>n.price)search(n.right,price,r); else{ r.add(n.drink); search(n.left,price,r); search(n.right,price,r);} } }
