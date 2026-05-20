package com.pharmacy.model;

public class NarcoticMedicine extends Medicine {
    private static final long serialVersionUID = 1L;

    private int deaScheduleLevel;
    private int dispenseLimit;

    public NarcoticMedicine() {
        super();
        setNarcotic(true);
        setScheduled(true);
    }

    @Override
    public String getDetails() {
        return super.getDetails() + " | Narcotic schedule " + deaScheduleLevel;
    }

    public boolean withinDispenseLimit(int qty) {
        return dispenseLimit <= 0 || qty <= dispenseLimit;
    }

    public int getDeaScheduleLevel() { return deaScheduleLevel; }
    public void setDeaScheduleLevel(int v) { this.deaScheduleLevel = v; }
    public int getDispenseLimit() { return dispenseLimit; }
    public void setDispenseLimit(int v) { this.dispenseLimit = v; }
}
