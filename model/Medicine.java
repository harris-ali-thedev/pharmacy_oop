package com.pharmacy.model;

import java.time.LocalDate;

public class Medicine extends Entity implements Deactivatable, Searchable {
    private static final long serialVersionUID = 1L;

    private String  name;
    private String  genericName;
    private String  category;
    private String  barcode;
    private double  unitPrice;
    private double  unitCost;
    private int     stockQty;
    private int     reorderLevel;
    private boolean isNarcotic;
    private boolean isScheduled;
    private boolean active;
    private int     supplierId;
    private String  supplierID;
    private LocalDate expiryDate;
    private String  supplierName;
    private String  manufacturer;
    private String  dosageForm;
    private String  strength;

    public Medicine() {
        super();
        this.active       = true;
        this.reorderLevel = 20;
    }

    public Medicine(int id, String name, String category, double unitPrice, int stockQty) {
        super(id);
        this.name        = name;
        this.category    = category;
        this.unitPrice   = unitPrice;
        this.stockQty    = stockQty;
        this.active      = true;
        this.reorderLevel = 20;
    }

    // ── Abstract implementations ────────────────────────────
    @Override
    public String getDisplayName() {
        return name + (strength != null ? " " + strength : "") + " (" + category + ")";
    }

    @Override
    public boolean isValid() {
        return name != null && !name.isBlank() && unitPrice >= 0 && stockQty >= 0;
    }

    // ── Searchable ──────────────────────────────────────────
    @Override
    public boolean matches(String keyword) {
        if (keyword == null || keyword.isBlank()) return true;
        String kw = keyword.toLowerCase();
        return (name        != null && name.toLowerCase().contains(kw))
            || (genericName != null && genericName.toLowerCase().contains(kw))
            || (category    != null && category.toLowerCase().contains(kw))
            || (barcode     != null && barcode.toLowerCase().contains(kw))
            || (manufacturer != null && manufacturer.toLowerCase().contains(kw));
    }

    // ── Deactivatable ───────────────────────────────────────
    @Override public boolean isActive()        { return active; }
    @Override public void setActive(boolean v) { this.active = v; }

    // ── Business logic ──────────────────────────────────────
    public boolean isLowStock()   { return stockQty <= reorderLevel && stockQty > 0; }
    public boolean isOutOfStock() { return stockQty <= 0; }
    public double  getMargin()    { return unitPrice - unitCost; }
    public double  getMarginPct() {
        return unitCost == 0 ? 0 : (getMargin() / unitCost) * 100.0;
    }

    public String getDetails() {
        return String.format("%s | %s | Rs %.2f | Stock: %d",
            name, category != null ? category : "Uncategorized", unitPrice, stockQty);
    }

    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    public boolean isLowStock(int threshold) {
        return stockQty <= threshold && stockQty > 0;
    }

    public void updateStock(int delta) {
        int updated = stockQty + delta;
        if (updated < 0) throw new IllegalArgumentException("Stock cannot become negative");
        stockQty = updated;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public String getMedicineID() {
        return String.valueOf(getId());
    }

    public void deductStock(int qty) {
        if (qty > stockQty) throw new IllegalArgumentException("Insufficient stock for " + name);
        stockQty -= qty;
    }

    public void addStock(int qty) {
        if (qty <= 0) throw new IllegalArgumentException("Quantity must be positive");
        stockQty += qty;
    }

    // ── Getters / Setters ───────────────────────────────────
    public String  getName()                     { return name; }
    public void    setName(String v)             { this.name = v; }
    public String  getGenericName()              { return genericName; }
    public void    setGenericName(String v)      { this.genericName = v; }
    public String  getCategory()                 { return category; }
    public void    setCategory(String v)         { this.category = v; }
    public String  getBarcode()                  { return barcode; }
    public void    setBarcode(String v)          { this.barcode = v; }
    public void    setUnitPrice(double v)        { this.unitPrice = v; }
    public double  getUnitCost()                 { return unitCost; }
    public void    setUnitCost(double v)         { this.unitCost = v; }
    public int     getStockQty()                 { return stockQty; }
    public void    setStockQty(int v)            { this.stockQty = v; }
    public int     getStockQuantity()            { return stockQty; }
    public void    setStockQuantity(int v)       { this.stockQty = v; }
    public int     getReorderLevel()             { return reorderLevel; }
    public void    setReorderLevel(int v)        { this.reorderLevel = v; }
    public boolean isNarcotic()                  { return isNarcotic; }
    public void    setNarcotic(boolean v)        { this.isNarcotic = v; }
    public boolean isScheduled()                 { return isScheduled; }
    public void    setScheduled(boolean v)       { this.isScheduled = v; }
    public int     getSupplierId()               { return supplierId; }
    public void    setSupplierId(int v)          { this.supplierId = v; this.supplierID = String.valueOf(v); }
    public String  getSupplierID()               { return supplierID != null ? supplierID : String.valueOf(supplierId); }
    public void    setSupplierID(String v)       { this.supplierID = v; }
    public LocalDate getExpiryDate()             { return expiryDate; }
    public void    setExpiryDate(LocalDate v)    { this.expiryDate = v; }
    public String  getSupplierName()             { return supplierName; }
    public void    setSupplierName(String v)     { this.supplierName = v; }
    public String  getManufacturer()             { return manufacturer; }
    public void    setManufacturer(String v)     { this.manufacturer = v; }
    public String  getDosageForm()               { return dosageForm; }
    public void    setDosageForm(String v)       { this.dosageForm = v; }
    public String  getStrength()                 { return strength; }
    public void    setStrength(String v)         { this.strength = v; }

    @Override
    public String toString() { return name + " [Stock: " + stockQty + "]"; }
}
