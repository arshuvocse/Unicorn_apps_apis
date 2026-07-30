using DataLayer.Repository.Interface;
using DataLayer.ResponseModel;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace OrderProcessAPI.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class QuotedPriceController : ControllerBase
    {
        private readonly IQuotedPrice _repository;

        public QuotedPriceController(IQuotedPrice repository)
        {
            _repository = repository;
        }

        [HttpGet("GetQuotedPrice")]
        public IActionResult GetData()
        {
            Response result = new Response();
            return Ok(_repository.GetAllPrice());

        }

        [HttpGet("GetQuotedPrice2")]
        public IActionResult GetData2()
        {
            Response result = new Response();
            return Ok(_repository.GetAllPriceTest());

        }
    }
}
