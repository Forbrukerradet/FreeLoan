package no.finansportalen.freecalc.freeloan.calc;



public class PriceStorageStep {
    
    /**
     * Lower limit for the rate segment.
     */
    private double lowerLimit = 0;
    
    /**
     * Upper limit for the rate segment.
     * Double.MAX_VALUE = "unlimited"
     */
    private double upperLimit = Double.MAX_VALUE;
    
    /**
     * The periodical fee for each payment in segment.
     */
    private double periodicalFee;
    
    /**
     * OBLIGATORY: The annual interest rate in the segment (as % per anno)
     */
    private double annualInterest;

    public PriceStorageStep() {}
    
    /**
     * 
     * @param annualInterest The annual interest rate in the segment (as % per anno)
     */
    public PriceStorageStep(double annualInterest) {
        this.annualInterest = annualInterest;
    }
    
    
    /**
     * 
     * @param annualInterest The annual interest rate in the segment (as % per anno)
     * @param periodicalFee The periodical fee for each payment in segment.
     */
    public PriceStorageStep(double annualInterest, double periodicalFee) {
        this.annualInterest = annualInterest;
        this.periodicalFee = periodicalFee;
    }
    
    /**
     * 
     * @param periodicalFee The periodical fee for each payment in segment.
     * @param annualInterest The annual interest rate in the segment (as % per anno)
     * @param lowerLimit Lower limit for the rate segment.
     * @param upperLimit Upper limit for the rate segment.
     */
    public PriceStorageStep(double annualInterest, double periodicalFee, double lowerLimit, double upperLimit) {
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
        this.periodicalFee = periodicalFee;
        this.annualInterest = annualInterest;
    }

    public double getLowerLimit() {
        return lowerLimit;
    }

    /**
     * @param lowerLimit Lower limit for the rate segment.
     */
    public void setLowerLimit(double lowerLimit) {
        this.lowerLimit = lowerLimit;
    }

    public double getUpperLimit() {
        return upperLimit;
    }

    /**
     * @param upperLimit Upper limit for the rate segment.
     * Double.MAX_VALUE = "unlimited"
     */
    public void setUpperLimit(double upperLimit) {
        this.upperLimit = upperLimit;
    }

    public double getPeriodicalFee() {
        return periodicalFee;
    }

    /**
     * @param periodicalFee The periodical fee for each payment in segment.
     */
    public void setPeriodicalFee(double periodicalFee) {
        this.periodicalFee = periodicalFee;
    }

    public double getAnnualInterest() {
        return annualInterest;
    }

    /**
     * @param <b>(OBLIGATORY)</b> The annual interest rate in the segment (as % per anno)
     */
    public void setAnnualInterest(double annualInterest) {
        this.annualInterest = annualInterest;
    }
    
}