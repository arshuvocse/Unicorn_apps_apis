using Microsoft.AspNetCore.Mvc;
using SalesSolutionWebAPI.Models;
using SalesSolutionWebAPI.Repository;
using System.Web.Http;


namespace SalesSolutionWebAPI.Controllers
{
    public class AttendanceController : ApiController
    {
        AttendanceDAL aDal = new AttendanceDAL();


        //[HttpPost]
        //public IHttpActionResult SavePunInInfo(Attendance aInfo)
        //{
        //    return Json(aDal.SavePunchIn(aInfo));

        //}

        //[HttpPost]
        //public IHttpActionResult SavePunOutInfo(Attendance aInfo)
        //{
        //    return Json(aDal.SavePunchOut(aInfo));

        //}

        [System.Web.Http.HttpPost]
        [System.Web.Http.Route("api/Attendance/SavePunch")]
     
        public IHttpActionResult SavePunchTotalInfo(Attendance aInfo)
        {
            return Json(aDal.SavePunchIn_PunchOut(aInfo));
             
        }

        [System.Web.Http.HttpPost]
        [System.Web.Http.Route("api/Attendance/GetPunchInOutInfo")]
        public IHttpActionResult GetPunchInOutInfo(int EmpInfoId)
        {
            return Json(aDal.GetInOutInfo(EmpInfoId));

        }

        [System.Web.Http.HttpGet]
        [System.Web.Http.Route("api/Attendance/GetShiftCategoryList")]
        public IHttpActionResult GetShiftCategoryList(int empId)
        {
            return Json(aDal.GetShiftCategoryList(empId));
        }

        [System.Web.Http.HttpPost]
        [System.Web.Http.Route("api/Attendance/SaveShiftCategoryList")]
        public IHttpActionResult SaveShiftCategoryList(EmpShiftCategoryRequest request)
        {
            return Json(aDal.SaveShiftCategoryList(request));
        }

        [System.Web.Http.HttpGet]
        [System.Web.Http.Route("api/Attendance/GetPunchWindow")]
        public IHttpActionResult GetPunchWindow(int empId)
        {
            return Json(aDal.GetAttendancePunchWindow(empId));
        }

        [System.Web.Http.HttpPost]
        [System.Web.Http.Route("api/Attendance/SavePunchWindow")]
        public IHttpActionResult SavePunchWindow(AttendancePunchWindowRequest request)
        {
            return Json(aDal.SaveAttendancePunchWindow(request));
        }

        [System.Web.Http.HttpPost]
        [System.Web.Http.Route("api/Attendance/RemovePunchWindow")]
        public IHttpActionResult RemovePunchWindow(AttendancePunchWindowRequest request)
        {
            return Json(aDal.RemoveAttendancePunchWindow(request));
        }

    }
}
