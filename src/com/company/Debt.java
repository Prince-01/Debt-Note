package com.company;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Kamil on 2015-08-07.
 */
public class Debt {
    private double initialDebt;
    private List<Pair<Date, Double>> increases = new ArrayList<>();
    private List<Pair<Date, Double>> debtPercentage = new ArrayList<>();
    private List<Pair<Date, Integer>> debtRecurrence = new ArrayList<>();

    public void removeIncrease(CalcInfo c) {

        for (int i = 0; i < increases.size(); i++)
            if (c.date.compareTo(increases.get(i).getKey()) == 0) {
                increases.remove(i);
                break;
            }

    }
    public void changePercentage(CalcInfo c, double newPercentage) {
        Date dayBefore = new Date(c.date.getTime() - 1000 * 3600 * 24);

        setDebtPercentage(dayBefore, newPercentage);
    }

    public class CalcInfo {
        public Date date;
        public double value;
        public MODIFICATIONS modification;

        public CalcInfo(Date d, double v, MODIFICATIONS mod) {
        date = d;
        value = v;
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


    public void setInitialDebt(double initialDebt) {
        this.initialDebt = initialDebt;
    }

    public List<CalcInfo> calculateInSteps(Date limit) {
        List<CalcInfo> result = new ArrayList<>();

        if(debtPercentage.size() != 0) {
            Date d = convDate(debtPercentage.get(0).getKey());
            int percCntg = 0;
            int incrCntg = 0;
            int recuCntg = 0;

            while (d.compareTo(limit) <= 0) {
                while (incrCntg < increases.size() && d.compareTo(convDate(increases.get(incrCntg).getKey())) == 1)
                    result.add(new CalcInfo(increases.get(incrCntg).getKey(), increases.get(incrCntg++).getValue(), MODIFICATIONS.INCREASE_ADDITION));
                while (percCntg < debtPercentage.size() - 1 && !(d.compareTo(convDate(debtPercentage.get(percCntg).getKey())) >= 0 && d.compareTo(convDate(debtPercentage.get(percCntg + 1).getKey())) < 1))
                    percCntg++;
                result.add(new CalcInfo(d, debtPercentage.get(percCntg).getValue(), MODIFICATIONS.INCREASE_PERCENTAGE));
                while (incrCntg < increases.size() && d.compareTo(convDate(increases.get(incrCntg).getKey())) == 0)
                    result.add(new CalcInfo(increases.get(incrCntg).getKey(), increases.get(incrCntg++).getValue(), MODIFICATIONS.INCREASE_ADDITION));

                while (recuCntg < debtRecurrence.size() - 1)
                    if (!(d.compareTo(convDate(debtRecurrence.get(recuCntg).getKey())) >= 0 && d.compareTo(convDate(debtRecurrence.get(recuCntg + 1).getKey())) < 1))
                        result.add(new CalcInfo(debtRecurrence.get(recuCntg).getKey(), debtRecurrence.get(recuCntg++).getValue(), MODIFICATIONS.INCREASE_RECURRENCE));

                d = new Date(d.getTime() + 1000 * 3600 * 24 * debtRecurrence.get(recuCntg).getValue());
            }
        } else {
            for(Pair<Date, Double> d : increases)
                result.add(new CalcInfo(d.getKey(), d.getValue(), MODIFICATIONS.INCREASE_ADDITION));
        }

        return result;
    }

    public double calculateDebt(Date limit) {
        List<CalcInfo> out = calculateInSteps(limit);
        double outf = initialDebt;

        for(CalcInfo cinfo : out)
            outf = cinfo.perform(outf);

        return outf;
    }

    public void increaseNow(Date date, double increase) {
        increases.add(new Pair<>(date, increase));


        increases.sort((a, b) -> a.getKey().compareTo(b.getKey()));
    }

    public void setDebtPercentage(Date startingDate, double debtPercentage) {
        this.debtPercentage.add(new Pair<>(startingDate, debtPercentage));

        this.debtPercentage.sort((a, b) -> a.getKey().compareTo(b.getKey()));
    }

    public void setRecurrence(Date date, int rec) {
        debtRecurrence.add(new Pair<>(date, rec));

        debtRecurrence.sort((a, b) -> a.getKey().compareTo(b.getKey()));
    }
}
