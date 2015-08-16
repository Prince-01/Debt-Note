package com.company;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Kamil on 2015-08-07.
 */
public class Debt {
    private double initialDebt;
    private List<CalcInfo> increases = new ArrayList<>();
    private List<CalcInfo> debtPercentage = new ArrayList<>();

    public void removeIncrease(CalcInfo c) {

        for (int i = 0; i < increases.size(); i++)
            if (c.date.compareTo(increases.get(i).date) == 0) {
                increases.remove(i);
                break;
            }

    }
    public void changePercentage(CalcInfo c, double newPercentage) {
        Date dayBefore = new Date(c.date.getTime() - 1000 * 3600 * 24);

        setDebtPercentage(dayBefore, newPercentage, c.recurrence);
    }

    public class CalcInfo {
        public Date date;
        public double value;
        public int recurrence;
        public MODIFICATIONS modification;

        public CalcInfo(Date d, double v, int r, MODIFICATIONS mod) {
            date = d;
            value = v;
            recurrence = r;
            modification = mod;
        }

        double perform(double in) {
            if(modification == MODIFICATIONS.INCREASE_ADDITION)
                return value + in;
            else if(modification == MODIFICATIONS.INCREASE_PERCENTAGE)
                return in * (1 + value / 100);
            return -1;
        }
    }

    public enum MODIFICATIONS {
        INCREASE_ADDITION,
        INCREASE_PERCENTAGE,
        INCREASE_RECURRENCE
    };

    private Date convDate(Date d) {
        return new Date(d.getTime() - d.getTime() % (1000 * 3600 * 24));
    }

    private boolean isTheSameDay(Date d1, Date d2) {
        return convDate(d1).getTime() / (1000 * 3600 * 24) == convDate(d2).getTime() / (1000 * 3600 * 24);
    }

    private void removeRedundancy() {
        for(int i = 0; i < debtPercentage.size() - 1; i++)
            if(isTheSameDay(debtPercentage.get(i).date, debtPercentage.get(i + 1).date))
                debtPercentage.remove(i--);
    }

    public void setInitialDebt(double initialDebt) {
        this.initialDebt = initialDebt;
    }

    private List<CalcInfo> getIncreases(Date d) {
        List<CalcInfo> res = new ArrayList<>();

        for(CalcInfo c : increases)
            if(isAppliable(c, d))
                res.add(c);
        return res;
    }

    private int getUpperIndexOfIncreaseBefore(int from, Date to) {
        while (from < increases.size() && to.compareTo(convDate(increases.get(from).date)) == 1)
            from++;
        return from;
    }

    private int getUpperIndexOfIncreaseAt(int from, Date to) {
        while (from < increases.size() && to.compareTo(convDate(increases.get(from).date)) == 0)
            from++;
        return from;
    }

    private boolean isAppliable(CalcInfo c, Date now) {
        return c.recurrence == 0 && isTheSameDay(now, c.date) || (c.recurrence != 0 && convDate(now).getTime() >= convDate(c.date).getTime() && (convDate(now).getTime() - convDate(c.date).getTime()) % (1000 * 3600 * 24 * c.recurrence) == 0);
    }

    private Date moveDateByNDays(Date d, int n) {
        return new Date(d.getTime() + 1000 * 3600 * 24 * n);
    }

    public List<CalcInfo> calculateInSteps(Date limit) {
        List<CalcInfo> result = new ArrayList<>();
        if(debtPercentage.size() != 0) {
            Date d = new Date(increases.size() > 0 ? Math.min(convDate(debtPercentage.get(0).date).getTime(), convDate(increases.get(0).date).getTime()) : convDate(debtPercentage.get(0).date).getTime());
            int percCntg = 0;
            while (d.compareTo(limit) <= 0) {
                percCntg = giveCorrectPercentageIndex(d, percCntg);
                if(isAppliable(debtPercentage.get(percCntg), d))
                    result.add(new CalcInfo(d, debtPercentage.get(percCntg).value, debtPercentage.get(percCntg).recurrence, MODIFICATIONS.INCREASE_PERCENTAGE));

                result.addAll(getIncreases(d));

                d = moveDateByNDays(d, 1);
            }
        } else {
            result.addAll(increases);
        }

        return result;
    }

    private int giveCorrectPercentageIndex(Date d, int percCntg) {
        while (percCntg < debtPercentage.size() - 1 && !(d.compareTo(convDate(debtPercentage.get(percCntg).date)) >= 0 && d.compareTo(convDate(debtPercentage.get(percCntg + 1).date)) < 1))
            percCntg++;
        return percCntg;
    }

    public double calculateDebt(Date limit) {
        List<CalcInfo> out = calculateInSteps(limit);
        double outf = initialDebt;

        for(CalcInfo cinfo : out)
            outf = cinfo.perform(outf);

        return outf;
    }

    public void increaseBy(Date date, double increase, int recurrence) {
        this.increases.add(new CalcInfo(date, increase, recurrence, MODIFICATIONS.INCREASE_ADDITION));

        increases.sort((a, b) -> a.date.compareTo(b.date));
    }

    public void setDebtPercentage(Date startingDate, double debtPercentage, int recurrence) {
        this.debtPercentage.add(new CalcInfo(startingDate, debtPercentage, recurrence, MODIFICATIONS.INCREASE_PERCENTAGE));

        this.debtPercentage.sort((a, b) -> a.date.compareTo(b.date));
        removeRedundancy();
    }
}
