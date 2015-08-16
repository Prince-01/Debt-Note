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

    private boolean isTheSameDay(Date d1, Date d2) {
        return d1.getTime() % (1000 * 3600 * 24) == d2.getTime() % (1000 * 3600 * 24);
    }

    private void removeRedundancy() {
        for(int i = 0; i < debtPercentage.size() - 1; i++)
            if(isTheSameDay(debtPercentage.get(i).getKey(), debtPercentage.get(i + 1).getKey()))
                debtPercentage.remove(i--);
        for(int i = 0; i < debtRecurrence.size() - 1; i++)
            if(isTheSameDay(debtRecurrence.get(i).getKey(), debtRecurrence.get(i + 1).getKey()))
                debtRecurrence.remove(i--);
    }

    public void setInitialDebt(double initialDebt) {
        this.initialDebt = initialDebt;
    }

    private List<CalcInfo> getIncreasesFromInterval(int from, int to) {
        List<CalcInfo> res = new ArrayList<>();

        for(;from < to; from++)
            res.add(new CalcInfo(increases.get(from).getKey(), increases.get(from).getValue(), MODIFICATIONS.INCREASE_ADDITION));

        return res;
    }

    private int getUpperIndexOfIncreaseBefore(int from, Date to) {
        while (from < increases.size() && to.compareTo(convDate(increases.get(from).getKey())) == 1)
            from++;
        return from;
    }

    private int getUpperIndexOfIncreaseAt(int from, Date to) {
        while (from < increases.size() && to.compareTo(convDate(increases.get(from).getKey())) == 1)
            from++;
        return from;
    }

    private List<CalcInfo> getRecurrenceFromInterval(int from, int to) {
        List<CalcInfo> res = new ArrayList<>();

        for(;from < to; from++)
            res.add(new CalcInfo(debtRecurrence.get(from).getKey(), debtRecurrence.get(from).getValue(), MODIFICATIONS.INCREASE_RECURRENCE));

        return res;
    }

    private int getUpperIndexOfRecurrence(int from, Date to) {
        while (from < debtRecurrence.size() - 1)
            if (!(to.compareTo(convDate(debtRecurrence.get(from).getKey())) >= 0 &&
                  to.compareTo(convDate(debtRecurrence.get(from + 1).getKey())) < 1))
                from++;
        return from;
    }

    private Date moveDateByNDats(Date d, int n) {
        return new Date(d.getTime() + 1000 * 3600 * 24 * n);
    }

    public List<CalcInfo> calculateInSteps(Date limit) {
        List<CalcInfo> result = new ArrayList<>();

        if(debtPercentage.size() != 0) {
            Date d = convDate(debtPercentage.get(0).getKey());
            int percCntg = 0;
            int incrCntg = 0;
            int recuCntg = 0;
            int from = 0;

            while (d.compareTo(limit) <= 0) {
                from = incrCntg;
                incrCntg = getUpperIndexOfIncreaseBefore(from, d);
                result.addAll(getIncreasesFromInterval(from, incrCntg));

                percCntg = giveCorrectPercentageIndex(d, percCntg);
                result.add(new CalcInfo(d, debtPercentage.get(percCntg).getValue(), MODIFICATIONS.INCREASE_PERCENTAGE));

                from = incrCntg;
                incrCntg = getUpperIndexOfIncreaseAt(from, d);
                result.addAll(getIncreasesFromInterval(from, incrCntg));

                from = recuCntg;
                recuCntg = getUpperIndexOfRecurrence(from, d);
                result.addAll(getRecurrenceFromInterval(from, recuCntg));

                d = moveDateByNDats(d, debtRecurrence.get(recuCntg).getValue());
            }

            from = incrCntg;
            incrCntg = Math.max(getUpperIndexOfIncreaseAt(incrCntg, limit), getUpperIndexOfIncreaseBefore(incrCntg, limit));
            result.addAll(getIncreasesFromInterval(from, incrCntg));
        } else {
            for(Pair<Date, Double> d : increases)
                result.add(new CalcInfo(d.getKey(), d.getValue(), MODIFICATIONS.INCREASE_ADDITION));
        }

        return result;
    }

    private int giveCorrectPercentageIndex(Date d, int percCntg) {
        while (percCntg < debtPercentage.size() - 1 && !(d.compareTo(convDate(debtPercentage.get(percCntg).getKey())) >= 0 && d.compareTo(convDate(debtPercentage.get(percCntg + 1).getKey())) < 1))
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

    public void increaseBy(Date date, double increase) {
        increases.add(new Pair<>(date, increase));


        increases.sort((a, b) -> a.getKey().compareTo(b.getKey()));
    }

    public void setDebtPercentage(Date startingDate, double debtPercentage) {
        this.debtPercentage.add(new Pair<>(startingDate, debtPercentage));

        this.debtPercentage.sort((a, b) -> a.getKey().compareTo(b.getKey()));
        removeRedundancy();
    }

    public void setRecurrence(Date date, int rec) {
        debtRecurrence.add(new Pair<>(date, rec));

        debtRecurrence.sort((a, b) -> a.getKey().compareTo(b.getKey()));
        removeRedundancy();
    }
}
