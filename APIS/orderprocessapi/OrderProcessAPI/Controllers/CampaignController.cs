using DataLayer.Repository.Interface;
using DataLayer.ResponseModel;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using DataLayer.Models;
using DataLayer.ViewModels;

namespace OrderProcessAPI.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class CampaignController : ControllerBase
    {
        private readonly ICampaign _repository;
        public CampaignController(ICampaign repostiry)
        {
            _repository = repostiry;
        }

        [HttpGet("GetCurrentCampaign")]
        public IActionResult GetCurrentCampaign(int customerId)
        {
            Response result = new Response();
            return Ok(_repository.GetCurrentCampaign(customerId));

        }
        [HttpPost("GetCustomerWiseCampaign")]
        public IActionResult GetCustomerWiseCampaign(List<CampaignInfoOrderWise> aOrderDetailses)
        {
            Response result = new Response();
            return Ok(_repository.GetCustomerWiseCampaign(aOrderDetailses));

        }

        [HttpPost("GetOrderProductWiseCampaign")]
        public IActionResult GetOrderProductWiseCampaign(OrderMaster orderMasters)
        {
            Response result = new Response();
            return Ok(_repository.GetOrderProductWiseCampaign(orderMasters));

        }
        [HttpPost("GetOrderProductWiseCampaignOther")]
        public IActionResult GetOrderProductWiseCampaignOther(OrderMaster orderMasters)
        {
            Response result = new Response();
            return Ok(_repository.GetOrderProductWiseCampaignOther(orderMasters));

        }
    }
}
