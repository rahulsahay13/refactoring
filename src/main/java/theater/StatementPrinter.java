package theater;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

/**
 * Generates a formatted statement for a given invoice of performances.
 * Refactored per Tasks 2.1–2.4: helpers for amount, credits, USD formatting,
 * split loops for totals, and removed temporary variables.
 */
public class StatementPrinter {
    private final Invoice invoice;
    private final Map<String, Play> plays;

    public StatementPrinter(Invoice invoice, Map<String, Play> plays) {
        this.invoice = invoice;
        this.plays = plays;
    }

    /**
     * Returns the plaintext statement for the associated invoice.
     *
     * @return the formatted statement
     */
    public String statement() {
        final StringBuilder result =
                new StringBuilder("Statement for " + invoice.getCustomer() + System.lineSeparator());

        // loop #1: build per-performance lines only
        for (Performance performance : invoice.getPerformances()) {
            result.append(String.format(
                    "  %s: %s (%s seats)%n",
                    getPlay(performance).getName(),
                    usd(getAmount(performance)),
                    performance.getAudience()
            ));
        }

        // loop #2: total amount
        final int totalAmount = getTotalAmount();

        // loop #3: total volume credits
        final int totalCredits = getTotalVolumeCredits();

        result.append(String.format("Amount owed is %s%n", usd(totalAmount)));
        result.append(String.format("You earned %s credits%n", totalCredits));
        return result.toString();
    }

    // ---- Task 2.1: Extract switch → getAmount; getPlay helper; remove play param; inline temp ----

    private int getAmount(final Performance performance) {
        final Play play = getPlay(performance);
        final int audience = performance.getAudience();
        int result;

        switch (play.getType()) {
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
                throw new RuntimeException(String.format("unknown type: %s", play.getType()));
        }

        return result;
    }

    private Play getPlay(final Performance performance) {
        return plays.get(performance.getPlayID());
    }

    // ---- Task 2.2: Extract volume credits contribution per performance ----

    private int getVolumeCredits(final Performance performance) {
        int result = Math.max(performance.getAudience() - Constants.BASE_VOLUME_CREDIT_THRESHOLD, 0);
        if ("comedy".equals(getPlay(performance).getType())) {
            result += performance.getAudience() / Constants.COMEDY_EXTRA_VOLUME_FACTOR;
        }
        return result;
    }

    // ---- Task 2.3: Remove frmt variable; introduce usd(int cents) helper ----

    private String usd(final int amountInCents) {
        return NumberFormat.getCurrencyInstance(Locale.US)
                .format(((double) amountInCents) / Constants.PERCENT_FACTOR);
    }

    // ---- Task 2.4: Split loops and replace temps with queries ----

    private int getTotalAmount() {
        int result = 0;
        for (Performance performance : invoice.getPerformances()) {
            result += getAmount(performance);
        }
        return result;
    }

    private int getTotalVolumeCredits() {
        int result = 0;
        for (Performance performance : invoice.getPerformances()) {
            result += getVolumeCredits(performance);
        }
        return result;
    }
}
