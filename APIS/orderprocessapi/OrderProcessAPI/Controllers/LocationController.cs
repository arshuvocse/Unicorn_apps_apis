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
    public class LocationController : ControllerBase
    {
        private readonly ILocation _repository;
        public LocationController(ILocation repository)
        {
            _repository = repository;
        }


        [HttpPost("UpdateCustomerLocation")]
        public IActionResult UpdateCustomerLocation(Cordinate location)
        {
            Response result = new Response();
            result = _repository.UpdateCustomerLocation(location);
            return Ok(result);

        }
    }
}
