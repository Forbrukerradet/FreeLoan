package no.finansportalen.freecalc.freeloan.calc;

import no.finansportalen.freecalc.common.AnnuityLoanPeriod;
import no.finansportalen.freecalc.common.Utils;
import no.finansportalen.freecalc.freeloan.calc.FreeLoanException.FreeLoanExceptionType;

class IntervallengthCalc {

    /**
     * The size of the principal in this segment.
     */
    private double upperlimit;

    /**
     * Nominal, annual interest rate. RATE CANNOT BE ZERO (!= 0)
     */
    private double rate;

    /**
     * Remaining principal after the interval is over
     */
    private double lowerlimit;

    /**
     * The remaining number of periodical payments for the whole loan when the function is called. CANNOT BE ZERO (!= 0)
     */
    private double periods;

    /**
     * NORMAL: Annuities are rounded after normal rules UP: Rounded up DOWN. Rounded down
     */
    private Utils.RoundDirection roundDirection;

    /**
     * false: Payment rounded to nearest 1/100 true: Rounded to nearest integer
     */
    private boolean roundToInteger;

    /**
     * Factor the nominal annual rate in percent is divided by to obtain the rate in decimal fraction
     */
    private int rateDivisor;

    /**
     * The interest from an eventual residual/balloon added to each period payment.
     */
    private double interestAmountRes;

    /**
     * Boolean. If 'false', we use normal annuities (annuity-immediate). Otherwise annuities in advance (annuity-due).
     */
    private boolean advance;

    /**
     * <p>
     * <b>WHAT THE FUNCTION DOES:</b>
     * </p>
     * 
     * <p>
     * It computes how many periods it takes to pay the segment's principal 'upperlimit' down to the amount 'lowerlimit'
     * at the given interest rate and the given number of periods ('periodsRemaining') that are left of the loan period.
     * </p>
     * 
     * <p>
     * The number of periods cannot be a decimal number. The answer is thus rounded up to the nearest integer, because
     * we want to make sure that the principal in the interval is payed down. (When there is subsequent intervals, the
     * interest rate does not change before the principal limit is surpassed).
     * </p>
     * 
     * <p>
     * When the number of periods are rounded up, we don't "hit" the principal limit accurately. Normally, the loan will
     * be payed further down - the remaining principal is lower than the limit. This remaining principal is computed and
     * returned in 'answer[3]'.
     * </p>
     * 
     * <p>
     * When we compute the number of periods, we don't include periodical fees, because these fees don't affect the
     * remaining principal in the bank's books.
     * </p>
     * 
     * <p>
     * The periodical payment is rounded before the computations begin.
     * </p>
     * 
     * 
     * <p><b>DERIVATION OF FORMULAS</b></p>
     * 
     * <p>
     * a) <b>ANNUITY-IMMEDIATE</b>
     * </p>
     * 
     * <p>
     * Consider a loan of 2.5 millions, where you pay 3.95% as long as the remaining principal is greater than 2
     * millions. You pay 4.05% when the loan is between 1 million and 2 millions. You pay 4.15% if the loan is paid off
     * below 1 million. The example loan runs for 20 years with monthly payments, 240 payments in all:
     * </p>
     * 
     * <p>
     * We can easily compute the first annuity, using the annuity formula:
     * </p>
     * 
     ********************************************************************************* 
     * <p><b>FORMULA FOR ANNUITY-IMMEDIATE:</b></p>
     * 
     * annuity = loan * (1 - k) / (k - Math.pow(k,termnumber+1));<br>
     * where<br>
     * k = 1/(1+rate);<br>
     ********************************************************************************* 
     * 
     * <p>
     * It will be 15,083.72
     * </p>
     * 
     * <p>
     * At some point, the first threshold 2,000,000 will be passed, and the rate changes. But how many periods will this
     * take?
     * </p>
     * 
     * <p>
     * We know that if we pay 15.083.72 per month in 240 months, we will have paid the 2,5 million. The present value
     * over 240 terms is 2,5 millions:
     * </p>
     * 
     * <p>
     * PV(240) = 2,5 millons
     * </p>
     * 
     * <p>
     * After an unknown number of periods – x –, we will pass the 2 million threshold. Then, the present value of the
     * remaining loan will be 2 millions.
     * </p>
     * 
     * <p>
     * PV(x) = 2 millons
     * </p>
     * 
     * 
     * <p>
     * PV(240) - PV(x) = 500,000
     * </p>
     * 
     * 
     * <p>
     * We rearrange the annuity formula with respect to the loan:
     * </p>
     * 
     * <p>
     * capital = annuity * (k – k^(termnum+1)) /(1 - k)
     * </p>
     * 
     * <p>
     * 2,000,000 = annuity * (k – k^(x+1)) /(1 - k)
     * </p>
     * 
     * <p>
     * We call 2,000,000 ‘PV’ and the annuity ‘a’:
     * </p>
     * 
     * <p>
     * PV = a * (k – k^(x+1)) /(1 - k) => (PV * (1 – k)/a )= (k – k^(x+1)) => (PV * (1 – k)/a) –k = – k^(x+1) => (PV *
     * (1 – k)/a) –k = – k^(x+1)
     * </p>
     * 
     * <p>
     * We multiply by -1:
     * </p>
     * 
     * <p>
     * k – (PV * (1 – k)/a) = k^(1+x)
     * </p>
     * 
     * <p>
     * The whole left side consists of constants. We can set
     * </p>
     * 
     * <p>
     * II C = k – (PV * (1 – k)/a) I+II C = k^(1+x)
     * </p>
     * 
     * <p>
     * Taking the logarithm of both sides:
     * </p>
     * 
     * <p>
     * log(C) = (1 + x) * log (k) => log(C) = log (k)+ (x * log (k)) => (log(C) - log (k))/log(k) = x
     * </p>
     * 
     ********************************************************************************* 
     * <p><b>TERM NUMBER FORMULA FOR ANNUITY-IMMEDIATE</b></p>
     * 
     * terms = (log(C)- log (k))/log(k)<br>
     * where:<br>
     * k = 1/(1+rate);<br>
     * C = k – (PV * (1 – k)/a)<br>
     * a = annuity<br>
     * PV = present value<br>
     * (capital/loan/principal)<br>
     ********************************************************************************* 
     * 
     * <p>
     * Subsituting the variable names:
     * </p>
     * 
     * <p>
     * nomrate = 3.95% r = nomrate/1200 k = 1/(1+r) => k = 0.996719133
     * </p>
     * 
     * <p>
     * PV = 2,000,000 a = 15,083.72 => x = 174.515769
     * </p>
     * 
     * <p>
     * Thus, with an annuity of 15,083.72, the present value is 2,000,000 if the loan runs 174.515769 terms/payments.
     * </p>
     * 
     * <p>
     * As term numbers in the real world are always integers, we have to round this number down in order to be certain
     * that the 2 million threshold is passed, and the interest rate thus can change.
     * </p>
     * 
     * <p>
     * 174,515769 => 174
     * </p>
     * 
     * <p>
     * The number of periods in the first interval, where the loan runs with an interest rate of 3.95%, is thus 240 – 174
     * = 66. We repeat this procedure with eventual subsequent intervals.
     * </p>
     * 
     * 
     * <p>
     * b) <b>ANNUITY-DUE</b>
     * </p>
     * 
     * <p>
     * For loans where the annuities are paid in advance, annuity-due, we make a similar derivation:
     * </p>
     ********************************************************************************* 
     * <p><b>FORMULA FOR ANNUITY-DUE:</b></p>
     * 
     * annuity = loan * (1 - k) / (1 - Math.pow(k,termnumber));<br>
     * where<br>
     * k = 1/(1+rate);<br>
     ********************************************************************************* 
     * 
     * <p>
     * We use short forms of the variable names:
     * </p>
     * 
     * <p>
     * a = PV * (1-k) / (1 - Math.pow(k,x)) => (1 - Math.pow(k,x)) = PV * (1-k) / a => - Math.pow(k,x) = (PV * (1-k) /
     * a) - 1 => 1 - (PV * (1-k) / a) = Math.pow(k,x)
     * </p>
     * 
     * 
     * <p>
     * The whole left side consists of constants. We can set
     * </p>
     * 
     * <p>
     * II C = 1 – (PV * (1 – k)/a) I+II C = k^x
     * </p>
     * 
     * <p>
     * Taking the logarithm of both sides:
     * </p>
     * 
     * <p>
     * log(C) = x * log (k) => x = log(C)/ log(k)
     * </p>
     * 
     ********************************************************************************* 
     * <p><b>TERM NUMBER FORMULA FOR ANNUITY-DUE</b></p>
     * 
     * terms = log(C)/ log(k)<br>
     * where:<br>
     * k = 1/(1+rate);<br>
     * C = 1 – (PV * (1 – k)/a)<br>
     * a = annuit<br>
     * PV = present value<br>
     * (capital/loan/principal)<br>
     ********************************************************************************* 
     * 
     * <p>
     * NB! These two formulas don't support negative interest rate, as that would lead to a k < 1
     * </p>
     * 
     * <p>
     * And log(k) is not defines for negative values of 'k'.
     * </p>
     */
    public AnnuityLoanPeriod calculate() throws FreeLoanException {
        
        double fullannuity, C, a, remaintime;

        double k = 1 / (1 + rate / rateDivisor);
        
        if (k < 0) {
            // Negative interest rate not supported
            throw new FreeLoanException(FreeLoanExceptionType.FAILING_CONVERGENCE); 
        }

        
        if (advance) {

            if (periods != 1) {
                fullannuity = upperlimit * (1 - k) / (1 - Math.pow(k, periods));
            } else {
                // 'periods' = 1 would give divitions with zero
                throw new FreeLoanException(FreeLoanExceptionType.FAILING_CONVERGENCE);
            }
        
            // In order to find the correct number of periods, we must use the actually paid - rounded - annuities:

            a = Utils.roundoff(fullannuity, roundDirection, roundToInteger);

            C = 1 - (lowerlimit * (1 - k)/a);

            remaintime = Math.log(C)/Math.log(k); 

        } else {

            if (periods != 0) {
                fullannuity = upperlimit * (1 - k) / (k - Math.pow(k, periods + 1));
            } else {
                throw new FreeLoanException(FreeLoanExceptionType.FAILING_CONVERGENCE);
            }
        
            // In order to find the correct number of periods, we must use the actually paid - rounded - annuities:

            a = Utils.roundoff(fullannuity, roundDirection, roundToInteger);

            C = k - (lowerlimit * (1 - k)/a);

            remaintime = (Math.log(C) - Math.log(k))/Math.log(k);

        }


        /* As the time elapsed in this interval is 'elapsed' = 'periods' minus 'remaintime', we must round 'remaintime' DOWN to 
        be certain the threshold is really passed: */

        
        remaintime = Math.floor(remaintime);


        // The difference between the unrounded and rounded annuity
        double remainder = fullannuity - a; 

        /*
         * REMAINING PRINCIPAL
         * 
         * 
         * This is one of the most subtle/tricky parts of FreeLoan.
         * 
         * We can only pay rounded annuities. One thousandth of a cent does not exist.
         * 
         * The present value of the rounded payments do not exactly match the present value of the theoretical payments
         * writtten with an 'infinite' number of decimal fractions.
         * 
         * But the bank will still deduct the installment part of the annuity from the booked principal. For each term,
         * these two values will differ.
         * 
         * This discrepancy will follow us through the whole computation if we apply the annuity formula directly in
         * order to find the remaining principal.
         * 
         * We find the remaining principal by:
         * 
         * o Computing the principal 'upperlimit_adjusted' using the rounded annuity 'annuity_true' o Computing
         * 'deviation' - the difference between 'upperlimit_adjusted' and 'upperlimit' - the latter a parameter to the
         * function o Computing 'remainingprincipal' as the sum of the present value at the end of the interval using
         * 'annuity_true' and the forward discounted deviation.
         * 
         * 
         * In case the loan has a residual/ballon part that remains at the end of the loan period, the interest amount
         * for an eventual such part of the loan is found in the parameter 'interestAmountRes'. It must be taken into
         * account when rounding:
         */

        // Interest on residual + annuity
        double fullannuityround = Utils.roundoff(fullannuity + interestAmountRes, roundDirection, roundToInteger);
        
        // The rounded payment minus the unrounded interest amount
        double annuity_true = fullannuityround - interestAmountRes;
        
        // The number of periods it takes to pay the principal lower than 'lowerlimit'
        double elapsed = periods - remaintime;
        
        // The number of periods it takes to pay the principal lower than 'lowerlimit'
        // ALTERNATIVELY: var elapsed = Math.ceil(periodsRemaining - remaintime);

        /*
         * We apply the annuity formula again:
         * 
         * annuity = principal * (1 - k) / (k - Math.pow(k,calculation_periods+1))
         * 
         * .. with respect to the principal:
         * 
         * principal = annuity * (k - Math.pow(k,calculation_periods+1))/ (1 - k)
         * 
         * .. if we got an annuity-due (payment in advance) loan:
         * 
         * principal = annuity * (1 - Math.pow(k,calculation_periods))/ (1 - k)
         * 
         * The adjusted principal, 'upperlimit_adjusted' deviates a little from 'upperlimit', as the former is computed
         * with rounded annuities:
         */

        double upperlimit_adjusted;

        if (advance) {
            upperlimit_adjusted = annuity_true * (1 - Math.pow(k, periods)) / (1 - k);
        } else {
            upperlimit_adjusted = annuity_true * (k - Math.pow(k, periods + 1)) / (1 - k);
        }

        /*
         * We call our small deviation - the difference between the inital principal computed with rounded and unrounded
         * payments - 'deviation':
         */

        double deviation = upperlimit - upperlimit_adjusted;

        /*
         * As time passes, this initial, small error will be charged with interest and grows. At any point in time, the
         * remainding debt will be the present value of the rounded, ordinary payments, plus the value of the
         * rate-incurred deviation:
         */

        double remainingprincipal;

        if (advance) {
            remainingprincipal = annuity_true * (1 - Math.pow(k, remaintime)) / (1 - k) + deviation
                    * Math.pow(1 + rate / rateDivisor, elapsed - 1);
        } else {
            remainingprincipal = annuity_true * (k - Math.pow(k, remaintime + 1)) / (1 - k) + deviation
                    * Math.pow(1 + rate / rateDivisor, elapsed);
        }

        /*
         * FIVE ANSWERS
         * 
         * We will return both the annuity and the number of periods it took to pay the loan down to 'lowerlimit' from
         * 'upperlimit' with that annity. We also wish to keep the new, adjusted value of the "outgoing" principal.
         */

        AnnuityLoanPeriod answer = new AnnuityLoanPeriod();
        answer.setPayment(a);
        
        // The real result of the function - the number of periods it takes to pay down this segment of the loan
        answer.setNumberOfTerms(elapsed);
        
        // The remaining principal
        answer.setLowerSegmentLimit(remainingprincipal);
        
        // Same as the parameter
        answer.setUpperSegmentLimit(upperlimit);
        
        // Cents/fractions of cents we pay too much or too little at each annuity due to rounding
        answer.setRemainder(remainder);

        return answer;

    }

    /**
     * @param upperlimit The size of the principal in this segment.
     */
    public void setUpperlimit(double upperlimit) {
        this.upperlimit = upperlimit;
    }

    /** 
     * @param rate Nominal, annual interest rate. RATE CANNOT BE ZERO (!= 0)
     */
    public void setRate(double rate) {
        this.rate = rate;
    }

    /**
     * @param lowerlimit Remaining principal after the interval is over
     */
    public void setLowerlimit(double lowerlimit) {
        this.lowerlimit = lowerlimit;
    }

    /**
     * @param periods_remaining The remaining number of periodical payments for the whole loan when
     * the function is called. CANNOT BE ZERO (!= 0)
     */
    public void setPeriods(double periods_remaining) {
        this.periods = periods_remaining;
    }

    /**
     * @param roundDirection NORMAL: Annuities are rounded after normal rules UP: Rounded up DOWN. Rounded down
     */
    public void setRoundDirection(Utils.RoundDirection round_direction) {
        this.roundDirection = round_direction;
    }

    /**
     * @param roundToInteger false: Payment rounded to nearest 1/100 true: Rounded to nearest integer
     */
    public void setRoundToInteger(boolean roundToInteger) {
        this.roundToInteger = roundToInteger;
    }

    /**
     * @param rate_divisor Factor the nominal annual rate in percent is divided by to obtain the rate in decimal fraction
     */
    public void setRateDivisor(int rate_divisor) {
        this.rateDivisor = rate_divisor;
    }

    /**
     * @param interest_amount_res The interest from an eventual residual/balloon added to each period payment.
     */
    public void setInterestAmountRes(double interest_amount_res) {
        this.interestAmountRes = interest_amount_res;
    }

    /**
     * @param advance Boolean. If 'false', we use normal annuities (annuity-immediate). Otherwise annuities in
     * advance (annuity-due).
     */
    public void setAdvance(boolean advance) {
        this.advance = advance;
    }
}