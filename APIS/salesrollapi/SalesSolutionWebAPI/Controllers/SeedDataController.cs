using Newtonsoft.Json;
using SalesSolutionWebAPI.Repository;
using System.Data;
using System.Web.Http;

namespace SalesSolutionWebAPI.Controllers
{
    public class SeedDataController : ApiController
    {
        private readonly SeedDataDAL _aRepo = new SeedDataDAL();

        [HttpGet]
        [Route("api/SeedData/GetDivisionAll")]
        public IHttpActionResult GetDivision()
        {
            return Json(_aRepo.GetDivisions_All());
        }

        [HttpGet]
        [Route("api/SeedData/GetBSPDivisionAll")]
        public IHttpActionResult GetBSPDivision()
        {
            return Json(_aRepo.GetBSPDivisions_All());
        }

        [HttpGet]
        [Route("api/SeedData/GetDistrict")]
        public IHttpActionResult GetDistrict()
        {
            return Json(_aRepo.GetDistrict());
        }

        [HttpGet]
        [Route("api/SeedData/GetBSPDistrict")]
        public IHttpActionResult GetBSPDistrict()
        {
            return Json(_aRepo.GetBSPDistrict());
        }
        [HttpGet]
        [Route("api/SeedData/GetThana")]
        public IHttpActionResult GetThana()
        {
            return Json(_aRepo.GetThana());
        }
        
        [HttpGet]
        [Route("api/SeedData/GetBSPThana")]
        public IHttpActionResult GetBSPThana()
        {
            return Json(_aRepo.GetBSPThana());
        }

        [HttpGet]
        [Route("api/SeedData/GetStation")]
        public IHttpActionResult GetStation()
        {
            return Json(_aRepo.GetStation());
        }

        [HttpGet]
        [Route("api/SeedData/GetDCStoreStockList")]
        public IHttpActionResult GetDCStoreStockList(string CustomerMasterId)
        {
            DataTable dt= _aRepo.GetDCStoreStockList(CustomerMasterId);
            string JSONresult;
            JSONresult = JsonConvert.SerializeObject(dt);
            dynamic json = JsonConvert.DeserializeObject(JSONresult);
            return Json(json);
        }

        [HttpGet]
        [Route("api/SeedData/GetEMPList")]
        public IHttpActionResult GetEMPList(string CustomerMasterId)
        {
            DataTable dt= _aRepo.GetEMPList(CustomerMasterId);
            string JSONresult;
            JSONresult = JsonConvert.SerializeObject(dt);
            dynamic json = JsonConvert.DeserializeObject(JSONresult);
            return Json(json);
        }
    }
}
