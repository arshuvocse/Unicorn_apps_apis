package com.creatrix.unicorn.Activity.Reports.Model;

// One row of api/Reports/GetTourPlanReport (sp_Webapi_Get_TourPlanReport)
public class ModelTourPlanReport {
    int EmpInfoId;
    String EmpCode;
    String EmpName;
    String TourPlanDate;
    String VisitType;
    String Shift;
    String DoctorCustomer;
    String AreaName;
    String MarketName;
    String Purpose;
    String Objective;
    String ApprovalStatus;

    public int getEmpInfoId() {
        return EmpInfoId;
    }

    public String getEmpCode() {
        return EmpCode;
    }

    public String getEmpName() {
        return EmpName;
    }

    public String getTourPlanDate() {
        return TourPlanDate;
    }

    public String getVisitType() {
        return VisitType;
    }

    public String getShift() {
        return Shift;
    }

    public String getDoctorCustomer() {
        return DoctorCustomer;
    }

    public String getAreaName() {
        return AreaName;
    }

    public String getMarketName() {
        return MarketName;
    }

    public String getPurpose() {
        return Purpose;
    }

    public String getObjective() {
        return Objective;
    }

    public String getApprovalStatus() {
        return ApprovalStatus;
    }
}
