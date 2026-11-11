using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using DataLayer.ViewModels;

namespace DataLayer.Models
{
    public class OrderMaster
    {
        public int OrderId { get; set; }
        public int EmpId { get; set; }
        public int ComunitId { get; set; }
        public string CustomerCode { get; set; }
        public string SubmittedDate { get; set; }
        public string CollectionDate { get; set; }
        public string DeliveryDate { get; set; }
        public string Remarks { get; set; }
        public string OrderType { get; set; }
        public string PaymentType { get; set; }
        public string PaymentDate { get; set; }
        public double TpPercentage { get; set; }
        // where the order was submitted from (app GPS); optional
        public string Latitude { get; set; }
        public string Longitude { get; set; }
        public string StreetAddress { get; set; }


        public List<OrderDetails> OrderDetails { get; set; }
        public List<CampaignMaster> CampaignMasters { get; set; }
        


    }
}
