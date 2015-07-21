package no.finansportalen.freecalc.freecard;

public class JsonProduct {
    
    
    private String issuer;
    private String cardname;
    
    /**
     * Fixed annual fee for holding the credit card, always paid in advance
     */
    private Double an_ff;
    
    /**
     * Percentage annual fee for holding the credit card, as percentage of the credit limit, paid in advance
     */
    private Double an_pf;
    
    /**
     * Nominal interest rate for purchases (for everything unless a separate rate for cash is provided)
     */
    private Double nom_rate_purc;
    
    /**
     * The scope of the provided interest rate - month ("mnd") or year ("aar")
     */
    private String rateperiod_purc;
    
    /**
     * Maximum number of interest free days for purchases
     */
    private Integer interestfree_days;
    
    /**
     * Fixed periodic fee to be paid with any downpay term, normally monthly.
     */
    private Double pe_ff;
    
    /**
     * Fixed fee, cash withdrawal from own bank during opening hours
     */
    private Double cw_hb_bh_ff;
    
    /**
     * Fixed fee, cash withdrawal from own bank outside opening hours
     */
    private Double cw_hb_oh_ff;
    
    /**
     * Fixed fee, cash withdrawal from other bank during opening hours
     */
    private Double cw_ob_bh_ff;
    
    /**
     * Fixed fee, cash withdrawal from other bank outside opening hours
     */
    private Double cw_ob_oh_ff;
    
    /**
     * Percentage fee, domestic cash withdrawal as a percentage of the withdrawal
     */
    private Double cw_do_pf;
    
    /**
     * Percentage fee, cash withdrawal abroad as a percentage of the withdrawal (in addition to an eventual currency fee)
     */
    private Double cw_eu_pf;
    
    /**
     * Fixed fee, cash withdrawal abroad (fixed sum)
     */
    private Double cw_eu_ff;
    
    /**
     * Percentage exchange fee, cash withdrawal abroad (percentage)
     */
    private Double ex_eu_pf;
    
    /**
     * Fixed fee purchase, domestically
     */
    private Double pu_do_ff;
    
    /**
     * Fixed fee purchase abroad
     */
    private Double pu_eu_ff;
    
    /**
     * Minimum, monthly payment as a percentage of used credit
     */
    private Double minpay_perc;
    
    /**
     * Minimum, monthly payment in currency units
     */
    private Double minpay_units;
    
    
    
    
    public String getIssuer() {
        return issuer;
    }
    
    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
    
    public String getCardname() {
        return cardname;
    }
    
    public void setCardname(String cardname) {
        this.cardname = cardname;
    }
    
    /**
     * Fixed annual fee for holding the credit card, always paid in advance
     */
    public Double getAn_ff() {
        return an_ff;
    }
    
    public void setAn_ff(Double an_ff) {
        this.an_ff = an_ff;
    }
    
    /**
     * Percentage annual fee for holding the credit card, as percentage of the credit limit, paid in advance
     */
    public Double getAn_pf() {
        return an_pf;
    }
    
    public void setAn_pf(Double an_pf) {
        this.an_pf = an_pf;
    }
    
    /**
     * Nominal interest rate for purchases (for everything unless a separate rate for cash is provided)
     */
    public Double getNom_rate_purc() {
        return nom_rate_purc;
    }
    
    public void setNom_rate_purc(Double nom_rate_purc) {
        this.nom_rate_purc = nom_rate_purc;
    }
    
    /**
     * The scope of the provided interest rate - month ("mnd") or year ("aar")
     */
    public String getRateperiod_purc() {
        return rateperiod_purc;
    }
    
    public void setRateperiod_purc(String rateperiod_purc) {
        this.rateperiod_purc = rateperiod_purc;
    }
    
    /**
     * Maximum number of interest free days for purchases
     */
    public Integer getInterestfree_days() {
        return interestfree_days;
    }
    
    public void setInterestfree_days(Integer interestfree_days) {
        this.interestfree_days = interestfree_days;
    }
    
    /**
     * Fixed periodic fee to be paid with any downpay term, normally monthly.
     */
    public Double getPe_ff() {
        return pe_ff;
    }
    
    public void setPe_ff(Double pe_ff) {
        this.pe_ff = pe_ff;
    }
    
    /**
     * Fixed fee, cash withdrawal from own bank during opening hours
     */
    public Double getCw_hb_bh_ff() {
        return cw_hb_bh_ff;
    }
    
    public void setCw_hb_bh_ff(Double cw_hb_bh_ff) {
        this.cw_hb_bh_ff = cw_hb_bh_ff;
    }
    
    /**
     * Fixed fee, cash withdrawal from own bank outside opening hours
     */
    public Double getCw_hb_oh_ff() {
        return cw_hb_oh_ff;
    }
    
    public void setCw_hb_oh_ff(Double cw_hb_oh_ff) {
        this.cw_hb_oh_ff = cw_hb_oh_ff;
    }
    
    /**
     * Fixed fee, cash withdrawal from other bank during opening hours
     */
    public Double getCw_ob_bh_ff() {
        return cw_ob_bh_ff;
    }
    
    public void setCw_ob_bh_ff(Double cw_ob_bh_ff) {
        this.cw_ob_bh_ff = cw_ob_bh_ff;
    }
    
    /**
     * Fixed fee, cash withdrawal from other bank outside opening hours
     */
    public Double getCw_ob_oh_ff() {
        return cw_ob_oh_ff;
    }
    
    public void setCw_ob_oh_ff(Double cw_ob_oh_ff) {
        this.cw_ob_oh_ff = cw_ob_oh_ff;
    }
    
    /**
     * Percentage fee, domestic cash withdrawal as a percentage of the withdrawal
     */
    public Double getCw_do_pf() {
        return cw_do_pf;
    }
    
    public void setCw_do_pf(Double cw_do_pf) {
        this.cw_do_pf = cw_do_pf;
    }
    
    /**
     * Percentage fee, cash withdrawal abroad as a percentage of the withdrawal (in addition to an eventual currency fee)
     */
    public Double getCw_eu_pf() {
        return cw_eu_pf;
    }
    
    public void setCw_eu_pf(Double cw_eu_pf) {
        this.cw_eu_pf = cw_eu_pf;
    }
    
    /**
     * Fixed fee, cash withdrawal abroad (fixed sum)
     */
    public Double getCw_eu_ff() {
        return cw_eu_ff;
    }
    
    public void setCw_eu_ff(Double cw_eu_ff) {
        this.cw_eu_ff = cw_eu_ff;
    }
    
    /**
     * Percentage exchange fee, cash withdrawal abroad (percentage)
     */
    public Double getEx_eu_pf() {
        return ex_eu_pf;
    }
    
    public void setEx_eu_pf(Double ex_eu_pf) {
        this.ex_eu_pf = ex_eu_pf;
    }
    
    /**
     * Fixed fee purchase, domestically
     */
    public Double getPu_do_ff() {
        return pu_do_ff;
    }
    
    public void setPu_do_ff(Double pu_do_ff) {
        this.pu_do_ff = pu_do_ff;
    }
    
    /**
     * Fixed fee purchase abroad
     */
    public Double getPu_eu_ff() {
        return pu_eu_ff;
    }
    
    public void setPu_eu_ff(Double pu_eu_ff) {
        this.pu_eu_ff = pu_eu_ff;
    }
    
    /**
     * Minimum, monthly payment as a percentage of used credit
     */
    public Double getMinpay_perc() {
        return minpay_perc;
    }
    
    public void setMinpay_perc(Double minpay_perc) {
        this.minpay_perc = minpay_perc;
    }
    
    /**
     * Minimum, monthly payment in currency units
     */
    public Double getMinpay_units() {
        return minpay_units;
    }
    
    public void setMinpay_units(Double minpay_units) {
        this.minpay_units = minpay_units;
    }
    
}
