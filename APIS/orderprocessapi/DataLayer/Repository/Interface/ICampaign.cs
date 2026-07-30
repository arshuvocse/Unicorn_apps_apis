using DataLayer.ResponseModel;
using DataLayer.ViewModels;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using DataLayer.Models;

namespace DataLayer.Repository.Interface
{
   public interface ICampaign
    {
        List<CampaignMaster> GetCurrentCampaign(int customerId);
        List<CampaignMaster> GetCustomerWiseCampaign(List<CampaignInfoOrderWise> aOrderDetailses);
        List<OrderDetails> GetOrderProductWiseCampaign(OrderMaster orderMasters);
        List<OrderDetails> GetOrderProductWiseCampaignOther(OrderMaster orderMasters);
    }
}
