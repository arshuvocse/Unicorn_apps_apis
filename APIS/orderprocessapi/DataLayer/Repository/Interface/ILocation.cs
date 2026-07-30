using DataLayer.Models;
using DataLayer.ResponseModel;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DataLayer.Repository.Interface
{
   public interface ILocation
    {
        Response UpdateCustomerLocation(Cordinate cordinate);
    }
}
