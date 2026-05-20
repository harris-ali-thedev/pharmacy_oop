package com.pharmacy.model;

import java.io.Serializable;

public interface Payable extends Serializable {
    void processPayment(double amount);
    String generateReceipt();
    String getPaymentType();
}
