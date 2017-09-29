package cz.fi.muni.pa165.currency;

import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.Assert.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CurrencyConvertorImplTest {
    private CurrencyConvertor convertor;
    private ExchangeRateTable table;
    private Currency source = Currency.getInstance("EUR");
    private Currency target = Currency.getInstance("CZK");

    @Before
    public void init() throws ExternalServiceFailureException {
        table = mock(ExchangeRateTable.class);
        when(table.getExchangeRate(source, target)).thenReturn(new BigDecimal("0.001"));
        convertor = new CurrencyConvertorImpl(table);
    }

    @Test
    public void testConvert() {
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(convertor.convert(source, target, new BigDecimal(5))).isEqualTo(new BigDecimal("0.00"));
        softly.assertThat(convertor.convert(source, target, new BigDecimal(15))).isEqualTo(new BigDecimal("0.02"));
        softly.assertThat(convertor.convert(source, target, new BigDecimal("4.9"))).isEqualTo(new BigDecimal("0.00"));
        softly.assertThat(convertor.convert(source, target, new BigDecimal("14.9"))).isEqualTo(new BigDecimal("0.01"));

        softly.assertAll();
    }

    @Test
    public void testConvertWithNullSourceCurrency() {
        assertThatThrownBy(() -> convertor.convert(null, target, BigDecimal.ONE))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testConvertWithNullTargetCurrency() {
        assertThatThrownBy(() -> convertor.convert(source, null, BigDecimal.ONE))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testConvertWithNullSourceAmount() {
        assertThatThrownBy(() -> convertor.convert(source, target, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testConvertWithUnknownCurrency() throws ExternalServiceFailureException {
        when(table.getExchangeRate(source, target)).thenReturn(null);
        assertThatThrownBy(() -> convertor.convert(source, target, BigDecimal.ONE))
                .isInstanceOf(UnknownExchangeRateException.class);
    }

    @Test
    public void testConvertWithExternalServiceFailure() throws ExternalServiceFailureException {
        doThrow(ExternalServiceFailureException.class)
                .when(table).getExchangeRate(source, target);
        assertThatThrownBy(() -> convertor.convert(source, target, BigDecimal.ONE))
                .isInstanceOf(UnknownExchangeRateException.class);
    }

}
