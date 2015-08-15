import com.company.Debt;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by Kamil on 2015-08-07.
 */
public class DebtTest {
    Debt debt;
    double money;
    final double precision = 0.000001;

    @Before
    public void setUp() throws Exception {
        debt = new Debt();

        money = 1500;
        debt.setInitialDebt(money);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void DebtWithInitialValue_IsWorthInitialValue() {
        assertEquals(money, debt.calculateDebt(new Date()), precision);
    }

    @Test
    public void IncreasedDebt_IsWorthInitialValuePlusIncrease() {
        double increase = 500;

        debt.increaseNow(increase);

        assertEquals(money + increase, debt.calculateDebt(new Date()), precision);
    }

    @Test
    public void TenPercent_ResultsWithTenPercentOfDebtHigh() {
        debt.setDebtPercentage(new Date(), 10);
        debt.setRecurrence(new Date(), 5);

        assertEquals(money * 1.1, debt.calculateDebt(new Date()), precision);
        Date nextFiveDays = new Date();
        nextFiveDays.setTime(new Date().getTime() + 1000 * 3600 * 24 * 10);
        assertEquals(money * 1.21 * 1.1, debt.calculateDebt(nextFiveDays), precision);
    }

    @Test
    public void TenPercent_ResultsWithTenPercentOfDebtHighWithIncrease() {
        debt.increaseNow(100);
        debt.setDebtPercentage(new Date(), 10);
        debt.setRecurrence(new Date(), 5);

        assertEquals(money * 1.1 + 100, debt.calculateDebt(new Date()), precision);
        Date nextFiveDays = new Date();
        nextFiveDays.setTime(new Date().getTime() + 1000 * 3600 * 24 * 10);
        assertEquals((money * 1.1 + 100) * 1.21, debt.calculateDebt(nextFiveDays), precision);

        List<Debt.CalcInfo> out = debt.calculateInSteps(nextFiveDays);
    }

    @Test
    public void RemoveAddition_ResultsWithUnchangedDebt() {
        debt.increaseNow(100);
        debt.setDebtPercentage(new Date(), 10);
        debt.setRecurrence(new Date(), 5);

        assertEquals(money * 1.1 + 100, debt.calculateDebt(new Date()), precision);
        Date nextFiveDays = new Date();
        nextFiveDays.setTime(new Date().getTime() + 1000 * 3600 * 24 * 10);
        assertEquals((money * 1.1 + 100) * 1.21, debt.calculateDebt(nextFiveDays), precision);

        List<Debt.CalcInfo> out = debt.calculateInSteps(nextFiveDays);

        for(Debt.CalcInfo c : out)
            if(c.modification == Debt.MODIFICATIONS.INCREASE_ADDITION)
                debt.removeIncrease(c);

        assertEquals(money * 1.1 * 1.21, debt.calculateDebt(nextFiveDays), precision);
    }
}