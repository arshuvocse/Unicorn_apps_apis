package com.creatrix.unicorn.Activity.Doctor.TourePlan.Model;

import java.util.ArrayList;
import java.util.List;

/** Body of api/TourPlan/SaveDayWiseTourPlan (DayWiseTourPlanDTO). */
public class DayWiseTourPlanReq {
    public int EmpInfoId;
    public String TourPlanDate;
    public boolean IsFinalSubmit = true;
    public List<Integer> CustomerIds = new ArrayList<>();
    /** ChamberId 0 = doctor-wise (any chamber of the doctor). */
    public List<DoctorChamber> DoctorChambers = new ArrayList<>();

    public static class DoctorChamber {
        public int DoctorId;
        public int ChamberId;

        public DoctorChamber(int doctorId, int chamberId) {
            DoctorId = doctorId;
            ChamberId = chamberId;
        }
    }
}
