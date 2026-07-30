using SalesSolutionWebAPI.Models;
using SalesSolutionWebAPI.Repository;
using System;
using System.Web.Http;

namespace SalesSolutionWebAPI.Controllers
{
    public class TADAController : ApiController
    {
        CalculationDAL _repo = new CalculationDAL();

        [HttpGet]
        [Route("api/TADA/GetTourPlanForTada")]
        public IHttpActionResult GetTourPlanForTada(int empId, string tourDate)
        {
            return Json(_repo.Get_TourPlanForTadaClaim(empId, Convert.ToDateTime(tourDate)));
        }

        [HttpGet]
        [Route("api/TADA/GetTadaAmount")]
        public IHttpActionResult GetTadaAmount(int userId)
        {
            return Json(_repo.GetTadaAmount(userId));
        }

        [HttpPost]
        [Route("api/TADA/SaveTadaClaim")]
        public IHttpActionResult SaveTadaClaim(TadaClaim ainfo)
        {
            return Json(_repo.SaveTadaClaim(ainfo));
        }
    }
}
