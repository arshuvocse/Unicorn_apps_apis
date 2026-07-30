namespace SalesSolutionWebAPI.Models
{
    public class ResultInfo
    {

        public bool isSuccess { get; set; }
        public bool isValiCheck { get; set; }
        public bool isError { get; set; }
        public string ErrorMessage { get; set; }
        public string Msd { get; set; }


    }

    public class ResultInfoNew
    {
        public string PunchInBtn { get; set; }
        public string PunchOUTBtn { get; set; }
        public int? ShiftId { get; set; }
        public string ShiftInTime { get; set; }
        public string ShiftOutTime { get; set; }
        public string ShiftText { get; set; }
    }


    public class OnOffButtonForCustomerDAO
    {



        public int btnCustProvider { get; set; }
        public int btnCustMS   { get; set; }
        public int btnDocProvider { get; set; }
        public int btnDocMS   { get; set; }

    }

}
