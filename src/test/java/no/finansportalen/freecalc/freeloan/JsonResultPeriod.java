package no.finansportalen.freecalc.freeloan;

public class JsonResultPeriod {

    public JsonResultPeriod() {
    }

    public JsonResultPeriod(int error) {
        this.error = error;
    }

    int error = 0;

    
    
    // for AnnuityLoan

    /**
     * Net periodic payment, exclusive of fees, rounded
     */
    private double annPayment;

    /**
     * number of terms; The number of periodic payments it takes to pay down this segment of the loan
     */
    private double annNumberOfTerms;

    /**
     * periodic fee Practical to have here
     */
    private double annPeriodicFee;

    /**
     * adjusted lower segment limit Since the number of periods must be and integer, you rarely hit the segment limit
     * accurately. The next segments limits must be adjusted accordingly.
     */
    private double annLowerSegmentLimit;
    /**
     * upper segment limit Normally the same as 'priceStorage[step][2]', but could also be adjusted
     */
    private double annUpperSegmentLimit;

    /**
     * Fractions of cents we pay to little or too much each time due to rounding
     */
    private double annRemainder;// ;

    
    
    // for SerialLoan

    /**
     * The whole rounded payment for period 'i', included installment and fees.
     */
    private double serPayment;

    /**
     * The installment for period 'i'
     */
    private double serInstallment;

    /**
     * The fee for period 'i'
     */
    private double serFee;

    public double getAnnPayment() {
        return annPayment;
    }

    public void setAnnPayment(double annPayment) {
        this.annPayment = annPayment;
    }

    public double getAnnNumberOfTerms() {
        return annNumberOfTerms;
    }

    public void setAnnNumberOfTerms(double annNumberOfTerms) {
        this.annNumberOfTerms = annNumberOfTerms;
    }

    public double getAnnPeriodicFee() {
        return annPeriodicFee;
    }

    public void setAnnPeriodicFee(double annPeriodicFee) {
        this.annPeriodicFee = annPeriodicFee;
    }

    public double getAnnLowerSegmentLimit() {
        return annLowerSegmentLimit;
    }

    public void setAnnLowerSegmentLimit(double annLowerSegmentLimit) {
        this.annLowerSegmentLimit = annLowerSegmentLimit;
    }

    public double getAnnUpperSegmentLimit() {
        return annUpperSegmentLimit;
    }

    public void setAnnUpperSegmentLimit(double annUpperSegmentLimit) {
        this.annUpperSegmentLimit = annUpperSegmentLimit;
    }

    public double getAnnRemainder() {
        return annRemainder;
    }

    public void setAnnRemainder(double annRemainder) {
        this.annRemainder = annRemainder;
    }

    public double getSerPayment() {
        return serPayment;
    }

    public void setSerPayment(double serPayment) {
        this.serPayment = serPayment;
    }

    public double getSerInstallment() {
        return serInstallment;
    }

    public void setSerInstallment(double serInstallment) {
        this.serInstallment = serInstallment;
    }

    public double getSerFee() {
        return serFee;
    }

    public void setSerFee(double serFee) {
        this.serFee = serFee;
    }

}
