package vending;

import java.util.ArrayList;
import java.util.List;

/** Demonstrates required direct implementations of sort and search algorithms. */
public final class SearchSortUtil {
    private SearchSortUtil() {}

    /** Selection sort by price, used by the manager inventory view. */
    public static List<Beverage> selectionSortByPrice(CustomLinkedList<Beverage> input) {
        List<Beverage> list = toList(input);
        for (int i = 0; i < list.size() - 1; i++) {
            int min = i;
            for (int j = i + 1; j < list.size(); j++) if (list.get(j).getPrice() < list.get(min).getPrice()) min = j;
            Beverage tmp = list.get(i);
            list.set(i, list.get(min));
            list.set(min, tmp);
        }
        return list;
    }

    /** Linear search by name, used by the manager search box. */
    public static Beverage linearSearchByName(CustomLinkedList<Beverage> input, String keyword) {
        String lower = keyword == null ? "" : keyword.toLowerCase();
        for (Beverage beverage : input) if (beverage.getName().toLowerCase().contains(lower)) return beverage;
        return null;
    }

    private static List<Beverage> toList(CustomLinkedList<Beverage> input) {
        List<Beverage> list = new ArrayList<>();
        for (Beverage beverage : input) list.add(beverage);
        return list;
    }
}
