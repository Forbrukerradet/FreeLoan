package no.finansportalen.freecalc.common;

public abstract class Period {

    /**
     * Net periodic payment, exclusive of fees, rounded OR The whole rounded payment for period 'i', included installment and fees
     */
    private double payment;
    
    /**
     * periodic fee OR The fee for period 'i'
     */
    private double periodicFee;
    
    
    
    /**
     * @return Net periodic payment, exclusive of fees, rounded OR The whole rounded payment for period 'i', included installment and fees
     */
    public double getPayment() {
        return payment;
    }

    public void setPayment(double payment) {
        this.payment = payment;
    }
    
    /**
     * @return periodic fee OR The fee for period 'i'
     */
    public double getPeriodicFee() {
        return periodicFee;
    }
    
    public void setPeriodicFee(double periodicFee) {
        this.periodicFee = periodicFee;
    }
    
    
}
