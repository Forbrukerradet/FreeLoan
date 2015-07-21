package no.finansportalen.freecalc.common;

public class AnnuityLoanPeriod extends Period {

    /**
     * The number of periodic payments it takes to pay down this segment of the loan
     */
    private double numberOfTerms;

    /**
     * Adjusted lower segment limit Since the number of periods must be and integer, you rarely hit the segment limit
     * accurately. The next segments limits must be adjusted accordingly.
     */
    private double lowerSegmentLimit;

    /**
     * Upper segment limit. Normally the same as periodicFee, but could also be adjusted
     */
    private double upperSegmentLimit;

    /**
     * Remainder; Fractions of cents we pay to little or too much each time due to rounding
     */
    private double remainder;
    
    
    
    
    
    

    /**
     * @return The number of periodic payments it takes to pay down this segment of the loan
     */
    public double getNumberOfTerms() {
        return numberOfTerms;
    }

    public void setNumberOfTerms(double numberOfTerms) {
        this.numberOfTerms = numberOfTerms;
    }

    /**
     * @return Adjusted lower segment limit Since the number of periods must be and integer, you rarely hit the segment limit
     * accurately. The next segments limits must be adjusted accordingly.
     */
    public double getLowerSegmentLimit() {
        return lowerSegmentLimit;
    }

    public void setLowerSegmentLimit(double lowerSegmentLimit) {
        this.lowerSegmentLimit = lowerSegmentLimit;
    }

    /**
     * @return Upper segment limit. Normally the same as periodicFee, but could also be adjusted
     */
    public double getUpperSegmentLimit() {
        return upperSegmentLimit;
    }

    public void setUpperSegmentLimit(double upperSegmentLimit) {
        this.upperSegmentLimit = upperSegmentLimit;
    }

    /**
     * @return Remainder; Fractions of cents we pay to little or too much each time due to rounding
     */
    public double getRemainder() {
        return remainder;
    }

    public void setRemainder(double remainder) {
        this.remainder = remainder;
    }

}
