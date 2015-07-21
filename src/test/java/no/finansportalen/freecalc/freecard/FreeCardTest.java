package no.finansportalen.freecalc.freecard;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import no.finansportalen.freecalc.FreeCalcTest;
import no.finansportalen.freecalc.common.Utils.RoundDirection;
import no.finansportalen.freecalc.freecard.calc.FreeCard;
import no.finansportalen.freecalc.freecard.result.FreeCardResult;
import no.finansportalen.freecalc.freeloan.calc.FreeLoanException;

import org.junit.Test;

import com.google.gson.reflect.TypeToken;

/**
 * <p>
 * Testing the credit card function creditCard() with 'live' data from Finansportalen.
 * </p>
 * 
 * <p>
 * <b>NAMING CONVENTIONS</b>
 * </p>
 * 
 * Type of fee:<br>
 * ff - fixed fee<br>
 * pf - percentage fee<br>
 * ex - exchange fee<br>
 * pe - periodic fee (for instance per month, not per transaction)<br>
 * <br>
 * 
 * 
 * Segment:<br>
 * lo - low (from low limit)<br>
 * hi - high (to high limit)<br>
 * <br>
 * 
 * Type of transaction:<br>
 * cw - cash withdrawal<br>
 * pu - purchase<br>
 * tr - transfer to account<br>
 * mo - mobile payment<br>
 * cu - currency withdrawal<br>
 * bi - paying bills in the internet bank with your credit card<br>
 * <br>
 * 
 * Where:<br>
 * hb - home bank (user's current bank) cash machine<br>
 * ch - at the counter of home bank<br>
 * ob - other bank's cash machine<br>
 * co - at the counter of other bank<br>
 * sh - in a shop<br>
 * do - domestically<br>
 * eu - abroad, inside europe<br>
 * wo - outside europe<br>
 * <br>
 * 
 * When:<br>
 * bh - business hours<br>
 * oh - other hours<br>
 * <br>
 * 
 * 
 * Minimum rule:<br>
 * mi - the combined fixed and percentage fee can not be lower<br>
 * <br>
 * 
 * Segment number:<br>
 * 01, 02, 03 etc<br>
 * <br>
 * 
 * Example:<br>
 * pu_wo_ff_01 - fixed fee for purchase outside europe, segment one<br>
 * pu_wo_pf_01 - percentage fee for purchase outside europe, segment one<br>
 * pu_wo_lo_01 - lower amount limit for fixed fee for purchase outside europe, segment one<br>
 * pu_wo_hi_01 - higher amount limit for fixed fee for purchase outside europe, segment one<br>
 * pu_wo_mi_01 - minimum amount in fee for the transaction fee for purchase outside europe (sum of fixed and percentage fee)<br>
 * pu_wo_ex_01 - percentage currency exchange fee for purchase outside europe in addition to other fees.<br>
 * pu_wo - purchase outside europe<br>
 * cw_wo - cash withdrawals outside Europe<br>
 * cw_ch - cash withdrawal at the counter of home bank<br>
 * cw_co - cash withdrawal at the counter of other bank<br>
 * cu_hb - currency withdrawal in own bank (cash machine)<br>
 * cw_sh - cash withdrawal in a shop<br>
 * tr - transfer money from your credit card to your ordinary bank account<br>
 * bi - fee for paying bills in the internet bank with the creditcard<br>
 * mo - fee for paying with the mobile phone in a shop<br>
 * pu_do - fee for domestic purchases<br>
 * pu_eu - fee for purchases abroad, but within Europe<br>
 * o - origination fee (one time fee at the startup of the card)<br>
 * pb - paper bill in the snail mail
 */
public class FreeCardTest extends FreeCalcTest<JsonProduct, JsonResult> {

    public FreeCardTest() {
        super(new TypeToken<ArrayList<JsonProduct>>(){}, new TypeToken<ArrayList<JsonResult>>(){}, "freecard", "creditcarddata.json");
    }
    
    @Test
    public void testCase1() {
        FreeCardTestInput input = new FreeCardTestInput();
        input.paybMonths = 12;
        input.creditLimit = 100000;
        input.am_cw_hb_oh = 1000;
        input.nr_cw_hb_oh = 1 ;
        input.am_cw_eu = 1000;
        input.nr_cw_eu = 1;
        input.am_pu_do = 1000;
        input.nr_pu_do = 10;
        input.am_pu_eu = 1000;
        input.nr_pu_eu = 3;
        input.sumInitDebtFirstMonthOnly = true;
        
        testFreeCard(input, "testCase1Results.json");
    }
    
    @Test
    public void testCase2() {
        FreeCardTestInput input = new FreeCardTestInput();
        input.am_cw_hb_bh = 200;
        input.nr_cw_hb_bh = 2;
        input.am_cw_eu = 2000;
        input.nr_cw_eu = 4;
        input.am_pu_do = 1000; 
        input.nr_pu_do = 10;
        input.am_pu_eu = 1000;
        input.nr_pu_eu = 3;
        input.am_mo = 500;
        input.nr_mo = 3;
        input.paybMonths = 6; 
        input.paperBill = true;
        input.creditLimit = 100;
        input.cw_wo_pf = 10;
        input.cw_wo_ff = 40;
        input.cw_ch_ff = 200;
        input.pu_eu_pf = 5;
        input.pe_pf = 10;
        input.pu_do_pf = 5;
        input.pb_ff = 50;
        input.o_ff = 600;
        input.roundDirection = RoundDirection.UP;
        input.roundToInteger = true;
        input.ignoreRemainder = true;
        input.cashIncrease = 5;
        input.includeCurrency = true;
        
        testFreeCard(input, "testCase2Results.json");
    }
    
    
    @Test
    public void testCase3() {
        FreeCardTestInput input = new FreeCardTestInput();
        input.paybMonths = 27;
        input.creditLimit = 777777;
        input.am_cw_sh = 200;
        input.nr_cw_sh = 3;
        input.am_pu_do = 30000;
        input.nr_pu_do = 5;
        input.sumInitDebtFirstMonthOnly = true;
        input.roundDirection = RoundDirection.DOWN;
        input.includeCurrency = true;
        
        testFreeCard(input, "testCase3Results.json");
    }
    
    
    @Test
    public void testCase4() {
        FreeCardTestInput input = new FreeCardTestInput();
        input.paybMonths = 12;
        input.creditLimit = 10;
        input.am_cw_hb_oh = 10;
        input.nr_cw_hb_oh = 1;
        
        testFreeCard(input, "testCase4Results.json");
    }
    

    public void testFreeCard(FreeCardTestInput input, String resultsFile) {

        // Not current Finansportalen scope used?
        if (!input.sumInitDebtFirstMonthOnly) {
            input.nr_cw_hb_bh *= 12;
            input.nr_cw_hb_oh *= 12;
            input.nr_cw_ch *= 12;
            input.nr_cw_sh *= 12;
            input.nr_cu_hb *= 12;
            input.nr_cw_ob_bh *= 12;
            input.nr_cw_ob_oh *= 12;
            input.nr_cw_co *= 12;
        }
        

        /* Net cash withdrawals and net purchases (before fees):*/
        
        // Cash withdrawal and fees from own bank during business hours
        double sumCash = input.am_cw_hb_bh * input.nr_cw_hb_bh;
        
        // Cash withdrawal and fees from own bank outside business hours
        sumCash += input.am_cw_hb_oh * input.nr_cw_hb_oh;
        
        // Cash withdrawal and fees when withdrawing at counter at own bank
        sumCash += input.am_cw_ch * input.nr_cw_ch;
        
        // Cash withdrawal and fees when withdrawing cash in a shop
        sumCash += input.am_cw_sh * input.nr_cw_sh;
        
        // Domestic currency withdrawal in own bank's machine
        sumCash += input.am_cu_hb * input.nr_cu_hb;
        
        // Domestic cash machine withdrawal during other bank's opening hours  
        sumCash += input.am_cw_ob_bh * input.nr_cw_ob_bh;
        
        // Domestic cash machine withdrawal when other bank is closed
        sumCash += input.am_cw_ob_oh * input.nr_cw_ob_oh;
        
        // Domestic withdrawal at the counter of other bank
        sumCash += input.am_cw_co * input.nr_cw_co;
        
        // Cash withdrawal in Europe outside Norway
        sumCash += input.am_cw_eu * input.nr_cw_eu;
        
        // Cash withdrawal outside Europe
        sumCash += input.am_cw_wo * input.nr_cw_wo;        

        
        
        /* Transfer to own account and bill payment are currently categorized as cash withdrawals */
        // Transfer to own account from credit card account
        sumCash += input.am_tr * input.nr_tr;
        // Bill payment from credit card account
        sumCash += input.am_bi * input.nr_bi;          


        
        // Domestic purchase 
        double sumPurc = input.am_pu_do * input.nr_pu_do;
        
        // Purchase in Europe (except home country) 
        sumPurc += input.am_pu_eu * input.nr_pu_eu;
        
        // Purchase outside Europa
        sumPurc += input.am_pu_wo * input.nr_pu_wo;        

        // Mobile payment - currently categorized as a purchase
        sumPurc += input.am_mo * input.nr_mo;       


        // Variables which are assigned new values for each bank while looping through the creditcarddata.json:
        double pFeeChange;
        double sumCashTransFees;
        double sumPurcTransFees;
        double nomRateCash;
        double termFee;
        double nomRatePurc;
        
        List<FreeCardResultWrapper> results = new ArrayList<FreeCardResultWrapper>();
        
        for(JsonProduct product : products) {
            
            input.pu_eu_ff = product.getPu_eu_ff();
            nomRatePurc = product.getNom_rate_purc();
            
            /*
             * Should we include the currency exchange fee in the computation of effective interest rate? (It's still
             * included in the total sum payed)
             */
            if (input.includeCurrency) {
                pFeeChange = product.getEx_eu_pf();
            } else {
                pFeeChange = 0;
            }


            /* Fallback to common fees if illustrative fields (planned for a new "Datafanger") are empty */
            
            // General 'european' fixed cash withdrawal fee is applied to all foreign countries 
            if (input.cw_wo_ff == 0) {
                input.cw_wo_ff = product.getCw_eu_ff();
            }
            
            // General 'european' percentual cash withdrawal fee is applied to all foreign countries
            if (input.cw_wo_pf == 0) {
                input.cw_wo_pf = product.getCw_eu_pf();
            }
            
            // General 'european' fixed purchase fee is applied to all foreign countries    
            if (input.pu_wo_ff == 0) {
                input.pu_wo_ff = input.pu_eu_ff;
            }
            
            // General 'european' percentual purchase fee is applied to all foreign countries
            if (input.pu_wo_pf == 0) {
                input.pu_wo_pf = input.pu_eu_pf;
            }
            
            // Normal fixed atm fee, when no separate fee is provided for withdrawal at own bank's counter
            if (input.cw_ch_ff == 0) {
                input.cw_ch_ff = product.getCw_hb_bh_ff();
            }
            
            // Normal perc. atm fee, when no separate fee is provided for withdrawal at own bank's counter
            if (input.cw_ch_pf == 0) {
                input.cw_ch_pf = product.getCw_do_pf();
            }
            
            // Normal fixed atm fee, when no separate fee is provided for withdrawal in a shop
            if (input.cw_sh_ff == 0) {
                input.cw_sh_ff = product.getCw_hb_bh_ff();
            }
            
            // Normal perc. atm fee, when no separate fee is provided for withdrawal in a shop
            if (input.cw_sh_pf == 0) {
                input.cw_sh_pf = product.getCw_do_pf();
            }
            
            // Normal fixed atm fee, when no separate fee is provided for currency withdrawal
            if (input.cu_hb_ff == 0) {
                input.cu_hb_ff = product.getCw_hb_bh_ff();
            }
            
            // Normal perc. atm fee, when no separate fee is provided for currency withdrawal
            if (input.cu_hb_pf == 0) {
                input.cu_hb_pf = product.getCw_do_pf();
            }
            
            // Normal fixed atm fee, when no separate fee is provided for withdrawal at other bank's counter
            if (input.cw_co_ff == 0) {
                input.cw_co_ff = product.getCw_hb_bh_ff();
            }
            
            // Normal perc. atm fee, when no separate fee is provided for withdrawal at other bank's counter
            if (input.cw_co_pf == 0) {
                input.cw_co_pf = product.getCw_do_pf();
            }
            
            // Normal fixed fee for purchases abroad applied if no separate "extraeuropean" fee is provided 
            if (input.pu_wo_ff == 0) {
                input.pu_wo_ff = input.pu_eu_ff;
            }
            
            // Normal perc. fee for purchases abroad applied if no separate "extraeuropean" fee is provided 
            if (input.pu_wo_pf == 0) {
                input.pu_wo_pf = input.pu_eu_pf;
            }
            
            // Normal fixed fee for purchases with mobile phone if no separate fee is provided
            if (input.mo_ff == 0) {
                input.mo_ff = product.getPu_do_ff();
            }
            
            // Normal perc. fee for purchases with mobile phone if no separate fee is provided
            if (input.mo_pf == 0) {
                input.mo_pf = input.pu_do_pf;
            }          
            

            
            /* (If fee for transfer to own account and fee for bill payment is not provided, it will here be considered free) */
            
            /* Cash withdrawal transaction dependent fees: */
            
            // Fee, cash withdrawal and fees from own bank during business hours
            sumCashTransFees = sumfees(input.am_cw_hb_bh, input.nr_cw_hb_bh, product.getCw_hb_bh_ff(), product.getCw_do_pf());
            
            // Fee, cash withdrawal and fees from own bank outside opening hours
            sumCashTransFees += sumfees(input.am_cw_hb_oh, input.nr_cw_hb_oh, product.getCw_hb_oh_ff(), product.getCw_do_pf());
            
            // Fee, cash withdrawal and fees when withdrawing at counter at own bank
            sumCashTransFees += sumfees(input.am_cw_ch, input.nr_cw_ch,  input.cw_ch_ff,  input.cw_ch_pf); 
            
            // Fee, cash withdrawal and fees when withdrawing cash in a shop
            sumCashTransFees += sumfees(input.am_cw_sh, input.nr_cw_sh, input.cw_sh_ff, input.cw_sh_pf);
            
            // Fee, domestic currency withdrawal in own bank's machine
            sumCashTransFees += sumfees(input.am_cu_hb, input.nr_cu_hb, input.cu_hb_ff, input.cu_hb_pf + pFeeChange); 
            
            // Fee, domestic cash machine withdrawal during other bank's opening hours  
            sumCashTransFees += sumfees(input.am_cw_ob_bh, input.nr_cw_ob_bh, product.getCw_ob_bh_ff(), product.getCw_do_pf());
            
            // Fee, domestic cash machine withdrawal when other bank is closed  
            sumCashTransFees += sumfees(input.am_cw_ob_oh, input.nr_cw_ob_oh, product.getCw_ob_oh_ff(), product.getCw_do_pf());
            
            // Fee, domestic withdrawal at the counter of other bank
            sumCashTransFees += sumfees(input.am_cw_co, input.nr_cw_co, input.cw_co_ff, input.cw_co_pf);
            
            // Fee, cash withdrawal in Europe outside Norway
            sumCashTransFees += sumfees(input.am_cw_eu, input.nr_cw_eu, product.getCw_eu_ff(), product.getCw_eu_pf() + pFeeChange); 
            
            // Fee, cash withdrawal outside Europe
            sumCashTransFees += sumfees(input.am_cw_wo, input.nr_cw_wo, input.cw_wo_ff, input.cw_wo_pf + pFeeChange); 


            
            /* Fees for transfer to own account and bill payment are currently categorized as cash withdrawals: */

            // Transfer to own account from credit card account
            sumCashTransFees += sumfees(input.am_tr, input.nr_tr, input.tr_ff, input.tr_pf);
            // Bill payment from credit card account
            sumCashTransFees += sumfees(input.am_bi, input.nr_bi, input.bi_ff, input.bi_pf);                 


            /* Summing up cash withdrawal debt: */
            /* Transaction fees pertaining to purchases: */
            
            // Domestic purchase fees 
            sumPurcTransFees = sumfees(input.am_pu_do, input.nr_pu_do, product.getPu_do_ff(), input.pu_do_pf);
            
            // Fee, purchase in Europe (except home country)
            sumPurcTransFees += sumfees(input.am_pu_eu, input.nr_pu_eu, input.pu_eu_ff, input.pu_eu_ff + pFeeChange);
            
            // Fee, purchase outside Europa
            sumPurcTransFees += sumfees(input.am_pu_wo, input.nr_pu_wo, input.pu_wo_ff, input.pu_wo_ff + pFeeChange);
            
            // Fee, mobile payment
            sumPurcTransFees += sumfees(input.am_mo, input.nr_mo, input.mo_ff, input.mo_pf);                 

            
            
            /* Summing up purchase debt: */

            /*
             * For each monthly payment, there might be "payback fees": One fixed fee is common. There are also
             * companies charging a monthly fee as a a percentage of the credit limit. Normal fees usually pertain to
             * the debt being serviced electronically. If you want a paper bill, there is an additional fee for that. We
             * collect all these sums, that don't change during the payment period, in the variable 'term_fee':
             */
            
            // Term fee
            termFee = product.getPe_ff() + (input.pe_pf * input.creditLimit / 100);
            
            // Paper bill fee
            if (input.paperBill) {
                termFee += input.pb_ff;
            } 

            /*
             * The percentage annual fee, an_pf, is computed as a fraction of the credit limit. The credit limit is
             * assumed fixed throughout the loan period, thus the fee is fixed too. Hence, we can add it to the fixed
             * fee:
             */
            double annFee = product.getAn_ff() +  (product.getAn_pf() * input.creditLimit / 100);

            
            /*
             * Annual interest rates for purchase and cash withdrawal
             * 
             * Mathematically, it is incorrect to simply multiply the monthly interest rate with 12 to obtain the annual
             * interest rate. This is still done here, in order to mirror what both the companies that report monthly
             * rate and the companies that report annual rate actually do: A company reporting 6% annual, nominal
             * interest rate will charge 0.5% per month (6/12). This is not mathematically correct, as this method does
             * not encompass the compounded interest rate at each capitalization within each year. But as long as this
             * is how the bank actually acts, we will only be able to compute the correct monthly payment by doing
             * likewise. For symmetry, we also multiply monthly rates with 12.
             */
            
            // "mnd" means month. As of July 2013, this is the common rate for purchase and withdrawals.
            if (product.getRateperiod_purc().equals("mnd")) {
                nomRatePurc = nomRatePurc * 12;  
            }


            /*
             * Separate nominal rate for cash withdrawals is currently not harvested. But for testing, we can add a
             * common increment in the calculator:
             */
            if (input.cashIncrease == 0) {
                nomRateCash = nomRatePurc;
            } else {
                nomRateCash = nomRatePurc + input.cashIncrease;
            }


            try {
                FreeCard freeCard = new FreeCard();
                freeCard.setReceivedCash(sumCash);
                freeCard.setReceivedPurchase(sumPurc);
                freeCard.setNumberOfMonths(input.paybMonths);
                freeCard.setInterestFreeDays(product.getInterestfree_days());
                freeCard.setRoundDirection(input.roundDirection);
                freeCard.setRoundToInteger(input.roundToInteger);
                freeCard.setIgnoreRemainder(input.ignoreRemainder);
                freeCard.setFeeCashTransaction(sumCashTransFees);
                freeCard.setFeePurcTransaction(sumPurcTransFees);
                freeCard.setFeeOrigination(input.o_ff);
                freeCard.setFeeAnnual(annFee);
                freeCard.setFeePeriod(termFee);
                freeCard.setRateCash(nomRateCash);
                freeCard.setRatePurchase(nomRatePurc);
                freeCard.setMinpayPerc(product.getMinpay_perc());
                freeCard.setMinpayUnits(product.getMinpay_units());
                FreeCardResult result = freeCard.calculate();
                results.add(new FreeCardResultWrapper(result));
            } catch (FreeLoanException e) {
                results.add(new FreeCardResultWrapper(e.getErrNum()));
            }
            
        }
        
        
        List<JsonResult> expectedResults = getExpectedResults(resultsFile);
        
        assertEquals(expectedResults.size(), results.size());
        Iterator<FreeCardResultWrapper> resultsIt = results.iterator();
        Iterator<JsonResult> expectedResultsIt = expectedResults.iterator();
        while(resultsIt.hasNext() && expectedResultsIt.hasNext()) {
            FreeCardResultWrapper resultWrapper = resultsIt.next();
            JsonResult expectedResult = expectedResultsIt.next();
            
            assertEquals(expectedResult.getError(), resultWrapper.getErrorNum());
            
            if(expectedResult.getError() == 0) {
                FreeCardResult result = resultWrapper.getResult();
                double precision = 0.00000001;
                assertEquals(expectedResult.getGovernmentEffectiveRate(), result.getGovernmentEffectiveRate(), precision);
                assertEquals(expectedResult.getGovernmentMonthlyPayment(), result.getGovernmentMonthlyPayment(), precision);
                assertEquals(expectedResult.getEffectiveRate(), result.getEffectiveRate(), precision);
                assertEquals(expectedResult.getMonthlyPayment(), result.getMonthlyPayment(), precision);
                assertEquals(expectedResult.getRemainder(), result.getRemainder(), precision);
            }
        }
        
        
    }

    
    private double sumfees(double sum, int times, double fixedfee, double percentfee) {
        if (times > 0 && sum > 0) {
            return times * (fixedfee + sum * percentfee / 100);
        } else {
            return 0.0;
        }
    }
    
    
    
    
    public static class FreeCardResultWrapper {
        
        FreeCardResult result;
        int errorNum = 0;
        
        public FreeCardResultWrapper(FreeCardResult result) {
            this.result = result;
        }
        
        public FreeCardResultWrapper(int errorNum) {
            this.errorNum = errorNum;
        }
        
        public FreeCardResult getResult() {
            return result;
        }
        
        public int getErrorNum() {
            return errorNum;
        }
        
        
    }
}
