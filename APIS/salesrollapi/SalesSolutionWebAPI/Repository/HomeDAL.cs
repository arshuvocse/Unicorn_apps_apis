using SalesSolutionWebAPI.DataManager;
using System;
using System.Collections.Generic;
using System.Data;
using System.Data.SqlClient;
using System.Linq;
using System.Web;

namespace SalesSolutionWebAPI.Repository
{

    public class HomeDAL
    {
        private DataAccessManager accessManager = new DataAccessManager();
        public DataTable GetAllApprovalPendingInfo(string pram, string Role)
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> aSqlParameterlist = new List<SqlParameter>();
                aSqlParameterlist.Add(new SqlParameter("@pram", pram));
                aSqlParameterlist.Add(new SqlParameter("@Role", Role));
                DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_AllApprovalPendingInfo", aSqlParameterlist);
                return dt;
            }
            catch (Exception e)
            {
                throw;
            }
            finally
            {
                accessManager.SqlConnectionClose();
            }
        }
    }
}