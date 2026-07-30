using Newtonsoft.Json;
using SalesSolutionWebAPI.Models;
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
    public class TourPlanController : ApiController
    {
        TourPlanDAL aDal = new TourPlanDAL();
        [HttpGet]
        [Route("api/TourPlan/GetTerritory")]
        public IHttpActionResult Get_TerritoryByEmpId(int empId)
        {
            DataTable dt = aDal.Get_TerritoryByEmpId(empId);
            string JSONresult;
            JSONresult = JsonConvert.SerializeObject(dt);
            dynamic json = JsonConvert.DeserializeObject(JSONresult);
            return Json(json);
        }


        [HttpGet]
        [Route("api/TourPlan/GetMarket")]
        public IHttpActionResult Get_marketByTerritoryId(int territoryId)
        {
            DataTable dt = aDal.Get_marketByTerritoryId(territoryId);
            string JSONresult;
            JSONresult = JsonConvert.SerializeObject(dt);
            dynamic json = JsonConvert.DeserializeObject(JSONresult);
            return Json(json);
        }

        [HttpGet]
        [Route("api/TourPlan/GetSubmarket")]
        public IHttpActionResult Get_SubmarketByTerritoryId(int marketId)
        {
            DataTable dt = aDal.Get_SubmarketByTerritoryId(marketId);
            string JSONresult;
            JSONresult = JsonConvert.SerializeObject(dt);
            dynamic json = JsonConvert.DeserializeObject(JSONresult);
            return Json(json);
        }

        [HttpGet]
        [Route("api/TourPlan/GetCustomer")]
        public IHttpActionResult Get_CustomerByMarketId(int marketId)
        {
            DataTable dt = aDal.Get_CustomerByMarketId(marketId);
            string JSONresult;
            JSONresult = JsonConvert.SerializeObject(dt);
            dynamic json = JsonConvert.DeserializeObject(JSONresult);
            return Json(json);
        }

        [HttpGet]
        [Route("api/TourPlan/GetShift")]
        public IHttpActionResult Get_Shift(int empId)
        {
            DataTable dt = aDal.Get_Shift(empId);
            string JSONresult;
            JSONresult = JsonConvert.SerializeObject(dt);
            dynamic json = JsonConvert.DeserializeObject(JSONresult);
            return Json(json);
        }

        [HttpGet]
        [Route("api/TourPlan/GetType")]
        public IHttpActionResult Get_TourPlanType(int empId)
        {
            DataTable dt = aDal.Get_TourPlanType(empId);
            string JSONresult;
            JSONresult = JsonConvert.SerializeObject(dt);
            dynamic json = JsonConvert.DeserializeObject(JSONresult);
            return Json(json);
        }

        [HttpGet]
        [Route("api/TourPlan/GetPurpose")]
        public IHttpActionResult Get_TourPlanPurpose(int empId)
        {
            DataTable dt = aDal.Get_TourPlanPurpose(empId);
            string JSONresult;
            JSONresult = JsonConvert.SerializeObject(dt);
            dynamic json = JsonConvert.DeserializeObject(JSONresult);
            return Json(json);
        }

        [HttpGet]
        [Route("api/TourPlan/GetPurposeForMarketVisit")]
        public IHttpActionResult Get_TourPlanPurposeForMarketVisit(int empId)
        {
            DataTable dt = aDal.Get_TourPlanPurposeForMarketVisit(empId);
            string JSONresult;
            JSONresult = JsonConvert.SerializeObject(dt);
            dynamic json = JsonConvert.DeserializeObject(JSONresult);
            return Json(json);
        }

        [HttpGet]
        [Route("api/TourPlan/GetPurposeForOtherVisit")]
        public IHttpActionResult Get_TourPlanPurposeForOtherVisit(int empId)
        {
            DataTable dt = aDal.Get_TourPlanPurposeForOtherVisit(empId);
            string JSONresult;
            JSONresult = JsonConvert.SerializeObject(dt);
            dynamic json = JsonConvert.DeserializeObject(JSONresult);
            return Json(json);
        }

        [HttpPost]
        
        [Route("api/TourPlan/SaveTourPlanList")]
        public IHttpActionResult AddTourPlan(TourPlanInfoMaster ainfo)
        {
            return Json(aDal.AddTourPlan(ainfo));
        }

        

        [HttpPost]
        
        [Route("api/TourPlan/SaveTourPlanList_vThree")]
        public IHttpActionResult SaveTourPlanList_vThree(TourPlanInfoMasterDAO ainfo)
        {
            return Json(aDal.SaveTourPlanList_vThree(ainfo));
        }



        [HttpPost]
        
        [Route("api/TourPlan/SaveTourPlanListNew")]
        public IHttpActionResult AddTourPlanNew(TourPlanInfoMaster ainfo)
        {
            return Json(aDal.AddTourPlanNew(ainfo));
        }

        [HttpGet]
        [Route("api/TourPlan/GetTourPlanData")]
        public IHttpActionResult Get_TourPlanData(int month,int year,int empId)
        {
            //return Json(aDal.Get_TourPlanData(month, year, empId));
            DataTable dt = aDal.Get_TourPlanData(month, year, empId);
            string JSONresult;
            JSONresult = JsonConvert.SerializeObject(dt);
            dynamic json = JsonConvert.DeserializeObject(JSONresult);
            return Json(json);
        }

        [HttpGet]
        [Route("api/TourPlan/GetTourPlanData_new")]
        public IHttpActionResult Get_TourPlanData_new(int month, int year, int empId)
        {
            DataTable dt = aDal.Get_TourPlanData_New(month, year, empId);
            string JSONresult;
            JSONresult = JsonConvert.SerializeObject(dt);
            dynamic json = JsonConvert.DeserializeObject(JSONresult);
            return Json(json);
        }

        [HttpGet]
        [Route("api/TourPlan/GetTourPlanForWorkedwith")]
        public IHttpActionResult GetTourPlanForWorkedwith(int empId, string tDate)
        {
            DataTable dt = aDal.GetTourPlanForWorkedwith(empId, tDate);
            if (dt == null || dt.Rows.Count == 0)
            {
                return Json(new { });
            }

            string JSONresult;
            JSONresult = JsonConvert.SerializeObject(dt);
            dynamic json = JsonConvert.DeserializeObject(JSONresult);
            return Json(json[0]);
        }

        [HttpPost]
        [Route("api/TourPlan/ValidateTimeInRange")]
        public IHttpActionResult ValidateTimeInRange(string MainTime, string ShiftInfo)
        {
            try
            {
                if (string.IsNullOrEmpty(MainTime) || string.IsNullOrEmpty(ShiftInfo))
                {
                    return BadRequest("MainTime or ShiftInfo cannot be null or empty.");
                }

                DataTable dt = aDal.ValidateTimeInRange(MainTime, ShiftInfo);
                if (dt != null && dt.Rows.Count > 0)
                {
                    bool isValid = Convert.ToBoolean(dt.Rows[0]["IsValid"]);
                    string message = dt.Rows[0]["Message"].ToString();
                    return Json(new
                    {
                        IsValid = isValid,
                        Message = message
                    });
                }

                return Json(new
                {
                    IsValid = false,
                    Message = "No data returned or invalid time range."
                });
            }
            catch (Exception ex)
            {
                return InternalServerError(ex);
            }
        }

        [HttpGet]
        [Route("api/TourPlan/GetMorningEveningTime")]
        public IHttpActionResult GetMorningEveningTime(string ShiftInfo)
        {
            DataTable dt = aDal.GetMorningEveningTime(ShiftInfo);
            if (dt == null || dt.Rows.Count == 0)
            {
                return Json(new { });
            }

            string JSONresult;
            JSONresult = JsonConvert.SerializeObject(dt);
            dynamic json = JsonConvert.DeserializeObject(JSONresult);
            return Json(json[0]);
        }

        [HttpGet]
        [Route("api/TourPlan/GetTourPlanForWorkedwithCopy")]
        public IHttpActionResult GetTourPlanForWorkedwithCopy(int empId, string tDate, string morEve)
        {
            DataTable dt = aDal.GetTourPlanForWorkedwithCopy(empId, tDate, morEve);
            if (dt == null || dt.Rows.Count == 0)
            {
                return Json(new { });
            }

            string JSONresult;
            JSONresult = JsonConvert.SerializeObject(dt);
            dynamic json = JsonConvert.DeserializeObject(JSONresult);
            return Json(json[0]);
        }

        [HttpGet]
        [Route("api/TourPlan/GetTourPlanEditbyId")]
        public IHttpActionResult GetTourPlanEditbyId(int TourPlanId, string tDate)
        {
            DataTable dt = aDal.GetTourPlanEditbyId(TourPlanId, tDate);
            if (dt == null || dt.Rows.Count == 0)
            {
                return Json(new { });
            }

            string JSONresult;
            JSONresult = JsonConvert.SerializeObject(dt);
            dynamic json = JsonConvert.DeserializeObject(JSONresult);
            return Json(json[0]);
        }

        [HttpGet]
        [Route("api/TourPlan/GetOtherMarketVisitListTourPlanEditbyId")]
        public IHttpActionResult GetOtherMarketVisitListTourPlanEditbyId(int TourPlanId, string tDate)
        {
            return Json(aDal.GetOtherMarketVisitListTourPlanEditbyId(TourPlanId, tDate));
        }
        
        [HttpGet]
        [Route("api/TourPlan/GetTourPlanDataDetail")]
        public IHttpActionResult GetTourPlanDataDetail(string month, string year, int empId, string Date)
        {
            //return Json(aDal.Get_TourPlanData(month, year, empId));

            return Json(aDal.Get_TourPlanDataDetail(month, year, empId, Date));
            
        }
         [HttpGet]
        [Route("api/TourPlan/GetTourPlanDataDetailNew")]
        public IHttpActionResult GetTourPlanDataDetailNew(string month, string year, int empId, string Date)
        {
            //return Json(aDal.Get_TourPlanData(month, year, empId));

            return Json(aDal.Get_TourPlanDataDetailNew(month, year, empId, Date));
            
        }


        [HttpPost]
        [Route("api/TourPlan/DelTourPlan")]
        public IHttpActionResult DelTourPlan(int id)
        {
            return Json(aDal.DeleteTourPlan(id));
        }

        [HttpGet]
        [Route("api/TourPlan/UpdateTourPlan")]

        public IHttpActionResult UpdateFinalTourSubmit(int empId, int month, int year, String remarks)
        {
            return Json(aDal.FinalSubmitTour(empId,month,year,remarks));
        }


        [HttpGet]
        [Route("api/TourPlan/GetTourPlanMasterData")]
        public IHttpActionResult GetTourPlanMasterData(int month, int year, int empId)
        {
            return Json( aDal.Get_TourPlanMasterData(month, year, empId));
            
        }



        [HttpGet]
        [Route("api/Doctor/GetTourPlan_Applog")]

        public IHttpActionResult GetTourPlan_Applog(string pram, string Role, string AppStatus, string FromDt, string ToDt, string EmpId, int? MonthValue, int?  YearValue)
        {
            return Json(aDal.GetTourPlan_ApplogDAL(pram, Role, AppStatus, FromDt, ToDt, EmpId, MonthValue,YearValue));
        }

        [HttpPost]
        [Route("api/Doctor/SaveTourPlan_Applog")]
        public IHttpActionResult SaveTourPlan_Applog(SaveTourPlan_Applog_DAO attendance)
        {
            return Json(aDal.SaveTourPlan_ApplogDAL(attendance));
        }



        [HttpGet]
        [Route("api/TourPlan/GetDWSP_Applog")]

        public IHttpActionResult GetDWSP_Applog(string pram, string Role, string AppStatus, string FromDt, string ToDt, string EmpId, int? MonthValue, int? YearValue)
        {
            return Json(aDal.GetDWSP_ApplogDAL(pram, Role, AppStatus, FromDt, ToDt, EmpId, MonthValue, YearValue));
        }

        [HttpPost]
        [Route("api/TourPlan/SavetDWSP_Applog")]
        public IHttpActionResult SavetDWSP_Applog(SaveDWSP_Applog_DAO attendance)
        {
            return Json(aDal.SaveDWSP_ApplogDAL(attendance));
        }


        [HttpPost]
        [Route("api/TourPlan/FinalSubmitDWSP")]

        public IHttpActionResult UpdateFinalDWSPSubmit(int empId, int month, int year, String remarks)
        {
            return Json(aDal.FinalSubmitDWSP(empId, month, year, remarks));
        }
    }
}
