package com.pharmacy.model;

public class CardPayment implements Payable {
    private static final long serialVersionUID = 1L;

    private String cardNumber;
    private String cardHolderName;
    private boolean approved;

    public CardPayment() {}

    public CardPayment(String cardNumber, String cardHolderName) {
        this.cardNumber = cardNumber;
        this.cardHolderName = cardHolderName;
    }

    @Override
    public void processPayment(double amount) {
        approved = cardNumber != null && cardNumber.replace(" ", "").length() >= 4 && amount >= 0;
        if (!approved) throw new IllegalArgumentException("Card payment declined");
    }

    @Override
    public String generateReceipt() {
        return "Card payment | " + getMaskedCard();
    }

    @Override
    public String getPaymentType() {
        return "Card";
    }

    public String getMaskedCard() {
        if (cardNumber == null || cardNumber.length() < 4) return "****";
        String digits = cardNumber.replace(" ", "");
        return "**** **** **** " + digits.substring(digits.length() - 4);
    }

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String v) { this.cardNumber = v; }
    public String getCardHolderName() { return cardHolderName; }
    public void setCardHolderName(String v) { this.cardHolderName = v; }
    public boolean isApproved() { return approved; }
}
