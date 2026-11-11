using SalesSolutionWebAPI.DataManager;
using SalesSolutionWebAPI.Models;
using System;
using System.Collections.Generic;
using System.Data;
using System.Data.SqlClient;

namespace SalesSolutionWebAPI.Repository
{
    /// <summary>
    /// Our own cell tower map (dbo.tbl_CellTowerLocation), learned from the reps' GPS fixes. It is
    /// consulted before any external geolocation service, so a known tower costs nothing and works
    /// where the public databases have no coverage.
    /// </summary>
    public class CellTowerDAL
    {
        private DataAccessManager accessManager = new DataAccessManager();

        /// <returns>the tower's position, or null when it has never been seen with GPS</returns>
        public ResolveLocationResponse Get_CellTowerLocation(GeoCellTower tower)
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> aSqlParameterlist = new List<SqlParameter>();
                aSqlParameterlist.Add(new SqlParameter("@RadioType", tower.RadioType));
                aSqlParameterlist.Add(new SqlParameter("@Mcc", tower.MobileCountryCode));
                aSqlParameterlist.Add(new SqlParameter("@Mnc", tower.MobileNetworkCode));
                aSqlParameterlist.Add(new SqlParameter("@Lac", tower.LocationAreaCode));
                aSqlParameterlist.Add(new SqlParameter("@CellId", tower.CellId));
                DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_CellTowerLocation", aSqlParameterlist);
                if (dt == null || dt.Rows.Count == 0)
                    return null;

                DataRow row = dt.Rows[0];
                return new ResolveLocationResponse
                {
                    Success = true,
                    Latitude = Convert.ToDouble(row["Latitude"]),
                    Longitude = Convert.ToDouble(row["Longitude"]),
                    Accuracy = Convert.ToDouble(row["AccuracyMeters"]),
                    Source = "cell",
                    Message = "ok"
                };
            }
            catch (Exception)
            {
                throw;
            }
            finally
            {
                accessManager.SqlConnectionClose();
            }
        }

        public bool Save_CellTowerLocation(LearnCellRequest request)
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> aSqlParameterlist = new List<SqlParameter>();
                aSqlParameterlist.Add(new SqlParameter("@RadioType", request.RadioType));
                aSqlParameterlist.Add(new SqlParameter("@Mcc", request.MobileCountryCode));
                aSqlParameterlist.Add(new SqlParameter("@Mnc", request.MobileNetworkCode));
                aSqlParameterlist.Add(new SqlParameter("@Lac", request.LocationAreaCode));
                aSqlParameterlist.Add(new SqlParameter("@CellId", request.CellId));
                aSqlParameterlist.Add(new SqlParameter("@Latitude", request.Latitude));
                aSqlParameterlist.Add(new SqlParameter("@Longitude", request.Longitude));
                aSqlParameterlist.Add(new SqlParameter("@AccuracyMeters", request.Accuracy));
                return accessManager.SaveData("sp_Webapi_Save_CellTowerLocation", aSqlParameterlist);
            }
            catch (Exception)
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
