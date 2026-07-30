using Newtonsoft.Json;
using SalesSolutionWebAPI.Models;
using SalesSolutionWebAPI.Repository;
using System.Data;
using System.Web.Http;

namespace SalesSolutionWebAPI.Controllers
{
    public class CustomerController : ApiController
    {
        CustomerDAL _aRepo = new CustomerDAL();

        [HttpGet]
        public IHttpActionResult GetCustomerByUSerId(int empId)
        {
            return Json(_aRepo.GetCustomerByUser(empId));

        }
        [HttpGet]
        [Route("api/Customer/GetCustomerByUserByMobileNo")]
        public IHttpActionResult GetCustomerByUserByMobileNo(int empId, string CellNo)
        {
            return Json(_aRepo.GetCustomerByUserByMobileNo(empId, CellNo));

        }


        [HttpGet]
        [Route("api/Customer/GetPersonByDivisionDistrict")]
        public IHttpActionResult GetPersonByDivisionDistrict(string DivisionId, string DistrictId, string ThanaId, string FromWhom)
        {
            DataTable dt = _aRepo.GetPersonByDivisionDistrictDAL(DivisionId, DistrictId, ThanaId, FromWhom);
            string JSONresult;
            JSONresult = JsonConvert.SerializeObject(dt);
            dynamic json = JsonConvert.DeserializeObject(JSONresult);
            return Json(json);
        }
        [HttpGet]
        [Route("api/Customer/GetCustomerList")]
        public IHttpActionResult GetCustomerList()
        {

            return Json(_aRepo.GetCustomerListDAL());
        }


        [HttpGet]
        [Route("api/Customer/GetCustomerType")]
        public IHttpActionResult GetCustomerType()
        {

            return Json(_aRepo.GetCustomerType());
        }


        [HttpPost]
        [Route("api/Customer/SaveCustomer")]

        public IHttpActionResult SaveCustomer(CustomerSM aInfo)
        {
            return Json(_aRepo.SaveCustomer(aInfo));
        }
        
        [HttpPost]
        [Route("api/Customer/UpdateCustomerBSP")]

        public IHttpActionResult UpdateCustomerBSP(CustomerSM aInfo)
        {
            return Json(_aRepo.UpdateCustomerBSP(aInfo));
        }


        [HttpGet]
        [Route("api/Customer/GetCustomer_Applog")]

        public IHttpActionResult GetCustomer_Applog(string pram, string Role, string AppStatus, string FromDt, string ToDt, string EmpId, string TerritoryId, string CustomerTypeId, string ProgramTypeId, string SMCTypeId)
        {
            return Json(_aRepo.GetCustomer_ApplogDAL(pram, Role, AppStatus, FromDt, ToDt, EmpId, TerritoryId, CustomerTypeId, ProgramTypeId, SMCTypeId));
        }

        [HttpPost]
        [Route("api/Customer/SaveCustomer_Applog")]
        public IHttpActionResult SaveCustomer_Applog(CustomerSaveAppLog_DAO attendance)
        {
            return Json(_aRepo.SaveCustomer_ApplogDAL(attendance));
        }



        [HttpPost]
        [Route("api/Customer/UpdateCustomerProvider")]

        public IHttpActionResult UpdateCustomerProvider(string empId, string CustomerId, string ProviderTypeId)
        {
            return Json(_aRepo.UpdateCustomerProviderDAL(empId, CustomerId, ProviderTypeId));
        }

        [HttpPost]
        [Route("api/Customer/UpdateCustomerMarket")]

        public IHttpActionResult UpdateCustomerMarket(CustomerMArket_DAO _DAO)
        {
            return Json(_aRepo.UpdateCustomerMarketDAL(_DAO));
        }
    }
}
