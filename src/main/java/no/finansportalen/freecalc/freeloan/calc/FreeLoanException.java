package no.finansportalen.freecalc.freeloan.calc;

public class FreeLoanException extends Exception {

    private static final long serialVersionUID = 1L;
    
    FreeLoanExceptionType type;

    public FreeLoanException(FreeLoanExceptionType type) {
        super(type.message());
        this.type = type;
    }
    
    public FreeLoanException(String missingFieldName) {
        super(FreeLoanExceptionType.PARAMETER_MISSING.message() + missingFieldName);
        this.type = FreeLoanExceptionType.PARAMETER_MISSING;
    }
    
    public int getErrNum() {
        return type.errNum();
    }
    
    public FreeLoanExceptionType getType() {
        return type;
    }





    public static enum FreeLoanExceptionType {
        
        INTEREST_PERIOD_TOO_LONG(-1, "Request for longer interest-only period than the bank offers"),
        FIRST_SEGMENT_NOT_DEFINED(-2, "First loan segment not defined"),
        BALLOON_TOO_SMALL(-3, "Balloon smaller than smallest loan offered"),
        BALLOON_TOO_BIG(-4, "Balloon bigger than biggest loan offered"),
        NO_SEGMENT_FOUND(-5, "No segment found (normally because the requested loan amount is too small or too big)"),
        FAILING_CONVERGENCE(-6, "Failing convergence at zero periods or -100% nominal interest rate"),
        UNSUPPORTED_COMBINATION_ADVANCE(-7, "Freeloan does not support the combination of separate, concurrent interest rate segments and annuities in advance"),
        PAYMENT_TOO_SMALL(-8, "The chosen periodic payment is too small to cover the interest on the loan"),
        UNSUPPORTED_COMBINATION_PERIODIC(-9, "The combination of separate, concurrent interest rate segments and user chosen periodic payment is not supported"),
        EFFECTIVE_RATE_WAS_NAN(-10, "After the calculations effective interest rate was NaN"),
        ANNUITY_FALL_BELOW_MIN_PAYMENT(-11, "With the chosen payback time, the annuity will fall below the required minimum payment"),
        PARAMETER_MISSING(-12, "Parameter missing: ");
        
        private final String message;
        private final int errNum;
        
        private FreeLoanExceptionType(int errNum, String message) {
            this.message = message;
            this.errNum = errNum;
        }

        public String message() {
            return message;
        }

        public int errNum() {
            return errNum;
        };
        
        
    }
}
