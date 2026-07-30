using Newtonsoft.Json;
using SalesSolutionWebAPI.Repository;
using System;
using System.Collections.Generic;
using System.Data;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;

namespace SalesSolutionWebAPI.Controllers
{
    public class ReportsController : ApiController
    {
        ReportsDAL aDal = new ReportsDAL();

        [HttpGet]
        [Route("api/Reports/GetProductStock")]
        public IHttpActionResult GetProductStock(int empId, string type)
        {
            DataTable dt = aDal.GetStockReport(empId, type);
            string JSONresult;
            JSONresult = JsonConvert.SerializeObject(dt);
            dynamic json = JsonConvert.DeserializeObject(JSONresult);
            return Json(json);
        }


        [HttpGet]
        [Route("api/Reports/GetSampleStockReport")]
        public IHttpActionResult GetSampleStock(int empId)
        {
            DataTable dt = aDal.GetSampleStockReport(empId);
            string JSONresult;
            JSONresult = JsonConvert.SerializeObject(dt);
            dynamic json = JsonConvert.DeserializeObject(JSONresult);
            return Json(json);
        }


        [HttpGet]
        [Route("api/Reports/GetCustomerPendingRejectList")]
        public IHttpActionResult GetCustomerPendingRejectList(int empId, string ApprovalStatus)
        {


            return Json(aDal.GetCustomerPendingRejectList(empId, ApprovalStatus));

        }

        [HttpGet]
        [Route("api/Reports/GetCustomerPendingRejectLists")]
        public IHttpActionResult GetCustomerPendingRejectLists(int empId)
        {


            return Json(aDal.GetCustomerPendingRejectLists(empId));

        }



        [HttpGet]
        [Route("api/Reports/GetCustomerUpdateInfo")]
        public IHttpActionResult GetCustomerUpdateInfo(int empId, string GroupId, string ZoneId, string AreaId, string TerritoryId, string SubTerritoryId, string MarketId)
        {


            return Json(aDal.GetCustomerUpdateInfoDAL(empId, GroupId, ZoneId, AreaId, TerritoryId, SubTerritoryId, MarketId));

        }


        [HttpGet]
        [Route("api/Reports/GetDWSPReport_Emp")]
        public IHttpActionResult GetDWSPReport_Emp(string MonthValue, string Year, string Role, string EmpId)
        {

            return Json(aDal.GetDWSPReport_Emp(MonthValue, Year, Role, EmpId));
        }


        [HttpGet]
        [Route("api/Reports/GetDoctorUpdateInfo")]
        public IHttpActionResult GetDoctorUpdateInfo(int empId, string GroupId, string ZoneId, string AreaId, string TerritoryId, string SubTerritoryId, string MarketId, string providertype, string pharmatype, string doctortype)
        {
            DataTable dt = aDal.GetDoctorUpdateInfoDAL(empId, GroupId, ZoneId, AreaId, TerritoryId, SubTerritoryId, MarketId, providertype, pharmatype, doctortype);
            string JSONresult;
            JSONresult = JsonConvert.SerializeObject(dt);
            dynamic json = JsonConvert.DeserializeObject(JSONresult);
            return Json(json);
        }




        [HttpGet]
        [Route("api/Reports/GetDoctorPendingReject")]
        public IHttpActionResult GetDoctorPendingReject(int empId, string ApprovalStatus)
        {
            DataTable dt = aDal.GetDoctorPendingReject(empId, ApprovalStatus);
            string JSONresult;
            JSONresult = JsonConvert.SerializeObject(dt);
            dynamic json = JsonConvert.DeserializeObject(JSONresult);
            return Json(json);
        }

        [HttpGet]
        [Route("api/Reports/GetDoctorPendingRejectS")]
        public IHttpActionResult GetDoctorPendingRejectS(int empId)
        {
            DataTable dt = aDal.GetDoctorPendingRejects(empId);
            string JSONresult;
            JSONresult = JsonConvert.SerializeObject(dt);
            dynamic json = JsonConvert.DeserializeObject(JSONresult);
            return Json(json);
        }

        //[HttpGet]
        //[Route("api/Reports/GetCampaingData")]
        //public IHttpActionResult GetCampaingData(int empId)
        //{
        //    DataTable dt = aDal.GetCampaingDaa(empId);
        //    string JSONresult;
        //    JSONresult = JsonConvert.SerializeObject(dt);
        //    dynamic json = JsonConvert.DeserializeObject(JSONresult);
        //    return Json(json);
        //}


        [HttpGet]
        [Route("api/Reports/GetCampaingData")]

        public IHttpActionResult GetCampaingData(int empId)
        {
            return Json(aDal.GetCampaingDaaNewDAL(empId));
        }


        [HttpGet]
        [Route("api/Reports/GetTargetvsAcchive")]
        public IHttpActionResult GetTargetvsAcchive(int empId, int month, int year)
        {
            DataTable dt = aDal.GetTargetvsAcchivementData(empId, month, year);
            string JSONresult;
            JSONresult = JsonConvert.SerializeObject(dt);
            dynamic json = JsonConvert.DeserializeObject(JSONresult);
            return Json(json);
        }

        [HttpGet]
        [Route("api/Reports/GetTadaList")]
        public IHttpActionResult GetTadaList(int userId)
        {
            DataTable dt = aDal.GetTadaList(userId);
            string JSONresult;
            JSONresult = JsonConvert.SerializeObject(dt);
            dynamic json = JsonConvert.DeserializeObject(JSONresult);
            return Json(json);
        }





        [HttpGet]
        [Route("api/Reports/GetCWSR")]
        public IHttpActionResult GetCustWiseOrderReport(string Role, string empId, string Month, string Year, string type, string ProviderType, string CustomerType)
        {

            return Json(aDal.GetCustWiseOrderReportDAL(Role, empId, Month, Year, type, ProviderType, CustomerType));

        }


        [HttpGet]
        [Route("api/Reports/GetEmployeeWiseTotalCount")]
        public IHttpActionResult GetEmployeeWiseTotalCount(string empId, string Date)
        {

            return Json(aDal.GetEmployeeWiseTotalCountDAL( empId, Date));

        }

        [HttpGet]
        [Route("api/Reports/GetPWSR")]
        public IHttpActionResult GetProductWiseOrderReport(string Role, string empId, string Month, string Year, string type, string ProviderType, string CustomerType)
        {

            return Json(aDal.GetProductWiseOrderReportDAL (Role, empId, Month,  Year, type, ProviderType, CustomerType));

        }

        [HttpGet]
        [Route("api/Reports/GetExpenseSummery")]
        public IHttpActionResult GetExpenseSummery(string Role, string empId, string Month, string Year)
        {
            //DataTable dt = aCusDal.GetOrderList_ForMIO(empId, FromDate, ToDate, AppStatus, GroupId, ZoneId, AreaId, TerritoryId, SubTerritoryId, MarketId);
            //string JSONresult;
            //JSONresult = JsonConvert.SerializeObject(dt);
            //dynamic json = JsonConvert.DeserializeObject(JSONresult);
            //return Json(json);


            return Json(aDal.GetExpenseSummeryDAL(Role, empId, Month, Year));
        }


        [HttpPost]
        [Route("api/Reports/ChangePassword")]

        public IHttpActionResult ChangePassword(string empId, string OldPass,   String NewPass)
        {
            return Json(aDal.ChangePasswordDAL(empId, OldPass, NewPass));
        }


        [System.Web.Http.HttpGet]
        [System.Web.Http.Route("api/Reports/GetOnOffButtonForCustomerChange")]
        public IHttpActionResult GetOnOffButtonForCustomerChange()
        {
            return Json(aDal.GetOnOffButtonForCustomerChangeDAL());

        }


        [HttpGet]
        [Route("api/Reports/GetTerritoryTargetvsAcchive")]
        public IHttpActionResult GetTerritoryTargetvsAcchive(int empId, string FromDate, string ToDate, string Role)
        {
            
            return Json(aDal.GetTerritoryTargetvsAcchive(empId, FromDate, ToDate, Role));
        }


    }
}
