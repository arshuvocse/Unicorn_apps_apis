using DataLayer.Models;
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
    public class OrderProcessController : ControllerBase
    {
        private readonly ILocation _repositoryD;
        private readonly IOrderProcess _repository;
        public OrderProcessController(IOrderProcess repository, ILocation repositoryd)
        {
            _repository = repository;
            _repositoryD = repositoryd;

        }

        [HttpPost("MakeOrder")]
        public IActionResult MakeOrder(OrderMaster aOrder)
        {
            return Ok(_repository.MakeOrder(aOrder));
        }



    }
}
