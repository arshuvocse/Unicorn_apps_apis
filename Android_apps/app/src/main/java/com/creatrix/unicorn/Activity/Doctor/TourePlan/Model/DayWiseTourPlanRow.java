package com.creatrix.unicorn.Activity.Doctor.TourePlan.Model;

/** One row of api/TourPlan/GetDayWiseTourPlan: Type "C" = customer, "D" = doctor (ChamberId 0 = doctor-wise). */
public class DayWiseTourPlanRow {
    public String TourPlanDate;
    public String Type;
    public int Id;
    public int ChamberId;
}
