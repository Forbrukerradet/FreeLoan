package no.finansportalen.freecalc.freecard;

public class JsonResult {

    int error = 0;
    Double governmentEffectiveRate;
    Double governmentMonthlyPayment;
    Double effectiveRate;
    Double monthlyPayment;
    Double remainder;
    
    public int getError() {
        return error;
    }

    public void setError(int error) {
        this.error = error;
    }

    public Double getGovernmentEffectiveRate() {
        return governmentEffectiveRate;
    }
    
    public void setGovernmentEffectiveRate(Double governmentEffectiveRate) {
        this.governmentEffectiveRate = governmentEffectiveRate;
    }
    
    public Double getGovernmentMonthlyPayment() {
        return governmentMonthlyPayment;
    }
    
    public void setGovernmentMonthlyPayment(Double governmentMonthlyPayment) {
        this.governmentMonthlyPayment = governmentMonthlyPayment;
    }
    
    public Double getEffectiveRate() {
        return effectiveRate;
    }
    
    public void setEffectiveRate(Double effectiveRate) {
        this.effectiveRate = effectiveRate;
    }
    
    public Double getMonthlyPayment() {
        return monthlyPayment;
    }
    
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
