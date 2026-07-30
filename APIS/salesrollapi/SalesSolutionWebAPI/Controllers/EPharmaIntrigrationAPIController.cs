using SalesSolutionWebAPI.Models;
using SalesSolutionWebAPI.Repository;
using SalesSolutionWebAPI.Utilities;
using System.Net;
using System.Web.Http;

namespace SalesSolutionWebAPI.Controllers
{
    [RoutePrefix("api/ePharmaIntrigrationAPI")]
    [EPharmaBasicAuthorize]
    public class EPharmaIntrigrationAPIController : ApiController
    {
        private readonly EPharmaIntrigrationDAL _repository = new EPharmaIntrigrationDAL();

        [HttpPost]
        [Route("SaveProviderInfo")]
        public IHttpActionResult SavePersonInfo(EPharmaPersonInfoRequest request)
        {
            if (request == null)
            {
                return BadRequest("Request body is required.");
            }

            if (!ModelState.IsValid)
            {
                return BadRequest(ModelState);
            }

            EPharmaPersonInfoResponse response = _repository.SavePersonInfo(request);
            if (!response.isSuccess)
            {
                return Content(HttpStatusCode.InternalServerError, response);
            }

            return Ok(response);
        }

        [HttpPost]
        [Route("SaveProviderDropoutInfo")]
        public IHttpActionResult SaveProviderDropoutInfo(EPharmaProviderDropoutRequest request)
        {
            if (request == null)
            {
                return BadRequest("Request body is required.");
            }

            if (!ModelState.IsValid)
            {
                return BadRequest(ModelState);
            }

            EPharmaProviderDropoutResponse response = _repository.SaveProviderDropoutInfo(request);
            if (!response.isSuccess)
            {
                return Content(HttpStatusCode.InternalServerError, response);
            }

            return Ok(response);
        }
    }
}
