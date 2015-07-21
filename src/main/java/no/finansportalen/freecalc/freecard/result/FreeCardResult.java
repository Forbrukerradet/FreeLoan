package no.finansportalen.freecalc.freecard.result;

public class FreeCardResult {
    
    Double governmentEffectiveRate;
    Double governmentMonthlyPayment;
    Double effectiveRate;
    Double monthlyPayment;
    Double remainder;

    
    /**
     * Effective, annual interest rate according to the "government template"
     */
    public Double getGovernmentEffectiveRate() {
        return governmentEffectiveRate;
    }
    
    /**
     * Effective, annual interest rate according to the "government template"
     */
    public void setGovernmentEffectiveRate(Double governmentEffectiveRate) {
        this.governmentEffectiveRate = governmentEffectiveRate;
    }
    
    /**
     * Annuity according to the "government template"
     */
    public Double getGovernmentMonthlyPayment() {
        return governmentMonthlyPayment;
    }
    
    /**
     * Annuity according to the "government template"
     */
    public void setGovernmentMonthlyPayment(Double governmentMonthlyPayment) {
        this.governmentMonthlyPayment = governmentMonthlyPayment;
    }
    
    
    /**
     * Effective, annual interest rate when taking interest-free period into account
     */
    public Double getEffectiveRate() {
        return effectiveRate;
    }
    
    
    /**
     * Effective, annual interest rate when taking interest-free period into account
     */
    public void setEffectiveRate(Double effectiveRate) {
        this.effectiveRate = effectiveRate;
    }
    
    
    /**
     * Annuity when taking interest-free period into account
     */
    public Double getMonthlyPayment() {
        return monthlyPayment;
    }
    
    
    /**
     * Annuity when taking interest-free period into account
     */
    public void setMonthlyPayment(Double monthlyPayment) {
        this.monthlyPayment = monthlyPayment;
    }
    
    
    public Double getRemainder() {
        return remainder;
    }
    
    public void setRemainder(Double remainder) {
        this.remainder = remainder;
    }
    
    
}
