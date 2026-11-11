using System;

namespace SalesSolutionWebAPI.Models
{
    public class Attendance
    {
        public int AttendanceId { get; set; }

        //public int MIOId { get; set; }

        public int EmpInfoId { get; set; }

        public String PunchInTime { get; set; }

        public string PInLat { get; set; }

        public string PInLog { get; set; }

        //public String PunchOutTime { get; set; }

        //public string POutLat { get; set; }

        //public string POutLong { get; set; }

        public DateTime EntryDate { get; set; }

        public string POutRemarks { get; set; }

        public DateTime AttendanceDate { get; set; }

        public int AttType { get; set; }
        public int? ShiftId { get; set; }
        public String AttAddress { get; set; }
        public String AttImg { get; set; }

    }

    public class EmpShiftCategoryRequest
    {
        public int EmpId { get; set; }
        public string ShiftCategory { get; set; }
    }

    public class AttendancePunchWindowRequest
    {
        public int EmpId { get; set; }
        public string PunchInFrom { get; set; } // HH:mm
        public string PunchInTo { get; set; }   // HH:mm
        public int? EntryBy { get; set; }
    }

}
