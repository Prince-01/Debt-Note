package com.company;

import java.util.Date;

public class Main {

    public static void main(String[] args) {
	// write your code here
        Debt t = new Debt();
        t.setInitialDebt(100);
        t.setDebtPercentage(new Date(), 10, 3);

        System.out.println(t.calculateDebt(new Date()));
        Date d = new Date((new Date()).getTime() + 1000 * 3600 * 24 * 3);
        Date d1 = new Date((new Date()).getTime() + 1000 * 3600 * 24 * 6);
        System.out.println(t.calculateDebt(d));
        t.increaseBy(d, -30, 0);
        System.out.println(t.calculateDebt(d));
        System.out.println(t.calculateDebt(d1));

        for(Debt.CalcInfo c : t.calculateInSteps(d1))
            System.out.println(c.date + ", " + c.modification + ": " + c.value);
    }
}
