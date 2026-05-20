package com.pharmacy.dao;

import com.pharmacy.model.Sale;
import java.time.LocalDate;
import java.util.*;

/**
 * Sale DAO.
 * Demonstrates: Generics, custom query methods, Lambda/Stream
 */
public class SaleDAO extends FileRepository<Sale> {

    private static SaleDAO instance;

    private SaleDAO() { super("sales"); }

    public static synchronized SaleDAO getInstance() {
        if (instance == null) instance = new SaleDAO();
        return instance;
    }

    @Override protected Class<Sale[]> getArrayClass() { return Sale[].class; }

    public List<Sale> findByDate(LocalDate date) {
        return filter(s -> s.getSaleDate().toLocalDate().equals(date));
    }

    public List<Sale> findByDateRange(LocalDate from, LocalDate to) {
        return filter(s -> {
            LocalDate d = s.getSaleDate().toLocalDate();
            return !d.isBefore(from) && !d.isAfter(to);
        });
    }

    public double totalRevenue(LocalDate date) {
        return findByDate(date).stream()
            .filter(s -> s.getStatus() == Sale.Status.COMPLETED)
            .mapToDouble(Sale::getGrandTotal)
            .sum();
    }

    public int transactionCount(LocalDate date) {
        return (int) findByDate(date).stream()
            .filter(s -> s.getStatus() == Sale.Status.COMPLETED)
            .count();
    }

    public List<Sale> findRecent(int limit) {
        List<Sale> all = new ArrayList<>(store.values());
        all.sort(Comparator.comparing(Sale::getSaleDate).reversed());
        return all.subList(0, Math.min(limit, all.size()));
    }

    /** Top-N medicines by quantity sold (returns name → qty map) */
    public Map<String, Integer> topMedicines(LocalDate from, LocalDate to, int limit) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        findByDateRange(from, to).stream()
            .filter(s -> s.getStatus() == Sale.Status.COMPLETED)
            .flatMap(s -> s.getItems().stream())
            .forEach(item -> counts.merge(item.getMedicineName(), item.getQuantity(), Integer::sum));

        return counts.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(limit)
            .collect(LinkedHashMap::new,
                     (m, e) -> m.put(e.getKey(), e.getValue()),
                     Map::putAll);
    }
}
