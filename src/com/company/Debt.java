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

    private Date convDate(Date d) {
        Date ret = new Date(d.getTime());

        ret.setSeconds(0);
        ret.setMinutes(0);
        ret.setHours(0);

        return ret;
    }


    public void setInitialDebt(double initialDebt) {
        this.initialDebt = initialDebt;
    }

    public double calculateDebt(Date limit) {
        double result = initialDebt;

        if(debtPercentage.size() != 0) {
            Date d = convDate(debtPercentage.get(0).getKey());
            int percCntg = 0;
            int incrCntg = 0;
            int recuCntg = 0;

            while (d.compareTo(limit) <= 0) {
                while (incrCntg < increases.size() && d.compareTo(convDate(increases.get(incrCntg).getKey())) == 1)
                    result += increases.get(incrCntg++).getValue();
                if (percCntg < debtPercentage.size() - 1) {
                    if (d.compareTo(convDate(debtPercentage.get(percCntg).getKey())) >= 0 && d.compareTo(convDate(debtPercentage.get(percCntg + 1).getKey())) < 1)
                        result *= (1 + debtPercentage.get(percCntg).getValue() / 100);
                    else
                        result *= (1 + debtPercentage.get(++percCntg).getValue() / 100);
                } else {
                    result *= (1 + debtPercentage.get(percCntg).getValue() / 100);
                }
                while (incrCntg < increases.size() && d.compareTo(convDate(increases.get(incrCntg).getKey())) == 0)
                    result += increases.get(incrCntg++).getValue();

                while (recuCntg < debtRecurrence.size() - 1)
                    if (!(d.compareTo(convDate(debtRecurrence.get(recuCntg).getKey())) >= 0 && d.compareTo(convDate(debtRecurrence.get(recuCntg + 1).getKey())) < 1))
                        recuCntg++;

                d.setTime(d.getTime() + 1000 * 3600 * 24 * debtRecurrence.get(recuCntg).getValue());
            }
        } else {
            for(Pair<Date, Double> d : increases)
                result += d.getValue();
        }

        return result;
    }

    public void increaseNow(double increase) {
        increases.add(new Pair<>(new Date(), increase));
    }

    public void setDebtPercentage(Date startingDate, double debtPercentage) {
        this.debtPercentage.add(new Pair<>(startingDate, debtPercentage));
    }

    public void setRecurrence(Date date, int rec) {
        debtRecurrence.add(new Pair<>(date, rec));
    }
}
