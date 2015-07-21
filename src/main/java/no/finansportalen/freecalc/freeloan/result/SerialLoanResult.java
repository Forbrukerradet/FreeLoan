package no.finansportalen.freecalc.freeloan.result;

import no.finansportalen.freecalc.common.SerialLoanPeriod;

public class SerialLoanResult extends FreeLoanResult<SerialLoanPeriod> {

    private double remainder;

    
    
    
    public double getRemainder() {
        return remainder;
    }

    public void setRemainder(double remainder) {
        this.remainder = remainder;
    }
    
}
