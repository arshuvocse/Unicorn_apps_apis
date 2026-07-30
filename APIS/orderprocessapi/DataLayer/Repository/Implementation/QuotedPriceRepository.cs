using Dapper;
using DataLayer.DataManager;
using DataLayer.Models;
using DataLayer.Repository.Interface;
using DataLayer.ResponseModel;
using DataLayer.ViewModels;
using System;
using System.Collections.Generic;
using System.Data;
using System.Data.SqlClient;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DataLayer.Repository.Implementation
{
    public class QuotedPriceRepository : IQuotedPrice
    {
        private DataAccessManager accessManager = new DataAccessManager();
        public List<QuotedPrice> GetAllPrice()
        {
            List<QuotedPrice> aList = new List<QuotedPrice>();
            Response result = new Response();
            try
            {
                using var sqlConnection = new SqlConnection(accessManager.ConnectionString(DataBase.SalesDB));
                sqlConnection.Open();
                aList = sqlConnection.Query<QuotedPrice>("sp_OPAPI_GetQuotedPrice", commandType: CommandType.StoredProcedure).ToList();
                //result.Data = aLIst;
            }
            catch (Exception ex)
            {
                //result.Status = Status.BadRequest;
                //result.Message = "Bad Request";
                //result.ErrorMessage = ex.Message.ToString();
            }


            return aList;
        }

        public Response GetAllPriceTest()
        {
            Response result = new Response();
            try
            {
                using var sqlConnection = new SqlConnection(accessManager.ConnectionString(DataBase.SalesDB));
                sqlConnection.Open();
               var aList = sqlConnection.Query<QuotedPrice>("sp_OPAPI_GetQuotedPrice", commandType: CommandType.StoredProcedure).ToList();
                result.Data = aList;
            }
            catch (Exception ex)
            {
                result.Status = Status.BadRequest;
                result.Message = "Bad Request";
                result.ErrorMessage = ex.Message.ToString();
            }


            return result;
        }
    }
}
