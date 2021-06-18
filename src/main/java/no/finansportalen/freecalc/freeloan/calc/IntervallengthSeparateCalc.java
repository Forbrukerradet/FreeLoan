package no.finansportalen.freecalc.freeloan.calc;

import java.util.ArrayList;

import no.finansportalen.freecalc.common.AnnuityLoanPeriod;
import no.finansportalen.freecalc.common.Utils;

class IntervallengthSeparateCalc {

    /**
     * 'segmentarray': An array with a row for each segment.
     * 
     * 
     * The top / latest / most expensive segment should be the last - the highest seat numbers - i 'segmentarray'.
     */
    private ArrayList<PriceStorageStepWrapper> segmentarray;

    /**
     * OBLIGATORY: Index for the segment we are computing the number of payment periods for
     */
    private int step;

    /**
     * OBLIGATORY: Number of payment periods remaining of the whole loan.
     */
    private double periodsRemaining;

    /**
     * NORMAL: Annuities are rounded after normal rules UP: Rounded up DOWN: Rounded down
     */
    private Utils.RoundDirection roundDirection;

    /**
     * false: Payment rounded to nearest 1/100 true: Rounded to nearest integer
     */
    private boolean roundToInteger;

    /**
     * Factor we divide the annual percentage interest rate by to obtain the periodical decimal fraction rate..
     */
    private int rateDivisor;

    private double interestAmountRes;

    
    
    /**
     * <p><b>WHAT THE FUNCTION DOES</b></p>
     * 
     * <p>The function 'calculate()' computes how many payments are needed to pay down the loan from the
     * upper to the lower loan limit of the loan segment 'step', provided all segments of the loan are served as annuity
     * loans with different interest rates, but installments from all segments are only used to reduce/pay down the
     * upper segment 'step'.</p>
     * 
     * <p>The difference from 'IntervallengthCalc.calculate()' is that the latter computes the number of payments in interecals
     * where the interest rate at any one time is the same for all parts of the loan. Here, in
     * 'calculate()', parallell interest rates could be used in different segments at the same time.</p>
     * 
     * <p>As the installments also in other segments than segment 'step' are reducing the principal, we need information
     * about all segments.</p>
     * 
     * 
     * <p>The number of periods must be an integer. This effects the the remaining principal.
     * The installment does not include bank charges, then forward the fee does not affect the principal
     * amount in the accounts.</p>
     * 
     * <p>Instalments are rounded according to the rules given by the function's parameters 'roundDirection',
     * 'roundToInteger' and 'rate_divisor'.</p>
     * 
     * 
     * 
     * 
     * <p><b>METHOD</b></p>
     * 
     * 
     * <p>We search for the number of periodic payment - 'periods' - neccessary to pay down the the principal in the segment
     * 'step'.</p>
     * 
     * <p>At the end of 'term', while all segments of the loan run as separate annuity loans, the value of the whole loan -
     * principal - should be paid down by the size of the segment 'periods'. Due to rounding, we will not precicely hit
     * the amount.</p>
     * 
     * <p>This amount, thus, will be the sum of the installments. The sum, in other words, is known. But we don't know the
     * part-installments in each segment. Hence, we cannot compute 'periods' directly. We have to do it through
     * iterations.</p>
     * 
     * <p>We use Newton's method in order to obtain effective iterations. It's described here:</p>
     * 
     * <p>
     * <a href='http://en.wikipedia.org/wiki/Newton%27s_method">http://en.wikipedia.org/wiki/Newton%27s_method'>
     * http://en.wikipedia.org/wiki/Newton%27s_method">http://en.wikipedia.org/wiki/Newton%27s_method
     * </a>
     * </p>
     * 
     * <p>We need a guess, a start value for the number of periods we are looking for. We use the interest rates and the
     * initial size of the segments to compute an weighted average interest rate</p>
     */
    public AnnuityLoanPeriod calculate() throws FreeLoanException {

        double segment;
        double weight;
        double weighted_rate = 0;
        PriceStorageStepWrapper curSegmentStepWrapper = segmentarray.get(step);
        PriceStorageStep curSegmentStep = curSegmentStepWrapper.getPriceStorageStep();
        
        // Principal in the interval at the start. (Step zero is not used).
        double start_principal = curSegmentStep.getUpperLimit() - segmentarray.get(1).getPriceStorageStep().getLowerLimit();

        // We run through all the steps
        for (int i = step; i > 0; i--) {

            PriceStorageStep curSegment = segmentarray.get(i).getPriceStorageStep();

            segment = curSegment.getUpperLimit() - curSegment.getLowerLimit();
            weight = segment / start_principal;
            weighted_rate += curSegment.getAnnualInterest() * weight;

        }

        /*
         * Then we can use the function 'IntervallengthCalc.calculate()' to make an estimate for the number of periods it will take to
         * pay the loan down to the wished size. The estimate is done as if there were no segments, only one homogenous
         * loan:
         */
        double periods;

        if (step > 1) {

            IntervallengthCalc intervalCalc = new IntervallengthCalc();

            intervalCalc.setUpperlimit(curSegmentStep.getUpperLimit());
            intervalCalc.setRate(weighted_rate);
            intervalCalc.setLowerlimit(curSegmentStep.getLowerLimit());
            intervalCalc.setPeriods(periodsRemaining);
            intervalCalc.setRoundDirection(roundDirection);
            intervalCalc.setRoundToInteger(roundToInteger);
            intervalCalc.setRateDivisor(rateDivisor);
            intervalCalc.setInterestAmountRes(interestAmountRes);
            intervalCalc.setAdvance(false);

            periods = intervalCalc.calculate().getNumberOfTerms();
        } else {
            periods = periodsRemaining;
        }

        /*
         * If there is only one segment, the number of steps equals the remaing number of periods for the whole loan.
         * 
         * Before we perform the iteration, we also want to compute the annity in each segment, plus a growth factor,
         * 'k', derived from the interest rate.
         * 
         * 
         * *********************************************************************************************
         *                                   DIFFERENTIATION OF 'sum avdrag'
         * ***********************************************************************************************
         * 
         * METHOD:
         * 
         * By the annuity formula, we can find out how many periods are required to pay all loan down to zero. Here, we
         * won't necessarily pay the loan to zero, but to a lower limit for the segment that is given in 'segmentarray'.
         * 
         * The number of periods it takes to pay the loan down to zero is in the parameter 'periodsRemaining'.
         * 
         * We use the annuity in the interval and compute how many periods it would take to pay the remaining loan to
         * zero.
         * 
         * The difference must be the number of peroids it would take to pay down the principal to the remaining loan.
         * 
         * Newton's method dictates that we differentialte the function with respect to the number of periods.
         * 
         * We use the annuity formula:
         * 
         * annuity = principal* (1 - k) / (k - Math.pow(k,calculation_periods+1)) 
         * where
         * k = 1/(1+rate)
         *
         * 
         * We will rephrase this with respect to principal:
         * 
         * 
         * principal = annuity * (k - Math.pow(k,eval(calculation_periods)+eval(1))) / (1 - k);
         * 
         * 
         * We want to compute how long time it would take to pay the remaining loan down to zero, and use other variable
         * names:
         * 
         * 
         * endprincipal = annuity* (k - Math.pow(k,eval(resttid)+eval(1))) / (1 - k);
         * 
         * 
         * During the iterations, we sum up the installments that are paid in each segment during the period. This sum
         * is the size of the principal at the beginning minus an eventual residue at the end:
         * 
         * 
         * sum_installments += segment - endprincipal;
         * 
         * We will differetiate with respect to the time variable 'remaintime". 'segment' is a constant - the derivative
         * of a constant is zero. We thus consentrate on the variable 'endprincipal', which is a function of
         * 'remaintime'
         * 
         * 
         * endprincipal = annuity* (k - Math.pow(k,eval(remaintime)+eval(1))) / (1 - k);
         * 
         * 
         * endprincipal = annuity* k / (1 - k) - annuity* Math.pow(k,eval(remaintime)+eval(1))) / (1 - k);
         * 
         * 
         * Differeentiation rules are on wikipedia: http://en.wikipedia.org/wiki/Differentiation_rules
         * 
         * The first term - annuity* k / (1 - k) - consists of constants with a derivative = 0. The other term is an
         * exponential function.
         * 
         * To differentiate the expontential function: f(x) = a^x => f'(x) = a^x * ln(a)
         * 
         * 
         * endprincipal' = - annuity* Math.pow(k,eval(remaintime)+eval(1)) * Math.log(k) / (1-k);
         * 
         * 
         * For a simpler expression
         * 
         * 
         * comptime = eval(remaintime)+eval(1)
         * 
         * 
         * endprincipal' = - annuity* Math.pow(k,eval(comptime) * Math.log(k) / (1-k);
         * 
         * 
         * 
         * 'endprincipal' contains the remaining principal after the period we are looking at. Installments paid in the
         * period are 'period_inst'. This amount is the principal at the start of eacrh period - 'startprincipal' -
         * minus the principal at the end, 'endprincipal':
         * 
         * 
         * period_inst = startprincipal - endprincipal;
         * 
         * 
         * As startprincipal is a constant, it "disappears" then differentiating:
         * 
         * 
         * period_inst' = -endprincipal'
         * 
         * 
         * We subsitute for "endprincipal'" that we just computed:
         * 
         * period_inst' = annuity* Math.pow(k,eval(comptime) * Math.log(k) / (1-k);
         * 
         * We will sum all 'period_inst'-amounts in the variable 'sum_installments'. The derived of 'sum_installments'
         * we shall call 'angle'.
         * 
         * 
         * *****************************************************************************************************
         *                                          ITERATIONS
         * *****************************************************************************************************
         */

        // Paid installment summed over all the segments
        double sum_installments = 0;
        
        // The sum of the annuities (installment and interest) in each interval
        double sum_annuity = 0;
        
        // The difference between computed and wanted installment sum
        double diff = 1;
        
        // The sum of the differentiated functions
        double angle = 0;
        
        // The size of this segment ('step')
        double downpaid = curSegmentStep.getUpperLimit() - curSegmentStep.getLowerLimit();
        
        double comptime;
        
        double k;

        // We calculate the first annuity in each segment, which will not change through iterations:

        for (int i = step; i > 0; i--) {

            PriceStorageStepWrapper curSegmentWrapper = segmentarray.get(i);
            PriceStorageStep curSegment = curSegmentWrapper.getPriceStorageStep();

            // The principal that is reduced through the installments in segment 'i':

            curSegmentWrapper.setTmpPrincipal(curSegment.getUpperLimit() - curSegment.getLowerLimit());

            // The discounting factor used in the annuity formula (called 'k' in the expression) for segment 'i':

            curSegmentWrapper.setTmpDiscountingFactor(1 / (1 + curSegment.getAnnualInterest() / rateDivisor));

            // Annuity in segment 'i':

            curSegmentWrapper.setTmpAnnuity(curSegmentWrapper.getTmpPrincipal() * (1 - curSegmentWrapper.getTmpDiscountingFactor())
                    / (curSegmentWrapper.getTmpDiscountingFactor() - Math.pow(curSegmentWrapper.getTmpDiscountingFactor(), periodsRemaining + 1)));

            // To be used in result-reporting:

            sum_annuity += curSegmentWrapper.getTmpAnnuity();

        }

        /*
         * We have already made a rough estimate over how many periods it will take to pay down the loan to a level
         * where the segment 'step' has been paid. We will use this value as the initial guess when searching for
         * 'remaintime':
         */

        double remaintime = periodsRemaining - periods;

        /* 'remaintime' is the only unknown variable in the following iterations */

        int rounds = 0;

        while (Math.abs(diff) > 0.001 && rounds < 100) {

            sum_installments = 0;
            angle = 0;

            for (int i = step; i > 0; i--) {

                PriceStorageStepWrapper curSegmentWrapper = segmentarray.get(i);

                // Help variable
                comptime = remaintime + 1;
                
                // The discount factor of segment 'i'
                k = curSegmentWrapper.getTmpDiscountingFactor();
                
                // The principal in segment 'i' at the start of 'periods'
                double startprincipal = curSegmentWrapper.getTmpPrincipal();
                
                // Remaining principal at the end of 'periods'
                double endprincipal = curSegmentWrapper.getTmpAnnuity() * (k - Math.pow(k, comptime)) / (1 - k);
                
                // Installment in the period 'periods' in segment 'i'
                double period_inst = startprincipal - endprincipal;
                
                // We sum the installments for each segment here.
                sum_installments += period_inst;
                
                // Summing the differentiated
                angle += curSegmentWrapper.getTmpAnnuity() * Math.pow(k, comptime) * Math.log(k) / (1 - k);

            }

            /*
             * We then compute the differnce between 'sum_installments' and the facit 'downpaid' (the principal of
             * segment 'step'). The two shall become equal.
             */

            diff = sum_installments - downpaid;

            /*
             * 'diff' is a function of 'remaintime'. We seek to find a 'remaintime' which makes 'diff' close to zero.
             * Since 'downpaid' is a constant, the differntiated of the function 'diff' is the derived of
             * 'sum_installments'. We have already computed the differentiated of 'sum_installments'. It is commpounded
             * in the variable 'angle'. So now, we can compute hoe much we will add or subtract from 'remaintime' to get
             * 'diff' closer to zero:
             */

            remaintime += -diff / angle;

            rounds++;

        }

        /*
         * THE NUMBER OF PERIODS MUST BE AN INTEGER
         * 
         * Our number for remaining periods after the current term is over, is so far a decimal fraction. There are no
         * corresponing payment stream in reality. Payments normally only accur at whole periods, so the answer must be
         * an integer.
         * 
         * In order to compute the number of periods it takes to pay down the current segment, we subtract 'remaintime'
         * from 'periodsRemaining': THe number of periods remainig at the start of the interval minus the periods
         * remaing at the end.
         * 
         * This number must be rounded up. But to achieve this, we first round 'remaintime' down
         */

        // Integer
        remaintime = Math.floor(remaintime);
        
        // The main function result
        double theseperiods = periodsRemaining - remaintime;

        /*
         * 
         * RESULT REPORTING
         * 
         * FIVE ANSWERS:
         * 
         * We will return both the number of periods it takes to pay down 'segment'. This is stored in 'theseperiods'.
         * We will also return the new adusted lower limit for the segment. Finally, we adjust for rounding. We thus
         * return an array as function result.
         * 
         * REMAINING PRINCIPAL / ADJUSTED LOWER LIMIT
         * 
         * With several parallell segments, different parts og the loan un with separate annities. But the installment
         * part of the annuities are subtracted only from the current segment 'step'. In order to compute the remaining
         * principal accurately, we have to transverse all the periods of the segment (the reason for this, is that
         * every period's installment must be computes separatelt after the periodic payment is rounded. Otherwise, we
         * would have used the annuity formula).
         * 
         * For the segments that are not paid down in the interval, the installment part does not change through the
         * interval. Hence, we start by determing the sum of these installments (if this is not the last segment, where
         * only the one segment is paid down).
         */
        

        double other_principal = 0;

        double other_annuity = 0;

        double other_rateamount = 0;

        double segmentannuity;

        double segmentrate;

        double segmentprincipal;

        double segmentrateamount;

        for (int i = step - 1; i > 0; i--) {

            PriceStorageStepWrapper curSegmentWrapper = segmentarray.get(i);
            
            // The segment's annuity
            segmentannuity = curSegmentWrapper.getTmpAnnuity();
            
            // The segments interest rate
            segmentrate = curSegmentWrapper.getPriceStorageStep().getAnnualInterest() / rateDivisor;
            
            // The segment's initial principal
            segmentprincipal = curSegmentWrapper.getTmpPrincipal();
            
            // The segment's interest amount (for each payment)
            segmentrateamount = segmentprincipal * segmentrate;
            
            // The other segments' interest amount
            other_rateamount += segmentrateamount;
            
            // The other segments' annuities
            other_annuity += segmentannuity;
            
            // The other segments' principal
            other_principal += segmentprincipal;

        }

        // Then we transverse all payments, compute the installment on subtract it from the principal of the segment:

        // This segment's principal (original loan amount)
        double partprinc = curSegmentStep.getUpperLimit() - other_principal;
        
        // The segment annuity plus other segment annuity
        double intannuity = curSegmentStepWrapper.getTmpAnnuity() + other_annuity;
        
        // The segment's forward rate
        segmentrate = curSegmentStep.getAnnualInterest() / rateDivisor;

        for (int j = 1; j <= periods; j++) {

            // The segment's interest amounts
            segmentrateamount = partprinc * segmentrate;
            
            // The step amount of interest
            double interest_segmentbelop = segmentrateamount + other_rateamount + interestAmountRes;

            double trinnavdrag = Utils.roundoff(intannuity + interestAmountRes, roundDirection, roundToInteger)
                    - interest_segmentbelop;
            
            // Customer may pay rounded annuities
            partprinc -= trinnavdrag;

        }

        // The portion of the principal that was avdratt plus the rest of the chair which was avdratt (which may be negative)
        double gjenstol = other_principal + partprinc;

        AnnuityLoanPeriod answer = new AnnuityLoanPeriod();
        
        // The annuity rounded according to the current rounding parameters
        answer.setPayment(Utils.roundoff(sum_annuity, roundDirection, roundToInteger));
        
        // The main answer feature gives: How many periods it takes to pay off segment 'step'
        answer.setNumberOfTerms(theseperiods);

        // Remaining principal at the end of the loan
        answer.setLowerSegmentLimit(gjenstol);
        
        // "Opening balance" for the principal
        answer.setUpperSegmentLimit(curSegmentStep.getUpperLimit());
        
        // Positive numbers: We have paid too little. Negative: we paid for myerest
        answer.setRemainder(sum_annuity - answer.getPayment());

        return answer;
    }
    
    

    /**
     * @param segmentarray 'segmentarray': An array with a row for each segment.<br>
     * 
     * 
     * The top / latest / most expensive segment should be the last - the highest seat numbers - i 'segmentarray'.
     */
    public void setSegmentarray(ArrayList<PriceStorageStep> segmentarray) {
        
        this.segmentarray = new ArrayList<IntervallengthSeparateCalc.PriceStorageStepWrapper>();
        
        for(PriceStorageStep step : segmentarray) {
            this.segmentarray.add(new PriceStorageStepWrapper(step));
        }
    }

    /**
     * @param step Index for the segment we are computing the number of payment periods for
     */
    public void setStep(int step) {
        this.step = step;
    }

    /**
     * @param periods_remaining Number of payment periods remaining of the whole loan.
     */
    public void setPeriodsRemaining(double periods_remaining) {
        this.periodsRemaining = periods_remaining;
    }

    /**
     * @param round_direction NORMAL: Annuities are rounded after normal rules UP: Rounded up DOWN: Rounded down
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
     * @param rate_divisor Factor we divide the annual percentage interest rate by to obtain the
     * periodical decimal fraction rate..
     */
    public void setRateDivisor(int rate_divisor) {
        this.rateDivisor = rate_divisor;
    }


    public void setInterestAmountRes(double interest_amount_res) {
        this.interestAmountRes = interest_amount_res;
    }
    
    
    
    
    private static class PriceStorageStepWrapper {
        
        PriceStorageStep priceStorageStep;
        private double tmpPrincipal;
        private double tmpDiscountingFactor;
        private double tmpAnnuity;
        
        public PriceStorageStepWrapper(PriceStorageStep step) {
            this.priceStorageStep = step;
        }
        
        public double getTmpPrincipal() {
            return tmpPrincipal;
        }

        public void setTmpPrincipal(double tmpPrincipal) {
            this.tmpPrincipal = tmpPrincipal;
        }

        public double getTmpDiscountingFactor() {
            return tmpDiscountingFactor;
        }

        public void setTmpDiscountingFactor(double tmpDiscountingFactor) {
            this.tmpDiscountingFactor = tmpDiscountingFactor;
        }

        public double getTmpAnnuity() {
            return tmpAnnuity;
        }

        public void setTmpAnnuity(double tmpAnnuity) {
            this.tmpAnnuity = tmpAnnuity;
        }

        public PriceStorageStep getPriceStorageStep() {
            return priceStorageStep;
        }
        
    }
}
