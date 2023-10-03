package fraj.lab.recruit.test2.atm;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ATMTest {
    @Mock
    PaymentProcessor paymentProcessor;
    @Mock
    AmountSelector amountSelector;
    @Mock
    CashManager cashManager;
    @InjectMocks
    ATM atm;

    @Test
    public void shouldNotRetrieveCashWhenAmountIsNegative() {
        assertThrows(ATMTechnicalException.class, () -> {
            when(amountSelector.selectAmount()).thenAnswer(i -> -50);

            atm.runCashWithdrawal();
        });
    }

    @Test
    public void shouldNotRetrieveCashWhenAmountIsZero() {
        assertThrows(ATMTechnicalException.class, () -> {
            when(amountSelector.selectAmount()).thenAnswer(i -> 0);

            atm.runCashWithdrawal();
        });
    }

    @Test
    public void shouldNotRetrieveCashWhenCashManagerCantDeliverCash() throws ATMTechnicalException {
        when(amountSelector.selectAmount()).thenReturn(50);
        when(cashManager.canDeliver(any(Integer.class))).thenReturn(false);

        ATMStatus result = atm.runCashWithdrawal();
        assertEquals(ATMStatus.CASH_NOT_AVAILABLE, result);
    }

    @Test
    public void shouldNotRetrieveCashWhenPaymentFails() throws ATMTechnicalException {
        when(amountSelector.selectAmount()).thenReturn(50);
        when(cashManager.canDeliver(any(Integer.class))).thenReturn(true);
        when(paymentProcessor.pay(any(Integer.class))).thenReturn(PaymentStatus.FAILURE);

        ATMStatus result = atm.runCashWithdrawal();
        assertEquals(ATMStatus.PAYMENT_REJECTED, result);
    }

    @Test
    public void shouldRetrieveCash() throws ATMTechnicalException {
        when(amountSelector.selectAmount()).thenReturn(50);
        when(cashManager.canDeliver(any(Integer.class))).thenReturn(true);
        when(paymentProcessor.pay(any(Integer.class))).thenReturn(PaymentStatus.SUCCESS);

        ATMStatus result = atm.runCashWithdrawal();
        verify(cashManager, times(1)).deliver(any(Integer.class));
        assertEquals(ATMStatus.DONE, result);
    }
}
