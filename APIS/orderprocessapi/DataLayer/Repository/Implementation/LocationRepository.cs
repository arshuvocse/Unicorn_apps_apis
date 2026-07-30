using DataLayer.DataManager;
using DataLayer.Models;
using DataLayer.Repository.Interface;
using DataLayer.ResponseModel;
using System;
using System.Collections.Generic;
using System.Data.SqlClient;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DataLayer.Repository.Implementation
{
    public class LocationRepository : ILocation
    {
        private DataAccessManager accessManager = new DataAccessManager();
        public Response UpdateCustomerLocation(Cordinate cordinate)
        {
            Response result = new Response();
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDB);
                
                List<SqlParameter> aSqlParameters = new List<SqlParameter>();
                aSqlParameters.Add(new SqlParameter("@latVl", cordinate.LatitudeValue));
                aSqlParameters.Add(new SqlParameter("@longV", cordinate.LongitudeValue));
                aSqlParameters.Add(new SqlParameter("@custtomerId", cordinate.Id));
                aSqlParameters.Add(new SqlParameter("@empId", cordinate.empId));
                aSqlParameters.Add(new SqlParameter("@streetaddress", cordinate.streetaddress));
                bool isSuccess= accessManager.UpdateData("sp_OPAPI_updateCustomerLocation", aSqlParameters);
                if (isSuccess)
                {
                    result.Message = "Success";
                }
                else
                {
                    result.Status = Status.BadRequest;
                    result.Message = "Error";
                }
            }
            catch (Exception ex)
            {
                result.Status = Status.BadRequest;
                result.Message = "Bad Request";
                result.ErrorMessage = ex.Message.ToString();
            }
            finally
            {
                accessManager.SqlConnectionClose();
            }

            return result;

        }
    }
}
