package no.finansportalen.freecalc.freeloan;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Iterator;

import no.finansportalen.freecalc.FreeCalcTest;
import no.finansportalen.freecalc.common.AnnuityLoanPeriod;
import no.finansportalen.freecalc.common.SerialLoanPeriod;
import no.finansportalen.freecalc.common.Utils;
import no.finansportalen.freecalc.freeloan.calc.FreeLoan;
import no.finansportalen.freecalc.freeloan.calc.FreeLoanException;
import no.finansportalen.freecalc.freeloan.calc.PriceStorageStep;
import no.finansportalen.freecalc.freeloan.result.AnnuityLoanResult;
import no.finansportalen.freecalc.freeloan.result.FreeLoanResult;
import no.finansportalen.freecalc.freeloan.result.SerialLoanResult;

import org.junit.Test;

import com.google.gson.reflect.TypeToken;


public class FreeLoanTest extends FreeCalcTest<JsonProduct, JsonResult> {
    
    public FreeLoanTest() {
        super(new TypeToken<ArrayList<JsonProduct>>(){}, new TypeToken<ArrayList<JsonResult>>(){}, "freeloan", "boliglan_published.json");
    }
    
    private FreeLoan prepareCalc(double received, double residual, Integer numberofperiods, Double firstPayment, int periods_per_year,
            int interestonly_periods, boolean annuity_due, Utils.RoundDirection round_direction, boolean roundToInteger,
            boolean remainder_handling, boolean ignore_origination, boolean rate_thresholds, boolean rate_segments,
            Utils.Accuracy accuracy, JsonProduct product) {


        // The number of capitalizations every year
        int capitalization_freq = 12 / emptyToZero(product.getKap_periode());

        // The price list in Finansportalen uses whole years. FreeLoan period lengths at your choice.
        int interestonly_periods_max = emptyToZero(product.getMax_avdragsfrihet()) * 12;

        // Processing fee - fixed
        double fee_processing = emptyToZero(product.getEtableringsgebyr());

        // Document fee - fixed
        double fee_document = emptyToZero(product.getDepotgebyr());

        // Origination fee as a percentage of the initial principal
        double fee_percentage = 0;

        // A periodic fee as a percentage of the inintial principal (the size of the loan facility)
        double fee_period_perc = emptyToZero(product.getProvisjon());

        /*
         * The interest rate functions handle a rate ladder with as many steps/segments we wish. Every step has four
         * elements: The minimum threshold for the step (lower limit), the maximum threshold, the periodical fee and the
         * nominal interest rate in the segment.
         * 
         * In this test version, we use the current format of Finansportalen.no, that only allows a maximum of five
         * interest rate segments.
         * 
         * We read these step data from the array 'boliglansmatrise', in the external file 'boliglandata.js', where we
         * find them from element 11, and put them into the array 'priceStorage':
         */

        ArrayList<PriceStorageStep> price_storage = new ArrayList<PriceStorageStep>();

        PriceStorageStep step1 = generateStep(product.getMin_belop_a(), product.getMax_belop_a(),
                product.getTermingebyr_1_a(), product.getNominell_rente_1_a(), null);
        price_storage.add(step1);

        PriceStorageStep step2 = generateStep(product.getMin_belop_b(), product.getMax_belop_b(),
                product.getTermingebyr_1_b(), product.getNominell_rente_1_b(), step1);
        if (step2 != null) {
            price_storage.add(step2);
            PriceStorageStep step3 = generateStep(product.getMin_belop_c(), product.getMax_belop_c(),
                    product.getTermingebyr_1_c(), product.getNominell_rente_1_c(), step2);
            if (step3 != null) {
                price_storage.add(step3);
                PriceStorageStep step4 = generateStep(product.getMin_belop_d(), product.getMax_belop_d(),
                        product.getTermingebyr_1_d(), product.getNominell_rente_1_d(), step3);
                if (step4 != null) {
                    price_storage.add(step4);
                    PriceStorageStep step5 = generateStep(product.getMin_belop_e(), product.getMax_belop_e(),
                            product.getTermingebyr_1_e(), product.getNominell_rente_1_e(), step4);
                    if (step5 != null) {
                        price_storage.add(step5);
                    }
                }
            }
        }

        double principal;

        if (!ignore_origination) {
            principal = received + fee_processing + fee_document + fee_percentage * (received + fee_processing + fee_document);
        } else {
            principal = received;
        }

        // I the loan the user requests is not smaller than the lower limit for this loan
        if (price_storage.get(0).getLowerLimit() <= principal) {

            /*
             * Even if the lowest allowed loan is for instance 500.000, the loan will still normally be paid down to a
             * lower amount during the payment period. The minimum limt only applies to a new loan. But what interest
             * rate applies then?
             * 
             * We presuppose that this is the same as the rate for the lowest segment. Consequently, for computation
             * purposes, we adjust the lowest limit to zero:
             */

            if (price_storage.get(0).getLowerLimit() > 0) {
                price_storage.get(0).setLowerLimit(0);
            }

            FreeLoan calc = new FreeLoan();
            calc.setReceived(received);
            calc.setNumberOfPeriods(numberofperiods);
            calc.setFirstPayment(firstPayment);
            calc.setPeriodsPerYear(periods_per_year);
            calc.setBalloon(residual);
            calc.setInterestonlyPeriods(interestonly_periods);
            calc.setRoundDirection(round_direction);
            calc.setRoundToInteger(roundToInteger);
            calc.setIgnoreRemainder(remainder_handling);
            calc.setIgnoreOrigination(ignore_origination);
            calc.setAnnuityDue(annuity_due);
            calc.setCapitalizationFreq(capitalization_freq);
            calc.setInterestonlyPeriodsMax(interestonly_periods_max);
            calc.setFeeProcessing(fee_processing);
            calc.setFeeDocument(fee_document);
            calc.setFeePercentage(fee_percentage);
            calc.setFeePeriodPerc(fee_period_perc);
            calc.setRateThresholds(rate_thresholds);
            calc.setRateSegments(rate_segments);
            calc.setPriceStorage(price_storage);
            calc.setAccuracy(accuracy);
            return calc;

        } else {
            return null;
        }
    }
    
    
    
    private FreeLoanResultWrapper<AnnuityLoanResult> getAnnuityResult(double received, double residual, Integer numberofperiods, Double firstPayment, int periods_per_year,
            int interestonly_periods, boolean annuity_due, Utils.RoundDirection round_direction,
            boolean roundToInteger, boolean remainder_handling, boolean ignore_origination, boolean rate_thresholds, boolean rate_segments, Utils.Accuracy accuracy, JsonProduct product) {
        
        FreeLoan calc = prepareCalc(received, residual, numberofperiods, firstPayment, periods_per_year, interestonly_periods, annuity_due, round_direction, roundToInteger,
                remainder_handling, ignore_origination, rate_thresholds, rate_segments, accuracy, product);
        
        if(calc != null) {
            try {
                return new FreeLoanResultWrapper<AnnuityLoanResult>(calc.annuityLoan());
            } catch(FreeLoanException e) {
                return new FreeLoanResultWrapper<AnnuityLoanResult>(e.getErrNum());
            }
        } else {
            return null;
        }
        
    }
    
    
    private FreeLoanResultWrapper<SerialLoanResult> getSerialResult(double received, double residual, Integer numberofperiods, Double firstPayment, int periods_per_year,
            int interestonly_periods, boolean annuity_due, Utils.RoundDirection round_direction,
            boolean roundToInteger, boolean remainder_handling, boolean ignore_origination, boolean rate_thresholds, boolean rate_segments, Utils.Accuracy accuracy, JsonProduct product) {
        
        FreeLoan calc = prepareCalc(received, residual, numberofperiods, firstPayment, periods_per_year, interestonly_periods, annuity_due, round_direction, roundToInteger,
                remainder_handling, ignore_origination, rate_thresholds, rate_segments, accuracy, product);
        
        if(calc != null) {
            try {
                return new FreeLoanResultWrapper<SerialLoanResult>(calc.serialLoan());
            } catch(FreeLoanException e) {
                return new FreeLoanResultWrapper<SerialLoanResult>(e.getErrNum());
            }
        } else {
            return null;
        }
        
    }
    
    
    @Test
    public void testSerialBulk1()
    {
        
        // DATA FROM USER:
        double received = 1500000;          // The net amount actually received by the borrower (Loan amount)
        double residual = 0;         // Part of the loan paid back in the last period (Residue / balloon)
        int numberofperiods = 240;       // PERIOD MODE: Either this first payment or 'numberofperiods' is obligatory. with 'firstpayment' != null, the number of periods are computed*)
        Double firstPayment = null;     // PAYMENT MODE: Either this loan time or 'firstpayment" is obligatory. With 'numberofperiods' > 0, the periodical payments (annuities) are computed.
        int periods_per_year = 12;     // How many peyment periods per year? (Payments per year)
        int interestonly_periods = 0; // Number of initial periods when only interest is paid (Interest-only periods)

        // In this test module, the user can decide the rounding rules. But normally, these must be filled in by the banks:

        boolean annuity_due = false;// 0: Annuity immediate. 1: Annuity due (Correspondingly for serial loans)(Immediate / due)
        
        Utils.RoundDirection round_direction = Utils.RoundDirection.NORMAL;        // 0: Rounded according to normal rules. 1: Rounded up 2. Rounded down (Rounding direction)
        boolean round_presision = false;       // 0: Rounded to nearest cend. 1: Rounded to nearest whole currency unit (Rounding precision)
        boolean remainder_handling = false;        // 0: Remainder paid/renumerated with the last payment. 1: Remainder ignored (Placement of remainder)
        boolean ignore_origination = false;        // 0: No. Orgination fees are included (as they should) 1: Yes. Ignore the fees (Treatment of origination fees)


        boolean rate_thresholds = false;      // 0: Rate never changes 1: Rate changes at certain thresholds. (Rate can change during the loan period?)
        boolean rate_segments = false;        // 0: Different rates cannot run concurrently in different segments 1: They can (Parallell loan segments with separate rates? )

        Utils.Accuracy accuracy = Utils.Accuracy.NORMAL;            // FOR ADMINISTRATOR 0: Fast, inaccurate 1: Normal 2: Extremely accurate (FOR ADMINISTRATOR: Accuracy for serial loans)
        
        
        processSerialBulkTest("serialLoanJSResults.json", received, residual, numberofperiods, firstPayment, periods_per_year, interestonly_periods, annuity_due, round_direction, round_presision, remainder_handling, ignore_origination, rate_thresholds, rate_segments, accuracy);
        
    }
    
    
    
    
    @Test
    public void testSerialBulk2()
    {
        
        // DATA FROM USER:
        double received = 89967890;          // The net amount actually received by the borrower (Loan amount)
        double residual = 50;         // Part of the loan paid back in the last period (Residue / balloon)
        int numberofperiods = 45;      // PERIOD MODE: Either this first payment or 'numberofperiods' is obligatory. with 'firstpayment' != null, the number of periods are computed*)
        Double firstPayment = null;     // PAYMENT MODE: Either this loan time or 'firstpayment" is obligatory. With 'numberofperiods' > 0, the periodical payments (annuities) are computed.
        int periods_per_year = 10;     // How many peyment periods per year? (Payments per year)
        int interestonly_periods = 2; // Number of initial periods when only interest is paid (Interest-only periods)

        // In this test module, the user can decide the rounding rules. But normally, these must be filled in by the banks:

        boolean annuity_due = true;// 0: Annuity immediate. 1: Annuity due (Correspondingly for serial loans)(Immediate / due)
        
        Utils.RoundDirection round_direction = Utils.RoundDirection.UP;        // 0: Rounded according to normal rules. 1: Rounded up 2. Rounded down (Rounding direction)
        boolean round_presision = true;       // 0: Rounded to nearest cend. 1: Rounded to nearest whole currency unit (Rounding precision)
        boolean remainder_handling = false;        // 0: Remainder paid/renumerated with the last payment. 1: Remainder ignored (Placement of remainder)
        boolean ignore_origination = true;        // 0: No. Orgination fees are included (as they should) 1: Yes. Ignore the fees (Treatment of origination fees)


        boolean rate_thresholds = true;      // 0: Rate never changes 1: Rate changes at certain thresholds. (Rate can change during the loan period?)
        boolean rate_segments = false;        // 0: Different rates cannot run concurrently in different segments 1: They can (Parallell loan segments with separate rates? )

        Utils.Accuracy accuracy = Utils.Accuracy.EXTREMELY_ACCURATE;            // FOR ADMINISTRATOR 0: Fast, inaccurate 1: Normal 2: Extremely accurate (FOR ADMINISTRATOR: Accuracy for serial loans)
        
        
        processSerialBulkTest("serialLoanJSResultsDiffParams1.json", received, residual, numberofperiods, firstPayment, periods_per_year, interestonly_periods, annuity_due, round_direction, round_presision, remainder_handling, ignore_origination, rate_thresholds, rate_segments, accuracy);
    }
    
    
    @Test
    public void testSerialBulk3()
    {
        
        // DATA FROM USER:
        double received = 9000;          // The net amount actually received by the borrower (Loan amount)
        double residual = 20;         // Part of the loan paid back in the last period (Residue / balloon)
        int numberofperiods = 3;       // PERIOD MODE: Either this first payment or 'numberofperiods' is obligatory. with 'firstpayment' != null, the number of periods are computed*)
        Double firstPayment = null;     // PAYMENT MODE: Either this loan time or 'firstpayment" is obligatory. With 'numberofperiods' > 0, the periodical payments (annuities) are computed.
        int periods_per_year = 12;     // How many peyment periods per year? (Payments per year)
        int interestonly_periods = 1; // Number of initial periods when only interest is paid (Interest-only periods)

        // In this test module, the user can decide the rounding rules. But normally, these must be filled in by the banks:

        boolean annuity_due = true;// 0: Annuity immediate. 1: Annuity due (Correspondingly for serial loans)(Immediate / due)
        
        Utils.RoundDirection round_direction = Utils.RoundDirection.DOWN;        // 0: Rounded according to normal rules. 1: Rounded up 2. Rounded down (Rounding direction)
        boolean round_presision = false;       // 0: Rounded to nearest cend. 1: Rounded to nearest whole currency unit (Rounding precision)
        boolean remainder_handling = true;        // 0: Remainder paid/renumerated with the last payment. 1: Remainder ignored (Placement of remainder)
        boolean ignore_origination = false;        // 0: No. Orgination fees are included (as they should) 1: Yes. Ignore the fees (Treatment of origination fees)


        boolean rate_thresholds = true;      // 0: Rate never changes 1: Rate changes at certain thresholds. (Rate can change during the loan period?)
        boolean rate_segments = false;        // 0: Different rates cannot run concurrently in different segments 1: They can (Parallell loan segments with separate rates? )

        Utils.Accuracy accuracy = Utils.Accuracy.NORMAL;            // FOR ADMINISTRATOR 0: Fast, inaccurate 1: Normal 2: Extremely accurate (FOR ADMINISTRATOR: Accuracy for serial loans)
        
        
        processSerialBulkTest("serialLoanJSResultsDiffParams2.json", received, residual, numberofperiods, firstPayment, periods_per_year, interestonly_periods, annuity_due, round_direction, round_presision, remainder_handling, ignore_origination, rate_thresholds, rate_segments, accuracy);
    }
    
    
    
    @Test
    public void testSerialBulk4()
    {
        
        // DATA FROM USER:
        double received = 1555555;          // The net amount actually received by the borrower (Loan amount)
        double residual = 1000;         // Part of the loan paid back in the last period (Residue / balloon)
        Integer numberofperiods = null;       // PERIOD MODE: Either this first payment or 'numberofperiods' is obligatory. with 'firstpayment' != null, the number of periods are computed*)
        Double firstPayment = 60000.0;     // PAYMENT MODE: Either this loan time or 'firstpayment" is obligatory. With 'numberofperiods' > 0, the periodical payments (annuities) are computed.
        int periods_per_year = 12;     // How many peyment periods per year? (Payments per year)
        int interestonly_periods = 0; // Number of initial periods when only interest is paid (Interest-only periods)

        // In this test module, the user can decide the rounding rules. But normally, these must be filled in by the banks:

        boolean annuity_due = false;// 0: Annuity immediate. 1: Annuity due (Correspondingly for serial loans)(Immediate / due)
        
        Utils.RoundDirection round_direction = Utils.RoundDirection.NORMAL;        // 0: Rounded according to normal rules. 1: Rounded up 2. Rounded down (Rounding direction)
        boolean round_presision = false;       // 0: Rounded to nearest cend. 1: Rounded to nearest whole currency unit (Rounding precision)
        boolean remainder_handling = false;        // 0: Remainder paid/renumerated with the last payment. 1: Remainder ignored (Placement of remainder)
        boolean ignore_origination = false;        // 0: No. Orgination fees are included (as they should) 1: Yes. Ignore the fees (Treatment of origination fees)


        boolean rate_thresholds = false;      // 0: Rate never changes 1: Rate changes at certain thresholds. (Rate can change during the loan period?)
        boolean rate_segments = false;        // 0: Different rates cannot run concurrently in different segments 1: They can (Parallell loan segments with separate rates? )

        Utils.Accuracy accuracy = Utils.Accuracy.NORMAL;            // FOR ADMINISTRATOR 0: Fast, inaccurate 1: Normal 2: Extremely accurate (FOR ADMINISTRATOR: Accuracy for serial loans)
        
        
        processSerialBulkTest("serialLoanJSResultsDiffParams3.json", received, residual, numberofperiods, firstPayment, periods_per_year, interestonly_periods, annuity_due, round_direction, round_presision, remainder_handling, ignore_origination, rate_thresholds, rate_segments, accuracy);
        
    }
    
    
    @Test
    public void testSerialBulk5()
    {
        
        // DATA FROM USER:
        double received = 89967890;          // The net amount actually received by the borrower (Loan amount)
        double residual = 50;         // Part of the loan paid back in the last period (Residue / balloon)
        Integer numberofperiods = null;      // PERIOD MODE: Either this first payment or 'numberofperiods' is obligatory. with 'firstpayment' != null, the number of periods are computed*)
        Double firstPayment = 500000.0;     // PAYMENT MODE: Either this loan time or 'firstpayment" is obligatory. With 'numberofperiods' > 0, the periodical payments (annuities) are computed.
        int periods_per_year = 10;     // How many peyment periods per year? (Payments per year)
        int interestonly_periods = 2; // Number of initial periods when only interest is paid (Interest-only periods)

        // In this test module, the user can decide the rounding rules. But normally, these must be filled in by the banks:

        boolean annuity_due = true;// 0: Annuity immediate. 1: Annuity due (Correspondingly for serial loans)(Immediate / due)
        
        Utils.RoundDirection round_direction = Utils.RoundDirection.UP;        // 0: Rounded according to normal rules. 1: Rounded up 2. Rounded down (Rounding direction)
        boolean round_presision = true;       // 0: Rounded to nearest cend. 1: Rounded to nearest whole currency unit (Rounding precision)
        boolean remainder_handling = false;        // 0: Remainder paid/renumerated with the last payment. 1: Remainder ignored (Placement of remainder)
        boolean ignore_origination = true;        // 0: No. Orgination fees are included (as they should) 1: Yes. Ignore the fees (Treatment of origination fees)


        boolean rate_thresholds = true;      // 0: Rate never changes 1: Rate changes at certain thresholds. (Rate can change during the loan period?)
        boolean rate_segments = false;        // 0: Different rates cannot run concurrently in different segments 1: They can (Parallell loan segments with separate rates? )

        Utils.Accuracy accuracy = Utils.Accuracy.EXTREMELY_ACCURATE;            // FOR ADMINISTRATOR 0: Fast, inaccurate 1: Normal 2: Extremely accurate (FOR ADMINISTRATOR: Accuracy for serial loans)
        
        
        processSerialBulkTest("serialLoanJSResultsDiffParams4.json", received, residual, numberofperiods, firstPayment, periods_per_year, interestonly_periods, annuity_due, round_direction, round_presision, remainder_handling, ignore_origination, rate_thresholds, rate_segments, accuracy);
        
    }

    
    private void processSerialBulkTest(String filename, double received, double residual, Integer numberofperiods, Double firstPayment, int periods_per_year,
            int interestonly_periods, boolean annuity_due, Utils.RoundDirection round_direction,
            boolean roundToInteger, boolean remainder_handling, boolean ignore_origination, boolean rate_thresholds, boolean rate_segments, Utils.Accuracy accuracy) {
        
        ArrayList<FreeLoanResultWrapper<SerialLoanResult>> results = new ArrayList<FreeLoanResultWrapper<SerialLoanResult>>();
        
        for(JsonProduct product : products) {
            FreeLoanResultWrapper<SerialLoanResult> result = getSerialResult(received, residual, numberofperiods, firstPayment, periods_per_year, interestonly_periods, annuity_due, round_direction, roundToInteger, remainder_handling, ignore_origination, rate_thresholds, rate_segments, accuracy, product);
            if(result != null) {
                results.add(result);
            }
            
        }
        
        ArrayList<JsonResult> expectedResults = getExpectedResults(filename);
        
        
        compareSerialResults(results, expectedResults);
        
    }

    private void compareSerialResults(ArrayList<FreeLoanResultWrapper<SerialLoanResult>> results, ArrayList<JsonResult> expectedResults) {
        
        assertEquals(expectedResults.size(), results.size());
        Iterator<FreeLoanResultWrapper<SerialLoanResult>> resultsIt = results.iterator();
        Iterator<JsonResult> expectedResultsIt = expectedResults.iterator();
        while(resultsIt.hasNext() && expectedResultsIt.hasNext()) {
            FreeLoanResultWrapper<SerialLoanResult> resultWrapper = resultsIt.next();
            JsonResult expectedResult = expectedResultsIt.next();
            
            assertEquals(expectedResult.error, resultWrapper.getError());
            
            if(expectedResult.error == 0) {
                SerialLoanResult result = resultWrapper.getResult();
                assertEquals(expectedResult.getEffectiveInterestRate(), result.getEffectiveInterestRate(), 0.00000001);
                assertEquals(expectedResult.getPaybackPeriodCount(), result.getPaybackPeriodCount(), 0.00000001);
                assertEquals(removeIterations(expectedResult.getResitude()), result.getRemainder(), 0.000001);
                assertEquals(expectedResult.getPeriods().length, result.getPeriods().length);
                for(int i = 1; i < expectedResult.getPeriods().length; i++) {
                    SerialLoanPeriod period = result.getPeriods()[i];
                    JsonResultPeriod expectedPeriod = expectedResult.getPeriods()[i];
                    assertEquals(expectedPeriod.getSerFee(), period.getPeriodicFee(), 0.0001);
                    assertEquals(expectedPeriod.getSerInstallment(), period.getInstallment(), 0.0001);
                    assertEquals(expectedPeriod.getSerPayment(), period.getPayment(), 0.0001);
                }
            }
        }
    }
    
    
    /**
     * Removes iterations and slash from JSON input string residue
     */
    private double removeIterations(String withIterations) {
        return Double.parseDouble(withIterations.replaceFirst(" / .*", ""));
    }
    
    
    @Test
    public void testAnnuityBulk1()
    {
        
        // DATA FROM USER:
        double received = 1500000;          // The net amount actually received by the borrower (Loan amount)
        double residual = 0;         // Part of the loan paid back in the last period (Residue / balloon)
        Integer numberofperiods = 240;       // PERIOD MODE: Either this first payment or 'numberofperiods' is obligatory. with 'firstpayment' != null, the number of periods are computed*)
        Double firstPayment = null;     // PAYMENT MODE: Either this loan time or 'firstpayment" is obligatory. With 'numberofperiods' > 0, the periodical payments (annuities) are computed.
        int periods_per_year = 12;     // How many peyment periods per year? (Payments per year)
        int interestonly_periods = 0; // Number of initial periods when only interest is paid (Interest-only periods)

        // In this test module, the user can decide the rounding rules. But normally, these must be filled in by the banks:

        boolean annuity_due = false;// 0: Annuity immediate. 1: Annuity due (Correspondingly for serial loans)(Immediate / due)
        
        Utils.RoundDirection round_direction = Utils.RoundDirection.NORMAL;        // 0: Rounded according to normal rules. 1: Rounded up 2. Rounded down (Rounding direction)
        boolean round_presision = false;       // 0: Rounded to nearest cend. 1: Rounded to nearest whole currency unit (Rounding precision)
        boolean remainder_handling = false;        // 0: Remainder paid/renumerated with the last payment. 1: Remainder ignored (Placement of remainder)
        boolean ignore_origination = false;        // 0: No. Orgination fees are included (as they should) 1: Yes. Ignore the fees (Treatment of origination fees)


        boolean rate_thresholds = false;      // 0: Rate never changes 1: Rate changes at certain thresholds. (Rate can change during the loan period?)
        boolean rate_segments = false;        // 0: Different rates cannot run concurrently in different segments 1: They can (Parallell loan segments with separate rates? )

        Utils.Accuracy accuracy = Utils.Accuracy.NORMAL;            // FOR ADMINISTRATOR 0: Fast, inaccurate 1: Normal 2: Extremely accurate (FOR ADMINISTRATOR: Accuracy for serial loans)
        
        
        processAnnuityBulkTest("annuityLoanJSResults.json", received, residual, numberofperiods, firstPayment, periods_per_year, interestonly_periods, annuity_due, round_direction, round_presision, remainder_handling, ignore_origination, rate_thresholds, rate_segments, accuracy);
    }
    
    
    
    @Test
    public void testAnnuityBulk2()
    {
        
        // DATA FROM USER:
        double received = 4500000;          // The net amount actually received by the borrower (Loan amount)
        double residual = 200;         // Part of the loan paid back in the last period (Residue / balloon)
        int numberofperiods = 240;      // PERIOD MODE: Either this first payment or 'numberofperiods' is obligatory. with 'firstpayment' != null, the number of periods are computed*)
        Double firstPayment = null;     // PAYMENT MODE: Either this loan time or 'firstpayment" is obligatory. With 'numberofperiods' > 0, the periodical payments (annuities) are computed.
        int periods_per_year = 24;     // How many peyment periods per year? (Payments per year)
        int interestonly_periods = 2; // Number of initial periods when only interest is paid (Interest-only periods)

        // In this test module, the user can decide the rounding rules. But normally, these must be filled in by the banks:

        boolean annuity_due = false;// 0: Annuity immediate. 1: Annuity due (Correspondingly for serial loans)(Immediate / due)
        
        Utils.RoundDirection round_direction = Utils.RoundDirection.UP;        // 0: Rounded according to normal rules. 1: Rounded up 2. Rounded down (Rounding direction)
        boolean round_presision = true;       // 0: Rounded to nearest cend. 1: Rounded to nearest whole currency unit (Rounding precision)
        boolean remainder_handling = false;        // 0: Remainder paid/renumerated with the last payment. 1: Remainder ignored (Placement of remainder)
        boolean ignore_origination = false;        // 0: No. Orgination fees are included (as they should) 1: Yes. Ignore the fees (Treatment of origination fees)


        boolean rate_thresholds = true;      // 0: Rate never changes 1: Rate changes at certain thresholds. (Rate can change during the loan period?)
        boolean rate_segments = true;        // 0: Different rates cannot run concurrently in different segments 1: They can (Parallell loan segments with separate rates? )

        Utils.Accuracy accuracy = Utils.Accuracy.NORMAL;            // FOR ADMINISTRATOR 0: Fast, inaccurate 1: Normal 2: Extremely accurate (FOR ADMINISTRATOR: Accuracy for serial loans)

        processAnnuityBulkTest("annuityLoanJSResultsDiffParams1.json", received, residual, numberofperiods, firstPayment, periods_per_year, interestonly_periods, annuity_due, round_direction, round_presision, remainder_handling, ignore_origination, rate_thresholds, rate_segments, accuracy);
    }
    
    
    
    
    @Test
    public void testAnnuityBulk3()
    {
        
        // DATA FROM USER:
        double received = 7900000;          // The net amount actually received by the borrower (Loan amount)
        double residual = 60;         // Part of the loan paid back in the last period (Residue / balloon)
        int numberofperiods = 125;     // PERIOD MODE: Either this first payment or 'numberofperiods' is obligatory. with 'firstpayment' != null, the number of periods are computed*)
        Double firstPayment = null;     // PAYMENT MODE: Either this loan time or 'firstpayment" is obligatory. With 'numberofperiods' > 0, the periodical payments (annuities) are computed.
        int periods_per_year = 12;     // How many peyment periods per year? (Payments per year)
        int interestonly_periods = 5; // Number of initial periods when only interest is paid (Interest-only periods)

        // In this test module, the user can decide the rounding rules. But normally, these must be filled in by the banks:

        boolean annuity_due = false;// 0: Annuity immediate. 1: Annuity due (Correspondingly for serial loans)(Immediate / due)
        
        Utils.RoundDirection round_direction = Utils.RoundDirection.DOWN;        // 0: Rounded according to normal rules. 1: Rounded up 2. Rounded down (Rounding direction)
        boolean round_presision = true;       // 0: Rounded to nearest cend. 1: Rounded to nearest whole currency unit (Rounding precision)
        boolean remainder_handling = true;        // 0: Remainder paid/renumerated with the last payment. 1: Remainder ignored (Placement of remainder)
        boolean ignore_origination = true;        // 0: No. Orgination fees are included (as they should) 1: Yes. Ignore the fees (Treatment of origination fees)


        boolean rate_thresholds = false;      // 0: Rate never changes 1: Rate changes at certain thresholds. (Rate can change during the loan period?)
        boolean rate_segments = true;        // 0: Different rates cannot run concurrently in different segments 1: They can (Parallell loan segments with separate rates? )

        Utils.Accuracy accuracy = Utils.Accuracy.NORMAL;            // FOR ADMINISTRATOR 0: Fast, inaccurate 1: Normal 2: Extremely accurate (FOR ADMINISTRATOR: Accuracy for serial loans)

        processAnnuityBulkTest("annuityLoanJSResultsDiffParams2.json", received, residual, numberofperiods, firstPayment, periods_per_year, interestonly_periods, annuity_due, round_direction, round_presision, remainder_handling, ignore_origination, rate_thresholds, rate_segments, accuracy);
    }
    
    @Test
    public void testAnnuityBulk4()
    {
        
        // DATA FROM USER:
        double received = 1500000;          // The net amount actually received by the borrower (Loan amount)
        double residual = 0;         // Part of the loan paid back in the last period (Residue / balloon)
        Integer numberofperiods = null;       // PERIOD MODE: Either this first payment or 'numberofperiods' is obligatory. with 'firstpayment' != null, the number of periods are computed*)
        Double firstPayment = 50000.0;     // PAYMENT MODE: Either this loan time or 'firstpayment" is obligatory. With 'numberofperiods' > 0, the periodical payments (annuities) are computed.
        int periods_per_year = 12;     // How many peyment periods per year? (Payments per year)
        int interestonly_periods = 0; // Number of initial periods when only interest is paid (Interest-only periods)

        // In this test module, the user can decide the rounding rules. But normally, these must be filled in by the banks:

        boolean annuity_due = false;// 0: Annuity immediate. 1: Annuity due (Correspondingly for serial loans)(Immediate / due)
        
        Utils.RoundDirection round_direction = Utils.RoundDirection.NORMAL;        // 0: Rounded according to normal rules. 1: Rounded up 2. Rounded down (Rounding direction)
        boolean round_presision = false;       // 0: Rounded to nearest cend. 1: Rounded to nearest whole currency unit (Rounding precision)
        boolean remainder_handling = false;        // 0: Remainder paid/renumerated with the last payment. 1: Remainder ignored (Placement of remainder)
        boolean ignore_origination = false;        // 0: No. Orgination fees are included (as they should) 1: Yes. Ignore the fees (Treatment of origination fees)


        boolean rate_thresholds = false;      // 0: Rate never changes 1: Rate changes at certain thresholds. (Rate can change during the loan period?)
        boolean rate_segments = false;        // 0: Different rates cannot run concurrently in different segments 1: They can (Parallell loan segments with separate rates? )

        Utils.Accuracy accuracy = Utils.Accuracy.NORMAL;            // FOR ADMINISTRATOR 0: Fast, inaccurate 1: Normal 2: Extremely accurate (FOR ADMINISTRATOR: Accuracy for serial loans)
        
        
        processAnnuityBulkTest("annuityLoanJSResultsDiffParams3.json", received, residual, numberofperiods, firstPayment, periods_per_year, interestonly_periods, annuity_due, round_direction, round_presision, remainder_handling, ignore_origination, rate_thresholds, rate_segments, accuracy);
    }
    
    
    @Test
    public void testAnnuityBulk5()
    {
        
        // DATA FROM USER:
        double received = 7568254;          // The net amount actually received by the borrower (Loan amount)
        double residual = 6000;         // Part of the loan paid back in the last period (Residue / balloon)
        Integer numberofperiods = null;       // PERIOD MODE: Either this first payment or 'numberofperiods' is obligatory. with 'firstpayment' != null, the number of periods are computed*)
        Double firstPayment = 120000.0;     // PAYMENT MODE: Either this loan time or 'firstpayment" is obligatory. With 'numberofperiods' > 0, the periodical payments (annuities) are computed.
        int periods_per_year = 24;     // How many peyment periods per year? (Payments per year)
        int interestonly_periods = 3; // Number of initial periods when only interest is paid (Interest-only periods)

        // In this test module, the user can decide the rounding rules. But normally, these must be filled in by the banks:

        boolean annuity_due = true;// 0: Annuity immediate. 1: Annuity due (Correspondingly for serial loans)(Immediate / due)
        
        Utils.RoundDirection round_direction = Utils.RoundDirection.NORMAL;        // 0: Rounded according to normal rules. 1: Rounded up 2. Rounded down (Rounding direction)
        boolean round_presision = true;       // 0: Rounded to nearest cend. 1: Rounded to nearest whole currency unit (Rounding precision)
        boolean remainder_handling = true;        // 0: Remainder paid/renumerated with the last payment. 1: Remainder ignored (Placement of remainder)
        boolean ignore_origination = true;        // 0: No. Orgination fees are included (as they should) 1: Yes. Ignore the fees (Treatment of origination fees)


        boolean rate_thresholds = true;      // 0: Rate never changes 1: Rate changes at certain thresholds. (Rate can change during the loan period?)
        boolean rate_segments = false;        // 0: Different rates cannot run concurrently in different segments 1: They can (Parallell loan segments with separate rates? )

        Utils.Accuracy accuracy = Utils.Accuracy.NORMAL;            // FOR ADMINISTRATOR 0: Fast, inaccurate 1: Normal 2: Extremely accurate (FOR ADMINISTRATOR: Accuracy for serial loans)
        
        
        processAnnuityBulkTest("annuityLoanJSResultsDiffParams4.json", received, residual, numberofperiods, firstPayment, periods_per_year, interestonly_periods, annuity_due, round_direction, round_presision, remainder_handling, ignore_origination, rate_thresholds, rate_segments, accuracy);
    }

    
    private void processAnnuityBulkTest(String filename, double received, double residual, Integer numberofperiods, Double firstPayment, int periods_per_year,
            int interestonly_periods, boolean annuity_due, Utils.RoundDirection round_direction,
            boolean roundToInteger, boolean remainder_handling, boolean ignore_origination, boolean rate_thresholds, boolean rate_segments, Utils.Accuracy accuracy) {
        
        ArrayList<FreeLoanResultWrapper<AnnuityLoanResult>> results = new ArrayList<FreeLoanResultWrapper<AnnuityLoanResult>>();
        
        for(JsonProduct product : products) {
            FreeLoanResultWrapper<AnnuityLoanResult> result = getAnnuityResult(received, residual, numberofperiods, firstPayment, periods_per_year, interestonly_periods, annuity_due, round_direction, roundToInteger, remainder_handling, ignore_origination, rate_thresholds, rate_segments, accuracy, product);
            if(result != null) {
                results.add(result); 
            }
        }
        
        
        ArrayList<JsonResult> expectedResults = getExpectedResults(filename);
        
        compareAnnuityResults(results, expectedResults);
        
    }


    private void compareAnnuityResults(ArrayList<FreeLoanResultWrapper<AnnuityLoanResult>> results, ArrayList<JsonResult> expectedResults) {
        assertEquals(expectedResults.size(), results.size());
        Iterator<FreeLoanResultWrapper<AnnuityLoanResult>> resultsIt = results.iterator();
        Iterator<JsonResult> expectedResultsIt = expectedResults.iterator();
        while(resultsIt.hasNext() && expectedResultsIt.hasNext()) {
            FreeLoanResultWrapper<AnnuityLoanResult> resultWrapper = resultsIt.next();
            JsonResult expectedResult = expectedResultsIt.next();
            
            assertEquals(expectedResult.error, resultWrapper.getError());
            
            if(expectedResult.error == 0) {
                AnnuityLoanResult result = resultWrapper.getResult();
                assertEquals(expectedResult.getEffectiveInterestRate(), result.getEffectiveInterestRate(), 0.00000001);
                assertEquals(expectedResult.getPaybackPeriodCount(), result.getPaybackPeriodCount(), 0.00000001);
                assertEquals(removeIterations(expectedResult.getResitude()), result.getResidue(), 0.0000001);
                if(!(result.getPeriods() == null && expectedResult.getPeriods() == null)) {
                    assertEquals(expectedResult.getPeriods().length, result.getPeriods().length);
                    for(int i = 1; i < expectedResult.getPeriods().length; i++) {
                        
                        AnnuityLoanPeriod period = result.getPeriods()[i];
                        JsonResultPeriod expectedPeriod = expectedResult.getPeriods()[i];
                        if(!(period == null && expectedPeriod == null)) {
                            assertEquals(expectedPeriod.getAnnLowerSegmentLimit(), period.getLowerSegmentLimit(), 0.0001);
                            assertEquals(expectedPeriod.getAnnUpperSegmentLimit(), period.getUpperSegmentLimit(), 0.0001);
                            assertEquals(expectedPeriod.getAnnPayment(), period.getPayment(), 0.0001);
                            assertEquals(expectedPeriod.getAnnRemainder(), period.getRemainder(), 0.0001);
                            assertEquals(expectedPeriod.getAnnPeriodicFee(), period.getPeriodicFee(), 0.0001);
                            assertEquals(expectedPeriod.getAnnNumberOfTerms(), period.getNumberOfTerms(), 0.0001);
                        }
                    }
                }
            }
        }
    }
    
    
    private static int emptyToZero(Integer number) {
        return number == null ? 0 : number;
    }
    
    private static double emptyToZero(Double number) {
        return number == null ? 0 : number;
    }
    
    
    private static PriceStorageStep generateStep(Double minBelop, Double maxBelop, int termingebyr, Double nominellRente, PriceStorageStep prevStep) {
        // If there is an interest rate for the segment
        if (nominellRente != null) {

            PriceStorageStep step = new PriceStorageStep();
            
            // Minimum limit for the segment
            step.setLowerLimit(minBelop);

            if (maxBelop != null) {
                // Maximum limit for the segment
                step.setUpperLimit(maxBelop);
            }

            // Periodic fee in the segment
            step.setPeriodicalFee(termingebyr);
            
            // Nominal interest rate in the segment
            step.setAnnualInterest(nominellRente);

            /*
             * DIRTY TRICK: ADJUSTMENT OF ADJACENT INTERVALS: In Finansportalen, the tradition dictates that interes
             * rate segments are reported with a gap between them. Segment one might be the interval 0 - 999.999 and the
             * next 1.000.000 - 1.999.999. The gap will produce a small error when computing the interest rate. Hence,
             * we "correct" the upper limt to the lower loimt in the next segment:
             */

            if (prevStep != null && (prevStep.getUpperLimit() != Double.MAX_VALUE || minBelop != 0)) {
                prevStep.setUpperLimit(minBelop);
            }

            return step;
        }

        return null;
    }
    
    
    public static class FreeLoanResultWrapper<T extends FreeLoanResult<?>> {
        T result = null;
        int error = 0;
        
        public FreeLoanResultWrapper(T result) {
            this.result = result;
        }
        
        public FreeLoanResultWrapper(int error) {
            this.error = error;
        }

        public T getResult() {
            return result;
        }

        public int getError() {
            return error;
        }
    }
    

}
