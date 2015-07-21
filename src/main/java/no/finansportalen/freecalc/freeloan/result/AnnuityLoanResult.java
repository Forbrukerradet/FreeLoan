package no.finansportalen.freecalc.freeloan.result;

import no.finansportalen.freecalc.common.AnnuityLoanPeriod;


public class AnnuityLoanResult extends FreeLoanResult<AnnuityLoanPeriod> {
    
    /**
     * What is left of the loan in the last term due to rounding
     */
    private double residue;

    
    
    

    /**
     * @return What is left of the loan in the last term due to rounding
     */
    public double getResidue() {
        return residue;
    }

    public void setResidue(double residue) {
        this.residue = residue;
    }
    
    
}
