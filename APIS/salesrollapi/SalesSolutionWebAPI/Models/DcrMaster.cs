using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace SalesSolutionWebAPI.Models
{
    public class DcrMaster
    {
       public int DoctorId { get; set; }
       public string DcrDate { get; set; }
       public string EntryTime { get; set; }
       public int? TourPlanTypeId { get; set; }
       public int? DocTPDetailsId { get; set; }
       public int? ChamberId { get; set; }
        public string Remarks { get; set; }
        public string   Type { get; set; }
       public string SessionUser { get; set; }
       public string EntryDate_Apps { get; set; }
        public string Latitude { get; set; }
        public string Longitude { get; set; }
        public string StreetAddress { get; set; }
        public List<DCRProduct> productList { get; set; }
        public List<DCRProduct> giftList { get; set; }
        public List<DCRProduct> sampleList { get; set; }
        public List<EmpInfo> aempList { get; set; }
        public List<doctorBrandDA> doctorBrand { get; set; }

    }
}
