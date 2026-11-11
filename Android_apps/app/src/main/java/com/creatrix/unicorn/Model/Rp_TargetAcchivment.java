package com.creatrix.unicorn.Model;

// One sub-territory row of api/Reports/GetTargetAchievement (target from tbl_Target_SubTerritoryMonthly).
public class Rp_TargetAcchivment {
    private String SubTerritoryCode;
    private String SubTerritoryName;
    private String TerritoryName;
    private double TargetAmt;
    private double SalesAmt;
    private double AchvPercent;
    private double BalanceAmt;

    public String getSubTerritoryCode() {
        return SubTerritoryCode;
    }

    public String getSubTerritoryName() {
        return SubTerritoryName;
    }

    public String getTerritoryName() {
        return TerritoryName;
    }

    public double getTargetAmt() {
        return TargetAmt;
    }

    public double getSalesAmt() {
        return SalesAmt;
    }

    public double getAchvPercent() {
        return AchvPercent;
    }

    public double getBalanceAmt() {
        return BalanceAmt;
    }
}
