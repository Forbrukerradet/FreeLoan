package no.finansportalen.freecalc.common;


public class Utils {

    public static enum RoundDirection {
        NORMAL, UP, DOWN
    }

    public static enum Accuracy {
        FAST, NORMAL, EXTREMELY_ACCURATE
    }

    public static AnnuityLoanPeriod[] copyArray(AnnuityLoanPeriod[] origin, int length) {
        int howMatchToTakeFromOrigin = origin == null ? 0 : Math.min(origin.length, length);
        AnnuityLoanPeriod[] res = new AnnuityLoanPeriod[length];
        for(int i = 0; i < howMatchToTakeFromOrigin; i++) {
            res[i] = origin[i];
        }
        return res;
    }

    /**
     * 
     * Rounds 'nummber' according to the parameters 'presision' and 'direction':
     * 
     * @param number number to round
     * @param direction NORMAL: Normal rounding rules apply UP: Rounds up DOWN: Rounds down
     * @param roundToInteger false: Rounds to two decimals 1: Rounds to integer
     */
    public static double roundoff(double number, RoundDirection direction, boolean roundToInteger) {
    
        double roundfact;
    
        if (!roundToInteger) {
            roundfact = 100;
        } else {
            roundfact = 1;
        }
    
        if (direction == RoundDirection.UP) {
            return Math.ceil(number * roundfact) / roundfact;
        } else if (direction == RoundDirection.DOWN) {
            return Math.floor(number * roundfact) / roundfact;
        } else {
            return Math.round(number * roundfact) / roundfact;
        }
    
    }
    
    

}
