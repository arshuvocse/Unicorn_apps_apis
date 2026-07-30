using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace SalesSolutionWebAPI.Models
{
    public class PrescriptionSM
    {
        public int DoctorId { get; set; }
       public int SessionUser { get; set; }
       public string PrescriptionDate { get; set; }
       public int PrescriptionTypeId { get; set; }
       public int? ChemberId { get; set; }
        public string ImageString { get; set; }
        public List<mProduct> aProList { get; set; }
    }
}