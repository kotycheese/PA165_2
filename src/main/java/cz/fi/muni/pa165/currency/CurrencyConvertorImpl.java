package cz.fi.muni.pa165.currency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Currency;


/**
 * This is base implementation of {@link CurrencyConvertor}.
 *
 * @author petr.adamek@embedit.cz
 */
public class CurrencyConvertorImpl implements CurrencyConvertor {

    private final ExchangeRateTable exchangeRateTable;
    //private final Logger logger = LoggerFactory.getLogger(CurrencyConvertorImpl.class);

    public CurrencyConvertorImpl(ExchangeRateTable exchangeRateTable) {
        this.exchangeRateTable = exchangeRateTable;
    }

    @Override
    public BigDecimal convert(Currency sourceCurrency, Currency targetCurrency, BigDecimal sourceAmount) {
        Logger logger = LoggerFactory.getLogger(this.getClass());
        logger.trace(String.format("Using CurrencyConvertorImpl.convert(%s, %s, %s)", sourceCurrency, targetCurrency, sourceAmount));
        if(sourceCurrency == null)
            throw new IllegalArgumentException("sourceCurrency must not be null.");
        if(targetCurrency == null)
            throw new IllegalArgumentException("targetCurrency must not be null.");
        if(sourceAmount == null)
            throw new IllegalArgumentException("sourceAmount must not be null.");

        BigDecimal rate;
        try {
            rate = exchangeRateTable.getExchangeRate(sourceCurrency, targetCurrency);
        } catch (ExternalServiceFailureException e) {
            logger.error(String.format("ExchangeRateTable.getExchangeRate(%s, %s) failed with message: \"%s\"", sourceCurrency, targetCurrency, e.getMessage()));
            throw new UnknownExchangeRateException(e.getMessage(), e);
        }

        if(rate == null) {
            logger.warn(String.format("Exchange rate from %s to %s was not found.", sourceCurrency, targetCurrency));
            throw new UnknownExchangeRateException("Exchange rate not found.");
        }

        return rate.multiply(sourceAmount).setScale(2, RoundingMode.HALF_EVEN);
    }

}
