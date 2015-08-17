package com.company;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Kamil on 2015-08-07.
 */
public class Debt {
    public enum MODIFICATIONS {
        INCREASE_ADDITION,
        INCREASE_PERCENTAGE
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

    private double initialDebt;
    private List<CalcInfo> additionIncreases = new ArrayList<>();
    private List<CalcInfo> percentageIncreases = new ArrayList<>();

    public void setInitialDebt(double initialDebt) {
        this.initialDebt = initialDebt;
    }

    public void increaseBy(Date date, double increase, int recurrence) {
        this.additionIncreases.add(new CalcInfo(date, increase, recurrence, MODIFICATIONS.INCREASE_ADDITION));

        additionIncreases.sort((a, b) -> a.date.compareTo(b.date));
    }

    public void setDebtPercentage(Date startingDate, double percentageIncreases, int recurrence) {
        this.percentageIncreases.add(new CalcInfo(startingDate, percentageIncreases, recurrence, MODIFICATIONS.INCREASE_PERCENTAGE));

        this.percentageIncreases.sort((a, b) -> a.date.compareTo(b.date));
        removeRedundancy();
    }

    public void removeIncrease(CalcInfo c) {

        for (int i = 0; i < additionIncreases.size(); i++)
            if (c.date.compareTo(additionIncreases.get(i).date) == 0) {
                additionIncreases.remove(i);
                break;
            }

    }
    public void changePercentageOnce(CalcInfo c, double newPercentage) {
        setDebtPercentage(c.date, newPercentage, c.recurrence);
        setDebtPercentage(moveDateByNDays(c.date, c.recurrence), 10, c.recurrence);
    }

    public List<CalcInfo> calculateInSteps(Date limit) {
        List<CalcInfo> result = new ArrayList<>();
        if(percentageIncreases.size() != 0) {
            Date d = new Date(additionIncreases.size() > 0 ? Math.min(convDate(percentageIncreases.get(0).date).getTime(), convDate(additionIncreases.get(0).date).getTime()) : convDate(percentageIncreases.get(0).date).getTime());
            while (d.compareTo(limit) <= 0) {
                result.addAll(getPercentageIncreases(d));

                result.addAll(getAdditionIncreases(d));

                d = moveDateByNDays(d, 1);
            }
        } else {
            result.addAll(additionIncreases);
        }

        result.sort((a, b) -> a.date.compareTo(b.date));

        return result;
    }

    public double calculateDebt(Date limit) {
        List<CalcInfo> out = calculateInSteps(limit);
        double outf = initialDebt;

        for(CalcInfo cinfo : out)
            outf = cinfo.perform(outf);

        return outf;
    }

    private Date convDate(Date d) {
        return new Date(d.getTime() - d.getTime() % (1000 * 3600 * 24));
    }

    private boolean isTheSameDay(Date d1, Date d2) {
        return d1.getTime() / (1000 * 3600 * 24) == d2.getTime() / (1000 * 3600 * 24);
    }

    private void removeRedundancy() {
        for(int i = 0; i < percentageIncreases.size() - 1; i++)
            if(isRecurrent(percentageIncreases.get(i)) && isRecurrent(percentageIncreases.get(i + 1)) && isTheSameDay(percentageIncreases.get(i).date, percentageIncreases.get(i + 1).date))
                percentageIncreases.remove(i--);

        for(int i = 0; i < additionIncreases.size() - 1; i++)
            if(isRecurrent(additionIncreases.get(i)) && isRecurrent(additionIncreases.get(i + 1)) && isTheSameDay(additionIncreases.get(i).date, additionIncreases.get(i + 1).date))
                additionIncreases.remove(i--);
    }

    private List<CalcInfo> getAdditionIncreases(Date d) {
        return leaveOnlyLastRecurrence(getAppliableCalcInfo(d, additionIncreases));

    }

    private List<CalcInfo> getPercentageIncreases(Date d) {
        return leaveOnlyLastRecurrence(getAppliableCalcInfo(d, percentageIncreases));
    }

    private List<CalcInfo> leaveOnlyLastRecurrence(List<CalcInfo> in) {
        List<CalcInfo> ret = new ArrayList<>();
        boolean inside = false;

        for (int i = in.size() - 1; i >= 0; i--)
            if(isRecurrent(in.get(i)) && !inside)
            {
                ret.add(in.get(i));
                inside = true;
            } else if(!isRecurrent(in.get(i)))
                ret.add(in.get(i));

        return ret;
    }

    private List<CalcInfo> getAppliableCalcInfo(Date d, List<CalcInfo> list) {
        return list.stream().filter(c -> isAppliable(c, d)).map(c -> {
            if (isRecurrent(c)) return new CalcInfo(d, c.value, c.recurrence, c.modification);
            return c;
        }).collect(Collectors.toList());
    }

    private boolean isAppliable(CalcInfo c, Date now) {
        return doesNonRecurrentHappensNow(c, now) || isRecurrenceApplyConditionFulfilled(c, now);
    }

    private boolean doesNonRecurrentHappensNow(CalcInfo c, Date now) {
        return !isRecurrent(c) && isTheSameDay(now, c.date);
    }

    private boolean isRecurrenceApplyConditionFulfilled(CalcInfo c, Date now) {
        return isRecurrent(c) && convDate(now).getTime() >= convDate(c.date).getTime() && (convDate(now).getTime() - convDate(c.date).getTime()) % (1000 * 3600 * 24 * c.recurrence) == 0;
    }

    private boolean isRecurrent(CalcInfo c) {
        return c.recurrence != 0;
    }

    private Date moveDateByNDays(Date d, int n) {
        return new Date(d.getTime() + 1000 * 3600 * 24 * n);
    }
}
