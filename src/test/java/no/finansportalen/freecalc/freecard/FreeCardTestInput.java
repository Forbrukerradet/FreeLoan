package no.finansportalen.freecalc.freecard;

import no.finansportalen.freecalc.common.Utils.RoundDirection;

class FreeCardTestInput {
    
    /**
     * Typical withdrawal amount from own bank ("home bank") during business hours
     */
    public double am_cw_hb_bh = 0;
    
    /**
     * Count of typical withdrawals from own bank ("home bank") during business hours
     */
    public int nr_cw_hb_bh = 0;
    
    /**
     * Typical withdrawal from own bank when closed
     */
    public double am_cw_hb_oh = 0;
    
    /**
     * Count of Typical withdrawal from own bank when closed
     */
    public int nr_cw_hb_oh = 0;
    
    /**
     * Typical withdrawal amount from own bank at the counter
     */
    public double am_cw_ch = 0;
    
    /**
     * Count of typical withdrawal from own bank at the counter
     */
    public int nr_cw_ch = 0;
    
    /**
     * Typical cash withdrawal in a shop
     */
    public double am_cw_sh = 0;
    
    /**
     * Count of typical cash withdrawals in a shop
     */
    public int nr_cw_sh = 0;
    
    /**
     * Typical foreign currency amount withdrawal in own banks cash machine
     */
    public double am_cu_hb = 0;
    
    /**
     * Count of typical foreign currency withdrawals in own banks cash machine
     */
    public int nr_cu_hb = 0;
    
    /**
     * Typical withdrawal from other bank during business hours
     */
    public double am_cw_ob_bh = 0;
    
    /**
     * Count of typical withdrawals from other bank during business hours
     */
    public int nr_cw_ob_bh = 0;
    
    /**
     * Typical withdrawal from other bank when closed
     */
    public double am_cw_ob_oh = 0;
    
    /**
     * Count of typical withdrawals from other bank when closed
     */
    public int nr_cw_ob_oh = 0;
    
    /**
     * Typical withdrawal from other bank at the counter
     */
    public double am_cw_co = 0;
    
    /**
     * Count of typical withdrawals from other bank at the counter
     */
    public int nr_cw_co = 0;
    
    /**
     * Typical withdrawal abroad, but inside Europe
     */
    public double am_cw_eu = 0;
    
    /**
     * Count of typical withdrawals abroad, but inside Europe
     */
    public int nr_cw_eu = 0;
    
    /**
     * Typical withdrawal outside Europe
     */
    public double am_cw_wo = 0;
    
    /**
     * Count of typical withdrawals outside Europe
     */
    public int nr_cw_wo = 0;
    
    /**
     * Purchase domestically
     */
    public double am_pu_do = 0;
    
    /**
     * Count of purchases domestically 
     */
    public int nr_pu_do = 0;
    
    /**
     * Purchase abroad, but inside Europe
     */
    public double am_pu_eu = 0;
    
    /**
     * Count of purchases abroad, but inside Europe
     */
    public int nr_pu_eu = 0;
    
    /**
     * Purchase outside Europe
     */
    public double am_pu_wo = 0;
    
    /**
     * Count of purchases outside Europe
     */
    public int nr_pu_wo = 0;
    
    /**
     * Transfer from credit card to ordinary account
     */
    public double am_tr = 0;
    
    /**
     * Count of transfers from credit card to ordinary account
     */
    public int nr_tr = 0;
    
    /**
     * Payment of bills from the credit card account
     */
    public double am_bi = 0;
    
    /**
     * Count of payments of bills from the credit card account
     */
    public int nr_bi = 0;
    
    /**
     * Payment with mobile phone
     */
    public double am_mo = 0;
    
    /**
     * Count of payments with mobile phone
     */
    public int nr_mo = 0;

    
    
    
    
    /**
     * Loan to be paid down to zero over this number of months
     * @GUI Payback time (in number of months)
     */
    public int paybMonths = 0;

    /**
     * Monthly bills in the snail mail?
     * @GUI Monthly bills in the snail mail?
     */
    public boolean paperBill = false;

    /**
     * My real or assumed credit limit
     * @GUI Credit limit
     */
    public int creditLimit = 0;

    
    
    
    // Illustrative fees. In GUI entered by us in calculator
    
    /**
     * Fixed fee for cash withdrawals abroad outside Europe
     */
    public double cw_wo_ff = 0;
    
    /**
     *  Percentage fee for cash withdrawals abroad outside Europe
     */
    public double cw_wo_pf = 0;

    /**
     * Fixed fee purchase abroad outside Europe
     */
    public double pu_wo_ff = 0;
    
    /**
     * Percentage fee purchase  abroad outside Europe
     */
    public double pu_wo_pf = 0;

    /**
     * Fixed fee for cash withdrawal at own bank's counter
     */
    public double cw_ch_ff = 0;
    
    /**
     * Percentage fee for cash withdrawal at own bank's counter
     */
    public double cw_ch_pf = 0;

    /**
     * Fixed fee for cash withdrawal at other bank's counter
     */
    public double cw_co_ff = 0;
    
    /**
     * Percentage fee for cash withdrawal at other bank's counter
     */
    public double cw_co_pf = 0;

    /**
     * Fixed fee for currency withdrawal in own bank's cash machine. (NOT an exchange fee).
     */
    public double cu_hb_ff = 0;
    
    /**
     * Percentage fee for currency withdrawal in own bank's cash machine. (NOT an exchange fee).
     */
    public double cu_hb_pf = 0;

    /**
     * Fixed fee for transfering cash from credit card to bank account
     */
    public double tr_ff = 0;
    
    /**
     * Percentage fee for transfering cash from credit card to bank account
     */
    public double tr_pf = 0;

    /**
     * Fixed fee when paying bills from the credit card in the internet bank
     */
    public double bi_ff = 0;
    
    /**
     * Percentage fee when paying bills from the credit card in the internet bank
     */
    public double bi_pf = 0;

    /**
     * Fixed fee when paying with the mobile phone, charging the credit card
     */
    public double mo_ff = 0;
    
    /**
     * Percentage fee when paying with the mobile phone, charging the credit card
     */
    public double mo_pf = 0;
    
    /**
     * Fixed fee when withdrawing cash in a shop
     */
    public double cw_sh_ff = 0;
    
    /**
     * Percentage fee when withdrawing cash in a shop
     */
    public double cw_sh_pf = 0;

    /**
     * Fixed fee for international purchases inside Europe
     */
    public double pu_eu_ff = 0;
    
    /**
     * Percentage fee for international purchases inside Europe
     */
    public double pu_eu_pf = 0;
    

    
    
    
    /**
     * Periodic (monthly) fee as percentage of credit limit
     * @GUI Periodic fee as percentage of credit limit col 4
     */
    public double pe_pf = 0;

    /**
     * Percentage fee for domestic purchases
     * @GUI Fee as percentage of domestic purchase col 1
     */
    public double pu_do_pf = 0;

    /**
     * Origination fee (one-time fixed fee paid when establishing the credit line)
     * @GUI One-time origination fee
     */
    public double o_ff = 0;
    
    /**
     * Fee for receiving paper bills in the snail mail
     * @GUI Fee for paper bill pr. snail mail
     */
    public double pb_ff = 0;

    /**
     * Nearest, up, or down? (id=round_rule)
     * @GUI Rounding direction
     */
    public RoundDirection roundDirection = RoundDirection.NORMAL;
    
    /**
     * Nearest whole currency unit og 1/100 of this?
     * @GUI Rounding precision
     */
    public boolean roundToInteger = false;

    /**
     * Add remainder to last term og discard?
     * @GUI Placement of remainder
     */
    public boolean ignoreRemainder = false;

    
    
    
    
    
    // Administrator rules:
    
    /**
     * Increase cash interest rate by this amount (as an illustration)
     * @GUI Increase interest rate for cash withdrawal by
     */
    public double cashIncrease = 0;
    
    /**
     * The current Finansportalen Credit Card Calculator computes the intital debt as the sum of the first month's
     * domestic usage and the first year's use abroad. Another approach is to sum up all usage for one year, which is
     * the alternative of this test client:
     * 
     * @GUI Initial debt computed as Finansportalen 2013
     */
    public boolean sumInitDebtFirstMonthOnly = false;


    /**
     * As default, currency exchange fees are not included in effective interest rate, only in total payment. If
     * 'includeCurrency' == true, it is included also in effective rate.
     * 
     * @GUI Include currency exchange fees in computation of effective interest rate
     */
    public boolean includeCurrency = false;
    
}
