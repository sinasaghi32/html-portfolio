package vending;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/** Binary search tree indexed by sales date for fast daily/monthly search. */
public class SalesTree {
    private Node root;
    private static class Node {
        LocalDate key;
        List<SalesRecord> records = new ArrayList<>();
        Node left;
        Node right;
        Node(SalesRecord record) { key = record.date(); records.add(record); }
    }

    public void insert(SalesRecord record) { root = insert(root, record); }

    private Node insert(Node node, SalesRecord record) {
        if (node == null) return new Node(record);
        int cmp = record.date().compareTo(node.key);
        if (cmp < 0) node.left = insert(node.left, record);
        else if (cmp > 0) node.right = insert(node.right, record);
        else node.records.add(record);
        return node;
    }

    public List<SalesRecord> searchByDate(LocalDate date) {
        Node node = root;
        while (node != null) {
            int cmp = date.compareTo(node.key);
            if (cmp == 0) return new ArrayList<>(node.records);
            node = cmp < 0 ? node.left : node.right;
        }
        return new ArrayList<>();
    }

    public List<SalesRecord> searchByMonth(YearMonth month) {
        List<SalesRecord> result = new ArrayList<>();
        collectMonth(root, month, result);
        return result;
    }

    private void collectMonth(Node node, YearMonth month, List<SalesRecord> out) {
        if (node == null) return;
        if (!YearMonth.from(node.key).isBefore(month)) collectMonth(node.left, month, out);
        if (YearMonth.from(node.key).equals(month)) out.addAll(node.records);
        if (!YearMonth.from(node.key).isAfter(month)) collectMonth(node.right, month, out);
    }
}
