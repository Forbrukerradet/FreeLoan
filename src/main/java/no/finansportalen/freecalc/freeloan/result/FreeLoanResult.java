package no.finansportalen.freecalc.freeloan.result;

import no.finansportalen.freecalc.common.Period;


public abstract class FreeLoanResult<T extends Period> {
    
    /**
     * Effective annual interest rate
     */
    private double effectiveInterestRate;
    
    /**
     * Number of iterations made to calculate residue
     */
    private int rounds;
    
    
    /**
     * Number of periods for payback
     */
    private double paybackPeriodCount;
    
    private T[] periods;

    
    
    /**
     * @return Effective annual interest rate
     */
    public double getEffectiveInterestRate() {
        return effectiveInterestRate;
    }

    public void setEffectiveInterestRate(double effectiveInterestRate) {
        this.effectiveInterestRate = effectiveInterestRate;
    }

    public T[] getPeriods() {
        return periods;
    }

    public void setPeriods(T[] periods) {
        this.periods = periods;
    }

    /**
     * @return Number of iterations made to calculate residue
     */
    public int getRounds() {
        return rounds;
    }

    public void setRounds(int rounds) {
        this.rounds = rounds;
    }

    /**
     * @return Number of periods for payback
     */
    public double getPaybackPeriodCount() {
        return paybackPeriodCount;
    }

    public void setPaybackPeriodCount(double paybackPeriodCount) {
        this.paybackPeriodCount = paybackPeriodCount;
    }
    
    
    
}