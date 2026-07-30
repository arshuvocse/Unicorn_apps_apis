using SalesSolutionWebAPI.Repository;
using System.Web.Http;
using System.Web.Mvc;
using RouteAttribute = System.Web.Http.RouteAttribute;

namespace SalesSolutionWebAPI.Controllers
{
    public class HomeController : ApiController
    {

    HomeDAL _aRepo = new HomeDAL();

      

        [System.Web.Http.HttpGet]
        [Route("api/Home/GetAllApprovalPendingInfo")]
        public IHttpActionResult GetAllApprovalPendingInfo(string pram, string Role)
        {

            return (IHttpActionResult)Json(_aRepo.GetAllApprovalPendingInfo(pram, Role));
        }
    }
}
