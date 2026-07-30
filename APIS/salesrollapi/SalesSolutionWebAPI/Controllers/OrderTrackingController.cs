using SalesSolutionWebAPI.Repository;
using System.Web.Http;

namespace SalesSolutionWebAPI.Controllers
{
    public class OrderTrackingController : ApiController
    {
        CustDemoDAL aCusDal = new CustDemoDAL();

        [HttpGet]
        [Route("api/OrderTracking/GetMioOrderList")]
        public IHttpActionResult GetMioOrderList(int empId, string FromDate, string ToDate, string AppStatus, string GroupId, string ZoneId, string AreaId, string TerritoryId, string SubTerritoryId, string MarketId)
        {
            return Json(aCusDal.GetOrderList_ForMIO(empId, FromDate, ToDate, AppStatus, GroupId, ZoneId, AreaId, TerritoryId, SubTerritoryId, MarketId));
        }
    }
}
