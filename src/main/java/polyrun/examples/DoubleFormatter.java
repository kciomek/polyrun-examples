package polyrun.examples;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class DoubleFormatter {

    private final DecimalFormat decimalFormat;

    public DoubleFormatter() {
        this.decimalFormat = new DecimalFormat("#.##");
        decimalFormat.setRoundingMode(RoundingMode.HALF_UP);
    }

    public String format(double value) {
        return decimalFormat.format(value);
    }

    public String format(double[] vector) {
        StringBuilder sb = new StringBuilder();

        for (double value : vector) {
            sb.append(format(value)).append("\t");
        }

        return sb.deleteCharAt(sb.length() - 1).toString();
    }
}
