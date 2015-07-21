package no.finansportalen.freecalc.common;

public class SerialLoanPeriod extends Period {

    /**
     *  The installment for period 'i'
     */
    private double installment;
    

    
    /**
     *  @return The installment for period 'i'
     */
    public double getInstallment() {
        return installment;
    }

    public void setInstallment(double installment) {
        this.installment = installment;
    }
}
