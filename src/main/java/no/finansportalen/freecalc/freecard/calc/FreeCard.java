package no.finansportalen.freecalc.freecard.calc;

import no.finansportalen.freecalc.common.AnnuityLoanPeriod;
import no.finansportalen.freecalc.common.Utils;
import no.finansportalen.freecalc.common.Utils.RoundDirection;
import no.finansportalen.freecalc.freecard.result.FreeCardResult;
import no.finansportalen.freecalc.freeloan.calc.FreeLoanException;
import no.finansportalen.freecalc.freeloan.calc.FreeLoanException.FreeLoanExceptionType;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * <b>OVERVIEW</b>
 * </p>
 * 
 * <p>
 * The calculator supports two different interest rates for different parts of the credit card charges - cash
 * withdrawals and purchases.
 * </p>
 * 
 * <p>
 * The calculator supports an optional interest free period for purchases.
 * </p>
 * 
 * <br>
 * <p>
 * The function 'calculate()' computes effective annual percentage rate (EAPR) under two assumptions:
 * </p>
 * 
 * 
 * <br>
 * <p>
 * a) According to a regulation issued by the Norwegian Consumer Ombudsman:<br>
 * <a href="http://www.forbrukerombudet.no/asset/4757/1/4757_1.pdf">http://www.forbrukerombudet.no/asset/4757/1/4757_1.pdf</a>
 * </p>
 * 
 * 
 * <p>
 * Here, all charges are assumed made the first day and paid back in 12 monthly annuities. An eventual interest free
 * period or other advantages (cash back) should not be taken into account.
 * </p>
 * 
 * <p>
 * In marketing, the banks should present an EAPR computet as the effective interest rate resulting from a loan of
 * 15.000 NOK. This should consist of 10 purchases in Norway, 3 purchases outside Norway, 1 cash withdrawal in Norway
 * and 1 cash withdrawal outside Norway, each of 1.000 NOK. All made the first day the customer has the card.
 * </p>
 * 
 * <p>
 * Although the user can choose other sums and usage pattern in the calculator, the computation method is still regarded
 * as binding, and the calculator always returns this "government rate" as one of the results.
 * </p>
 * 
 * <p>
 * The total payment is not covered by the regulation. The payment sums could thus be computed under more realistic
 * conditions.
 * </p>
 * 
 * <p>
 * (The Norwegian regulation is an application of a EU directive:<br>
 * <a href="http://eur-lex.europa.eu/LexUriServ/LexUriServ.do?uri=OJ:L:2008:133:0066:0092:EN:PDF">
 *  http://eur-lex.europa.eu/LexUriServ/LexUriServ.do?uri=OJ:L:2008:133:0066:0092:EN:PDF
 * </a>)
 * </p>
 * 
 * <br>
 * <p>
 * b) As above, but with a more realistic payback stream.
 * </p>
 * 
 * <p>
 * freeCard 1.0 also computes the effective interest rate when the interest-free period is taken into account and where
 * the normally delayed payment (15 - 20 days after the end of the month) is considered.
 * </p>
 * 
 * <p>
 * But even this more "realistic" method assumes that all charges are made the first day. So it is not completely
 * realistic. One could envisage a future version where the usage was assumed spread out in time; charges made evenly or
 * stocastically.
 * </p>
 * 
 * 
 * <br>
 * <br>
 * <p>
 * <b>NOT IN THIS VERSION</b>
 * </p>
 * 
 * <p>
 * There are two variables currently harvested in the Datafanger that we don't use here:
 * </p>
 * 
 * <p>
 * Renteberegning varekjøp ved kreditt Fakturadato/uttaksdato (rate computation when purchasing - on billday/on purchase
 * day) Renteberegning uttak ved kreditt Fakturadato/uttaksdato (rat computation when withdrawing cash - on bill day/on
 * withdrawal day)
 * </p>
 * 
 * <p>
 * Whatever time the interest runs from, will not override the Consumer Ombudsman's model. Furthermore, when there is an
 * interest free period for purchases, interest will not be computed until the end of this period anyway.
 * </p>
 * 
 * <p>
 * Finally, I don't think interest computation from bill day could ever pertain to cash withdrawals. It is dificult to
 * imagine that the banks would "give away" free interest days for cash (for purchases, the shops pay for the interest
 * free days).
 * </p>
 * 
 * <p>
 * The calculator handles only monthly payments and only annuity loans in arrears (annuity-immediate, or ordinaray
 * annuities).
 * </p>
 * 
 * 
 * <br>
 * <br>
 * <p>
 * <b>SECTIONS</b>
 * </p>
 * 
 * <ol>
 * <li>Preparation and adaption of data</li>
 * <li>The annuities</li>
 * <li>Effective interest rate</li>
 * <li>Result reporting</li>
 * </ol>
 * 
 * <br>
 * <br>
 * <p>
 * <b>THE PLAN: THREE ANNUITY SERIES</b>
 * </p>
 * 
 * <p>
 * The interest rate is computed by iterations. Before performing the iterations, we need to know each monthly payment.
 * </p>
 * 
 * <p>
 * Hence, all the relevant Payments, number of periods and remainders are first computed.
 * </p>
 * 
 * <p>
 * There might be different interest rates for cash withdrawals and purchases. In the case of interest free days, they
 * usually only pertain to purchases, not to cash withdrawals. The two streams might have to be treated differently.
 * </p>
 * 
 * <p>
 * Moreover, there are annual fees not fitting into the model with monthly annuities.
 * </p>
 * 
 * <p>
 * We thus make three annuitiy series:
 * </p>
 * 
 * <ol>
 * <li>The annuities paid back to the card company for cash withdrawal</li>
 * <li>The annuities paid back to the card company for purchases</li>
 * <li>The annuity series consisting of annual fees</li>
 * </ol>
 * 
 * <p>
 * As there is often an interest free period for purchases, but not for cash withdrawals, this model also allows us to
 * compute a more realistic interest rate computation, in addition to the model given in the public regulation.
 * </p>
 * 
 * <p>
 * (In a perfect world, we should be able to randomly spread out the purchases and withdrawal along the time line, for
 * again to achieve a more realistic picture of the usage pattern. This last wish is incomaptible with annuities, of
 * course. A feature with evenly spread charges to the card would, however be relatively easy to compute. But not in
 * this version.)
 * </p>
 * 
 * <br>
 * <br>
 * <p>
 * <b>ERRORS:</b>
 * </p>
 * 
 * <p>
 * When the computed payment is smaller than the minimum payment required in the parameters 'minpayPerc' and
 * 'minpayPayment', the loan must be paid back faster than the user has requested. It is less confusing to ask the user
 * to select a shorter payback period than to first compute the minimum payback time and then compute the effective
 * interest rate. Hence, in these cases an {@link FreeLoanException} is thrown.
 * </p>
 */
public class FreeCard {
    
    /**
     * EITHER.. The cash withdrawn on the card during the period, included all the transaction dependent fees
     */
    private double receivedCash;
    
    /**
     * ..AND/OR: The purchases done with the card during the period, included all the transaction dependent fees.
     */
    private double receivedPurchase;
    
    /**
     * OBLIGATORY
     */
    private Integer numberOfMonths;
    
    /**
     * The initial interest-free period (only applied to purchases) in number of days
     */
    private int interestFreeDays = 0;
    
    /**
     * Round direction for annuities
     */
    private RoundDirection roundDirection = RoundDirection.NORMAL;
    
    /**
     * false: Payment rounded to nearest 1/100 true: ..rounded to nearest integer
     */
    private boolean roundToInteger = false;
    
    /**
     * false: The "global" remainder at the end of the loan period is payed / compensated with the last payment<br>
     * true: ..is ignored
     */
    private boolean ignoreRemainder = false;
    
    /**
     * Sum of all transaction fees for cash withdrawals
     */
    private double feeCashTransaction = 0;
    
    /**
     * Sum of all transaction fees for purchases
     */
    private double feePurcTransaction = 0;
    
    /**
     * Processing fees: Sum of eventual inital one-time fees to be payed at the beginning of the loan period. 
     */
    private double feeOrigination = 0;
    
    /**
     * Fixed fee: Annual, fixed fee.
     */
    private double feeAnnual;
    
    /**
     * Fixed fee. Loans given as a credit line might have a periodical (monthly) fixed fee
     */
    private double feePeriod = 0;
    
    /**
     * OBLIGATORY: Interest rate for cash withdrawals might differ from interest on purchases
     */
    private Double rateCash;
    
    /**
     * OBLIGATORY: Interest rate for purchases might differ from interest on cash withdrawals
     */
    private Double ratePurchase;
    
    /**
     * The minimum, monthly payment as a percentage of current debt
     */
    private Double minpayPerc;
    
    /**
     * The minimum, monthly payment in currency units
     */
    private Double minpayUnits;
    
    
    
    public FreeCardResult calculate() throws FreeLoanException {
    
        checkMandatoryFields();
        
        /*
         * 1) PREPARATION AND ADAPTION OF DATA:
         * 
         * ASSUMPTIONS
         * 
         * a) Fees added to debt: Credit card fees are not paid cash, but assumed added to the debt.
         * 
         * b) Origination fee added to the cash withdrawal part of the debt: We presume here that the fixed start fee
         * which we have called 'feeOrigination' will be added to the cash withdrawal part of the loan. The reason for
         * this is twofold: Firstly, there might be a interest free period for purchases. This does not happen for cash
         * withdrawals. Secondly, the cash withdrawal might have a higher interest rate than the purchase. The lender
         * would probably place the fee in this higher rate segment.
         */

        double loanPurchase = receivedPurchase + feePurcTransaction;

        double loanCash = receivedCash + feeCashTransaction + feeOrigination;
                

        /*
         * There is also an annual fee ('feeAnnual'). This is handled further down as an annual payment stream.
         * 
         * 
         * 2) THE ANNUITIES
         * 
         * We first compute the present value of the three annuities when no allowance is made for the interest-free
         * period. We use the formulas we've already derived:
         * 
         * 
         * ********************************************************************************
         * FORMULA FOR ANNUITY-IMMEDIATE:
         * 
         * annuity = loan * (1 - k) / (k - Math.pow(k,termnumber+1));
         * where
         * k = 1/(1+rate);
         * ********************************************************************************
         * 
         * Annual fees are paid at the beginning of the year. Hence, we need the annuity-due version of the annuity
         * formula too:
         * 
         * 
         * ********************************************************************************
         * FORMULA FOR PRESENT VALUE (included payment at time zero):
         * 
         * pv = annuity * (1 - Math.pow(k,termnumber+1)) / (1 - k)
         * where
         * k = 1/(1+rate);
         * ********************************************************************************
         * 
         * We use our variable names:
         * 
         * termnumber -> numberOfMonths loan -> loanCash and loanPurchase
         */


        double purchaseAnnuity;

        double cashAnnuity;


        // Our version of the annuity formula utilzes the number of periods plus one:

        int plusMonth = numberOfMonths + 1;


        // The annuity for cash withdrawals. We add the annual fee to the "official" interest rate, hence signaling
        // that it has the same foundation and effect:

        double kc = 1 / (1 + rateCash / 1200); // 'k' from the formula above, for cash withdrawals.

        if (loanCash > 0) {
            cashAnnuity = loanCash * (1 - kc) / (kc - Math.pow(kc, plusMonth));
        } else {
            cashAnnuity = 0;
        }

        // The annuity for purchases. We add the annual fee to the "official" interest rate,

        double kp = 1 / (1 + ratePurchase / 1200); // 'k' from the formula above, for purchases.

        if (loanPurchase > 0) {
            // Interest in arrears for the "government" computation
            purchaseAnnuity = loanPurchase * (1 - kp) / (kp - Math.pow(kp, plusMonth));
        } else {
            purchaseAnnuity = 0;
        }

        double ka = 1 / (1 + rateCash / 100); // Annual discount factor

        int years = (int) Math.ceil((float)numberOfMonths / 12); // How many annual fees must we pay?
        
        // The ensuing logic is subtle: The annual fee is charged at the beginning of the first year, but payed with the
        // normal annuities. Hence, the present value of it does not equal the nominal sum.

        double annualFeePV = feeAnnual * (1 - Math.pow(ka, years)) / (1 - ka); // Present value of annuities in advance.

        double annualAnnuity = annualFeePV * (1 - kc) / (kc - Math.pow(kc, plusMonth)); // Monthly payments in arrears

        // The sum of the three annuities, included periodic fees.

        double sumAnnuity = cashAnnuity + purchaseAnnuity + annualAnnuity + feePeriod;

        double roundedAnnuity = Utils.roundoff(sumAnnuity, roundDirection, roundToInteger);


        /*
         * 
         * MONTHLY, MINIMUM PAYMENT
         * 
         * Consider the parameters to the function:
         * 
         * minpayPerc => The minimum, monthly payment as a percentage of current debt
         * minpayUnits => The minimum, monthly payment in nominal currency units (for instance NOK or GBP).
         * 
         * Most card issuers demand that the borrower pays back a minimum payment each month. Normally, this payment is
         * a percentage of the credit charged to the card, 'minpayPerc'. Neither should this amount fall below the
         * fixed sum 'minpayUnits'.
         * 
         * a) As this function 'calculate()' only handles annuity loans, the payments are equal in all periods. If the
         * first payment is smaller than the threshold in 'minpayUnits', all are. In this case, the minimum payment
         * will take the place of the computed annuity. The effect would be a shorter payback time than the calculator's
         * user has requested.
         * 
         * As this is a user input, it is not "our job" to recalculate the period. Instead, we will return an error
         * message: Choose a shorter payback period!
         * 
         * b) Likewise, if the minimum percentage of the initial debt exceeds the annuity, the payback period will be
         * shorter!
         * 
         * As the debt shrinks with time in our model, the minimum percentage will not exceed the annuities later if they
         * don't do it initially.
         */


        double initaldebt = loanCash + loanPurchase + feeAnnual; // What is charged to the card, included fees.

        double minfeePerc = minpayPerc / 100 * initaldebt; // Computing the minimum percentage payment in the first term

        if (minpayUnits > sumAnnuity || minfeePerc > sumAnnuity) {
            throw new FreeLoanException(FreeLoanExceptionType.ANNUITY_FALL_BELOW_MIN_PAYMENT);
        }


        // We are going to use a rate computing calculator {@link RateAnnuityCalc} -
        // that expects an {@link AnnuityLoanPeriod} object as one of the parameters.

        AnnuityLoanPeriod period = new AnnuityLoanPeriod();
        period.setPayment(roundedAnnuity);
        period.setNumberOfTerms(numberOfMonths);
        period.setPeriodicFee(0);
        period.setLowerSegmentLimit(0);
        period.setUpperSegmentLimit(receivedCash + receivedPurchase);
        period.setRemainder(sumAnnuity - roundedAnnuity);

        List<AnnuityLoanPeriod> periods = new ArrayList<AnnuityLoanPeriod>();
        periods.add(period);

        
        /*
         * 3) EFFECTIVE INTEREST RATE:
         * 
         * 
         * THE "GOVERNMENT FORMULA":
         * 
         * A Norwegian public regulation decides the general algorithm for computing the effective interest rate for
         * credit cards. One of the statutes is that all withdrawals and purchases shall be considered to have been made
         * the first day. This is our present value. We are looking for an interest rate that makes the present value of
         * the monthly payback stream equal to this present value.
         * 
         * To compute the effective interest rate, we use the rate computing calculator {@link RateAnnuityCalc}:
         */
                
        double rateGuess = ratePurchase / 12; // All credit cards have at least an interest rate for purchases

        double received = receivedPurchase + receivedCash;

        RateAnnuityCalc rateAnnuityCalc = new RateAnnuityCalc();
        rateAnnuityCalc.setReceived(received);
        rateAnnuityCalc.setCalculationPeriods(numberOfMonths);
        rateAnnuityCalc.setPayments(periods);
        rateAnnuityCalc.setHighestSegment(1);
        rateAnnuityCalc.setResidue(0.0);
        rateAnnuityCalc.setCapitalizationFreq(12);
        rateAnnuityCalc.setGuessrate(rateGuess);
        rateAnnuityCalc.setAdvance(false);

        double er = rateAnnuityCalc.calculate();


        /*
         * 
         * 
         * 
         * 
         * THE "ADVANCED" FORMULA
         * 
         * Most credit cards don't charge interest for purchases before after 45 / 50 days, while cash withdrawals are
         * charged with interest from the first day.
         * 
         * The "government formula" does not take this into consideration, as it is adapted to a public regulations
         * deciding how effective interest on credit cards should be computed: Every charge to the card - purchases and
         * withdrawals are assumed to be made the first day. Thereafter, the card is not charged more and the debt is
         * paid back in monthly annuities in arrears at the end of each month.
         * 
         * The guide states that an eventual interest free period should be ignored in this computation.
         * 
         * But the user might still want to see a more realistic computation. We can take the interest free period into
         * account by treating the payback stream from the purchases differently: We apply the present value formula for
         * annuities due - the first installment being paid at once, in time zero, at the very start of the time line.
         * This will emulate that there is no interest before the payment takes place. These annuities should be the
         * ones actually paid.
         * 
         * We compute the sommon annuity for the other elements, the payments pertaing to cash withdrawals and fees,
         * "normally", treating these as annuities in arrears.
         * 
         * I might be necessary to augment this latter payment stream further, by the time from the bill is issued until
         * the due date. The annuity in arrears already takes 30 interest rate bearing days into consideration, but
         * there might be 15/20 more.
         * 
         * (These additional days will be computed with the nominal interest rate computed by the 'bank-method': The
         * arithmetic average of the interest rate. By this method, when the monthly interest rate is 1%, the rate for
         * half a month is simply 1/2%. This is not mathematically correct, but it is the way the banks normally do it,
         * so we must emulate that here in order to obtain the actual payments.)
         * 
         * There are not two payment streams, only one. Both the interest-bearing cash withdrawals and the temporarily
         * interest free purchases are served with the same monthly payback stream, starting after 45 / 50 days.
         * 
         * Hence, we sum the payments, getting one payment stream and finding the present value of this.
         * 
         * But the normal present value in arrears formula stipulates that our observation point lays one month before
         * the first payment.
         * 
         * We, thus, have to further discount the payment stream with the 15/20 days longer, first period.
         * 
         * We have already computed the annuity for purchases, using the normal annuity formula with annuities in
         * arrears. Now, we recompute it with the annuities in advance-formula:
         * 
         * 
         * annuPurch = loanPurchase * (1 - kp) / (1 - Math.pow(kp,numberOfMonths)); // Applying the formula for
         * annuities in advance
         * 
         * annuOther = sumannuity - purchaseAnnuity; // Subtracting the old "purchase annuity", computed above,
         * stipulating annuities in arrears
         * 
         * annu = annuPurch + annuOther;
         * 
         * 
         * We are searching for an annual, effective interest rate. But since the payments are monthly, it is practical
         * to first find the monthly interest rate, 'r', which is a desimal number (0,015 means 1,5%). Each monthly
         * amount should be multiplied with 1,015 (1 + r) to obtain the next.
         * 
         * If something grows by 1,5% each month, how much does it grow by in one day?
         * 
         * If we assume daily capitalization:
         * 
         * (1 + rd) = (1 + r) ^ (1/30) // The daily growth rate is the 30th root of the monthly growth rate
         * 
         * Discouting means dividing by the growth factor, or multiplying by 1 / (1 + r). We call this factor k:
         * 
         * 
         * As k = 1 / (1 + r) => (1 + r) = 1/k // The monthly growth factor
         * 
         * (1 + r)^(1/30) = (1/k)^(1/30) // The daily growth factor
         * 
         * 
         * We now have the annuity stream. We first find the preliminary present value as such:
         * 
         * pvP = annu * (k - Math.pow(k,numberOfMonths+1)) / (1 - k)
         * 
         * With its monthly scope, this is the present value one month ahead of the first payment.
         * 
         * But the payment stream in reality starts later.
         * 
         * extraDays = dueDays - 30 // The number of days we shall discount the computed present value by
         * 
         * We will first compute the growth factor for the extra days. It is the daily growth factor, '(1/k)^(1/30)',
         * raised by the number of extra days:
         * 
         * extraGrowth = ((1/k)^(1/30))^extraDays
         * 
         * I follows as normal computation rules that (a^m)^n = a^(m * n), Thus..
         * 
         * extraGrowth = (1/k)^(1/30 * extraDays) = (1/k)^(extraDays/30)
         * 
         * The discount rate is the inverse of the growth rate: 1/ (1 + r) = k
         * 
         * Hence, we make a new discount rate, ke, for the 'extraDays' number of days:
         * 
         * ke = 1 / ((1/k)^(extraDays/30))
         * 
         * Or, in Javascript:
         * 
         * ke = 1 / Math.pow(1/k,extraDays/30)
         * 
         * 
         * We still maintain the assumption that all charges to the card are made the first day. Thus, the present value
         * equals the nominal, received sum.
         * 
         * The annuity formula gives us the payment stream that starts 30 days after we get the card. But the due date
         * is slightly later, normally 15-20 days into the next month. The bank will charge interest up to payment day,
         * so we must discount the preliminary present value we found, 'pvP', with these extra days to obtain the
         * present value.
         * 
         * received = pvP * ke // Discounted with the extra days. We substitute pvP with its formula:
         * 
         * received = (annu * (k - Math.pow(k,numberOfMonths+1)) / (1 - k)) * ke // We substitute 'ke' by 'ke = 1 /
         * Math.pow(1/k,extraDays/30)', as derived above:
         * 
         * received = (annu * (k - Math.pow(k,numberOfMonths+1)) / (1 - k)) * 1 / Math.pow(1/k,extraDays/30)
         * 
         * => (annu * (k - Math.pow(k,numberOfMonths+1)) / (1 - k)) / Math.pow(1/k,extraDays/30) - received = 0;
         * 
         * y = (annu * (k - Math.pow(k,numberOfMonths+1)) / (1 - k)) / Math.pow(1/k,extraDays/30) - received; //
         * Assuming a continous function
         * 
         * In order to use Newton's method (http://en.wikipedia.org/wiki/Newton%27s_method), we must also find the
         * derived of the function - y'
         * 
         * This is of course great fun, as we have a function with another function as one of its variables.
         * 
         * We'll proceed stepwise, by breaking up y in its parts and treat the parts separately. First in its two main
         * derivable factors:
         * 
         * y = (annu * (k - Math.pow(k,numberOfMonths+1)) / (1 - k)) / Math.pow(1/k,extraDays/30) - received;
         * 
         * a = annu * (k - Math.pow(k,numberOfMonths+1)) / (1 - k)
         * 
         * b = Math.pow(1/k,extraDays/30)
         * 
         * When we have found the derivatives of a and b, we can apply the product rule:
         * 
         * y' = (a'*b - a * b') / b^2
         * 
         * First a
         * 
         * a = annu * (k - Math.pow(k,numberOfMonths+1)) / (1 - k)
         * 
         * a consists of functions both in its nominator and denominator. We treat these separartely too:
         * 
         * aNo = annu * (k - Math.pow(k,numberOfMonths+1))
         * 
         * aDe = (1 - k)
         * 
         * We are looking to apply the quotient rule:
         * 
         * a' = (aNo' * aDe - aNo * aDe') / aDe ^2
         * 
         * First the derivatives:
         * 
         * aNo = annu * (k - Math.pow(k,numberOfMonths+1))
         * 
         * aNo' = annu * (1 - (numberOfMonths+1) * Math.pow(k,numberOfMonths))
         * 
         * aDe = (1-k)
         * 
         * aDe' = -1
         * 
         * a' = ((annu * (1 - (numberOfMonths+1) * Math.pow(k,numberOfMonths))) * (1-k) - annu * (k -
         * Math.pow(k,numberOfMonths+1)) * -1) / Math.pow(1 - k, 2)
         * 
         * a' = ((annu * (1 - (numberOfMonths+1) * Math.pow(k,numberOfMonths))) * (1-k) + annu * (k -
         * Math.pow(k,numberOfMonths+1))) / Math.pow(1 - k, 2)
         * 
         * (For convenience, we have already computed 'numberOfMonths + 1' above, in the variable 'plusmonth'
         * 
         * Then b:
         * 
         * b = Math.pow(1/k,extraDays/30)
         * 
         * Applying the chain & power rule:
         * 
         * 
         * ********************************************************************************
         * Combining the Chain Rule and the Power Rule
         * y = f(x) = [u(x)]^n
         * f'(x) = n * [u(x)]^n-1 * u'(x)
         * ********************************************************************************
         * 
         * b' = extraDays/30 * Math.pow(1/k,(extraDays/30)-1) * -1/Math.pow(k,2) // The last term is the derivative of
         * 1/k, which is -1/k^2
         * 
         * b' = - extraDays/30 * Math.pow(1/k,(extraDays/30)-1)/Math.pow(k,2)
         * 
         * We are approacthing our functions. Remember the quotient rule:
         * 
         * y' = (a'* b - a * b') / b^2 // We substitute the right hand factors with their formulas:
         * 
         * y' = ((annu * (1 - plusmonth * Math.pow(k,numberOfMonths))) * (1-k) + annu * (k - Math.pow(k,plusmonth))) /
         * Math.pow(1 - k, 2) * Math.pow(1/k,extraDays/30) - (annu * (k - Math.pow(k,plusmonth)) / (1 - k)) * -
         * (extraDays/30 * Math.pow(1/k,(extraDays/30)-1)/Math.pow(k,2))) / Math.pow(Math.pow(1/k,extraDays/30))^2
         * 
         * The term 'Math.pow(Math.pow(1/k,extraDays/30))^2' can be transformed to 'Math.pow(1/k,extraDays/30 * 2) =
         * Math.pow(1/k,extraDays/15)'. Hence..
         * 
         * y' = ((annu * (1 - plusmonth * Math.pow(k,numberOfMonths))) * (1-k) + annu * (k - Math.pow(k,plusmonth))) /
         * Math.pow(1 - k, 2) * Math.pow(1/k,extraDays/30) - (annu * (k - Math.pow(k,plusmonth)) / (1 - k)) * -
         * (extraDays/30 * Math.pow(1/k,(extraDays/30)-1)/Math.pow(k,2))) / Math.pow(1/k,extraDays/15)
         * 
         * y = (annu * (k - Math.pow(k,plusmonth)) / (1 - k)) / Math.pow(1/k,extraDays/30) - received;
         * 
         * Simple as that :)
         * 
         * The payments are computed with the bank's interest rate. All discounting is done with ours: The computed,
         * effective interest rate derived through iterations.
         * 
         * 
         * Before we can compute the effective interest rate, we must compute the annuity:
         */

        
        int dueDays = 45; // Normal number of days from the first day in the month until the bill is due? HARDCODED!
        int extraDays;

        if (interestFreeDays > 30) {
            extraDays = interestFreeDays - 30;
        } else {
            extraDays = dueDays - 30;
        }

        // Applying the formula for annuities in advance
        double annuPurch = loanPurchase * (1 - kp) / (1 - Math.pow(kp, numberOfMonths));

        /*
         * The value of the annuity pertaining to cash withdrawals and annual fee must accrue interest for another 15 /
         * 20 days, to be payed at the same day as the interest free purchase charges. To this end, we subtract the old
         * "purchase annuity", computed above. We also subtract the periodic fee, as that should not accrue interest. It
         * should have its nominal value at payday:
         */


        double annuOther = sumAnnuity - purchaseAnnuity - feePeriod;

        // The interest accrued for the period from the end of the month until the bill is due
        double extraRate = rateCash / 36000 * extraDays;

        // The annuity for cash withdrawals, augmented with interest until the bill is due - for another 15/20 days.
        double annuOtherInc = annuOther * (1 + extraRate);

        // As we are no on the correct day, we add the nominal value of the periodic fee again
        double annuUnrounded = annuPurch + annuOtherInc + feePeriod;

        // The monthly annuity, taking 30 interest free days for purchases into account
        double annu = Utils.roundoff(annuUnrounded, roundDirection, roundToInteger);

        // CONTROLLING FOR THE MINIMUM MONTHLY PAYMENT:
        if (minpayUnits > annu) {
            // We intrerupt the computation if the minimum payment exeeds the computed annuity
            throw new FreeLoanException(FreeLoanExceptionType.ANNUITY_FALL_BELOW_MIN_PAYMENT);
        }

        
        /*
         * 
         * COMPUTING THE REMAINDER
         * 
         * When computing an annuity, we typically get numbers as 234.938267484958. This is not payable in a bank.
         * 234.94 is, but these slightly too big annuity will make us overshoot the target payment at the end of the
         * loan period (we pay a little too much).
         * 
         * This amount is the remainder. It is either ignored or compensated. It it is compensated, this is normally
         * done with the last payment in the loan period.
         * 
         * We first compute the present value of the remainder. Then we compute the forward value, the value when it is
         * actually to be paid, with the last payment.
         * 
         * But we normally cannot pay the remainder either. We can pay only the rounded reminder.
         * 
         * To investigate how the remainder affects our computation of effective interest rate, we then again has to
         * compute the present value of the amount that is actually to be paid..
         */

        
        // The fist suggestion of monthly discount rate is set to the nominal discount rate for purchases.
        double k = kp;
        double remainder;
        double exp;
                
                
        if (!ignoreRemainder) { 

            // The remainder to be compensated / charged with the last payment

            double PVRounded = (annu * (k - Math.pow(k, plusMonth)) / (1 - k)) / Math.pow(1 / k, extraDays / 30.0);

            // Negative number: In customer's favour
            double PVUnrounded = (annuUnrounded * (k - Math.pow(k, plusMonth)) / (1 - k)) / Math.pow(1 / k, extraDays / 30.0); 

            // The unrounded present value of the remainder is the difference between these:
            double grossRemainderPV = PVUnrounded - PVRounded;

            // The forward value of the remainder (amount to be paid with the last term):
            double remainderFV = grossRemainderPV * Math.pow(1 / k, numberOfMonths) * Math.pow(1 / k, extraDays / 30.0);

            /*
             * We find the remainder we shall actually pay by summing the unrounded annuity with the unrounded
             * remainder. We then subtract the rounded annuity and round what is left. In this latter operation, we
             * round to the nearest number, as we assume the intention of this remainder is to correct rounding errors
             * done previously.
             */
            remainder = Utils.roundoff(Utils.roundoff(annuUnrounded + remainderFV, roundDirection, roundToInteger) - annu,
                    roundDirection, roundToInteger);

            /*
             * Now, we have found the actually paid/rounded remainder in the last term. The present value of it is will
             * vary with the effective interest rate we find. This present value must be included when we do the
             * iterations:
             * 
             * remainderPV = remainder * ( Math.pow(k, numberOfMonths) * Math.pow(k,extraDays/30));
             * 
             * => remainderPV = remainder * ( Math.pow(k, numberOfMonths+extraDays/30);
             * 
             * 
             * Where the exponentials can be summed:
             */
            exp = numberOfMonths + extraDays / 30.0;

            /*
             * remainderPV = remainder * Math.pow(k, exp);
             * 
             * We'll also need the differentiated of the present value of the remainder to add to the differentiated of the
             * present value of the whole loan. We use the quotient rule. Bearing in mind that 'remainder' now is a constant.
             * remainderPVDif = - remainder * (numberOfMonths + extraDays/30) * Math.round(1/k, numberOfMonths + extraDays/30 - 1)
             * / Math.round(1/k, numberOfMonths + extraDays/30 + 2);
             */

        } else {

            remainder = 0; // The remainder to be ignored

            exp = numberOfMonths + extraDays / 30.0; // Still used in one of the formulas

        } 



        // THE ITERATIONS

        int rounds = 0; // Counts the number of iterations

        // 'y' is the function value that we want to make close to '0'. Given av positive value here, so we can enter the 'while'-loop.
        double y = 1;

        // 'y' differentiated
        double yDif; 

        // Here, we set the accuracy we want.
        while (Math.abs(y) > 0.00001 && rounds < 100) 
        {

            y = (annu * (k - Math.pow(k, plusMonth)) / (1 - k)) * Math.pow(k, extraDays / 30.0) - received;

            // We add the present value of the remainder 'remainderPV':
            y += remainder * Math.pow(k, exp);

            yDif = (
                    (annu * (1 - plusMonth * Math.pow(k, numberOfMonths)) * (1 - k) + annu * (k - Math.pow(k, plusMonth))) /
                    Math.pow(1 - k, 2) *
                    Math.pow(1 / k, extraDays / 30.0)
                    +
                    annu *
                    (k - Math.pow(k, plusMonth)) /
                    (1 - k) *
                    extraDays /
                    30.0 *
                    Math.pow(1 / k, (extraDays / 30.0) - 1) /
                    Math.pow(k, 2)
                    ) /
                    
                    Math.pow(1 / k, extraDays / 15.0);

            // We add the differentiated of the present value of the remainder 'remainderPVDif':
            yDif += remainder * exp * Math.pow(k, exp - 1);

            double delta = -y / yDif; // The increase in 'k' necessary at the tangent's intersection with the PV-axis

            if (k + delta != 1) {
                k += delta; // We increase/decrease 'k' (the annuity function crashes at j==1)
            }

            rounds++;

        }

        // The effective, annual interest rate with the "advanced" model
        double erAdvanced = (Math.pow(1 / k, 12) - 1) * 100; 

        // This should not happen, but it does in the JS version if input data is wierd. So we
        // throw an exception to avoid problems later on.
        if(Double.isNaN(er) || Double.isNaN(erAdvanced)) {
            throw new FreeLoanException(FreeLoanExceptionType.EFFECTIVE_RATE_WAS_NAN);
        }
        
        /*
         * 4) RESULT REPORTING
         */

        FreeCardResult result = new FreeCardResult();
        result.setGovernmentEffectiveRate(er);
        result.setGovernmentMonthlyPayment(roundedAnnuity);
        result.setEffectiveRate(erAdvanced);
        result.setMonthlyPayment(annu);
        result.setRemainder(remainder);

        return result;

    }
    
    
    private void checkMandatoryFields() throws FreeLoanException {
        
        if(receivedCash == 0 && receivedPurchase == 0) {
            throw new FreeLoanException("receivedCash and/or receivedPurchase");
        }
        
        if(numberOfMonths == null) {
            throw new FreeLoanException("numberOfMonths");
        }
        
        if(rateCash == null) {
            throw new FreeLoanException("rateCash");
        }
        
        if(ratePurchase == null) {
            throw new FreeLoanException("ratePurchase");
        }
        
        if(minpayPerc == null) {
            throw new FreeLoanException("minpayPerc");
        }
        
        if(minpayUnits == null) {
            throw new FreeLoanException("minpayUnits");
        }
        
    }
    

    /**
     * The cash withdrawn on the card during the period, included all the transaction dependent fees.<br>
     * At least one of the parameters (receivedCash, receivedPurchase) have to be set.
     */
    public void setReceivedCash(double receivedCash) {
        this.receivedCash = receivedCash;
    }

    /**
     * The purchases done with the card during the period, included all the transaction dependent fees.<br>
     * At least one of the parameters (receivedCash, receivedPurchase) have to be set.
     */
    public void setReceivedPurchase(double receivedPurchase) {
        this.receivedPurchase = receivedPurchase;
    }

    public void setNumberOfMonths(Integer numberOfMonths) {
        this.numberOfMonths = numberOfMonths;
    }

    /**
     * The initial interest-free period (only applied to purchases) in number of days
     */
    public void setInterestFreeDays(int interestFreeDays) {
        this.interestFreeDays = interestFreeDays;
    }

    /**
     * Round direction for annuities
     */
    public void setRoundDirection(RoundDirection roundDirection) {
        this.roundDirection = roundDirection;
    }

    /**
     * false: Payment rounded to nearest 1/100 true: ..rounded to nearest integer
     */
    public void setRoundToInteger(boolean roundToInteger) {
        this.roundToInteger = roundToInteger;
    }

    /**
     * false: The "global" remainder at the end of the loan period is payed / compensated with the last payment<br>
     * true: ..is ignored
     */
    public void setIgnoreRemainder(boolean ignoreRemainder) {
        this.ignoreRemainder = ignoreRemainder;
    }

    /**
     * Sum of all transaction fees for cash withdrawals
     */
    public void setFeeCashTransaction(double feeCashTransaction) {
        this.feeCashTransaction = feeCashTransaction;
    }

    /**
     * Sum of all transaction fees for purchases
     */
    public void setFeePurcTransaction(double feePurcTransaction) {
        this.feePurcTransaction = feePurcTransaction;
    }

    /**
     * Processing fees: Sum of eventual inital one-time fees to be payed at the beginning of the loan period. 
     */
    public void setFeeOrigination(double feeOrigination) {
        this.feeOrigination = feeOrigination;
    }

    /**
     * Fixed fee: Annual, fixed fee.
     */
    public void setFeeAnnual(double feeAnnual) {
        this.feeAnnual = feeAnnual;
    }

    /**
     * Fixed fee. Loans given as a credit line might have a periodical (monthly) fixed fee
     */
    public void setFeePeriod(double feePeriod) {
        this.feePeriod = feePeriod;
    }

    /**
     * OBLIGATORY: Interest rate for cash withdrawals might differ from interest on purchases
     */
    public void setRateCash(Double rateCash) {
        this.rateCash = rateCash;
    }

    /**
     * OBLIGATORY: Interest rate for purchases might differ from interest on cash withdrawals
     */
    public void setRatePurchase(Double ratePurchase) {
        this.ratePurchase = ratePurchase;
    }

    /**
     * The minimum, monthly payment as a percentage of current debt
     */
    public void setMinpayPerc(Double minpayPerc) {
        this.minpayPerc = minpayPerc;
    }

    /**
     * The minimum, monthly payment in currency units
     */
    public void setMinpayUnits(Double minpayUnits) {
        this.minpayUnits = minpayUnits;
    }
    
}
