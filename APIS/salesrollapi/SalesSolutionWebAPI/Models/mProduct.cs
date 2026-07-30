namespace SalesSolutionWebAPI.Models
{
    public class mProduct
    {

        public int ProductId { get; set; }
        public decimal qty { get; set; }
        public string ProductName { get; set; }
        public string ProductCode { get; set; }
        public string ProductDes { get; set; }
        public string PackSize { get; set; }

        public decimal UnitPrice { get; set; }
        public decimal QuotedPrice { get; set; }

        public decimal VatPercentage { get; set; }
        public decimal VatAmountPerunit { get; set; }

        public int CustomerMasterId { get; set; }




    }


    public class DCRProduct
    {

        public int ProductId { get; set; }
        public decimal Quantity { get; set; }
        //public string ProductName { get; set; }
        //public string ProductCode { get; set; }
        //public string ProductDes { get; set; }
        //public string PackSize { get; set; }

        //public decimal UnitPrice { get; set; }
        //public decimal QuotedPrice { get; set; }

        //public decimal VatPercentage { get; set; }
        //public decimal VatAmountPerunit { get; set; }

        //public int CustomerMasterId { get; set; }




    }

}
