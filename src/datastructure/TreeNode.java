package datastructure;
import model.Drink;
public class TreeNode { public int price; public Drink drink; public TreeNode left,right; public TreeNode(Drink d){ price=d.getPrice(); drink=d; } }
