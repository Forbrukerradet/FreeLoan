package no.finansportalen.freecalc.freecard.calc;

import java.util.List;

import no.finansportalen.freecalc.common.AnnuityLoanPeriod;
import no.finansportalen.freecalc.freeloan.calc.FreeLoanException;


/**
 * 
 * <p>
 * For an ordered stream of payments, the calculator computes the effective, annual interest
 * rates through iterations.
 * </p>
 * 
 * <p>
 * The function 'calculate' returns one single number - the effective, annual interest rate in the common format:
 * 3,521% pro anno -> 3.521
 * </p>
 * 
 * 
 * <p>
 * We apply the annuity formula:
 * </p>
 * 
 * <br>
 * <br>
 * <p>
 * 1) <b>ANNUITY-IMMEDIATE</b>
 * </p>
 * 
 * <p>
 * (When 'advance == false')
 * </p>
 * 
 * 
 ********************************************************************************* 
 * <p>
 * <b>FORMULA FOR ANNUITY-IMMEDIATE:</b>
 * </p>
 * 
 * annuity = loan * (1 - k) / (k - Math.pow(k,termnumber+1));<br>
 * where<br>
 * k = 1/(1+rate);<br>
 ********************************************************************************* 
 * 
 * <p>
 * We want to compute 'rate'.
 * </p>
 * 
 * <p>
 * We use other variable names:
 * </p>
 * 
 * <p>
 * payment = principal * (1 - k) / (k - Math.pow(k,calculationPeriods+1));
 * </p>
 * 
 * <p>
 * We rearrange it with respect to 'principal':
 * </p>
 * 
 * <p>
 * payment/principal = (1 - k) / (k - Math.pow(k,calculationPeriods+1));
 * </p>
 * 
 * <p>
 * => principal * (1-k) = payment * (k - Math.pow(k,calculationPeriods+1));
 * </p>
 * 
 * <p>
 * => principal = payment * (k - Math.pow(k,calculationPeriods+1))/ (1-k);
 * </p>
 * 
 * <p>
 * We want the right hand expression to be a present value equal to 'principal'. For computation purposes, we thus call
 * the principal 'PV':
 * </p>
 * 
 * <p>
 * PV = payment * (k - Math.pow(k,calculationPeriods+1))/ (1-k);
 * </p>
 * 
 * <p>
 * For each interval/step we shall now compute the present value of the stream of payments from the beginning to the end
 * of the interval.
 * </p>
 * 
 * <p>
 * We do that by subtracting the present value of one stream of payments from another:
 * </p>
 * 
 * <ol>
 *  <li>
 *      A strem of payments with the current periodical amount running from the first payment in the loan to the last
 *      payment in the interval:<br>
 *      payment * (k - Math.pow(k,intervalEnd+1))/ (1-k)
 *  </li>
 * 
 *  <li>
 *      Minus a strem of payments with the same amount running from the first payment in the loan to the first payment in
 *      the interval:<br>
 *      payment * (k - Math.pow(k,intervalStart+1))/ (1-k)
 *  </li>
 * </ol>
 * 
 * 
 * <p>
 * The present value of the payment stream from the beginning to the end of the interval transpires by subtracting the
 * second expression from the first:
 * </p>
 * 
 * <p>
 * PV = payment * (k - Math.pow(k,intervalEnd+1))/ (1-k) - payment * (k - Math.pow(k,intervalStart+1))/ (1-k)
 * </p>
 * 
 * <p>
 * We simplify:
 * </p>
 * 
 * <p>
 * => PV = (payment * k - payment * Math.pow(k,intervalEnd+1) - payment * k + payment * Math.pow(k,intervalStart+1))/
 * 1-k
 * </p>
 * 
 * <p>
 * The terms 'payment * k' falls against each other, and we get:
 * </p>
 * 
 * <p>
 * => (payment * Math.pow(k,intervalStart+1) - payment * Math.pow(k,intervalEnd+1)) / 1-k
 * </p>
 * 
 * <p>
 * We can isolate 'payment / (1 - k)':
 * </p>
 * 
 * <p>
 * PV = (payment/(1-k)) * (Math.pow(k,intervalStart+1) - Math.pow(k,intervalEnd+1));
 * </p>
 * 
 * <p>
 * 'k' (the discount factor) is the unknown entity in our expression. The other variables are known.
 * </p>
 * 
 * 
 * <br>
 * <br>
 * <p>
 * 2) <b>ANNUITY-DUE</b>
 * </p>
 * 
 * <p>
 * (When 'advance == true')
 * </p>
 * 
 ********************************************************************************* 
 * <p>
 * <b>FORMULA FOR ANNUITY-DUE:</b>
 * </p>
 * 
 * annuity = loan * (1 - k) / (1 - Math.pow(k,termnumber));<br>
 * where<br>
 * k = 1/(1+rate);<br>
 ********************************************************************************* 
 * 
 * <p>
 * We reason the same way as for annuity-immediate, but use the annuity-due formula
 * </p>
 * 
 * <p>
 * PV = payment * (1 - Math.pow(k,termnumber))/(1 - k);
 * </p>
 * 
 * <p>
 * So that our present value becomes:
 * </p>
 * 
 * <p>
 * PV = payment * (1 - Math.pow(k,intervalEnd))/ (1-k) - payment * (1 - Math.pow(k,intervalStart))/ (1-k)
 * </p>
 * 
 * <p>
 * And the algebra:
 * </p>
 * 
 * <p>
 * PV = payment/(1-k) - payment * Math.pow(k,intervalEnd)/(1-k) - payment/(1-k) + payment *
 * Math.pow(k,intervalStart)/(1-k)
 * </p>
 * 
 * <p>
 * The first and third term falls, leaving:
 * </p>
 * 
 * <p>
 * PV = payment * Math.pow(k,intervalStart)/(1-k) - payment * Math.pow(k,intervalEnd)/(1-k)
 * </p>
 * 
 * <p>
 * Common factor payment/(1-k) put ouside:
 * </p>
 * 
 * <p>
 * PV = payment/(1-k) * (Math.pow(k,intervalStart) - Math.pow(k,intervalEnd))
 * </p>
 * 
 * 
 ************************************************************************************************************************************************ 
 * 
 * <p>
 * <b>NEWTON'S METHOD - DIFFERENTIATION</b>
 * </p>
 * 
 * <p>
 * No formula gives us the effective interest rate directly. We find it by trying a likely value for 'k', adjusting it
 * until we get the interest rate with the accuracy we want. Newton's method shortens this try and fail process. It
 * postulates that our 'k' will be found close to where the tangent to the curve in the present PV/k point crosses the
 * k-axes:
 * </p>
 * 
 * <p>
 * <a href="http://en.wikipedia.org/wiki/Newton%27s_method">http://en.wikipedia.org/wiki/Newton%27s_method</a>
 * </p>
 * 
 * <p>
 * The graph is in the PV/k space. A value of 'k' will give a value of 'PV': PV = f(k).
 * </p>
 * 
 * <p>
 * The steepness of the tangent line is given by the differentiated function with respect to 'k':
 * </p>
 * 
 * <p>
 * PV = (payment/(1-k)) * (Math.pow(k,intervalStart+1) - Math.pow(k,intervalEnd+1));
 * </p>
 * 
 * <p>
 * For easier differentiation, we handle the two expressions on each side of the multiplication sign separately. We call
 * them 'A' and 'B';
 * </p>
 * 
 * <p>
 * PV = A*B; where
 * </p>
 * 
 * <p>
 * A = payment/(1-k);
 * </p>
 * 
 * <p>
 * B = Math.pow(k,intervalStart+1) - Math.pow(k,intervalEnd+1);
 * </p>
 * 
 * <p>
 * One can find the differentiation rules on wikipedia:
 * <a href="http://en.wikipedia.org/wiki/Differentiation_rules">http://en.wikipedia.org/wiki/Differentiation_rules</a>
 * </p>
 * 
 * <p>
 * PV' = A'*B + A*B'
 * </p>
 * 
 * <p>
 * We fist differietiate 'A' separately according to the Quotient rule:
 * </p>
 * 
 * <p>
 * A' = (payment' * (1-k) - payment* (1-k)') / (1-k)^2
 * </p>
 * 
 * <p>
 * The differntiated of the constant 'payment' is 0. The differentiated of the variable 'k' er 1:
 * </p>
 * 
 * <p>
 * => A' = - payment* (1-k)' / (1-k)^2
 * </p>
 * 
 * <p>
 * => A' = payment / Math.pow(1-k,2)
 * </p>
 * 
 * <p>
 * Then we differentiate 'B' according to the Power rule (y = x^n => y' = n*x^(n-1)):
 * </p>
 * 
 * <p>
 * B = Math.pow(k,intervalStart+1) - Math.pow(k,intervalEnd+1);
 * </p>
 * 
 * <p>
 * B' = (intervalStart+1)*Math.pow(k,intervalStart) - (intervalEnd+1) * Math.pow(k,intervalEnd)
 * </p>
 * 
 * <p>
 * We then assemble the whole differentiated PV according to the Product rule:
 * </p>
 * 
 * <p>
 * PV' = (payment / Math.pow(1-k,2)) * (Math.pow(k,intervalStart+1) - Math.pow(k,intervalEnd+1)) +
 * (payment/(1-k))*((intervalStart+1)*Math.pow(k,intervalStart) - (intervalEnd+1) * Math.pow(k,intervalEnd));
 * </p>
 * 
 * <p>
 * As there is no notation for the differentiated in the programming language, we rename the differentiated
 * PV' 'PVDif':
 * </p>
 * 
 * <p>
 * PVDif = PV'
 * </p>
 * 
 * <br>
 * <br>
 * <p>
 * <b>ANNUITY-DUE</b>
 * </p>
 * 
 * <p>
 * (When 'advance == true')
 * </p>
 * 
 * <p>
 * We reason the same way:
 * </p>
 * 
 * <p>
 * PV = payment/(1-k) * (Math.pow(k,intervalStart) - Math.pow(k,intervallslutt))
 * </p>
 * 
 * <p>
 * A = payment/(1-k);
 * </p>
 * 
 * <p>
 * B = Math.pow(k,intervalStart) - Math.pow(k,intervalEnd);
 * </p>
 * 
 * <p>
 * One can find the differentiation rules on wikipedia: http://en.wikipedia.org/wiki/Differentiation_rules
 * </p>
 * 
 * <p>
 * PV' = A'*B + A*B'
 * </p>
 * 
 * <p>
 * We fist differietiate 'A' separately according to the Quotient rule:
 * </p>
 * 
 * <p>
 * A' = (payment' * (1-k) - payment* (1-k)') / (1-k)^2
 * </p>
 * 
 * <p>
 * Den dervierte av konstanten 'payment' er 0. Den deriverte av variablen 'k' er 1:
 * </p>
 * 
 * <p>
 * => A' = - payment* (1-k)' / (1-k)^2
 * </p>
 * 
 * <p>
 * => A' = - payment / Math.pow(1-k,2)
 * </p>
 * 
 * <p>
 * Then we differentiate 'B' according to the Power rule:
 * </p>
 * 
 * <p>
 * B = Math.pow(k,intervalStart) - Math.pow(k,intervalEnd);
 * </p>
 * 
 * <p>
 * B' = intervalStart * Math.pow(k,intervalStart-1) - intervalEnd * Math.pow(k,intervalEnd-1)
 * </p>
 * 
 * <p>
 * We then assemble the whole differentiated PV' according to the Product rule:
 * </p>
 * 
 * <p>
 * PV' = (payment / Math.pow(1-k,2)) * (Math.pow(k,intervalStart) - Math.pow(k,intervalEnd)) +
 * (payment/(1-k))*(intervalStart*Math.pow(k,intervalStart-1) - intervalEnd * Math.pow(k,intervalEnd-1));
 * </p>
 * 
 * <p>
 * As there is no notation for the differentiated in the programming language, we rename the differentiated
 * PV' 'PVDif':
 * </p>
 * 
 * <p>
 * PVDif = PV'
 * </p>
 ************************************************************************************************************************************************ 
 */
class RateAnnuityCalc {
    
    /**
     * The loan amount the borrower actually receives, net origination (start) fees
     */
    private Double received;
    
    /**
     * The total number of periodic payments the loan spans
     */
    private Integer calculationPeriods;
    
    /**
     * List containing all payments of the loan in each interval of the loan (an interval normally spans many payment terms)
     */
    private List<AnnuityLoanPeriod> payments;
    
    /**
     * The number of the highest 'x' in payments that contains data
     */
    private Integer highestSegment;
    
    /**
     * Due to rounding errors, a small amount could be due or outstanding at the end of the loan payment period
     */
    private Double residue;
    
    /**
     * How many times each year the loan is capitalized. 12 (monthly) is most common, but other frequencies are supported
     */
    private Integer capitalizationFreq;
    
    /**
     * The iterations need an interest rate to start with. A guess for the periodic interest rate in decimal form
     */
    private Double guessrate;
    
    /**
     * When 'true', the terms are payed in advance ("Annuity due"). Otherwise, payments in arrears - ordinary annuities
     */
    private Boolean advance;
    

    public double calculate() throws FreeLoanException {
        
        checkMandatoryFields();

        /*
         * ITERATIONS:
         * 
         * We have two, pretty much identical, alternative while-loops for computing annuity-due and annuity-immediate,
         * respectively. Instead, we could have performed frequent if-test inside the loops, but this would probably
         * have impaired performance more.
         * 
         * When the function is called, at least one nominal interest rate will be among the parameters - the rate in
         * the lowest segment. We skal use this rate as our first guess as to what the effective interest rate might be.
         * 
         * THe function does not use interest rate directly, only via the discount factor 'k'
         */
        double k = 1 / (1+guessrate/100);
        int rounds = 0; // Counts the number of iterations
        double PV = 0; // The present value
        double PVDif = 0; // the differiented of the present value
        double payment;
        double y = 1;  // 'y' is the function value that we want to make close to '0'. Given av value so we can enter the 'while'-loop.

        int intervalStart; // The number of payments from the start of the loan period until the interval starts
        int intervalEnd; // The number of payments from the start of the loan period until the interval ends


        // ANNUITIES IN ADVANCE: Annuities paid at the beginning of each period - annuity-due:
        if (advance) {

            // Here, we set the accuracy we want.
            while (Math.abs(y) > 0.000001 && rounds < 100) { 

                PV = 0; // The present value of the payments
                PVDif = 0; // The differentiated of the present value

                intervalStart = 0;
                intervalEnd = 0;

                /*
                 * Now, we compute the present value 'PV' and the corresponding differetiated 'PVDif' for each interval
                 * of the loan, given the current guess for the discount factor 'k.
                 * 
                 * In the case where the loan runse with different interest rates in succeeding intervals, we sum the
                 * present value for each. We also sum their differentiated values.
                 */
                for (int i = highestSegment - 1; i >= 0; i--) {

                    AnnuityLoanPeriod curPeriod = payments.get(i);

                    /*
                     * In order to compute the effective interest rate, we must compare all we pay with all we receive.
                     * Hence, we must include eventual fees in the periodic payments.
                     */
                    payment = curPeriod.getPayment() + curPeriod.getPeriodicFee();

                    intervalStart = intervalEnd; // The end of the former interval is the start of this

                    intervalEnd += curPeriod.getNumberOfTerms(); // The upper limit of this interval

                    PV += payment / (1 - k) * (Math.pow(k, intervalStart) - Math.pow(k, intervalEnd));

                    /*
                     * The sum of the differentiated of two functions is the sum of the difrentiated. Hence, we simply
                     * sum the differentiated in each interval
                     */
                    PVDif += (payment / Math.pow(1 - k, 2))
                            * (Math.pow(k, intervalStart) - Math.pow(k, intervalEnd))
                            + (payment / (1 - k))
                            * (intervalStart * Math.pow(k, intervalStart - 1) - intervalEnd
                                    * Math.pow(k, intervalEnd - 1));

                }

                /*
                 * One payment - the residue - is so far missing. We add the present value of the residue to the present
                 * value of the other payments. If it is paid in the last period, it must be discounted to find the
                 * present value. The residue is already rounded. In annuity-due (annuities in advance) the last payment
                 * is in period 'calculationPeriods-1':
                 */
                PV += residue * Math.pow(k, calculationPeriods - 1);

                /*
                 * We also want to add the differentiated of the residue to the differiented of the other payments.
                 * 
                 * The residue is a function of 'k', and we use the Power rule
                 * (http://en.wikipedia.org/wiki/Power_rule):
                 * 
                 * y = x^n => y' = n*x^(n-1):
                 * 
                 * PVRes = residue*Math.pow(k,calculationPeriods-1) => PVResDif =
                 * residue*(calculationPeriods-1)*Math.pow(k, calculationPeriods-2);
                 */
                PVDif += residue * (calculationPeriods - 1) * Math.pow(k, calculationPeriods - 2);

                y = PV - received; // Searching for a 'k' making y = 0. Since 'received' is a constant y' = PV'

                double delta = -y / PVDif; // The increase in 'k' necessary at the tangent's intersection with the PV-axis

                if (k + delta != 1) {
                    k += delta; // We increase/decrease 'k' (the annuity function crashes at k=1)
                }

                rounds++;

            }

        // "NORMAL" ANNUITIES: Annuities paid at the end of each period - annuity-immediate:
        } else {

            while (Math.abs(y) > 0.000001 && rounds < 100) // Here, we set the accuracy we are looking at.
            {

                PV = 0; // The present value of the payments
                PVDif = 0; // The differentiated of the present value

                intervalStart = 0;
                intervalEnd = 0;

                /*
                 * Now, we compute the present value 'PV' and the corresponding differetiated 'PVDif' for each interval
                 * of the loan, given the current guess for the discount factor 'k.
                 * 
                 * In the case where the loan runse with different interest rates in succeeding intervals, we sum the
                 * present value for each. We also sum their differentiated values.
                 */
                for (int i = highestSegment - 1; i >= 0; i--) {

                    AnnuityLoanPeriod curPeriod = payments.get(i);

                    /*
                     * In order to compute the effective interesT rate, we must compare all we pay with all we receive.
                     * Hence, we must include eventual fees in the periodic payments.
                     */
                    payment = curPeriod.getPayment() + curPeriod.getPeriodicFee();

                    intervalStart = intervalEnd; // The end of the former interval is the start og this

                    intervalEnd += curPeriod.getNumberOfTerms(); // The upper limit of this interval

                    PV += payment / (1 - k) * (Math.pow(k, intervalStart + 1) - Math.pow(k, intervalEnd + 1));

                    /*
                     * The sum of the differentiated of two functions is the sum of the difrentiated. Hence, we simply
                     * sum the differentiated in each interval
                     */
                    PVDif += (payment / Math.pow(1 - k, 2))
                            * (Math.pow(k, intervalStart + 1) - Math.pow(k, intervalEnd + 1))
                            + (payment / (1 - k))
                            * ((intervalStart + 1) * Math.pow(k, intervalStart) - (intervalEnd + 1)
                                    * Math.pow(k, intervalEnd));

                }

                /*
                 * One payment - the residue - is so far missing. We add the present value of the residue to the present
                 * value of the other paymen. If it is paid in the last period, it must be discounted to find the
                 * present value.
                 * 
                 * The residue was rounded when we computed it earlier:
                 */
                PV += residue * Math.pow(k, calculationPeriods);

                /*
                 * We also want to add the differentiated of the residue to the differiented of the other payments.
                 * 
                 * The residue is a function of 'k', and we use the Power rule
                 * (http://en.wikipedia.org/wiki/Power_rule):
                 * 
                 * y = x^n => y' = n*x^(n-1):
                 * 
                 * PVRes = residue*Math.pow(k,calculationPeriods) => PVResDif =
                 * residue*calculationPeriods*Math.pow(k, calculationPeriods-1);
                 */
                PVDif += residue * calculationPeriods * Math.pow(k, calculationPeriods - 1);

                y = PV - received; // Searching for a 'k' making y = 0. Since 'received' is a constant y' = PV'

                double delta = -y / PVDif; // The increase in 'k' necessary at the tangent's intersection with the PV-axis

                if (k + delta != 1) {
                    k += delta; // We increase/decrease 'k' (the annuity function crashes at k=1)
                }

                rounds++;

            }

        }


        /*
         * 'k' is a discouting factor that was defined as k = 1/(1+e) where 'e' is the periodic, effective interest rate
         * as decimal fraction. (1+e) is the growth rate for each period. Hence, the growth rate for a year is (1+e)^12
         * (if there are 12 capitalizations per year). The effective, annual interest rate in decimal fraction, thus, is
         * ((1+e)^12)-1. In percentage: er = (((1+e)^12)-1)*100. Since k = 1/(1+e) => (1+e) = 1/k, we substiute and get
         * er = ((1/k)^12-1)*100:
         */
        return (Math.pow(1 / k, capitalizationFreq) - 1) * 100; // the effective annual interest rate - the principal result of freeLoan

    }
    
    
    private void checkMandatoryFields() throws FreeLoanException {
        if(received == null) {
            throw new FreeLoanException("received");
        }
        
        if(calculationPeriods == null) {
            throw new FreeLoanException("calculationPeriods");
        }
        
        if(payments == null) {
            throw new FreeLoanException("paymentarray");
        }
        
        if(highestSegment == null) {
            throw new FreeLoanException("highestSegment");
        }
        
        if(residue == null) {
            throw new FreeLoanException("residue");
        }
        
        if(capitalizationFreq == null) {
            throw new FreeLoanException("capitalizationFreq");
        }
        
        if(guessrate == null) {
            throw new FreeLoanException("guessrate");
        }
        
        if(advance == null) {
            throw new FreeLoanException("advance");
        }
    }


    /**
     * The loan amount the borrower actually receives, net origination (start) fees
     */
    public void setReceived(Double received) {
        this.received = received;
    }

    /**
     * The total number of periodic payments the loan spans
     */
    public void setCalculationPeriods(Integer calculationPeriods) {
        this.calculationPeriods = calculationPeriods;
    }

    /**
     * List containing all payments of the loan in each interval of the loan (an interval normally spans many payment terms)
     */
    public void setPayments(List<AnnuityLoanPeriod> payments) {
        this.payments = payments;
    }

    /**
     * The number of the highest 'x' in payments that contains data
     */
    public void setHighestSegment(Integer highestSegment) {
        this.highestSegment = highestSegment;
    }

    /**
     * Due to rounding errors, a small amount could be due or outstanding at the end of the loan payment period
     */
    public void setResidue(Double residue) {
        this.residue = residue;
    }

    /**
     * How many times each year the loan is capitalized. 12 (monthly) is most common, but other frequencies are supported
     */
    public void setCapitalizationFreq(Integer capitalizationFreq) {
        this.capitalizationFreq = capitalizationFreq;
    }

    /**
     * The iterations need an interest rate to start with. A guess for the periodic interest rate in decimal form
     */
    public void setGuessrate(Double guessrate) {
        this.guessrate = guessrate;
    }

    /**
     * When 'true', the terms are payed in advance ("Annuity due"). Otherwise, payments in arrears - ordinary annuities
     */
    public void setAdvance(Boolean advance) {
        this.advance = advance;
    }
    
}
