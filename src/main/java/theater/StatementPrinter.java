package theater;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

/**
 * This class generates a statement for a given invoice of performances.
 */
public class StatementPrinter {
    private final Invoice invoice;
    private final Map<String, Play> plays;

    public StatementPrinter(Invoice invoice, Map<String, Play> plays) {
        this.invoice = invoice;
        this.plays = plays;
    }

    /**
     * Returns a formatted statement of the invoice associated with this printer.
     * @return the formatted statement
     * @throws RuntimeException if one of the play types is not known
     */
    public String statement() {
        final StringBuilder result = new StringBuilder("Statement for "
                + invoice.getCustomer() + System.lineSeparator());

        for (final Performance performance : invoice.getPerformances()) {
            // print line for this order
            result.append(String.format("  %s: %s (%s seats)%n",
                    getPlay(performance).getName(),
                    usd(getAmount(performance) / Constants.PERCENT_FACTOR),
                    performance.getAudience()));
        }

        result.append(String.format("Amount owed is %s%n",
                usd(totalAmount() / Constants.PERCENT_FACTOR)));
        result.append(String.format("You earned %s credits%n", totalVolumeCredits()));
        return result.toString();
    }

    private int totalAmount() {
        int result = 0;
        for (final Performance performance : invoice.getPerformances()) {
            result += getAmount(performance);
        }
        return result;
    }

    private int totalVolumeCredits() {
        int result = 0;
        for (final Performance performance : invoice.getPerformances()) {
            result += getVolumeCredits(performance);
        }
        return result;
    }

    /**
     * Calculates the amount owed for a given performance based on its play type.
     * @param performance the performance to calculate the amount for
     * @return the amount owed for the performance
     * @throws RuntimeException if the play type is not recognized
     */
    private int getAmount(final Performance performance) {
        final String type = getPlay(performance).getType();
        final int audience = performance.getAudience();
        int result = 0;
        switch (type) {
            case "tragedy":
                result = Constants.TRAGEDY_BASE_AMOUNT;
                if (audience > Constants.TRAGEDY_AUDIENCE_THRESHOLD) {
                    result += Constants.TRAGEDY_OVER_BASE_CAPACITY_PER_PERSON
                            * (audience - Constants.TRAGEDY_AUDIENCE_THRESHOLD);
                }
                break;
            case "comedy":
                result = Constants.COMEDY_BASE_AMOUNT;
                if (audience > Constants.COMEDY_AUDIENCE_THRESHOLD) {
                    result += Constants.COMEDY_OVER_BASE_CAPACITY_AMOUNT
                            + (Constants.COMEDY_OVER_BASE_CAPACITY_PER_PERSON
                            * (audience - Constants.COMEDY_AUDIENCE_THRESHOLD));
                }
                result += Constants.COMEDY_AMOUNT_PER_AUDIENCE * audience;
                break;
            default:
                throw new RuntimeException(String.format("unknown type: %s", type));
        }
        return result;
    }

    private String usd(final int aNumber) {
        final NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
        return format.format(aNumber);
    }

    private Play getPlay(final Performance aPerformance) {
        return plays.get(aPerformance.getPlayID());
    }

    /**
     * Calculates the volume credits for a given performance.
     * @param performance the performance to calculate volume credits for
     * @return the volume credits contribution from this performance
     */
    private int getVolumeCredits(final Performance performance) {
        int result = 0;
        result += Math.max(performance.getAudience()
                - Constants.BASE_VOLUME_CREDIT_THRESHOLD, 0);
        final Play play = getPlay(performance);
        if ("comedy".equals(play.getType())) {
            result += performance.getAudience() / Constants.COMEDY_EXTRA_VOLUME_FACTOR;
        }
        return result;
    }
}
