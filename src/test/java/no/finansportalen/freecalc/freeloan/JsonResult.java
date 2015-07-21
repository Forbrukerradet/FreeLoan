package no.finansportalen.freecalc.freeloan;


public class JsonResult {

    public JsonResult() {
    }

    public JsonResult(int error) {
        this.error = error;
    }

    int error = 0;

    /**
     * Effective annual interest rate
     */
    private double effectiveInterestRate;

    /**
     * What is left of the loan in the last term due to rounding
     */
    private String resitude;
    
    
    /**
     * Number of periods for payback
     */
    private double paybackPeriodCount;

    private JsonResultPeriod[] periods;

    
    
    
    public double getEffectiveInterestRate() {
        return effectiveInterestRate;
    }

    public void setEffectiveInterestRate(double effectiveInterestRate) {
        this.effectiveInterestRate = effectiveInterestRate;
    }

    public String getResitude() {
        return resitude;
    }

    public void setResitude(String resitude) {
        this.resitude = resitude;
    }

    public JsonResultPeriod[] getPeriods() {
        return periods;
    }

    public void setPeriods(JsonResultPeriod[] periods) {
        this.periods = periods;
    }

    public double getPaybackPeriodCount() {
        return paybackPeriodCount;
    }

    public void setPaybackPeriodCount(double paybackPeriodCount) {
        this.paybackPeriodCount = paybackPeriodCount;
    }
    
    

}