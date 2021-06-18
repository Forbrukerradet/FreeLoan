package no.finansportalen.freecalc.freeloan.calc;


import no.finansportalen.freecalc.common.AnnuityLoanPeriod;
import no.finansportalen.freecalc.common.SerialLoanPeriod;
import no.finansportalen.freecalc.common.Utils;
import no.finansportalen.freecalc.freeloan.calc.FreeLoanException.FreeLoanExceptionType;
import no.finansportalen.freecalc.freeloan.result.AnnuityLoanResult;
import no.finansportalen.freecalc.freeloan.result.SerialLoanResult;

import java.util.ArrayList;
import java.util.List;


/**
 * 
 * 
 * <p>
 * The class computes effective annual percentage rate (EAPR) for a loan. As a side product, other values that could
 * come useful are computed too.
 * </p>
 * 
 * 
 * <p>
 * The class handles three different types of loan segments:
 * </p>
 * 
 * <ol>
 * <li>Normal: The interest rate for the whole loan is determined by the initial size of the principal when the loan is
 * given. The rate does not change during the loan period. This is the most common and the simplest model.</li>
 * 
 * <li>Thresholds: The interest rate for the whole loan changes when the principal passes certain thresholds (the
 * parameter 'rateThresholds' == true). For instance, for a loan on 1,5 million, the rate might be 3.5% until it is paid
 * off below 1 million, when the interes rate might rise to 3.75%. But there is only one rate at the time for the whole
 * loan.</li>
 * 
 * <li>Concurrent rates: Separate segments of the loan have different interest rates at the same time. All installments
 * are deducted from the uppermost segment of the loan. This segment, thus, is payed dowm first. For instance, ther rate
 * for the segment between 0 and 500,000 could be 4%, between 500,000 and 1,000,000 i could be 3.5% and above 1,000,000
 * it could be 3%. All applying at the same time. (The parameter 'rateSegments' == true)</li>
 * </ol>
 * 
 * 
 * <p>
 * <b>PARMETERS:</b>
 * </p>
 * 
 * <p>
 * One of the two parameters 'firstpayment' or 'numberofperiods' must have a value. If both have a value,
 * 'numberofperiods' is used. Not all values for 'firstpayment' will be big enough to encompass a down payment of the
 * loan and will thus result in an error.
 * </p>
 * 
 * <p>
 * The function does not return the number of periods for loans that run with multiple, concurrent interest rates in
 * different segments ('rate_segments' == true).
 * </p>
 * 
 * <p>
 * For loans for where the rate can change ('rate_thresholds' == true), the number of periods are computed with the
 * first interest rate. This might be inaccurate.
 * </p>
 * 
 * <p>
 * When there is an initial period where only interest is paid ('interestonly_periods' {@literal >} 0) and the function computes
 * periods, the interest only periods are added to the payment time. The reason is that if it is subtracted, as is
 * otherwise the case, the periodic payments increase in the down payment period. Here, they would increase to an amount
 * bigger than the user has indicated he wants as as his periodical payment.
 * </p>
 * 
 * <p>
 * The combination of a chosen payment/annuity and a loan that changes interest rate after thresholds are passed is
 * possible, but almost surrealistic. This combination will result in an error.
 * </p>
 * 
 * <p>
 * 'ignoreOrigination': Several charges could be incurred when taking a loan. These fees and charges have different
 * names. Some are a percentage of the loan, some are a fixed sum. If the 'ignoreOrigination' variable is TRUE, these
 * fees are ignored when computing effective interest rate. It is logically correct to include them ('ignoreOrigination'
 * = false), but the function is able to do both.
 * </p>
 * 
 * <p>
 * 'annuity-due' == false implies Annuity-immediate: Annuity-immediate (interest in arrears): Equal payments are made at
 * the end of each period. This is the most common annuity loan model. Annuity-due (interest in advance): Payments are
 * made at the beginning of each period. For annuity loans, we presuppose that the whole of the annuity is paid in
 * advance. For serial loans, only the interest is paid in advance.
 * </p>
 * 
 * <p>
 * The annuity period could deviate from the payment period. For instance, the loan might have four annuities a year,
 * but still be paid monthly.
 * </p>
 * 
 * <p>
 * Origination fees: There might be several one-time fees payable at the beginning of the loan period: Arrangement fee,
 * processing fee, application fee, origination fee, appraisal fee, credit report fee, tax service fee, underwriting
 * fee, document preparation fee, wire transfer fee, office administration fee and many others. This function allows two
 * parameters for fees of a fixed sum, called 'feeProcessing' and 'feeDocument'.
 * </p>
 * 
 * <p>
 * Together, these one-time fees are referred to as "origination fees".
 * </p>
 * 
 * <p>
 * (There are normally recurring / periodical fees as well. One is in the variable 'feePeriodPerc'. Others are in the
 * array 'priceStorage').
 * </p>
 * 
 * <p>
 * There is also a parameter for a one-time percentage fee due at the beginning of the loan period: 'feePercentage'.
 * This if often the case for open credit facilities. We make the presumption that the loan is drawn to the credit limit
 * at the start of the loan period, so that this percentage is simply computed from the initial principal.
 * </p>
 * 
 * <p>
 * Normally, you are offered one interest rate for the whole loan sum for the whole loan period. But in some loan
 * contracts the interest rate changes when the loan is payed down beyond certain thresholds. This is signalled with
 * 'rateThresholds' == true.
 * </p>
 * 
 * <p>
 * Even if the interest rate might change when the principal passes certain thresholds, you normally have only one
 * interest rate at the time. But there are loan contracts where the loan might run with separate intrest rates in each
 * segment. For instance, ther rate for the segment between 0 and 500,000 could be 4%, between 500,000 and 1,000,000 i
 * could be 3.5% and above 1,000,000 it could be 3%. All applying at the same time. This is the case when 'rateSegments'
 * == true.
 * </p>
 * 
 * 
 * 
 * <p>
 * Each segment/step is defined by a lower and upper limit. That lowest segment comes first, at 'step' == 1. We
 * presuppose that the segments don't overlap.
 * </p>
 * 
 * 
 * 
 * <p>
 * <b>TERMINOLOGY</b>
 * </p>
 * 
 * <p>
 * These terms will be encoutered through FreeLoan:
 * </p>
 * 
 * <p>
 * "Principal": The remaining debt at any point during the loan period. The principal at the beginning of the loan
 * period might differ from the amount the borrower receives, as the bank might add fees to the debt. (Some sources
 * define the principal only as the size of the intital loan).
 * </p>
 * 
 * <p>
 * "Installment": A payment that dimishes the principal during the loan period. (Some sources use this term for all
 * payments, including interest. Here, only down payments of the principal are meant).
 * </p>
 * 
 * <p>
 * "Term": A regular payment, normally composed of installment, interest rate and fees, made by the borrower after
 * taking the loan.
 * </p>
 * 
 * <p>
 * "Received": The money the borrower actually receives when taking a loan. This might be lower than the debt booked
 * with the bank. Fees make out the difference.
 * </p>
 * 
 * <p>
 * "Loan period": The period from the loan is taken until it has been completely paid back.
 * </p>
 * 
 * <p>
 * "Capitalization": The process of computing accrued interest and fees. Normally, the bank will do this at all agreed
 * payment dates. In that case, the capitalization frequency is the same as the payment frequency. But sometimes they
 * are not. For instance, quarterly capitalization and monthly payments are not unusual.
 * </p>
 * 
 * <p>
 * "Segment": A 'segment' is a part of a loan currently running with a certain interest rate. For example the part of
 * the loan between one and two millions when this is the upper and lower limits for a rate step.
 * </p>
 * 
 * <p>
 * "Interval": An 'interval' is a period in time, for example the number of periods it takes for the principal to be
 * payed down from one segment threshold to the next.
 * </p>
 * 
 * <p>
 * In the simplest and most common loans, there is only one segment and one interval.
 * </p>
 * 
 * <p>
 * <b>INPUT CONTROL PRIOR TO CALLING FREELOAN</b>
 * </p>
 * 
 * <p>
 * The combination of an initial interest free period and running the calculator in period mode might produce absurd
 * results, for instance payback periods of thousands of years. So this combination should be avoided.
 * </p>
 * 
 */
public class FreeLoan {

    /**
     * OBLIGATORY: The loan amount received by the borrower. (Due to fees, this might deviate from the principal in the
     * bank's books).
     */
    private Double received = null;

    /**
     * PERIOD MODE: Either this first payment or 'numberofperiods' is obligatory. with 'firstpayment' != null, the number of periods are computed*)
     */
    private Double firstPayment = null;
    
    /**
     * PAYMENT MODE: Either this loan time or 'firstPayment" is obligatory. With 'numberOfPeriods' != null, the periodical payments (annuities) are computed.
     */
    private Integer numberOfPeriods = null;

    /**
     * OBLIGATORY: In order to compute annual, effective interest rate, some connection to years must be made.
     */
    private Integer periodsPerYear = null;

    /**
     * The planned residual value of the loan ("balloon") to be paid when the loan period is over.
     */
    private double balloon = 0;

    /**
     * Integer > 0: The initial interest-only period wanted by the borrower
     */
    private int interestonlyPeriods = 0;

    /**
     * NORMAL: Annuities are rounded after normal rules UP: Rounded up DOWN. Rounded down
     */
    private Utils.RoundDirection roundDirection = Utils.RoundDirection.NORMAL;

    /**
     * false: Payment rounded to nearest 1/100 true: ..rounded to nearest integer
     */
    private boolean roundToInteger = false;

    /**
     * false: The "global" remainder at the end of the loan period is payed / compensated with the last payment true:
     * ..is ignored
     */
    private boolean ignoreRemainder = false;

    /**
     * false: Origination fee added to the loan and included in the computation. true: Computation performed without
     * origination fee *)
     */
    private boolean ignoreOrigination = false;

    /**
     * false: Annuity-immediate true: Annuity-due
     */
    private boolean annuityDue = false;

    /**
     * 0: 12 (Capitalization 12 times a year). Integer>0: Any number of capitalizations per year.
     */
    private int capitalizationFreq = 0;

    /**
     * Integer >= 0: The maximal interest only-period offered by the bank. In years.
     */
    private int interestonlyPeriodsMax = 0;

    /**
     * Number >= 0: Processing fee: A one-time fee of a fixed sum to be payed at the beginning of the loan period.
     */
    private double feeProcessing = 0;

    /**
     * Number >= 0: Document preparation fee: A one-time fee of a fixed sum to be payed at the beginning of the loan
     * period.
     */
    private double feeDocument = 0;

    /**
     * Number >= 0: Percentage fee: One-time fee to be payed at the beginning of the loan period computed out of the
     * principle (gross loan). 2 = 2%.
     */
    private double feePercentage = 0;

    /**
     * 0: No percentage fee. Number: Percentage. Loans given as a credit line have a periodical has a fee as a percentage
     * of principal PER PERIOD
     */
    private double feePeriodPerc = 0;

    /**
     * false: No - intial interest rate is fixed for the whole loan for the whole loan period. true: Rate might change
     */
    private boolean rateThresholds = false;

    /**
     * false: No - all segments of the loan has the same interest rate. true: Every segment might have separate interest
     * rates.
     */
    private boolean rateSegments = false;

    /**
     * OBLIGATORY: Contains information about product segments. It must contain at least one element with annualInterest
     * step set
     * 
     * 'priceStorage[0]' is not used (setter adds a dummy) because JS implementation was written so
     */
    private PriceStorageStep[] priceStorage = null;
    
    /**
     * Only for serial loans. Does not apply to annuity loans.
     */
    private Utils.Accuracy accuracy = Utils.Accuracy.NORMAL;

    
    
    /**
     * 
     * <p>PREPARATION AND ADAPTION OF DATA</p>
     * <p>INNDATA CONTROL - ERRORS CAUSING INTERRUPTION</p>
     * 
     * <p>Indata control are pressupposedly performed outside of the function. But also a little here.</p>
     */
    private void preprocess() throws FreeLoanException {


        
        // If the the customer wants a longer period when he only pays interest than the bank offers
        // The user wishes a period where only interest is payed
        if (interestonlyPeriods > 0) {

            if (interestonlyPeriodsMax == 0 || interestonlyPeriods > interestonlyPeriodsMax) {
                throw new FreeLoanException(FreeLoanExceptionType.INTEREST_PERIOD_TOO_LONG);
            }

        }

        // The parameter 'priceStorage' is an array. It must have at least one element:
        if (priceStorage.length < 1) {
            throw new FreeLoanException(FreeLoanExceptionType.FIRST_SEGMENT_NOT_DEFINED);
        }

        // .. and return an error number if the requested residual/ballon payment is smaller than the smallest loan
        // offered by the bank:
        if (balloon < priceStorage[1].getLowerLimit()) {
            throw new FreeLoanException(FreeLoanExceptionType.BALLOON_TOO_SMALL);
        }

        // We also return an error value if the residual/ballon payment is bigger than the upper limit of the loan
        // product:
        if (priceStorage[priceStorage.length - 1].getUpperLimit() > 0
                && balloon > priceStorage[priceStorage.length - 1].getUpperLimit()) {
            throw new FreeLoanException(FreeLoanExceptionType.BALLOON_TOO_BIG);
        }
        
        
        /*
         * Correspondingly, FreeLoan does not support the combination of a user chosen periodic payment and loan models
         * where different interest rates run in concurrent loan segments:
         */
        if (firstPayment != null && firstPayment != 0 && rateSegments) {
            throw new FreeLoanException(FreeLoanExceptionType.UNSUPPORTED_COMBINATION_PERIODIC);
        }
        

        // The function uses a default capitalization frequency of 12 (monthly) - although an error message might be
        // more appropriate..
        if (capitalizationFreq == 0) {
            capitalizationFreq = 12;
        }

    }
    
    

    
    
    /**
     * 
     * 
     * <p><b>EFFECTIVE INTEREST RATE FOR ANNUITY LOANS</b></p>
     * 
     * <p><b>WHAT THE FUNCTION DOES:</b></p>
     * 
     * <p>The function computes effective annual percentage rate (EAPR) for an annuity loan. As a side product, other
     * values that could come useful are computed too.
     * 
     * 
     * <p>The function handles three different types of loan segments:
     * 
     * <ol>
     * <li>The interest rate for the whole loan is determined by the initial size of the principal when the loan is
     * given. The rate does not change during the loan period. This is the most common and the simplest model.</li>
     * 
     * <li>The interest rate for the whole loan changes when the principal passes certain thresholds (the parameter
     * 'rateThresholds' == true)</li>
     * 
     * <li>Separate segments of the loan have different interest rates at the same time. All installments are deducted
     * from the uppermost segment of the loan. This segment, thus, is payed dowm first. For instance, ther rate for the
     * segment between 0 and 500,000 could be 4%, between 500,000 and 1,000,000 i could be 3.5% and above 1,000,000 it
     * could be 3%. All applying at the same time. (The parameter 'rateSegments' == true)</li>
     * </ol>
     * 
     * <p><b>SECTIONS:</b></p>
     * 
     * <p>The function consists of seven separate, numbered sections:</p>
     * 
     * <ol>
     * <li>Preparation and adaption of data</li>
     * <li>An eventual residual/balloon payment</li>
     * <li>The number of periods in each interval</li>
     * <li>An eventual interest-only period</li>
     * <li>Global residue</li>
     * <li>Effective interest rate</li>
     * <li>Result reporting</li>
     * </ol>
     * 
     * <p><b>THE PLAN</b></p>
     * 
     * <p>The interest rate is computed by iterations. Before performing the iterations, we need to know the annity and
     * over how many periods it runs. When we have a loan that changes interest rate during the payment period, we could
     * have adjacent intervals with different annuities. The function also supports parallell interest rates.</p>
     * 
     * <p>Hence, all the relevant annuities, number of periods and remainders are first computed and stored in the array
     * 'interval_data'.</p>
     * 
     * <p>Generally, we use the object properties to compute the values in 'interval_data' before we use the values in
     * 'interval_data' to compute the result(s)</p>
     */
    public AnnuityLoanResult annuityLoan() throws FreeLoanException {

        preprocess();
        
        /* 
        * Freloan is not able to compute the effecive interest rate for a loan running with different interest rates at
        * the same time if the loan is an annuity loan with annuity-due - annuities in advance. When 'rateSegments ==
        * true', 'annuityDue == true' and 'serial == false' at the same time, error is thrown
        */
       if (rateSegments && annuityDue) {
           throw new FreeLoanException(FreeLoanExceptionType.UNSUPPORTED_COMBINATION_ADVANCE);
       }

        /*
         * 
         * 1) PREPARATION AND ADAPTION OF DATA:
         * 
         * DATA IN A FORMAT SUITABLE FOR OUR FORMULAS:
         * 
         * It is mathematically correct to take all fees into consideration when computing effective interest rate. But
         * the function also supports the ommittance of the start/orgination fees:
         */

        double principal;

        if (ignoreOrigination) {
            principal = received;
        } else {
            // What the user receives plus fees
            principal = (received + feeDocument) * (100 + feePercentage) / 100;

            principal += feeProcessing;

        }

        // Error if the residual/ballon payment is greater than the principal:

        if (balloon > principal) {
            throw new FreeLoanException(FreeLoanExceptionType.UNSUPPORTED_COMBINATION_ADVANCE);
        }

        /*
         * Total number of calculation periods are the total number of periods, 'numberOfPeriods' divided by
         * 'periodsPerYear' to find the number of years. We then multiply this by the capitalization frequency,
         * 'capitalizationFreq' to find the number of calculation periods.
         */

        // periodsPerYear
        double calculation_periods = 0;
        
        // In JS version, if numberOfPeriods is not set, then calculation_periods stays zero. So are doing we.
        if(numberOfPeriods != null) {
            calculation_periods = numberOfPeriods / (double) periodsPerYear * capitalizationFreq;
        }
        

        // The number of intial interest-only periods (where no installments are paid):
        double installment_grace_periods = interestonlyPeriods / (double) periodsPerYear * capitalizationFreq;

        // The number of "normal" periodical payments that include installment:
        double installment_periods = calculation_periods - installment_grace_periods;

        /*
         * 'rate_divisor' is the divisor the nominal, annual interest rate must be divided by to obtain the periodic
         * interest rate. For instance, a nominal interest rate of 6% equals a nominal monthly decimal rate of 0,005 =>
         * 6/1200.
         */

        int rate_divisor = 100 * capitalizationFreq;

        
        //If the user has chosen to a periodic payment,'firstpayment', rather than the number of periods:


        if (firstPayment != null && firstPayment != 0) {
            
            /* If 'firstpayment' is smaller than the interest rate part, the payment cannot service the loan: */

            if (principal * priceStorage[1].getAnnualInterest() / rate_divisor > firstPayment) {
                throw new FreeLoanException(FreeLoanExceptionType.PAYMENT_TOO_SMALL);
            }

            
            /* If 'firstpayment' equals the interest rate part, we can service the loan only if the balloon equals the principal: */
            if (principal * priceStorage[1].getAnnualInterest() / rate_divisor == firstPayment && principal != balloon) {
                throw new FreeLoanException(FreeLoanExceptionType.PAYMENT_TOO_SMALL);
            }

       }
        
        
        /*
         * 2) AN EVENTUAL RESIDUAL (BALLOON PAYMENT):
         * 
         * The residual/balloon payment is the eventual part of the loan that is not payed back through the periodic
         * payments, but rather through a "balloon" payment at the end of the loan period.
         * 
         * We subtract the balloon from all segment limits before computing the annuity and the length of the payment
         * period for each segment.
         * 
         * ADJUSTMENT OF SEGMENT LIMITS:
         * 
         * When the loan has a planned balloon, we divide it in two:
         * 
         * - The balloon is treated as an interest-only loan
         * 
         * - The other part of the loan is treated as an ordinary annuity loan
         * 
         * But by doing this, the limits for each loan segment in 'priceStorage' will no longer encompass the correct
         * loan amount. If the loan is to have a planned residual (balloon payment) we thus make an adjusted copy of
         * 'priceStorage' where the amount 'balloon' are subtracted from all the limit values.
         * 
         * We place these adjusted limits in a new array called 'price_storage_cop'.
         */

        // The interest amount for the residual (balloon) for each periodic payment
        double interest_amount_res = 0;

        // A copy of 'priceStorage' with adjusted segment limits
        ArrayList<PriceStorageStep> price_storage_cop = new ArrayList<PriceStorageStep>();

        // The remaining principal (less balloon) to be serviced as an annuity loan
        double principal_ann;

        principal_ann = principal - balloon;

        if (balloon > 0) {

            // 'price_storage_cop' could have fewer steps than the original 'priceStorage'
            int nr = 1;

            for (int i = 1; i < priceStorage.length; i++) {

                PriceStorageStep curPriceStorageStep = priceStorage[i];
                /*
                 * If the balloon is lower than the upper limit for the segment, or we are in the first (lower) segment
                 * - where an empty value is interpreted as "unlimited" - this is a relevant segment:
                 */

                if (balloon <= curPriceStorageStep.getUpperLimit()) {

                    if (nr == 1 && price_storage_cop.isEmpty()) {
                        // the 0 element is a dummy
                        price_storage_cop.add(new PriceStorageStep());
                    }

                    price_storage_cop.add(new PriceStorageStep());

                    PriceStorageStep curPriceStorageCop = price_storage_cop.get(nr);

                    if (curPriceStorageStep.getLowerLimit() > balloon) {
                        curPriceStorageCop.setLowerLimit(curPriceStorageStep.getLowerLimit() - balloon);
                    } else {
                        curPriceStorageCop.setLowerLimit(0);
                    }

                    /* If upperLimit is greater than the balloon: */

                    if (curPriceStorageStep.getUpperLimit() >= balloon) {
                        curPriceStorageCop.setUpperLimit(curPriceStorageStep.getUpperLimit() - balloon);
                    } else {
                        curPriceStorageCop.setUpperLimit(0);
                    }

                    // The periodic fee is the same
                    curPriceStorageCop.setPeriodicalFee(curPriceStorageStep.getPeriodicalFee());

                    // The interest rate is the same
                    curPriceStorageCop.setAnnualInterest(curPriceStorageStep.getAnnualInterest());

                    // If there is one more segment..
                    if (curPriceStorageCop.getUpperLimit() != 0) {
                        nr++;
                    }

                }

            }

            // PERIODICAL INTEREST AMOUNT for the balloon:

            if (rateSegments) {

                /*
                 * When 'rateSegments' == true, and the balloon spans more then one segment in 'priceStorage', the
                 * interest rate amount for the balloon is computed using more than one interest rate. However, since
                 * the size of the residual/balloon does not change throught the loan period, the periodical interest
                 * rate amount does not either.
                 * 
                 * We first search for the highest interest rate segment that overlaps the resiudal. We can infer this
                 * from the length of the two arrays/matrixes 'priceStorage' and 'price_storage_cop'. If they have the
                 * same length, this means that the balloon is computed with one interest rate only, and this rate is
                 * the same as the lowest rate in 'priceStorage'. If 'priceStorage' if one segment longer, there is
                 * probably two interest rates for 'balloon'
                 */

                // +1 guarantees at least one run-through
                int start = priceStorage.length - price_storage_cop.size() + 1;

                double high;

                for (int i = start; i > 0; i--) {

                    PriceStorageStep curPriceStorageStep = priceStorage[i];

                    if (balloon > curPriceStorageStep.getLowerLimit() || curPriceStorageStep.getLowerLimit() == 0) {

                        // Double.MAX_VALUE => unlimted
                        if (curPriceStorageStep.getUpperLimit() == Double.MAX_VALUE) {
                            high = balloon;
                        } else {
                            high = curPriceStorageStep.getUpperLimit();
                        }

                        if (balloon > high) {
                            interest_amount_res += (high - curPriceStorageStep.getLowerLimit())
                                    * curPriceStorageStep.getAnnualInterest() / rate_divisor;
                        } else {
                            interest_amount_res += (balloon - curPriceStorageStep.getLowerLimit())
                                    * curPriceStorageStep.getAnnualInterest() / rate_divisor;
                        }

                    }
                }

            }

            
        // In order for the algoritm to be stringent, we use 'price_storage_cop' also when there is no balloon:
        } else {
        
            // 'price_storage_cop' now is a copy of 'priceStorage'
            price_storage_cop = new ArrayList<PriceStorageStep>();
            for (int i = 0; i < priceStorage.length; i++) {
                price_storage_cop.add(priceStorage[i]);
            }
        }

        /*
         * 
         * 3) THE NUMBER OF PERIODS IN EACH INTERVAL:
         * 
         * In order to compute the effective interest rate, we have to know the amount of every payment between the
         * lender and the borrower and on what time they occur.
         * 
         * This part of the program determines how many payments are made with each interest rate (most often, there is
         * only one) and what the payment in that interval is.
         * 
         * (When there is only one interest rate, there is of course only one period - the whole loan period - and one
         * annuity/payment amount).
         * 
         * The values are stored in the array 'interval_data', which subsequently will be used during the
         * iterations in order to compute the effective interest rate.
         * 
         * We find nominal interest rates, fees and segment limits in the function parmeter array 'priceStorage'. In
         * this section, we use an adjusted copy, called 'price_storage_cop'. The segment limits in 'price_storage_cop'
         * might have been reduced by the size of an eventual residual/balloon payment.
         * 
         * Two external objects, 'IntervallenghtCalc' and 'IntervalllenghtSeparateCalc', computes the number of payments in each
         * interval. They return this number in an object were the annuity / periodic payment is also computed.
         * 
         * We traverse the array 'price_storage_cop' backwords, because the principal is highest at the beginning,
         * normally overlapping the higher segments of 'price_storage_cop', eventually being paid down to lower
         * segments.
         * 
         * The thresholds are not passed unless we pay installments, so we are searching for the number of periods only
         * in the segments of the loan where installments are paid. An interest-only period is handled later.
         * 
         * All annuities are composed by an interest part and and installment part. For each term, the installment is
         * subtracted from the principal.
         * 
         * When the principal passes an eventual segment threshold, we should change to the next interest rate. We
         * notice at which time this happens, and store this number of periods in 'interval_data'. We also store the
         * annuity of the interval in 'interval_data'.
         */

        // Periodic payments left of the overall loan period
        double periods_remaining = installment_periods;

        // Lower limit for the current loan segment
        double lowlimit;

        // Upper limit for the current loan segment
        double highlimit;

        // Interest rate in the current loan segment
        double interest_segment = 0;

        // Index of the current segment in 'priceStorage'
        int interval = 0;

        // Index of the the highest index in 'priceStorage'
        int highest_segment = 1;

        // Periodic interest amount in an interest-only period
        double rate_am_int_only = 0;

        // Set to 'true' to interrupt the traversing of 'priceStorage'
        boolean found = false;

        // Index of the current segment/row in 'priceStorage'
        int i = price_storage_cop.size() - 1;

        // Intermediate variable (storage of lower segment limit)
        double segm_lowlim;

        // Intermediate variable (storage of upper segment limit)
        double segm_highlim;

        // Intermediate variable (storage of upper segment limit)
        double upper;

        // Intermediate variable (number of relevant segments in 'priceStorage')
        int step;

        // Intermediate variable
        double annuity_unrounded = 0;

        // Intermediate variable
        int nextsegment;

        // An array that stores intermediate values
        AnnuityLoanPeriod[] interval_data = new AnnuityLoanPeriod[i + 1];

        while (i >= 1 && found == false) {

            AnnuityLoanPeriod curIntervalData = new AnnuityLoanPeriod();
            interval_data[i] = curIntervalData;

            PriceStorageStep curPriceStorageCop = price_storage_cop.get(i);

            // The lower limit of the segment, 'price_storage_cop', derived from 'priceStorage'

            // Current segment's lower limit
            segm_lowlim = curPriceStorageCop.getLowerLimit();

            // If segm_highlim does not exist, it is interpreted as unlimited
            // Current segment's upper limit
            segm_highlim = curPriceStorageCop.getUpperLimit();

            /*
             * Now having detected the lower and upper limit of the segment, we check if any part of the principal lays
             * inside the interval. We use 'principal_ann', less an eventual residual/ballon payment, as our principal:
             */
            if (((principal_ann >= segm_lowlim) && (principal_ann <= segm_highlim)) || interval > 0) {

                /*
                 * 
                 * Interpretation of the above conditions:
                 * 
                 * a) principal_ann>=segm_lowlim:
                 * 
                 * If the principal is greater than or equals the lower limit of the current segment, this might be a segment
                 * that overlaps the whole or part of the loan.
                 * 
                 * b) principal_ann<=segm_highlim:
                 * 
                 * If additionally the principal is less or equal to the upper segment limit,
                 * we have found a segment that encompasses the whole or part of the loan.
                 * 
                 * c) 'interval' > 0:
                 * 
                 * If we already found a valid segment in an earlier loop, then we now that this segment is part of the
                 * loan (since we transverse the array from above)
                 * 
                 * If the interest rate is the same throughout the loan ('rateThresholds' == false), we don't have to
                 * check any more segments. We just retrive this rate from 'price_storage_cop'.
                 * 
                 * This is not the case if 'rateSegments' == true. Then we must use all relevant segments in
                 * 'price_storage_cop'.
                 */

                // If there cannot be separate, parallell rates ..
                if (!rateSegments) {

                    // If the interest rate does not change ..
                    if (!rateThresholds) {

                        /*
                         * IF ONLY ONE INTEREST RATE..
                         * 
                         * In this case, there is only one interest segment - consisting of all periods (see type i) in
                         * the introductory comments to this function).
                         * 
                         * The interest rate is the same throughout the loan ('rateThresholds' == false), we don't have
                         * to check any more segments. We just retrive this rate from 'price_storage_cop' in the current
                         * segment and stop the looping
                         */

                        // Number of segments in the loan
                        step = 1;

                        // Low limit of the one segment
                        lowlimit = 0;

                        // High limit of the one segment
                        highlimit = principal_ann;

                        // End looping
                        found = true;
                        
                        /* When there are more than one segment, the variable 'periods_remaining' will be 
                        updated for each call of the function 'calculate()' of IntervallengthCalc. But not here, where there
                        is only one interest rate. 

                        When the first payment, rather than the number of loan terms, are given. We compute
                        the number of loan terms here and store the value in the variable 'periods_remaining' by means 
                        of the external function 'termnumAnnu()':*/

                    } else {

                        /*
                         * IF ONLY ONE INTEREST RATE AT THE TIME..
                         * 
                         * The interest rate for the whole loan might change when certain thresholds are overstepped
                         * (see type ii) in the introductory comments to this function).
                         */

                        // All relevant segments will be used during the computation
                        step = i;

                        /*
                         * We store the number of relevant segments. There are always at least one. Since we are
                         * counting backwards, from the upper segment, 'highest_segment' - that was initially set to '1'
                         * - is set only once:
                         */

                        if (highest_segment == 1) {
                            highest_segment = i;
                        }

                        // For the current segment
                        lowlimit = segm_lowlim;

                        if (principal_ann <= segm_highlim) {
                            highlimit = principal_ann;
                        } else if ((step + 1) < interval_data.length
                                && interval_data[step + 1].getLowerSegmentLimit() > 0) {
                            highlimit = (interval_data[step + 1].getLowerSegmentLimit());
                        } else {
                            highlimit = segm_highlim;
                        }

                    }

                    /*
                     * ..IN BOTH CASES:
                     * 
                     * In getAnnualInterest() lies the interest rate of the current segment:
                     */

                    interest_segment = price_storage_cop.get(i).getAnnualInterest();

                    /*
                     * Interest amount from an eventual balloon:
                     * 
                     * In order for the object IntervallengthCalc to be able to round the periodic payments
                     * correctly, we must know the interest amount for the balloon. When 'rateSegments' == false, as in
                     * this part of the program, the interest amount of the balloon is computed with the same interest
                     * rate at the rest of the loan:
                     */

                    interest_amount_res = balloon * interest_segment / rate_divisor;

                    /*
                     * The the object IntervallengthCalc computes how many periods it takes to pay down the current
                     * segment.
                     * 
                     * If the value for the number of periods - 'numberOfPeriods' - is not given, but 'firstPayment' is
                     * given instead, we must first compute the number of periods - here expected in the parameter
                     * 'periods_remaining'. For this purpose, we use the function 'termnumAnnu()':
                     */


                    // If the user has entered the annuity rather than the number of payments
                    if (firstPayment != null && firstPayment != 0) {

                            /* Only the net part of the payment - when fees are deducted - are used by the bank
                            to service the loan. We thus have to deduct the fixed fee and the eventual percentage fee.
                            We also deduct the interest of an eventual balloon: */

                            double netpayment = (firstPayment * 12 / capitalizationFreq) - price_storage_cop.get(i).getPeriodicalFee() - highlimit * feePeriodPerc / 100 - interest_amount_res;

                            /* Here, 'periods_remaining' is not given, so we must compute it: */

                            periods_remaining = termnumAnnu(highlimit, netpayment, price_storage_cop.get(i).getAnnualInterest()/rate_divisor, annuityDue);
                    }


                    /* The function 'intervallength' computes how many periods it takes to pay down the current segment. 
                     * It also computes other useful numbers and return them as a row in the array/matrix
                     * 'interval_data' (see the definition of the elements in 'interval_data' further above):
                     */

                    IntervallengthCalc intervalCalc = new IntervallengthCalc();

                    intervalCalc.setUpperlimit(highlimit);
                    intervalCalc.setRate(interest_segment);
                    intervalCalc.setLowerlimit(lowlimit);
                    intervalCalc.setPeriods(periods_remaining);
                    intervalCalc.setRoundDirection(roundDirection);
                    intervalCalc.setRoundToInteger(roundToInteger);
                    intervalCalc.setRateDivisor(rate_divisor);
                    intervalCalc.setInterestAmountRes(interest_amount_res);
                    intervalCalc.setAdvance(annuityDue);

                    interval_data[step] = intervalCalc.calculate();

                } else {

                    /*
                     * ALL SEGMENTS WITH SEPARATE, PARALLELL INTEREST RATES..
                     * 
                     * 
                     * 'rateSegments' == 'true' means the interest rate for the whole loan might change when certain
                     * thresholds are overstepped (see type iii) in the introductory comments to this function).
                     */

                    step = i;
                    PriceStorageStep curPriceStorageCopStep = price_storage_cop.get(step);
                    /*
                     * In order for the object IntervallengthSeparateCalc to be able to compute the whole payment of
                     * the interval - the sum of the payments in the separate, parallell segments - it must have
                     * information of all segments.
                     * 
                     * This information resides in the function parameter 'priceStorage' and the derived version we use
                     * here - 'price_storage_cop'. But a subtle problem arises: Since the number of periods in each
                     * intervall must be an integer, we will hardly ever find a number of periods that make the
                     * principal being paid down to precisely the lower segment limit. Normally, our installments will
                     * have paid the principal down below the limit by some cents. This remaining principal must be set
                     * as the new upper limit of the next segment.
                     * 
                     * We must check whether such a limit was set in the former segment. Since we are looping backwords,
                     * the former segment is the segment 'step+1'.
                     * 
                     * 
                     * We store the number of relevants segments. Since 'highest_segment' is initiated to '1', it will
                     * be set only once:
                     */

                    if (highest_segment == 1) {
                        highest_segment = i;
                    }

                    /*
                     * If the upper limit is not set, or the principal is smaller than the maximum limit, the highest
                     * limit equals the principal:
                     */

                    if (principal_ann <= segm_highlim) {
                        curPriceStorageCopStep.setUpperLimit(principal_ann);
                    }

                    /*
                     * In getLowerSegmentLimit() for the former segment lies the real, functional lower limit of that
                     * segment. We check whether it exists. If it does, it is also the upper limit for the current
                     * segment:
                     */

                    nextsegment = step + 1;

                    if (nextsegment < interval_data.length) {
                        AnnuityLoanPeriod nextIntervalDataSegment = interval_data[nextsegment];
                        if (nextIntervalDataSegment.getLowerSegmentLimit() > 0
                                && nextIntervalDataSegment.getLowerSegmentLimit() != Double.MAX_VALUE) {
                            curPriceStorageCopStep.setUpperLimit(nextIntervalDataSegment.getLowerSegmentLimit());
                        }
                    }

                    /*
                     * Now, we can invoke the function IntervallengthSeparateCalc() that returns a fully computed segment
                     */
                    IntervallengthSeparateCalc calc = new IntervallengthSeparateCalc();
                    calc.setSegmentarray(price_storage_cop);
                    calc.setStep(step);
                    calc.setPeriodsRemaining(periods_remaining);
                    calc.setRoundDirection(roundDirection);
                    calc.setRoundToInteger(roundToInteger);
                    calc.setRateDivisor(rate_divisor);
                    calc.setInterestAmountRes(interest_amount_res);

                    interval_data[step] = calc.calculate();

                    if (installment_grace_periods > 0)

                    {

                        /*
                         * No installment is being paid in an interest-only period. We just have to sum up the interest
                         * amounts of all parallell segments overlapping the whole loan.
                         */
                        if (curPriceStorageCopStep.getUpperLimit() > principal_ann) {
                            upper = principal_ann;
                        } else {
                            upper = curPriceStorageCopStep.getUpperLimit();
                        }

                        /*
                         * 'rate_am_int_only' - the periodic interest amount during an interest only-period will be used
                         * in the section for 'installment_grace_periods' below:
                         */
                        rate_am_int_only += (upper - curPriceStorageCopStep.getLowerLimit())
                                * curPriceStorageCopStep.getAnnualInterest() / rate_divisor;

                    }

                }

                AnnuityLoanPeriod curIntervalDataStep = interval_data[step];

                if (balloon > 0)

                {

                    /*
                     * If this loan has a residual/ballon, the periodic payment we have computed is too low. It does not
                     * contain the interest amount for the part of the loan where only interest is paid.
                     * 
                     * We have to add this amount here.
                     * 
                     * Since it in somE cases impacts the rounding, we add the remainder.
                     * 
                     * But if the remainer is to be ignored - 'ignoreRemainder == true', we simply round the remainer as
                     * if is.
                     */

                    // If the remainder after paying off the loan is not to be ignored
                    if (ignoreRemainder) {

                        annuity_unrounded = curIntervalDataStep.getPayment() + interest_amount_res
                                + curIntervalDataStep.getRemainder();

                        /*
                         * As we now have added the interest amount for the balloon, we round again and subsitute the
                         * old remainder with the new:
                         */

                        curIntervalDataStep.setPayment(Utils.roundoff(annuity_unrounded, roundDirection, roundToInteger));

                        /*
                         * The remainder when rounding the periodical payment was computed either in the function
                         * IntervallengthCalc.calculate() or the function IntervallengthSeparateCalc.calculate() and is placed by
                         * curIntervalDataStep.setRemainder()
                         * 
                         * But when we computed a new periodical periodical payment, we rounded again and thus get a new
                         * remainder.
                         */

                        curIntervalDataStep.setRemainder(annuity_unrounded - curIntervalDataStep.getPayment());

                    } else {
                        curIntervalDataStep.setPayment(Utils.roundoff(curIntervalDataStep.getPayment() + interest_amount_res,
                                roundDirection, roundToInteger));
                    }

                }

                /*
                 * In order to have all data relevant for the interest rate computation in one place, we also add the
                 * periodical fee to 'interval_data':
                 */

                curIntervalDataStep.setPeriodicFee(price_storage_cop.get(step).getPeriodicalFee());

                /*
                 * Some loans might have a periodical fee that is computed as a percentage of the credit limit instead of
                 * or in addition to the fixed fee. We make the presumption that the loan is drawn fully to the credit
                 * limit at the beginning of the loan period, so that the fee is always computed from the initial
                 * principal (the initial loan plus fees):
                 */

                if (feePeriodPerc > 0) {
                    curIntervalDataStep.setPeriodicFee(curIntervalDataStep.getPeriodicFee() + principal * feePeriodPerc
                            / 100);
                }

                periods_remaining -= curIntervalDataStep.getNumberOfTerms();

                interval++;

            }

            // We loop the array 'price_storage_cop' backwards
            i--;
        }

        // STOP if no interval is detected, usually because the loan amount is too big or too small:

        if (interval == 0) {
            throw new FreeLoanException(FreeLoanExceptionType.NO_SEGMENT_FOUND);
        }

        /*
         * 
         * 4) AN EVENTUAL INTEREST-ONLY PERIOD: ASSYMETRIC MODES:
         * 
         * When there is an initial interest-only period, the symmetry brakes between period mode and payment mode. When
         * there is 'firstpayment' provided by the user (period mode), this payment is interpreted as having to cover
         * the highest payment during the whole payment period. When there is an intial interest free period, this will
         * NOT be the first payment, but one of the payments after he starts paying off the loan. In payment mode,
         * however, we return the first payment.
         * 
         */

        double unrounded;

        if (installment_grace_periods > 0) {

            /*
             * In the array 'interval_data', we now have stored all the information we need in order to
             * compute the effective interest rate, beginning in row one (x = 1). Corresponding information about an
             * interest-only period will be put in a new last row of the array.
             */

            // We get a new step in the staircase, and increment the counter.
            highest_segment++;

            
            interval_data = Utils.copyArray(interval_data, highest_segment + 1);

            /*
             * The periodic interest amount paid during the interest-only periods, 'rate_am_int_only', was only computed
             * for loans with separate, parallel interest rates:
             */

            if (rateSegments) {
                unrounded = rate_am_int_only + interest_amount_res;
            } else {
                // Otherwise, the periodic interest amount is simply the interest rate in the highest relevant segment, 'interest_segment', multiplied with the principal:
                unrounded = principal * interest_segment / rate_divisor;
            }

            // We create the new matrix row and fill it with data:

            AnnuityLoanPeriod intervalDataHighestSegment = new AnnuityLoanPeriod();

            interval_data[highest_segment] = intervalDataHighestSegment;

            intervalDataHighestSegment.setPayment(Utils.roundoff(unrounded, roundDirection, roundToInteger));

            // The number of terms/periods we pay this periodic payment:

            intervalDataHighestSegment.setNumberOfTerms(installment_grace_periods);

            // The size of the principal at the end of the interval. Here, it's the same as at the beginning, since no
            // installments are paid:

            intervalDataHighestSegment.setLowerSegmentLimit(principal_ann);

            // The interest-only period can only run from the beginning of the loan period. Then, the principal is yet
            // not paid down:

            intervalDataHighestSegment.setUpperSegmentLimit(principal_ann);

            // Remainder when rounding the periodic payment:

            intervalDataHighestSegment.setRemainder(unrounded - intervalDataHighestSegment.getPayment());

            // The periodic fee is set to the same amount as for the first ordinary annuity period:

            intervalDataHighestSegment.setPeriodicFee(priceStorage[highest_segment - 1].getPeriodicalFee());

            // There could also be a periodic fee computed as a percentage of the principal:
            if (feePercentage > 0) {
                intervalDataHighestSegment.setPeriodicFee(intervalDataHighestSegment.getPeriodicFee() + principal
                        * feePeriodPerc / 100);
            }

        }

        /*
         * 
         * 5) GLOBAL REMAINDER
         * 
         * 
         * COMPUTING THE GLOBAL REMAINDER
         * 
         * When computing an annuity, we typically get numbers as 234.938267484958. This is not payable in a bank.
         * 234.94 is, but these slightly too big annuity will make us overshoot the target payment at the end of the
         * loan period (we pay a little too much).
         * 
         * This amount is the "global residue". It is either ignored or compensated. It it is compensated, this is
         * normally done with the last payment in the loan period. Freeloan also supports it being compensated for in
         * the first payment.
         */

        int disko;
        double residue;
        double gross_last;

        // 'disko' is a divisor used when rounding
        if (roundToInteger) {
            disko = 1;
        } else {
            disko = 100;
        }

        /*
         * 'interval_data[1].getLowerSegmentLimit()' contains the remaining principal at the end of each loan interval. When the last
         * segment is paid, the remaining principal will be the global residue for the whole loan. The last segment to
         * be paid is segment 1. (Of course, normally there is only one segment).
         * 
         * We will now compute the non-rounded, last payment in the loan, included remainder and balloon. We call it
         * 'gross_last'.
         * 
         * 'gross_last' is the sum of:
         * 
         * 
         * 'interval_data[1].getPayment()' -> the rounded annuity
         * 
         * 'interval_data[1].getRemainder()' -> the remainder after rounding the annuity, stored in
         * 
         * 'interval_data[1].getLowerSegmentLimit()' -> the non-raounded remainder of the principal after all periodic paymenst are made
         * 
         * 'balloon' -> an eventuelt residual/ballon payment (stored in the variable 'balloon')
         * 
         * 'interval_data[i].getPeriodicFee()' -> the periodic fee
         * 
         * 
         * When there is more than one segment, we pay the loan "backwords" through the array 'interval_data'. The last
         * interval's values are always in row 1. Hence, we use 'interval_data[1]...' to compute the precise global
         * residue:
         */

        AnnuityLoanPeriod intervalDataFirst = interval_data[1];

        gross_last = intervalDataFirst.getPayment() + intervalDataFirst.getRemainder()
                + intervalDataFirst.getLowerSegmentLimit() + balloon + intervalDataFirst.getPeriodicFee();

        /*
         * The residue to be paid/compensated for with the last payment is the last payment - 'gross_last' - minus the
         * rounded, ordinary payment:
         */

        if (!ignoreRemainder) {
            residue = Math.round(gross_last * disko) / (double) disko
                    - (intervalDataFirst.getPayment() + intervalDataFirst.getPeriodicFee());
        } else {
            residue = Math.round(balloon * disko) / (double) disko;
        }

        /*
         * 
         * 6) EFFECTIVE INTEREST RATE:
         * 
         * As we now have an array/matrix containing all amounts due and the number of periods they should be paid, we
         * can compute the effective interest rate.
         * 
         * We apply the annuity formula:
         * 
         * 
         * 1) ANNUITY-IMMEDIATE
         * 
         * (When 'advance == true')
         * 
         * 
         * 
         * 
         * FORMULA FOR ANNUITY-IMMEDIATE:
         * annuity = loan * (1 - k) / (k - Math.pow(k,termnumber+1));
         * where
         * k = 1/(1+rate);
         * 
         * 
         * We want to compute 'rate'.
         * 
         * We use other variable names:
         * 
         * payment = principal * (1 - k) / (k - Math.pow(k,calculation_periods+1));
         * 
         * We rearrange it with respect to 'principal':
         * 
         * payment/principal = (1 - k) / (k - Math.pow(k,calculation_periods+1));
         * 
         * => principal * (1-k) = payment * (k - Math.pow(k,calculation_periods+1));
         * 
         * => principal = payment * (k - Math.pow(k,calculation_periods+1))/ (1-k);
         * 
         * We want the right hand expression to be a present value equal to 'principal'. For computation purposes, we
         * thus call the principal 'PV':
         * 
         * PV = payment * (k - Math.pow(k,calculation_periods+1))/ (1-k);
         * 
         * We formerly computed the values for the array 'interval_data'.
         * 
         * For each interval/step we shall now compute the present value of the stream of payments from the beginning to
         * the end of the interval.
         * 
         * We do that by subtracting the present value of one stream of payments from another:
         * 
         * 
         * 1) A strem of payments with the current periodical amount running from the first payment in the loan to the
         * last payment in the interval:
         * 
         * payment * (k - Math.pow(k,interval_end+1))/ (1-k)
         * 
         * 2) Minus a strem of payments with the same amount running from the first payment in the loan to the first
         * payment in the interval:
         * 
         * payment * (k - Math.pow(k,interval_start+1))/ (1-k)
         * 
         * 
         * The present value of the payment stream from the beginning to the end of the interval transpires by
         * subtracting the second expression from the first:
         * 
         * PV = payment * (k - Math.pow(k,interval_end+1))/ (1-k) - payment * (k - Math.pow(k,interval_start+1))/ (1-k)
         * 
         * We simplify:
         * 
         * => PV = (payment * k - payment * Math.pow(k,interval_end+1) - payment * k + payment *
         * Math.pow(k,interval_start+1))/ 1-k
         * 
         * The terms 'payment * k' falls against each other, and we get:
         * 
         * => (payment * Math.pow(k,interval_start+1) - payment * Math.pow(k,interval_end+1)) / 1-k
         * 
         * We can isolate 'payment / (1 - k)':
         * 
         * PV = (payment/(1-k)) * (Math.pow(k,interval_start+1) - Math.pow(k,interval_end+1));
         * 
         * 'k' (the discount factor) is the unknown entity in our expression. The other variables are known.
         * 
         * 
         * 
         * 
         * 2) ANNUITY-DUE
         * 
         * (When 'advance == false')
         * 
         * FORMULA FOR ANNUITY-DUE:
         * annuity = loan * (1 - k) / (1 - Math.pow(k,termnumber));
         * where
         * k = 1/(1+rate);
         * 
         * 
         * We reason the same way as for annuity-immediate, but use the annuity-due formula
         * 
         * PV = payment * (1 - Math.pow(k,termnumber))/(1 - k);
         * 
         * So that our present value becomes:
         * 
         * PV = payment * (1 - Math.pow(k,interval_end))/ (1-k) - payment * (1 - Math.pow(k,interval_start))/ (1-k)
         * 
         * And the algebra:
         * 
         * PV = payment/(1-k) - payment * Math.pow(k,interval_end)/(1-k) - payment/(1-k) + payment *
         * Math.pow(k,interval_start)/(1-k)
         * 
         * The first and third term falls, leaving:
         * 
         * PV = payment * Math.pow(k,interval_start)/(1-k) - payment * Math.pow(k,interval_end)/(1-k)
         * 
         * Common factor payment/(1-k) put ouside:
         * 
         * PV = payment/(1-k) * (Math.pow(k,interval_start) - Math.pow(k,interval_end))
         * 
         * 
         * 
         * 
         * **************************************************************************************************************
         * *********************************
         * 
         * 
         * 
         * NEWTON'S METHOD - DIFFERENTIATION
         * 
         * No formula gives us the effective interest rate directly. We find it by trying a likely value for 'k',
         * adjusting it until we get the interest rate with the accuracy we want. Newton's method shortenes this try and
         * fail process. It postulates that our 'k' will be found close to where the tangent to the curve in the present
         * PV/k point crosses the k-axes:
         * 
         * http://en.wikipedia.org/wiki/Newton%27s_method
         * 
         * The graph is in the PV/k space. A value of 'k' will give a value of 'PV': PV = f(k).
         * 
         * The steepness of the tangent line is given by the differentiated function with respect to 'k':
         * 
         * PV = (payment/(1-k)) * (Math.pow(k,interval_start+1) - Math.pow(k,interval_end+1));
         * 
         * For easier differentiation, we handle the two expressions on each side of the multiplication sign separately.
         * We call them 'A' and 'B';
         * 
         * PV = A*B; where
         * 
         * A = payment/(1-k);
         * 
         * B = Math.pow(k,interval_start+1) - Math.pow(k,interval_end+1);
         * 
         * One can find the differentiation rules on wikipedia: http://en.wikipedia.org/wiki/Differentiation_rules
         * 
         * PV' = A'*B + A*B'
         * 
         * We fist differietiate 'A' separately according to the Quotient rule:
         * 
         * A' = (payment' * (1-k) - payment* (1-k)') / (1-k)^2
         * 
         * The differntiated of the constant 'payment' is 0. The differentiated of the variable 'k' er 1:
         * 
         * => A' = - payment* (1-k)' / (1-k)^2
         * 
         * => A' = payment / Math.pow(1-k,2)
         * 
         * Then we differentiate 'B' according to the Power rule (y = x^n => y' = n*x^(n-1)):
         * 
         * B = Math.pow(k,interval_start+1) - Math.pow(k,interval_end+1);
         * 
         * B' = (interval_start+1)*Math.pow(k,interval_start) - (interval_end+1) * Math.pow(k,interval_end)
         * 
         * We then assemble the whole differentiated PV according to the Product rule:
         * 
         * PV' = (payment / Math.pow(1-k,2)) * (Math.pow(k,interval_start+1) - Math.pow(k,interval_end+1)) +
         * (payment/(1-k))*((interval_start+1)*Math.pow(k,interval_start) - (interval_end+1) *
         * Math.pow(k,interval_end));
         * 
         * As there is no notation for the differentiated in the programming language, we rename the differentiated
         * PV' 'PV_dif':
         * 
         * PV_dif = PV'
         * 
         * 
         * 
         * ANNUITY-DUE
         * 
         * 
         * (When 'advance == false')
         * 
         * We reason the same way:
         * 
         * PV = payment/(1-k) * (Math.pow(k,interval_start) - Math.pow(k,intervallslutt))
         * 
         * A = payment/(1-k);
         * 
         * B = Math.pow(k,interval_start) - Math.pow(k,interval_end);
         * 
         * One can find the differentiation rules on wikipedia: http://en.wikipedia.org/wiki/Differentiation_rules
         * 
         * PV' = A'*B + A*B'
         * 
         * We fist differietiate 'A' separately according to the Quotient rule:
         * 
         * A' = (payment' * (1-k) - payment* (1-k)') / (1-k)^2
         * 
         * Den dervierte av konstanten 'payment' er 0. Den deriverte av variablen 'k' er 1:
         * 
         * => A' = - payment* (1-k)' / (1-k)^2
         * 
         * => A' = - payment / Math.pow(1-k,2)
         * 
         * Then we differentiate 'B' according to the Power rule:
         * 
         * B = Math.pow(k,interval_start) - Math.pow(k,interval_end);
         * 
         * B' = interval_start * Math.pow(k,interval_start-1) - interval_end * Math.pow(k,interval_end-1)
         * 
         * We then assemble the whole differentiated PV' according to the Product rule:
         * 
         * PV' = (payment / Math.pow(1-k,2)) * (Math.pow(k,interval_start) - Math.pow(k,interval_end)) +
         * (payment/(1-k))*(interval_start*Math.pow(k,interval_start-1) - interval_end * Math.pow(k,interval_end-1));
         * 
         * As there is no notation for the differentiated in the programming language, we rename the differentiated
         * PV' 'PV_dif':
         * 
         * PV_dif = PV'
         * 
         * **************************************************************************************************************
         * *********************************
         * 
         * 
         * 
         * ITERATIONS:
         * 
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

        // First suggestion: The array 'priceStorage' is a parameter to the function
        double k = 1 / (1 + priceStorage[1].getAnnualInterest() / rate_divisor);

        // Counts the number of iterations
        int rounds = 0;

        // The present value
        double PV = 0;

        // the differiented of the present value
        double PV_dif = 0;

        // The peridoical payment in the annuity loan, included fees
        double payment;

        // 'y' is the function value that we want to make close to '0'. Givaen av value so we can enter the
        // 'while'-loop.
        double y = 1;

        // The number of payments from the start of the loan period until the interval starts
        double interval_start;

        // The number of payments from the start of the loan period until the interval ends
        double interval_end;

        // Annuities paid at the beginning of each period - annuity-due
        if (annuityDue)

        {

            // Here, we set the accuracy we want.
            while (Math.abs(y) > 0.000001 && rounds < 100) {

                // The present value of the payments
                PV = 0;

                // The differentiated of the present value
                PV_dif = 0;

                interval_start = 0;
                interval_end = 0;

                /*
                 * Now, we compute the present value 'PV' and the corresponding differetiated 'PV_dif' for each interval
                 * of the loan, given the current guess for the discount factor 'k.
                 * 
                 * In the case where the loan runse with different interest rates in succeeding intervals, we sum the
                 * present value for each. We also sum their differentiated values.
                 */

                for (i = highest_segment; i > 0; i--) {

                    /*
                     * In order to compute the effective interest rate, we must compare all we pay with all we receive.
                     * Hence, we must include eventual fees in the periodic payments. The fee is in
                     * 'interval_data[i].getPeriodicFee()':
                     */

                    AnnuityLoanPeriod curIntervalData = interval_data[i];

                    payment = curIntervalData.getPayment() + curIntervalData.getPeriodicFee();

                    // The end of the former interval is the start of this
                    interval_start = interval_end;

                    // The upper limit of this interval
                    interval_end += curIntervalData.getNumberOfTerms();

                    PV += payment / (1 - k) * (Math.pow(k, interval_start) - Math.pow(k, interval_end));

                    /*
                     * The sum of the differentiated of two functions is the sum of the difrentiated. Hence, we simply
                     * sum the differentiated in each interval
                     */

                    PV_dif += (payment / Math.pow(1 - k, 2))
                            * (Math.pow(k, interval_start) - Math.pow(k, interval_end))
                            + (payment / (1 - k))
                            * (interval_start * Math.pow(k, interval_start - 1) - interval_end
                                    * Math.pow(k, interval_end - 1));

                }

                /*
                 * One payment - the residue - is so far missing. We add the present value of the residue to the present
                 * value of the other payments. If it is paid in the last period, it must be discounted to find the
                 * present value. The residue is already rounded. In annuity-due (annuities in advance) the last payment
                 * is in period 'calculation_periods-1':
                 */

                PV += residue * Math.pow(k, calculation_periods - 1);

                /*
                 * We also want to add the differentiated of the residue to the differiented of the other payments.
                 * 
                 * The residue is a function of 'k', and we use the Power rule
                 * (http://en.wikipedia.org/wiki/Power_rule):
                 * 
                 * y = x^n => y' = n*x^(n-1):
                 * 
                 * PV_res = residue*Math.pow(k,calculation_periods-1) => PV_res_dif =
                 * residue*(calculation_periods-1)*Math.pow(k, calculation_periods-2);
                 */

                PV_dif += residue * (calculation_periods - 1) * Math.pow(k, calculation_periods - 2);

                // Searching for a 'k' making y = 0. Since 'received' is a constant y' = PV'
                y = PV - received;

                // The increase in 'k' necessary at the tangent's intersection with the PV-axis
                double delta = -y / PV_dif;

                // We increase/decrease 'k' (the annuity function crashes at k=1)
                if (k + delta != 1) {
                    k += delta;
                }

                rounds++;

            }

         // Annuities paid at the end of each period - annuity-immediate - "normal" annuities.
        } else {
            // Here, we set the accuracy we are looking at.
            while (Math.abs(y) > 0.000001 && rounds < 100) {
                // The present value of the payments
                PV = 0;

                // The differentiated of the present value
                PV_dif = 0;

                interval_start = 0;
                interval_end = 0;

                /*
                 * Now, we compute the present value 'PV' and the corresponding differetiated 'PV_dif' for each interval
                 * of the loan, given the current guess for the discount factor 'k.
                 * 
                 * In the case where the loan runse with different interest rates in succeeding intervals, we sum the
                 * present value for each. We also sum their differentiated values.
                 */

                for (i = highest_segment; i > 0; i--) {

                    AnnuityLoanPeriod curIntervalData = interval_data[i];

                    /*
                     * In order to compute the effective interes rate, we must compare all we pay with all we receive.
                     * Hence, we must include eventual fees in the periodic payments. The fee is in
                     * 'interval_data[i].getPeriodicFee()':
                     */

                    payment = curIntervalData.getPayment() + curIntervalData.getPeriodicFee();

                    // The end of the former interval is the start og this
                    interval_start = interval_end;

                    // The upper limit of this interval
                    interval_end += curIntervalData.getNumberOfTerms();

                    PV += payment / (1 - k) * (Math.pow(k, interval_start + 1) - Math.pow(k, interval_end + 1));

                    /*
                     * The sum of the differentiated of two functions is the sum of the difrentiated. Hence, we simply
                     * sum the differentiated in each interval
                     */

                    PV_dif += (payment / Math.pow(1 - k, 2))
                            * (Math.pow(k, interval_start + 1) - Math.pow(k, interval_end + 1))
                            + (payment / (1 - k))
                            * ((interval_start + 1) * Math.pow(k, interval_start) - (interval_end + 1)
                                    * Math.pow(k, interval_end));

                }

                /*
                 * One payment - the residue - is so far missing. We add the present value of the residue to the present
                 * value of the other paymen. If it is paid in the last period, it must be discounted to find the
                 * present value.
                 * 
                 * The residue was rounded when we computed it earlier:
                 */
                PV += residue * Math.pow(k, calculation_periods);

                /*
                 * We also want to add the differentiated of the residue to the differiented of the other payments.
                 * 
                 * The residue is a function of 'k', and we use the Power rule
                 * (http://en.wikipedia.org/wiki/Power_rule):
                 * 
                 * y = x^n => y' = n*x^(n-1):
                 * 
                 * PV_res = residue*Math.pow(k,calculation_periods) => PV_res_dif =
                 * residue*calculation_periods*Math.pow(k, calculation_periods-1);
                 */
                PV_dif += residue * calculation_periods * Math.pow(k, calculation_periods - 1);

                // Searching for a 'k' making y = 0. Since 'received' is a constant y' = PV'
                y = PV - received;

                // The increase in 'k' necessary at the tangent's intersection with the PV-axis
                double delta = -y / PV_dif;

                // We increase/decrease 'k' (the annuity function crashes at k=1)
                if (k + delta != 1) {
                    k += delta;
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

        // 'er' is the effective annual interest rate - the principal result of the function
        double er = (Math.pow(1 / k, capitalizationFreq) - 1) * 100;
        
        
        // This should not happen, but it does in the JS version if input data is wierd. So we
        // throw an exception to avoid problems later on.
        if(Double.isNaN(er)) {
            throw new FreeLoanException(FreeLoanExceptionType.EFFECTIVE_RATE_WAS_NAN);
        }
        

        
        /*
         * 
         * 7) RESULT REPORTING
         * 
         * 
         * The function primarily computes the effective interest rate. But as a bi-product, several other avalues were
         * computed along the way. These could come handy during the result presentation - for instance the length of
         * the intervals, the fees, the balloon.
         */
        AnnuityLoanResult res = new AnnuityLoanResult();
        res.setEffectiveInterestRate(er);
        res.setResidue(residue);
        res.setRounds(rounds);
        res.setPeriods(interval_data);
        
        /* Normally, we pay more the last periods when there is an inital interest only-period. But when the borrower has specified that he is able
        to pay only a maximum sum - 'firstpayment' - per month, we have to prolong the payment period instead: */
        
        
        // contains the number of payback periods.
        double paybackPeriodCount = 0; 
        
        if (rateThresholds && firstPayment != null && firstPayment > 0) {
            
            // Where there are several intervals, we sum them up
            for (i=1;i<interval_data.length;i++) {
                paybackPeriodCount += interval_data[i].getNumberOfTerms();
            }
            
            paybackPeriodCount += installment_grace_periods;
            
        // The payback period in number of payments
        } else {
            paybackPeriodCount = interval_data[1].getNumberOfTerms() + installment_grace_periods;  
        }

        res.setPaybackPeriodCount(paybackPeriodCount);
        
        
        return res;

    }

    
    
    /**
     * <p>
     * <b>WHAT IT DOES:</b>
     * </p>
     * 
     * <p>
     * The function returns the number of periods it will take to pay down 'loan' to zero given the periodic payment
     * 'annuity' and the periodic interest rate 'rate'. Either when annuities are paid in advance ('due' = true) or by
     * ordinary annuities, paid in arrears.
     * </p>
     * 
     * 
     * <p>
     * <b>DERIVATION:</b>
     * </p>
     * 
     * <p>
     * <b>1) Annuity immediate:</b>
     * </p>
     * 
     * <p>
     * For ordinary annuities (annuities in arrears / annuity immediate) the formula is
     * </p>
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
     * 'rate' being the periodical interest rate in decimal form.
     * </p>
     * 
     * <p>
     * (k - Math.pow(k,termnumber+1)) * annuity = loan * (1 - k)
     * </p>
     * 
     * <p>
     * k - Math.pow(k,termnumber+1) = loan * (1 - k) / annuity
     * </p>
     * 
     * <p>
     * Math.pow(k,termnumber+1) = k - (loan * (1 - k) / annuity)
     * </p>
     * 
     * <p>
     * C = k - (loan * (1 - k) / annuity)
     * </p>
     * 
     * <p>
     * Math.pow(k,termnumber+1) = C
     * </p>
     * 
     * <p>
     * The logarithm rule for exponential functions: log a^x = x * log a
     * </p>
     * 
     * <p>
     * log k *(termnumber+1) = log C
     * </p>
     * 
     * <p>
     * termnumber+1 = log C / log k
     * </p>
     * 
     * <p>
     * termnumber = log C / log k - 1
     * </p>
     * 
     * 
     * <p>
     * <b>2) Annuity due:</b>
     * </p>
     * 
     * <p>
     * For annuities in advance / annuity due, the formula is:
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
     * 
     * <p>
     * annuity = loan * (1 - k) / (1 - Math.pow(k,termnumber));
     * </p>
     * 
     * <p>
     * where k = 1 / (1 + r), 'r' being the periodical interest rate in decimal form.
     * </p>
     * 
     * <p>
     * (1 - Math.pow(k,termnumber)) * annuity = loan * (1 - k)
     * </p>
     * 
     * <p>
     * 1 - Math.pow(k,termnumber) = loan * (1 - k) / annuity
     * </p>
     * 
     * <p>
     * Math.pow(k,termnumber) = 1 - (loan * (1 - k) / annuity)
     * </p>
     * 
     * <p>
     * C = 1 - (loan * (1 - k) / annuity)
     * </p>
     * 
     * <p>
     * Math.pow(k,termnumber) = C
     * </p>
     * 
     * <p>
     * The logarithm rule for exponential functions: log a^x = x * log a
     * </p>
     * 
     * <p>
     * log k * termnumber = log C
     * </p>
     * 
     * <p>
     * termnumber = log C / log k
     * </p>
     * 
     * <p>
     * DECIMALS: Per definition, period numbers must be integers. This rounding must happen outside this function, as it
     * returns a decimal number.
     * </p>
     * 
     * @param loan
     *            The size of the gross loan, included orgination fees. annuity -> The periodic (often monthly) payment
     * @param annuity
     *            Periodic payment
     * @param rate
     *            Nominal, periodic interest rate due -> TRUE if the annuities are paid in advance
     * @param due
     *            'due' = true -> annuities are paid in advance, 'due' = false -> ordinary annuities, paid in arrears.
     * 
     * @return The number of periods it will take to pay down 'loan' to zero
     */
    private double termnumAnnu(double loan, double annuity, double rate, boolean due) {
    
        double k = 1 / (1 + rate);
    
        if (due) {
            double C = 1 - (loan * (1 - k) / annuity);
            return Math.log(C) / Math.log(k);
        } else {
            double C = k - (loan * (1 - k) / annuity);
            return Math.log(C) / Math.log(k) - 1;
        }

    }
    




    /**
     * <p>
     * <b>WHAT THE FUNCTION DOES:</b>
     * </p>
     * 
     * 
     * <p>
     * The function computes effective interest rate for a serial loan. As a side product, other values that could come
     * useful are computed too.
     * </p>
     * 
     * 
     * <p>
     * The function handles three different types of loan segments:
     * </p>
     * 
     * <ol>
     * <li>The interes rate for the whole loan is determined by the initial size of the principal when the loan is
     * given. The rate does not change during the loan period. This is the most common and the simplest model.</li>
     * 
     * <li>The interest rate for the whole loan changes when the principal passes certain thresholds (the parameter
     * 'rateThresholds' == true)</li>
     * 
     * <li>Separate segments of the loan have different interest rates at the same time. All installments are deducted
     * from the uppermost segment of the loan. This segment, thus, is payed dowm first. For instance, ther rate for the
     * segment between 0 and 500,000 could be 4%, between 500,000 and 1,000,000 i could be 3.5% and above 1,000,000 it
     * could be 3%. All applying at the same time. (The parameter 'rateSegments' == true)</li>
     * </ol>
     * 
     * 
     * <p>
     * <b>SECTIONS:</b>
     * </p>
     * 
     * <p>
     * The function consists of seven separate, numbered sections:
     * </p>
     * 
     * <ol>
     * <li>Preparation and adaption of data</li>
     * <li>Computation of all period payments and the remainder</li>
     * <li>Computation or the effective interest rate</li>
     * <li>Result reporting</li>
     * </ol>
     * 
     * 
     * <p>
     * <b>THE PLAN</b>
     * </p>
     * 
     * <p>
     * The interest rate is computed by iterations. Before performing the iterations, we need to know the size of every
     * payment and the time on when it is paid. When we have a loan that changes interest rate during the payment
     * period, we could have subsequent intervals with different rates. The function also supports parallell interest
     * rates.
     * </p>
     * 
     * <p>
     * Hence, all the payments, rounded and placed in their respective period must be computed and stored in the array
     * 'interval_data'.
     * </p>
     * 
     * <p>
     * Generally, we use the object properties to compute the values in 'interval_data' before we use the values in
     * 'interval_data' to compute the result(s) that will be returned.
     * </p>
     * 
     * <p>
     * <b>ASSUMPTION FOR "PERIOD MODE"</b>
     * </p>
     * 
     * <p>
     * In "period mode", when the first payment is known and we compute the number of periods, we assume that the first
     * payment is the greatest of all payments during the loan period. This would be true in all normal cases, but wrong
     * if the interest rate increases dramatically after a while.
     * </p>
     */
    public SerialLoanResult serialLoan() throws FreeLoanException {

        
        preprocess();
        
        
        /*
         * 1) PREPARATION AND ADAPTION OF DATA:
         * 
         * 
         * DATA IN A FORMAT SUITABLE FOR OUR FORMULAS:
         * 
         * It is mathematically correct to take all fees into consideration when computing effective interest rate. But
         * the function also supports the ommittance of the start/orgination fees:
         */

        double principal;

        if (ignoreOrigination) {
            principal = received;
        } else {

            // What the user receives plus fees
            principal = (received + feeDocument) * (100 + feePercentage) / 100;

            principal += feeProcessing;

        }

        // Error if the balloon is greater than the principal:
        if (balloon > principal) {
            throw new FreeLoanException(FreeLoanExceptionType.BALLOON_TOO_SMALL);
        }
        
        
        /*
         * Total number of calculation periods are the total number of periods, 'numberOfPeriods' divided by
         * 'periodsPerYear' to find the number of years. We then multiply this by the capitalization frequency,
         * 'capitalizationFreq' to find the number of calculation periods.
         */
        
        /*
         * 'rate_divisor' is the divisor the nominal, annual interest rate must be divided by to obtain the periodic
         * interest rate. For instance, a nominal interest rate of 6% equals a nominal monthly decimal rate of 0,005 =>
         * 6/1200.
         */

        int rate_divisor = 100 * capitalizationFreq;
        
        
        /*
         * What is the upper segment n 'priceStorage'? The function '.length' returns the length inclusive element zero,
         * that is not in use. The last element, thus, is 'element.length-1':
         */
        int num_segm = priceStorage.length - 1;
        
        
        
        /* Identifying the lowest segment that contains the upper loan amount limit: */

        boolean found = false;

        while (!found) {
            if (principal > priceStorage[num_segm].getLowerLimit() || num_segm == 1) {
                found = true;
            } else {
                num_segm--;
            }
        }

        double termnumber;

        if (firstPayment != null && firstPayment != 0) {

            /*
             * Computing the number of periods of a serial loan is simple. It is the net loan dividied by the net
             * installment. The net installment it the net first payment - 'netpayment' - minus the interest part.
             * 
             * First, we must deduct fees from the payment, as these are not going towards paying off the loan:
             */

            double netpayment = (firstPayment * 12 / capitalizationFreq) - priceStorage[num_segm].getPeriodicalFee() - principal
                    * feePeriodPerc / 100;

            /*
             * Then, we deduct the first interest payment. ('price_storage[1][4]' contains the first annual, nominal
             * rate).
             * 
             * If there is an initial interest-only period, the number of terms are either undetermed, or 'fistpayment'
             * must be assumed
             * 
             * to mean the fist payment of the period after the interest-only period, when we are starting to pay off
             * the loan.
             */

            double netinstall = netpayment - priceStorage[num_segm].getAnnualInterest() / rate_divisor * principal;

            // If the net payment does not cover the interest rate, the loan cannot be payed back:

            if (netinstall < 0) {
                throw new FreeLoanException(FreeLoanExceptionType.PAYMENT_TOO_SMALL);
            }

            // The number of periods are simply the principal divided on the periodical installment:
            // Not sure if normal rounding is correct here, but..
            termnumber = Math.ceil((principal - balloon) / netinstall); 

        } else {
            termnumber = numberOfPeriods;
        }

        double calculation_periods = Math.ceil(termnumber / periodsPerYear * capitalizationFreq);

        // The number of intial interest-only periods (where no installments are paid):

        double installment_grace_periods = interestonlyPeriods / (double) periodsPerYear * capitalizationFreq;

        // The number of "normal" periodical payments that include installment:

        double installment_periods = calculation_periods - installment_grace_periods;



        /*
         * 
         * 
         * 2) COMPUTATION OF ALL PERIOD PAYMENTS AND THE REMAINDER
         * 
         * 
         * IN THIS SECTION:
         * 
         * In this section, we compute the periodic payments. In a serial loan all payments are different. If the
         * amounts were not rounded, we could have used the formula for serial loans. But in the real world, we cannot
         * pay amounts with many decimal fractions, only whole currenty units and cents. Hence, we must compute on the
         * basis of the rounded sums that are acutally paid. This requires transversing the loan period by period,
         * computing all payments separately.
         * 
         * Each payment normally consists of three parts:
         * 
         * o The interest part
         * o The installment part
         * o The fee part
         * 
         * In a serial loan, the installment is the same for all payments: The principal divided on the number of
         * payment periods.
         * 
         * We store the values in the intermediate array 'per_pay' (for "periodical payment"). Further down, 'per_pay'
         * is used for the computation of effective interest rate.
         * 
         * The content of 'per_pay' for period 'i' is:
         * 
         * pay_per[i][0] -> The interest amount for the part of the loan for which installments are NOT paid in this
         * period
         * 
         * pay_per[i][1] -> The interest amount for the part of the loan for which installments are paid in this
         * period
         * 
         * pay_per[i][2] -> The whole payment for this period, included installment and fees.
         * 
         * 
         * Some of the computed data will be stored in sub-res, whick is part of result reporting:
         * 
         * 
         * FOR RESULTREPORTING
         * 
         * When we must compute all payments anyway, we might as well return them in the function result, so that they
         * could be used for instance for a graph.
         * 
         * We want to return the whole period payment and specify the installment part and the fee part
         * 
         * 
         * Example: If the loan has 12 annual payments and runs for 20 years, there will be 240 different rows in 'pay_per'.
         */

        // The part of the payment that makes the principal smaller. In serial loan, the same for all periods:

        double installment;

        // 'installment_periods' = 0 means only interest

        if (installment_periods != 0) {
            installment = (principal - balloon) / installment_periods;
        } else {
            installment = 0;
        }

        // Remaining principal after each installment. Before we start subtracting installments, the whole intial
        // principal remains:

        double rem_princ = principal;

        int pres = 0;

        // Divisor indicating the requested presicion when rounding..
        if (!roundToInteger) {
            pres = 100;
        } else {
            pres = 1;
        }

        /*
         * COMPUTING IN ADVANCE NUMBERS THAT ARE USED OFTEN: If the loan is concurrently running with different interes
         * rates in different segments, all installments are still subtracted only from the uppermost, remaining
         * segment. Hence, during the periods it takes to pay down the segment, the interest and fees in the other
         * segments are the same for all periods in the interval. In order to avoid computing this in each period, we
         * compute the amounts in advance and put them in the array 'segm_int_am':
         * 
         * The interest amount for the part of the loan for which installments are NOT paid in this period:
         */

        // The size of the part of the principal that is in this segment
        double segm_size;

        // "Segment's interest amount"
        double[] segm_int_am = new double[num_segm];

        for (int i = 1; i < num_segm; i++) {

            PriceStorageStep curStep = priceStorage[i];

            /*
             * If the upper limit in
             * the segment is smaller than the principal, or Double.MAX_VALUE (which means 'unlimted' in FreeLoan):
             */

            if (curStep.getUpperLimit() <= principal || curStep.getUpperLimit() == Double.MAX_VALUE || i == 1) {

                /*
                 * If different interest rates are applied concurrently in different segments, the interest rate amount
                 * is computed with the separate segment rate:
                 */

                if (rateSegments) {

                    double segment;

                    // "Unlimited" as the upper limit means the segment size equals the principal minus the segment's
                    // lower limit:

                    if (curStep.getUpperLimit() == Double.MAX_VALUE) {
                        segment = principal - curStep.getLowerLimit();
                    } else {
                        segment = curStep.getUpperLimit() - curStep.getLowerLimit();
                    }

                    // The interest amount of the segment
                    segm_int_am[i] = segment * curStep.getAnnualInterest() / rate_divisor;

                    // Added to the previously estimated amount of interest in higher segments
                    if (i > 1) {
                        segm_int_am[i] += segm_int_am[i - 1];
                    }

                } else {

                    /*
                     * The same interest rate is applied for the whole remaining principal. This rate is the highest
                     * relevant segment's. As the whole loan is encompassed, we don't need to add up the segments.
                     */

                    // "Ulimited' means the principal
                    if (curStep.getUpperLimit() == Double.MAX_VALUE) {
                        segm_size = principal;
                    } else {
                        segm_size = curStep.getUpperLimit();
                    }

                    // The segmentrate above, for the part of the loan currently serviced, is applied to the rest of the
                    // loan:

                    if (i < num_segm) {
                        segm_int_am[i] = segm_size * priceStorage[1 + i].getAnnualInterest() / rate_divisor;
                    } else {
                        segm_int_am[i] = segm_size * curStep.getAnnualInterest() / rate_divisor;
                    }

                }

            }
        }

        // Two dimentional array where we store the periodical paymenst. See definition above.
        double[][] pay_per = new double[(int) Math.round(calculation_periods) + 1][3];

        // Two dimentional array for certain parts of the result. See definition above.
        SerialLoanPeriod[] sub_res = new SerialLoanPeriod[(int) Math.round(calculation_periods) + 1];

        // What currently remains of the principal in the segment
        double rem_segm = 0;

        // The start segment
        int now_segm = num_segm;

        // Periodic fee for the current period
        double fee_per;

        // Payment for the current period
        double paym_per = 0;

        // Interest payment for the current period
        double int_per;

        // Installment for the current period
        double inst_per;

        // When the payment is rounded, the remainder is deducted from /added to the principal.
        double adj_inst;

        // The first period of the loan
        int first;

        // If interest should be paid in advance, we start at period zero, otherwise at period one:
        if (annuityDue) {
            first = 0;
        } else {
            first = 1;
        }

        // We traverse the whole loan, period by period:
        for (int i = first; i <= calculation_periods; i++) {

            // 1. In what segment are we? We started at the top, and change for each principal limit we pass.
            if (now_segm > 1 && rem_princ < priceStorage[now_segm].getLowerLimit()) {
                now_segm--;
            }

            // 2. Remaining principal in the segment (the part that is payed down in payment 'i'):
            // Remaining principal in this segment
            if (now_segm == 1 || !rateThresholds) {
                rem_segm = rem_princ;
            } else {
                rem_segm = rem_princ - priceStorage[now_segm - 1].getUpperLimit();
            }

            // 3. The interest amount for this payment in this segment of the loan:

            double termren;

            if (!rateThresholds) {

                /*
                 * When the interest rate cannot change during the loan, it is determined by the rate when you took up
                 * the loan. We find this rate in 'priceStorage[num_segm].getAnnualInterest()'. (The array 'priceStorage' is a parameter
                 * to the function. See above.).
                 */

                termren = priceStorage[num_segm].getAnnualInterest() / rate_divisor;
                pay_per[i][0] = 0;

            } else {

                // WHen the interst rate can change during the loan, it is determined by the number of the segment we're
                // in - 'now_segm':
                termren = priceStorage[now_segm].getAnnualInterest() / rate_divisor;

                // 4. The interest amount for the part of the loan for which installments are NOT paid in this period:
                if (now_segm > 1) {
                    pay_per[i][0] = segm_int_am[now_segm - 1];
                } else {
                    pay_per[i][0] = 0;
                }

            }

            // The interest amount for the part of the loan for which installments ARE paid in this period
            pay_per[i][1] = rem_segm * termren;

            /*
             * The total interest amount for this period. In a loan with interest paid in advance, there is no interest
             * payment in the very last payment:
             */

            if (annuityDue && i == calculation_periods) {
                int_per = 0;
            } else {
                int_per = pay_per[i][0] + pay_per[i][1];
            }

            /*
             * 5. Then we are ready to compute the periodical payment, including the period's installment:
             * 
             * Firstly, period zero exists only in interest-in-advance loans. The way we have defined it, there is not
             * installment (payoff of the capital) in period zero:
             */

            if (i == 0) {
                inst_per = 0;
            } else {
                inst_per = installment;
            }

            // Secondly, if we are not in an interest-only period..

            if (i > installment_grace_periods) {
                paym_per = int_per + inst_per;
            } else {
                paym_per = int_per;
            }

            // 6. We add the periodical fee (the segment's fee is used):

            fee_per = priceStorage[now_segm].getPeriodicalFee();

            /*
             * For certain loans, there is a fee as a percentage of the borrowing limit. Here, we interpret this as a
             * percentage of the intial principal:
             */

            fee_per += feePeriodPerc * principal;

            paym_per += fee_per;

            /*
             * 7. Rounding
             * 
             * 
             * The periodic payment is rounded according to the rules given as parameters to the object:
             * 'roundDirection' and 'roundPresision':
             */

            // Normal rounding
            if (roundDirection == Utils.RoundDirection.NORMAL) {
                pay_per[i][2] = Math.round(paym_per * pres) / (double) pres;
            } else if (roundDirection == Utils.RoundDirection.UP) {
                pay_per[i][2] = Math.ceil(paym_per * pres) / pres;
            } else if (roundDirection == Utils.RoundDirection.DOWN) {
                pay_per[i][2] = Math.floor(paym_per * pres) / pres;
            }

            /* The remainder of the periodic payment after rounding is added to / subtracted from the principal: */

            // Adjusted installment after rounding.
            adj_inst = pay_per[i][2] - int_per - fee_per;

            /*
             * 8. The remaining principal.
             * 
             * We subtract the installment part of the payment from the remaining principal:
             */

            rem_princ -= adj_inst;

            SerialLoanPeriod curSubRes = new SerialLoanPeriod();

            // For result reporting:
            sub_res[i] = curSubRes;

            // The whole rounded payment for period 'i', included installment and fees.
            curSubRes.setPayment(pay_per[i][2]);

            // The installment for peroid 'i'
            curSubRes.setInstallment(adj_inst);

            // The fee for period 'i'
            curSubRes.setPeriodicFee(fee_per);

        }

        /*
         * When all periods are paid, the remaining principal contains the compounded rounding errors and an eventual
         * residual/balloon payment. For rounding purposes, We add the last ordinary payment and the ramaining
         * principal, round them and subtract the ordinary payment again to find the remaining principal:
         */

        double remainder;

        if (!ignoreRemainder) {
            remainder = Math.round((rem_princ + paym_per) * pres) / (double) pres
                    - pay_per[(int) Math.round(calculation_periods)][2];
        } else {
            remainder = Math.round((balloon + paym_per) * pres) / (double) pres
                    - pay_per[(int) Math.round(calculation_periods)][2];
        }

        /*
         * 3) COMPUTATION OF EFFECTIVE INTEREST RATE
         * 
         * 
         * The effective interest rate does not transpire directly through a formula. I must be found through repeated
         * guesses, where you try to move your guess closer to the answer for each iteration.
         * 
         * The best algorithm is Newton's method:
         * 
         * Newton's method is well described in Wikipedia: http://en.wikipedia.org/wiki/Newton%27s_method
         * 
         * Where 'PV' is the present value and 'k' a growth/discount factor, Newton's method regards the problem as
         * finding the point where the function graph in the PV/k space crosses the 'k'-line. At the point for our
         * guess, the tangent to the graph points in approximately the right direction, says Newton.
         * 
         * But to find the gradient of the tangent, we must differentiate the function. Because we have rounded
         * payments, we can't use the function for serial loans. Hence, we don't have a function to differentiate. So we
         * do the second best, we use the s e c a n t.
         * 
         * We select at point close to 'k' as the second point where the secant line intersects the graph. If these two
         * points are very close, the secant will have approximately the same gradient as the tangent.
         * 
         * Testing indicates that the number of iterations are about the same as when applying Newton's method.
         * 
         * 4-5 guesses are normally sufficient to achieve an accuracy of ten decimal fractions.
         * 
         * 
         * As our first guess for the growth factor 'k', we will use the nominal interest rate in the lowest segment,
         * segment 1. To convert to decimal rate, we divide by 100. We add 1 to obtain a growth factor, and compute the
         * n'th root of this factor, n being the number of capitalisations annually:
         */

        double s = Math.pow(priceStorage[1].getAnnualInterest() / 100 + 1, 1 / (double) capitalizationFreq) - 1;

        // From the rate 's', we compute our first guess for the discount/growth factor 'k', which is the unknown in the
        // subsequent iterations:

        double k;

        // Initiall guess: Nominal rate = effective rate
        if (s != -1) {
            k = 1 / (1 + s);
        } else {
            throw new FreeLoanException(FreeLoanExceptionType.INTEREST_PERIOD_TOO_LONG);
        }

        // The other point where the secant crosses the graph, 'g', that is one millionth smaller than 'k'
        double g = k - k / 1000000;

        // The present value of the periodic payments, discounted with the best guess 'k'
        double NV_round;

        // The present value of the periodic payments, discounted with the marginally smaller 'g'
        double NV_round_alt;

        // The present value of the remainder, discounted with the best guess 'k'
        double NV_rem;

        // The present value of the remainder, discounted with the marginally smaller 'g'
        double NV_rem_alt;

        // 'y' is the present value of all payments minus the initial principal
        double y;

        // Function value close to 'y', constitutes one of the coordinates for the alternative point
        double z;

        // The gradient for the secant throught the two points y/k og z/g.
        double grad;

        // The increase (increment) we give 'k' in order to get closer to result
        double inc;

        // Counts the iterations
        int rounds = 0;

        /*
         * For each iteration, we compare the present value of all our expenses with the value of what we receive. By
         * running more iterations, we can get this differenve - y - as small as we want. But accuract impairs speed,
         * and the administator can choose the accuracy. From this choice, we set the maximal value of 'y' here called
         * 'comp':
         */
        double comp;

        if (accuracy == Utils.Accuracy.FAST) {
            comp = Math.round(principal / 5000);
        } else if (accuracy == Utils.Accuracy.NORMAL) {
            comp = principal / 50000000;
        } else {
            comp = principal / 50000000000000l;
        }

        // In order for the iterations to start, 'y' must have a value greater than 'comp'
        y = comp + 1;

        // 4-5 iterations are normally enough - we limit it at 100.
        while (Math.abs(y) > comp && rounds < 100) {

            // Present values are computed again in each iteration:
            NV_round = 0;
            NV_round_alt = 0;
            NV_rem = 0;
            NV_rem_alt = 0;

            // We traverse the array with all the periodic payments and compute the present value:
            for (int i = first; i <= calculation_periods; i++) {

                // The present value of the rounded periodic payment with discount factor 'k', our principal guess:

                NV_round += pay_per[i][2] * Math.pow(k, i);

                // The present value of the rounded periodic payment with alternative discount factor 'g':

                NV_round_alt += pay_per[i][2] * Math.pow(g, i);

            }

            // Computing the present value of the remainder:

            NV_rem = remainder * Math.pow(k, calculation_periods);

            // Computing the present value of the remainder with alternative discount factor:

            NV_rem_alt = remainder * Math.pow(g, calculation_periods);

            /*
             * We try to make 'y' - the differenve between the present value of all payments minus the initial principal
             * as close to zero as possible:
             */

            y = NV_round + NV_rem - received;

            // A value close to 'y', being one of the coordinates for the second slice point of the secant:

            z = NV_round_alt + NV_rem_alt - received;

            // The gradient for a line (the secant) running through the two function points
            grad = (z - y) / (g - k);

            // Increment based on the secant's gradient
            inc = -y / grad;

            // We increase 'k' by the increment, hoping to make 'y' closer to zero
            k += inc;

            // We also update the value of 'g', used to make the other intersection point
            g = k - k / 100000000000l;

            rounds++;

        }

        /*
         * 4) RESULT REPORTING
         * 
         * e = effective periodical interest rate er = effective annual interest rate
         * 
         * k = 1/ 1 + e k (1+e) = 1 1/k = 1 + e e = 1/k - 1
         * 
         * er = (((1/k)^capitalization_freqr)-1)*100
         * 
         * In javascript notation:
         * 
         * er = (Math.pow(1/k,capitalizationFreq)-1)*100;
         * 
         * The principal function result is the effective annural interest rate. But as a bi-produkt, several other
         * enteties are computed too. They could come handy in a result presentation.
         * 
         */

        // Effective annual interest rate in percent
        double er = (Math.pow(1 / k, capitalizationFreq) - 1) * 100;

        SerialLoanResult res = new SerialLoanResult();

        res.setEffectiveInterestRate(er);


        res.setRemainder(remainder);
        res.setRounds(rounds);

        res.setPeriods(sub_res);
        res.setPaybackPeriodCount(termnumber);

        return res;

    }


    
    /**
     * @param received <b>(OBLIGATORY)</b> The loan amount received by the borrower.
     * (Due to fees, this might deviate from the principal in the
     * bank's books).
     */
    public void setReceived(Double received) {
        this.received = received;
    }
    
    /**
     * <p>
     * <b>ENABLES PAYMENT MODE</b>
     * </p>
     * 
     * @param numberOfPeriods The loan time. This calculator only computes loans that are given over whole years.
     * 
     * <p>
     * Either this loan time or 'firstPayment" is obligatory. With 'numberOfPeriods' set, the periodical payments (annuities) are computed.
     * </p>
     */
    public void setNumberOfPeriods(Integer numberOfPeriods) {
        this.numberOfPeriods = numberOfPeriods;
    }
    
    
    /**
     * 
     * <p>
     * <b>ENABLES PERIOD MODE</b>
     * </p>
     * 
     * @param firstPayment Amount of the desired first payment
     * 
     * <p>
     * Either this first payment or 'numberOfPeriods' is obligatory. With 'firstPayment' set, the number of periods are computed
     * </p>
     */
    public void setFirstPayment(Double firstPayment) {
        this.firstPayment = firstPayment;
    }


    /**
     * @param periods_per_year <b>(OBLIGATORY)</b> In order to compute annual, effective interest rate,
     * some connection to years must be made.
     */
    public void setPeriodsPerYear(Integer periods_per_year) {
        this.periodsPerYear = periods_per_year;
    }

    /**
     * @param balloon The planned residual value of the loan ("balloon") to be paid when the loan period is over.
     * 
     * <p>Default: 0</p>
     */
    public void setBalloon(double balloon) {
        this.balloon = balloon;
    }

    /**
     * @param interestonly_periods Integer {@literal >} 0: The initial interest-only period wanted by the borrower
     * 
     * <p>Default: 0</p>
     */
    public void setInterestonlyPeriods(int interestonly_periods) {
        this.interestonlyPeriods = interestonly_periods;
    }

    /**
     * @param round_direction <b>NORMAL:</b> Annuities are rounded after normal rules <b>UP:</b> Rounded up
     * <b>DOWN:</b> Rounded down
     * 
     * <p>Default: NORMAL</p>
     */
    public void setRoundDirection(Utils.RoundDirection round_direction) {
        this.roundDirection = round_direction;
    }

    /**
     * @param roundToInteger <b>false:</b> Payment rounded to nearest 1/100 <b>true:</b> ..rounded to nearest integer
     * 
     * <p>Default: false</p>
     */
    public void setRoundToInteger(boolean roundToInteger) {
        this.roundToInteger = roundToInteger;
    }

    /**
     * @param remainder_handling <b>false:</b> The "global" remainder at the end of the loan period is payed / compensated with the last payment
     * <b>true:</b> ..is ignored
     * 
     * <p>Default: false</p>
     */
    public void setIgnoreRemainder(boolean remainder_handling) {
        this.ignoreRemainder = remainder_handling;
    }

    /**
     * @param ignore_origination <b>false:</b> Origination fee added to the loan and included in the computation.
     * <b>true:</b> Computation performed without origination fee
     * 
     * <p>Default: false</p>
     */
    public void setIgnoreOrigination(boolean ignore_origination) {
        this.ignoreOrigination = ignore_origination;
    }


    /**
     * @param annuity_due <b>false:</b> Annuity-immediate
     * <b>true:</b> Annuity-due
     * 
     * <p>Default: false</p>
     */
    public void setAnnuityDue(boolean annuity_due) {
        this.annuityDue = annuity_due;
    }

    /**
     * @param capitalization_freq <b>0:</b> 12 (Capitalization 12 times a year). <b>Integer{@literal >} 0:</b> Any number of
     * capitalizations per year.
     * 
     * <p>Default: 0</p>
     */
    public void setCapitalizationFreq(int capitalization_freq) {
        this.capitalizationFreq = capitalization_freq;
    }

    /**
     * @param interestonly_periods_max Integer {@literal >=} 0: The maximal interest only-period offered by the bank. In years.
     * 
     * <p>Default: 0</p>
     */
    public void setInterestonlyPeriodsMax(int interestonly_periods_max) {
        this.interestonlyPeriodsMax = interestonly_periods_max;
    }

    /**
     * @param fee_processing Number {@literal >=} 0: Processing fee: A one-time fee of a fixed sum to be payed at the beginning of the loan period.
     * 
     * <p>Default: 0</p>
     */
    public void setFeeProcessing(double fee_processing) {
        this.feeProcessing = fee_processing;
    }

    /**
     * @param fee_document Number {@literal >=} 0: Document preparation fee: A one-time fee of a fixed sum to be payed at the beginning of the loan
     * period.
     * 
     * <p>Default: 0</p>
     */
    public void setFeeDocument(double fee_document) {
        this.feeDocument = fee_document;
    }

    /**
     * @param fee_percentage Number {@literal >=} 0: Percentage fee: One-time fee to be payed at the beginning of the loan period computed out of the
     * principle (gross loan). 2 = 2%.
     * 
     * <p>Default: 0</p>
     */
    public void setFeePercentage(double fee_percentage) {
        this.feePercentage = fee_percentage;
    }

    /**
     * @param fee_period_perc <b>0:</b> No percentage fee. Number: <b>Percentage.</b> Loans given as a credit line have a periodical has a fee as a percentage
     * of principal PER PERIOD
     * 
     * <p>Default: 0</p>
     */
    public void setFeePeriodPerc(double fee_period_perc) {
        this.feePeriodPerc = fee_period_perc;
    }

    /**
     * @param rate_thresholds <b>false:</b> No - intial interest rate is fixed for the whole loan for the whole loan period.
     * <b>true:</b> Rate might change
     * 
     * <p>Default: false</p>
     */
    public void setRateThresholds(boolean rate_thresholds) {
        this.rateThresholds = rate_thresholds;
    }

    /**
     * @param rate_segments <b>false:</b> All segments of the loan has the same interest rate.
     * <b>true:</b> Every segment might have separate interest rates.
     * 
     * <p>Default: false</p>
     */
    public void setRateSegments(boolean rate_segments) {
        this.rateSegments = rate_segments;
    }

    /**
     * @param priceStorage <b>(OBLIGATORY)</b> Contains information about product segments.
     * It must contain at least one element with annualInterest set
     */
    public void setPriceStorage(List<PriceStorageStep> priceStorage) {
        // IN FreeLoan JS implementation there were no first elements in arrays
        priceStorage.add(0, null);
        this.priceStorage = priceStorage.toArray(new PriceStorageStep[priceStorage.size()]);
    }
    
    /**
     * Only for serial loans. Does not apply to annuity loans.
     * 
     * @param accuracy
     * <p>Default: NORMAL</p>
     */
    public void setAccuracy(Utils.Accuracy accuracy) {
        this.accuracy = accuracy;
    }
    
    
}