using SalesSolutionWebAPI.DataManager;
using SalesSolutionWebAPI.Models;
using System;
using System.Collections.Generic;
using System.Data;
using System.Data.SqlClient;
using System.Linq;
using System.Web;

namespace SalesSolutionWebAPI.Repository
{
    public class TourPlanDAL
    {

        private DataAccessManager accessManager = new DataAccessManager();

        public DataTable Get_TerritoryByEmpId(int empId)
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> aSqlParameterlist = new List<SqlParameter>();
                aSqlParameterlist.Add(new SqlParameter("@empId", empId));
                DataTable dt = accessManager.GetDataTable("sp_Webapi_GetTerritoryByEmpId", aSqlParameterlist);
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

        public DataTable Get_marketByTerritoryId(int territoryId)
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> aSqlParameterlist = new List<SqlParameter>();
                aSqlParameterlist.Add(new SqlParameter("@territoryId", territoryId));
                DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_MarketByTerritoryId", aSqlParameterlist);
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
        public DataTable Get_SubmarketByTerritoryId(int marketId)
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> aSqlParameterlist = new List<SqlParameter>();
                aSqlParameterlist.Add(new SqlParameter("@marketId", marketId));
                DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_SubMarketByMarketId", aSqlParameterlist);
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

        public DataTable Get_CustomerByMarketId(int marketId)
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> aSqlParameterlist = new List<SqlParameter>();
                aSqlParameterlist.Add(new SqlParameter("@marketId", marketId));
                DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_CustomerByMarketId", aSqlParameterlist);
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

        public DataTable Get_Shift(int empId)
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> aSqlParameterlist = new List<SqlParameter>();
                aSqlParameterlist.Add(new SqlParameter("@empId", empId));
                DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_Shift", aSqlParameterlist);
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

        public DataTable Get_TourPlanType(int empId)
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> aSqlParameterlist = new List<SqlParameter>();
                aSqlParameterlist.Add(new SqlParameter("@empId", empId));
                DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_TourPlanType", aSqlParameterlist);
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

        public DataTable Get_TourPlanPurpose(int empId)
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> aSqlParameterlist = new List<SqlParameter>();
                aSqlParameterlist.Add(new SqlParameter("@empId", empId));
                DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_TourPlanPurpose", aSqlParameterlist);
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
        
        public DataTable Get_TourPlanPurposeForMarketVisit(int empId)
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> aSqlParameterlist = new List<SqlParameter>();
                aSqlParameterlist.Add(new SqlParameter("@empId", empId));
                DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_TourPlanPurposeForMarketVisit", aSqlParameterlist);
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
        
        public DataTable Get_TourPlanPurposeForOtherVisit(int empId)
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> aSqlParameterlist = new List<SqlParameter>();
                aSqlParameterlist.Add(new SqlParameter("@empId", empId));
                DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_TourPlanPurposeForOtherVisit", aSqlParameterlist);
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

        public static bool ValidateTourPlan(List<TourPlanInfoNewDAO> tourPlanList)
        {
            int MarketVisitCount = 0;
            int OtherVisitCount = 0;
            bool IsMorningSet = false;
            bool IsEveningSet = false;

            foreach (var item in tourPlanList)
            {
                // Check Market Visit
                if (item.IsMarketVisit == "1")
                {
                    MarketVisitCount++;
                    if (MarketVisitCount > 2)
                    {
                        return false; // MarketVisit cannot be true more than once
                    }

                    // If MarketVisit is true, OtherVisit should be false
                    if (item.IsOtherVisit == "1")
                    {
                        return false; // Both MarketVisit and OtherVisit cannot be true simultaneously
                    }
                }

                // Check Other Visit
                if (item.IsOtherVisit == "1")
                {
                    OtherVisitCount++;
                    if (OtherVisitCount > 1)
                    {
                        return false; // OtherVisit cannot be true more than once
                    }

                    // If OtherVisit is true, MarketVisit should be false
                    if (item.IsMarketVisit == "1")
                    {
                        return false; // Both MarketVisit and OtherVisit cannot be true simultaneously
                    }
                }

                // Check Morning Visit
                if (item.IsMorning == "1")
                {
                    if (IsMorningSet)
                    {
                        return false; // IsMorning cannot be true more than once
                    }
                    IsMorningSet = true;
                }

                // Check Evening Visit
                if (item.IsEvening == "1")
                {
                    if (IsEveningSet)
                    {
                        return false; // IsEvening cannot be true more than once
                    }
                    IsEveningSet = true;
                }
            }

            return true;
        }

        public static bool ValidateTourPlanTwo(List<TourPlanInfoNewDAO> tourPlanList)
        {
            int _mrn = 0;
            int _evn = 0;
            bool IsMorningEveSet = false;


                    foreach (var item in tourPlanList)
                    {
                        if (item.IsMarketVisit == "1")
                        {
                            IsMorningEveSet = true;
                            // Check Morning Visit
                            if (item.IsMorning == "1")
                            {
                        
                                _mrn++;
                            }

                            // Check Evening Visit
                            if (item.IsEvening == "1")
                            {
                       
                                _evn++;
                            }
                        }

                        }
                    if (IsMorningEveSet)
               {
                    if (_mrn + _evn == 2)
                    {

                    }
                    else
                    {
                        return false;
                    }

                }


            return true;
        }
        public ResultInfo SaveTourPlanList_vThree(TourPlanInfoMasterDAO aInfo)
        {


            ResultInfo aInformation = new ResultInfo();
            try

            {

              
                DateTime? TourPlanDate = null;
                int? EmpInfoId = null;

                
                bool isValid = ValidateTourPlan(aInfo.aTourPlanInfo); // Validate tour plan info
                bool isValidTwo = ValidateTourPlanTwo(aInfo.aTourPlanInfo); // Validate tour plan info


                if (isValid)
                {

                    if (isValidTwo)
                    {
                        try

                        {
                            accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                            List<SqlParameter> sqlDel = new List<SqlParameter>();
                            foreach (TourPlanInfoNewDAO item in aInfo.aTourPlanInfo)
                            {
                                TourPlanDate = item.TourPlanDate;
                                EmpInfoId = item.EmpInfoId;
                            }
                            sqlDel.Add(new SqlParameter("@TourDate", TourPlanDate ?? (object)DBNull.Value ?? (object)DBNull.Value));
                            sqlDel.Add(new SqlParameter("@empId", EmpInfoId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                            aInformation.isSuccess = accessManager.DeleteData("sp_Webapi_Del_TourPlanInfoForEmpDate", sqlDel);

                            DateTime entryDtae = DateTime.Now;

                            foreach (TourPlanInfoNewDAO item in aInfo.aTourPlanInfo)
                            {
                                int pk = 0;
                                List<SqlParameter> gSqlParameterList = new List<SqlParameter>();
                                gSqlParameterList.Add(new SqlParameter("@marketId", item.MarketId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                                gSqlParameterList.Add(new SqlParameter("@marketIdEnd", item.MarketIdEnd ?? (object)DBNull.Value ?? (object)DBNull.Value));
                                gSqlParameterList.Add(new SqlParameter("@PurposeId", item.TPId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                                gSqlParameterList.Add(new SqlParameter("@TourDate", item.TourPlanDate ?? (object)DBNull.Value ?? (object)DBNull.Value));
                                gSqlParameterList.Add(new SqlParameter("@empId", item.EmpInfoId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                                gSqlParameterList.Add(new SqlParameter("@SerialNo", item.SerialNo ?? (object)DBNull.Value ?? (object)DBNull.Value));


                                gSqlParameterList.Add(new SqlParameter("@IsMorning", item.IsMorning ?? (object)DBNull.Value ?? (object)DBNull.Value));
                                gSqlParameterList.Add(new SqlParameter("@IsEvening", item.IsEvening ?? (object)DBNull.Value ?? (object)DBNull.Value));

                                gSqlParameterList.Add(new SqlParameter("@Starttime", item.Starttime ?? (object)DBNull.Value ?? (object)DBNull.Value));

                                gSqlParameterList.Add(new SqlParameter("@Endtime", item.Endtime ?? (object)DBNull.Value ?? (object)DBNull.Value));

                                gSqlParameterList.Add(new SqlParameter("@IsMarketVisit", item.IsMarketVisit ?? (object)DBNull.Value ?? (object)DBNull.Value));

                                gSqlParameterList.Add(new SqlParameter("@IsOtherVisit", item.IsOtherVisit ?? (object)DBNull.Value ?? (object)DBNull.Value));
                                gSqlParameterList.Add(new SqlParameter("@Objective", item.Objective ?? (object)DBNull.Value ?? (object)DBNull.Value));
                                gSqlParameterList.Add(new SqlParameter("@VisitedWithEmpInfoId", item.VisitedWithEmpInfoId ?? (object)DBNull.Value ?? (object)DBNull.Value));


                                pk = accessManager.SaveDataReturnPrimaryKey("sp_Webapi_Save_TourPlanInfo_vThree", gSqlParameterList);

                                if (pk > 0)
                                {
                                    aInformation.isSuccess = true;

                                    if (item.aVisitedMarketList != null)
                                    {
                                        foreach (VisitedMarketDao itemCus in item.aVisitedMarketList)
                                        {
                                            List<SqlParameter> agf = new List<SqlParameter>();

                                            agf.Add(new SqlParameter("@marketId", itemCus.marketId));


                                            agf.Add(new SqlParameter("@pk", pk));




                                            aInformation.isSuccess = accessManager.SaveData("sp_Webapi_Save_TPMarketDetail", agf);

                                        }
                                    }

                                }
                            }



                        }

                        catch (Exception exception)
                        {
                            accessManager.SqlConnectionClose(true);
                            aInformation.isSuccess = false;
                            aInformation.ErrorMessage = exception.Message;
                            throw exception;
                        }
                        finally
                        {
                            accessManager.SqlConnectionClose();
                        }
                    }
                    else
                    {
                        aInformation.isSuccess = false;
                        aInformation.ErrorMessage = "";
                        aInformation.Msd = "Please select both Morning and Evening!";
                    }
                }
                else
                {
                    aInformation.isSuccess = false;
                    aInformation.ErrorMessage = "";
                    aInformation.Msd = "Already Exist!";
                }





        }catch (Exception ex)
            {

            }

            return aInformation;



        }
        
        
        
        public ResultInfo AddTourPlan(TourPlanInfoMaster aInfo)
        {


            ResultInfo aInformation = new ResultInfo();
            try

            {

                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> sqlDel = new List<SqlParameter>();
                DateTime? TourPlanDate = null;
                int? EmpInfoId = null;
                foreach (TourPlanInfo item in aInfo.aTourPlanInfo)
                {
                    TourPlanDate = item.TourPlanDate;
                    EmpInfoId = item.EmpInfoId;
                }
                sqlDel.Add(new SqlParameter("@TourDate", TourPlanDate ?? (object)DBNull.Value ?? (object)DBNull.Value));
                sqlDel.Add(new SqlParameter("@empId", EmpInfoId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                aInformation.isSuccess = accessManager.DeleteData("sp_Webapi_Del_TourPlanInfoForEmpDate", sqlDel);

                DateTime entryDtae = DateTime.Now;

                foreach (TourPlanInfo item in aInfo.aTourPlanInfo)
                {
                    int pk = 0;
                    List<SqlParameter> gSqlParameterList = new List<SqlParameter>();
                    //gSqlParameterList.Add(new SqlParameter("@IsMarketWise", item.IsMarketWise ?? (object)DBNull.Value ?? (object)DBNull.Value));
                    //gSqlParameterList.Add(new SqlParameter("@territoryId", item.TerritoryId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                    gSqlParameterList.Add(new SqlParameter("@marketId", item.MarketId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                    //gSqlParameterList.Add(new SqlParameter("@subMarketId", item.SMId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                    //gSqlParameterList.Add(new SqlParameter("@customerId", item.CustomerMasterId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                    //gSqlParameterList.Add(new SqlParameter("@shiftId", item.ShiftId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                    //gSqlParameterList.Add(new SqlParameter("@typeId", item.TourTypeId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                    gSqlParameterList.Add(new SqlParameter("@PurposeId", item.TPId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                    //gSqlParameterList.Add(new SqlParameter("@Comment", item.Comment ?? (object)DBNull.Value ?? (object)DBNull.Value));
                    gSqlParameterList.Add(new SqlParameter("@TourDate", item.TourPlanDate ?? (object)DBNull.Value ?? (object)DBNull.Value));
                    gSqlParameterList.Add(new SqlParameter("@empId", item.EmpInfoId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                    gSqlParameterList.Add(new SqlParameter("@SerialNo", item.SerialNo ?? (object)DBNull.Value ?? (object)DBNull.Value));
                    pk = accessManager.SaveDataReturnPrimaryKey("sp_Webapi_Save_TourPlanInfo", gSqlParameterList);

                    if (pk > 0)
                    {
                        aInformation.isSuccess = true;

                        if (item.aCustomerMasterList != null)
                        {
                            foreach (CustomerMasterDAO itemCus in item.aCustomerMasterList)
                            {
                                List<SqlParameter> agf = new List<SqlParameter>();

                                agf.Add(new SqlParameter("@CustomerMasterId", itemCus.CustomerMasterId));


                                agf.Add(new SqlParameter("@pk", pk));




                                aInformation.isSuccess = accessManager.SaveData("sp_Webapi_Save_TPCustomerDetail", agf);

                            }
                        }

                    }
                }



            }
            catch (Exception exception)
            {
                accessManager.SqlConnectionClose(true);
                aInformation.isSuccess = false;
                aInformation.ErrorMessage = exception.Message;
                throw exception;
            }
            finally
            {
                accessManager.SqlConnectionClose();
            }

            return aInformation;



        }
        
        
        
        public ResultInfo AddTourPlanNew(TourPlanInfoMaster aInfo)
        {


            ResultInfo aInformation = new ResultInfo();
            if (validationTourplanNew(aInfo, aInformation))
            {
                try

                {



                    accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                    List<SqlParameter> sqlDel = new List<SqlParameter>();
                    DateTime? TourPlanDate = null;
                    int? EmpInfoId = null;
                    foreach (TourPlanInfo item in aInfo.aTourPlanInfo)
                    {
                        TourPlanDate = item.TourPlanDate;
                        EmpInfoId = item.EmpInfoId;
                    }
                    sqlDel.Add(new SqlParameter("@TourDate", TourPlanDate ?? (object)DBNull.Value ?? (object)DBNull.Value));
                    sqlDel.Add(new SqlParameter("@empId", EmpInfoId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                    aInformation.isSuccess = accessManager.DeleteData("sp_Webapi_Del_TourPlanInfoForEmpDate", sqlDel);

                    DateTime entryDtae = DateTime.Now;



                    foreach (TourPlanInfo item in aInfo.aTourPlanInfo)
                    {
                        int pk = 0;
                        List<SqlParameter> gSqlParameterList = new List<SqlParameter>();
                        //gSqlParameterList.Add(new SqlParameter("@IsMarketWise", item.IsMarketWise ?? (object)DBNull.Value ?? (object)DBNull.Value));
                        //gSqlParameterList.Add(new SqlParameter("@territoryId", item.TerritoryId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                        gSqlParameterList.Add(new SqlParameter("@marketId", item.MarketId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                        //gSqlParameterList.Add(new SqlParameter("@subMarketId", item.SMId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                        //gSqlParameterList.Add(new SqlParameter("@customerId", item.CustomerMasterId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                        //gSqlParameterList.Add(new SqlParameter("@shiftId", item.ShiftId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                        //gSqlParameterList.Add(new SqlParameter("@typeId", item.TourTypeId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                        gSqlParameterList.Add(new SqlParameter("@PurposeId", item.TPId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                        //gSqlParameterList.Add(new SqlParameter("@Comment", item.Comment ?? (object)DBNull.Value ?? (object)DBNull.Value));
                        gSqlParameterList.Add(new SqlParameter("@TourDate", item.TourPlanDate ?? (object)DBNull.Value ?? (object)DBNull.Value));
                        gSqlParameterList.Add(new SqlParameter("@empId", item.EmpInfoId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                        gSqlParameterList.Add(new SqlParameter("@SerialNo", item.SerialNo ?? (object)DBNull.Value ?? (object)DBNull.Value));



                        bool? IsMorning = false;

                        try
                        {
                            IsMorning = item.IsMorning == "1" ? true : false;
                        }
                        catch (Exception ex)
                        {

                        }


                        bool? IsEvening = false;

                        try
                        {
                            IsEvening = item.IsEvening == "1" ? true : false;
                        }
                        catch (Exception ex)
                        {

                        }

                        bool? IsStartTime = false;

                        try
                        {
                            IsStartTime = item.IsStartTime == "1" ? true : false;
                        }
                        catch (Exception ex)
                        {

                        }

                        bool? IsEndtime = false;

                        try
                        {
                            IsEndtime = item.IsEndtime == "1" ? true : false;
                        }
                        catch (Exception ex)
                        {

                        }

                        gSqlParameterList.Add(new SqlParameter("@IsMorning", IsMorning ?? (object)DBNull.Value ?? (object)DBNull.Value));
                        gSqlParameterList.Add(new SqlParameter("@IsEvening", IsEvening ?? (object)DBNull.Value ?? (object)DBNull.Value));
                        gSqlParameterList.Add(new SqlParameter("@IsStartTime", IsStartTime ?? (object)DBNull.Value ?? (object)DBNull.Value));
                        gSqlParameterList.Add(new SqlParameter("@Starttime", item.Starttime ?? (object)DBNull.Value ?? (object)DBNull.Value));
                        gSqlParameterList.Add(new SqlParameter("@VisitedWithEmpInfoId", item.VisitedWithEmpInfoId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                        gSqlParameterList.Add(new SqlParameter("@IsEndtime", IsEndtime ?? (object)DBNull.Value ?? (object)DBNull.Value));
                        gSqlParameterList.Add(new SqlParameter("@Endtime", item.Endtime ?? (object)DBNull.Value ?? (object)DBNull.Value));


                        pk = accessManager.SaveDataReturnPrimaryKey("sp_Webapi_Save_TourPlanInfo_new", gSqlParameterList);

                        if (pk > 0)
                        {
                            aInformation.isSuccess = true;

                            if (item.aCustomerMasterList != null)
                            {
                                foreach (CustomerMasterDAO itemCus in item.aCustomerMasterList)
                                {
                                    List<SqlParameter> agf = new List<SqlParameter>();

                                    agf.Add(new SqlParameter("@CustomerMasterId", itemCus.CustomerMasterId));


                                    agf.Add(new SqlParameter("@pk", pk));




                                    aInformation.isSuccess = accessManager.SaveData("sp_Webapi_Save_TPCustomerDetail", agf);

                                }
                            }

                            if (aInformation.isSuccess)
                            {
                                aInformation.isSuccess = true;
                                aInformation.Msd = "Submitted Successfully!!";
                            }

                        }

                        else
                        {
                            aInformation.isSuccess = false;
                            aInformation.Msd = "Something went wrong!";

                        }
                    }



                }
                catch (Exception exception)
                {
                    accessManager.SqlConnectionClose(true);
                    aInformation.isSuccess = false;
                    aInformation.ErrorMessage = exception.Message;
                    throw exception;
                }
                finally
                {
                    accessManager.SqlConnectionClose();
                }
            }

            return aInformation;



        }

        private static bool validationTourplanNew(TourPlanInfoMaster aInfo, ResultInfo msg)
        {

            bool isValid = true;
           
            int StartTimeCount = 0;
            int EndTimeFoundCount = 0;


            int E_StartTimeCount = 0;
            int E_EndTimeFoundCount = 0;

            foreach (TourPlanInfo item in aInfo.aTourPlanInfo)
            {
                bool isMorning = false;
                bool isStartTime = false;
                bool isEndTime = false;

                  bool E_IsEvening = false;
                bool E_isStartTime = false;
                bool E_isEndTime = false;

                try
                {
                    isMorning = item.IsMorning == "1";
                    isStartTime = item.IsStartTime == "1";
                    isEndTime = item.IsEndtime == "1";
                }
                catch (Exception ex)
                {
                    // Handle exception if necessary
                }


                try
                {
                    E_IsEvening = item.IsEvening == "1";
                    E_isStartTime = item.IsStartTime == "1";
                    E_isEndTime = item.IsEndtime == "1";
                }
                catch (Exception ex)
                {
                    // Handle exception if necessary
                }


                  if (isMorning)
                {
                    if (isStartTime)
                    {
                        StartTimeCount++;
                    }

                    if (isEndTime)
                    {
                        EndTimeFoundCount++;
                    }
                }



                if (E_IsEvening)
                {
                    if (E_isStartTime)
                    {
                        E_StartTimeCount++;
                    }

                    if (E_isEndTime)
                    {
                        E_EndTimeFoundCount++;
                    }
                }




            }

            if (StartTimeCount >= 2)
            {
                msg.isSuccess = false;

                msg.Msd = "Plsease Correct Start time and End time";

                isValid = false;
            }
            if (EndTimeFoundCount >= 2)
            {
                msg.isSuccess = false;
                msg.Msd = "Plsease Correct Start time and End time";
                isValid = false;

            }  
            
            
            if (E_StartTimeCount >= 2)
            {
                msg.isSuccess = false;

                msg.Msd = "Plsease Correct Start time and End time";

                isValid = false;
            }
            if (E_EndTimeFoundCount >= 2)
            {
                msg.isSuccess = false;
                msg.Msd = "Plsease Correct Start time and End time";
                isValid = false;

            }

            return isValid;
        }

        public string NewMethod(string pram, string Role, List<SqlParameter> gSqlParameterList)
        {
            if (Role == "AM")
            {

                string pramArr = pram.Trim();
                if (pramArr.Contains('='))
                {
                    string[] emp = pramArr.Split('=');

                    string AreId = emp[1].Trim();


                    List<SqlParameter> gSqlEmp = new List<SqlParameter>();

                    gSqlEmp.Add(new SqlParameter("@AreId", AreId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                    DataTable dtEmp = accessManager.GetDataTable("sp_Webapi_Get_AMMultipleArea", gSqlEmp);



                    if (dtEmp.Rows.Count > 0)
                    {
                        string ArrAreaId = "";
                        string MainArrAreaId = "";
                        for (var i = 0; i < dtEmp.Rows.Count; i++)

                        {
                            if (dtEmp.Rows[i]["AreaId"] != DBNull.Value)
                            {
                                ArrAreaId = ArrAreaId + (dtEmp.Rows[i]["AreaId"].ToString()) + ',';

                            }
                        }
                        MainArrAreaId = ArrAreaId.TrimEnd(',');

                        pram = " AND View_Webapi_EmployeeFieldForceInfo.EmpAreaId in (" + MainArrAreaId + ")";
                    }

                    else
                    {

                        pram = " AND View_Webapi_EmployeeFieldForceInfo.EmpAreaId in (" + AreId + ")";
                    }
                    gSqlParameterList.Add(new SqlParameter("@param", pram ?? (object)DBNull.Value ?? (object)DBNull.Value));

                }
            }

            else if (Role == "DZSM")
            {

                string pramArr = pram.Trim();
                if (pramArr.Contains('='))
                {
                    string[] emp = pramArr.Split('=');

                    string AreId = emp[1].Trim();


                    List<SqlParameter> gSqlEmp = new List<SqlParameter>();

                    gSqlEmp.Add(new SqlParameter("@AreId", AreId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                    DataTable dtEmp = accessManager.GetDataTable("sp_Webapi_Get_DZSMMultipleArea", gSqlEmp);

                    string ArrAreaId = "";
                    string MainArrAreaId = "";
                    for (var i = 0; i < dtEmp.Rows.Count; i++)

                    {
                        if (dtEmp.Rows[i]["RegionId"] != DBNull.Value)
                        {
                            ArrAreaId = ArrAreaId + (dtEmp.Rows[i]["RegionId"].ToString()) + ',';

                        }
                    }
                    MainArrAreaId = ArrAreaId.TrimEnd(',');

                    pram = " AND View_Webapi_EmployeeFieldForceInfo.EmpRegionId in (" + MainArrAreaId + ")";
                    gSqlParameterList.Add(new SqlParameter("@param", pram ?? (object)DBNull.Value ?? (object)DBNull.Value));

                }
            }
            else
            {
                gSqlParameterList.Add(new SqlParameter("@param", pram ?? (object)DBNull.Value ?? (object)DBNull.Value));

            }

            return pram;
        }

        public DataTable Get_TourPlanData(int month,int year,int empId)
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> aSqlParameterlist = new List<SqlParameter>();
                aSqlParameterlist.Add(new SqlParameter("@month", month));
                aSqlParameterlist.Add(new SqlParameter("@year", year));
                aSqlParameterlist.Add(new SqlParameter("@empId", empId));
                DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_TourPlanInfo", aSqlParameterlist);


                
                //List<MessageDao> aList = new List<MessageDao>();
                //MessageDao aInfo = new MessageDao();
                 
                //    aInfo.isSuccess = "200";
                 

                //List<TourPlanInfoMasterDao> aDetailsList = new List<TourPlanInfoMasterDao>();
                //TourPlanInfoMasterDao aDetails;
                //for (var kk = 0; kk < dt.Rows.Count; kk++)

                //{

                //    aDetails = new TourPlanInfoMasterDao();
                //    if (dt.Rows[kk]["TourPlanId"] != DBNull.Value)
                //    {
                //        aDetails.TourPlanId = Convert.ToInt32(dt.Rows[kk]["TourPlanId"].ToString());
                //    }

                //    if (dt.Rows[kk]["TourPlanId"] != DBNull.Value)
                //    {
                //        aDetails.TourPlanId = Convert.ToInt32(dt.Rows[kk]["TourPlanId"].ToString());
                //    }



                //    aDetailsList.Add(aDetails);
                //}
                //aInfo.aTourPlanInfo = aDetailsList;
                //aList.Add(aInfo);

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



        public DataTable Get_TourPlanData_New(int month, int year, int empId)
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> aSqlParameterlist = new List<SqlParameter>();
                aSqlParameterlist.Add(new SqlParameter("@month", month));
                aSqlParameterlist.Add(new SqlParameter("@year", year));
                aSqlParameterlist.Add(new SqlParameter("@empId", empId));
                DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_TourPlanInfo_New", aSqlParameterlist);
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

        public DataTable GetTourPlanForWorkedwith(int empId, string tDate)
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> aSqlParameterlist = new List<SqlParameter>();
                aSqlParameterlist.Add(new SqlParameter("@empId", empId));
                aSqlParameterlist.Add(new SqlParameter("@tDate", tDate));
                DataTable dt = accessManager.GetDataTable("sp_Webapi_GetTourPlanForWorkedwith", aSqlParameterlist);
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

        public DataTable ValidateTimeInRange(string MainTime, string ShiftInfo)
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> aSqlParameterlist = new List<SqlParameter>();
                aSqlParameterlist.Add(new SqlParameter("@MainTime", MainTime));
                aSqlParameterlist.Add(new SqlParameter("@ShiftInfo", ShiftInfo));

                SqlParameter isValidParam = new SqlParameter("@IsValid", SqlDbType.Bit);
                isValidParam.Direction = ParameterDirection.Output;
                aSqlParameterlist.Add(isValidParam);

                SqlParameter messageParam = new SqlParameter("@Message", SqlDbType.NVarChar, 255);
                messageParam.Direction = ParameterDirection.Output;
                aSqlParameterlist.Add(messageParam);

                DataTable dt = accessManager.GetDataTable("sp_ValidateTimeOutOfRange", aSqlParameterlist);
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

        public DataTable GetMorningEveningTime(string ShiftInfo)
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> aSqlParameterlist = new List<SqlParameter>();
                aSqlParameterlist.Add(new SqlParameter("@ShiftInfo", ShiftInfo));
                DataTable dt = accessManager.GetDataTable("sp_Webapi_GetMorningEveningTime", aSqlParameterlist);
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

        public DataTable GetTourPlanForWorkedwithCopy(int empId, string tDate, string morEve)
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> aSqlParameterlist = new List<SqlParameter>();
                aSqlParameterlist.Add(new SqlParameter("@empId", empId));
                aSqlParameterlist.Add(new SqlParameter("@tDate", tDate));
                aSqlParameterlist.Add(new SqlParameter("@morEve", morEve));
                DataTable dt = accessManager.GetDataTable("sp_Webapi_GetTourPlanForWorkedwithCopy", aSqlParameterlist);
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

        public DataTable GetTourPlanEditbyId(int TourPlanId, string tDate)
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> aSqlParameterlist = new List<SqlParameter>();
                aSqlParameterlist.Add(new SqlParameter("@TourPlanId", TourPlanId));
                aSqlParameterlist.Add(new SqlParameter("@tDate", tDate));
                DataTable dt = accessManager.GetDataTable("sp_Webapi_GetTourPlanEditbyId", aSqlParameterlist);
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

        public DataTable GetOtherMarketVisitListTourPlanEditbyId(int TourPlanId, string tDate)
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> aSqlParameterlist = new List<SqlParameter>();
                aSqlParameterlist.Add(new SqlParameter("@TourPlanId", TourPlanId));
                aSqlParameterlist.Add(new SqlParameter("@tDate", tDate));
                DataTable dt = accessManager.GetDataTable("sp_Webapi_GetOtherMarketVisitListTourPlanEditbyId", aSqlParameterlist);
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



        public DataTable Get_TourPlanMasterData_new(int month, int year, int empId)
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> aSqlParameterlist = new List<SqlParameter>();
                aSqlParameterlist.Add(new SqlParameter("@month", month));
                aSqlParameterlist.Add(new SqlParameter("@year", year));
                aSqlParameterlist.Add(new SqlParameter("@empId", empId));
                DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_TourPlanMasterData", aSqlParameterlist);



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
        public IList<TourPlanInfoMasterDao> Get_TourPlanDataDetail(string month, string year, int empId, string Date)
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> aSqlParameterlist = new List<SqlParameter>();
                aSqlParameterlist.Add(new SqlParameter("@month", month));
                aSqlParameterlist.Add(new SqlParameter("@year", year));
                aSqlParameterlist.Add(new SqlParameter("@empId", empId));
                aSqlParameterlist.Add(new SqlParameter("@Date", Date));
                DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_TourPlanInfoDetail", aSqlParameterlist);



                //List<MessageDao> aList = new List<MessageDao>();
                //MessageDao aInfo = new MessageDao();

                //    aInfo.isSuccess = "200";


                List<TourPlanInfoMasterDao> aDetailsList = new List<TourPlanInfoMasterDao>();
                TourPlanInfoMasterDao aDetails;
                for (var kk = 0; kk < dt.Rows.Count; kk++)

                {

                    aDetails = new TourPlanInfoMasterDao();
                    if (dt.Rows[kk]["TourPlanId"] != DBNull.Value)
                    {
                        aDetails.TourPlanId = Convert.ToInt32(dt.Rows[kk]["TourPlanId"].ToString());
                    }

                    if (dt.Rows[kk]["TPMaster"] != DBNull.Value)
                    {
                        aDetails.TPMaster = Convert.ToInt32(dt.Rows[kk]["TPMaster"].ToString());
                    }

                    if (dt.Rows[kk]["TourTypeId"] != DBNull.Value)
                    {
                        aDetails.TourTypeId = Convert.ToInt32(dt.Rows[kk]["TourTypeId"].ToString());
                    }

                    if (dt.Rows[kk]["TPId"] != DBNull.Value)
                    {
                        aDetails.TPId = Convert.ToInt32(dt.Rows[kk]["TPId"].ToString());
                    }

                    if (dt.Rows[kk]["EmpInfoId"] != DBNull.Value)
                    {
                        aDetails.EmpInfoId = Convert.ToInt32(dt.Rows[kk]["EmpInfoId"].ToString());
                    }

                    if (dt.Rows[kk]["TPMaster"] != DBNull.Value)
                    {
                        aDetails.TPMaster = Convert.ToInt32(dt.Rows[kk]["TPMaster"].ToString());
                    }



                    if (dt.Rows[kk]["GroupId"] != DBNull.Value)
                    {
                        aDetails.GroupId = Convert.ToInt32(dt.Rows[kk]["GroupId"].ToString());
                    }

                    if (dt.Rows[kk]["GroupName"] != DBNull.Value)
                    {
                        aDetails.GroupName = (dt.Rows[kk]["GroupName"].ToString());
                    }



                    if (dt.Rows[kk]["RegionId"] != DBNull.Value)
                    {
                        aDetails.RegionId = Convert.ToInt32(dt.Rows[kk]["RegionId"].ToString());
                    }

                    if (dt.Rows[kk]["RegionName"] != DBNull.Value)
                    {
                        aDetails.RegionName = (dt.Rows[kk]["RegionName"].ToString());
                    }



                    if (dt.Rows[kk]["AreaId"] != DBNull.Value)
                    {
                        aDetails.AreaId = Convert.ToInt32(dt.Rows[kk]["AreaId"].ToString());
                    }

                    if (dt.Rows[kk]["AreaName"] != DBNull.Value)
                    {
                        aDetails.AreaName = (dt.Rows[kk]["AreaName"].ToString());
                    }



                    if (dt.Rows[kk]["TerritoryId"] != DBNull.Value)
                    {  
                        aDetails.TerritoryId = Convert.ToInt32(dt.Rows[kk]["TerritoryId"].ToString());
                    }

                    if (dt.Rows[kk]["TerritoryName"] != DBNull.Value)
                    {
                        aDetails.TerritoryName = (dt.Rows[kk]["TerritoryName"].ToString());
                    }



                    if (dt.Rows[kk]["SubTerritoryId"] != DBNull.Value)
                    {
                        aDetails.SubTerritoryId = Convert.ToInt32(dt.Rows[kk]["SubTerritoryId"].ToString());
                    }

                    if (dt.Rows[kk]["SubTerritoryName"] != DBNull.Value)
                    {
                        aDetails.SubTerritoryName = (dt.Rows[kk]["SubTerritoryName"].ToString());
                    }





                    if (dt.Rows[kk]["MarketId"] != DBNull.Value)
                    {
                        aDetails.MarketId = Convert.ToInt32(dt.Rows[kk]["MarketId"].ToString());
                    }

                    if (dt.Rows[kk]["MarketName"] != DBNull.Value)
                    {
                        aDetails.MarketName = (dt.Rows[kk]["MarketName"].ToString());
                    }

                    if (dt.Rows[kk]["Comment"] != DBNull.Value)
                    {
                        aDetails.Comment =  (dt.Rows[kk]["Comment"].ToString());
                    }

                  

                    if (dt.Rows[kk]["TourTypeName"] != DBNull.Value)
                    {
                        aDetails.TourTypeName = (dt.Rows[kk]["TourTypeName"].ToString());
                    }


                    if (dt.Rows[kk]["TPName"] != DBNull.Value)
                    {
                        aDetails.TPName = (dt.Rows[kk]["TPName"].ToString());
                    }

                    if (dt.Rows[kk]["MonthValue"] != DBNull.Value)
                    {
                        aDetails.MonthValue = (dt.Rows[kk]["MonthValue"].ToString());
                    }
                    if (dt.Rows[kk]["YearValue"] != DBNull.Value)
                    {
                        aDetails.YearValue = (dt.Rows[kk]["YearValue"].ToString());
                    }


                    if (dt.Rows[kk]["DayValue"] != DBNull.Value)
                    {
                        aDetails.DayValue = (dt.Rows[kk]["DayValue"].ToString());
                    }


                    if (dt.Rows[kk]["TourPlanDate"] != DBNull.Value)
                    {
                        aDetails.TourPlanDate = (dt.Rows[kk]["TourPlanDate"].ToString());
                    }



                    if (dt.Rows[kk]["IsMorning"] != DBNull.Value)
                    {
                        aDetails.IsMorning = (dt.Rows[kk]["IsMorning"].ToString());
                    }

                    if (dt.Rows[kk]["IsEvening"] != DBNull.Value)
                    {
                        aDetails.IsEvening = (dt.Rows[kk]["IsEvening"].ToString());
                    }

                    if (dt.Rows[kk]["IsStartTime"] != DBNull.Value)
                    {
                        aDetails.IsStartTime = (dt.Rows[kk]["IsStartTime"].ToString());
                    }

                    if (dt.Rows[kk]["Starttime"] != DBNull.Value)
                    {
                        aDetails.Starttime = (dt.Rows[kk]["Starttime"].ToString());
                    }

                    if (dt.Rows[kk]["IsEndtime"] != DBNull.Value)
                    {
                        aDetails.IsEndtime = (dt.Rows[kk]["IsEndtime"].ToString());
                    }

                    if (dt.Rows[kk]["Endtime"] != DBNull.Value)
                    {
                        aDetails.Endtime = (dt.Rows[kk]["Endtime"].ToString());
                    } 

                    if (dt.Rows[kk]["IsFinalSubmit"] != DBNull.Value)
                    {
                        aDetails.IsFinalSubmit = (dt.Rows[kk]["IsFinalSubmit"].ToString());
                    }


                    if (dt.Rows[kk]["SerialNo"] != DBNull.Value)
                    {
                        aDetails.SerialNo = (dt.Rows[kk]["SerialNo"].ToString());
                    }

                    if (dt.Rows[kk]["ApprovalStatus"] != DBNull.Value)
                    {
                        aDetails.ApprovalStatus = (dt.Rows[kk]["ApprovalStatus"].ToString());
                    }
                      if (dt.Rows[kk]["VisitedWithEmpInfoId"] != DBNull.Value)
                    {
                        aDetails.VisitedWithEmpInfoId = (dt.Rows[kk]["VisitedWithEmpInfoId"].ToString());
                    }
                          if (dt.Rows[kk]["VisitedWithEmpName"] != DBNull.Value)
                    {
                        aDetails.VisitedWithEmpName = (dt.Rows[kk]["VisitedWithEmpName"].ToString());
                    }


                    List<SqlParameter> asqChem = new List<SqlParameter>();

                    asqChem.Add(new SqlParameter("@TourPlanId", dt.Rows[kk]["TourPlanId"]));
                  
                    
                    DataTable dtDoctorChem = accessManager.GetDataTable("sp_Webapi_Get_TPCustomerDetailList", asqChem);
                    List<CustomerDAO> aDetailsListDD = new List<CustomerDAO>();
                    CustomerDAO aChemberDetails;
                    for (var ch = 0; ch < dtDoctorChem.Rows.Count; ch++)

                    {

                        aChemberDetails = new CustomerDAO();
                        if (dtDoctorChem.Rows[ch]["CustomerMasterId"] != DBNull.Value)
                        {
                            aChemberDetails.CustomerMasterId = Convert.ToInt32(dtDoctorChem.Rows[ch]["CustomerMasterId"].ToString());
                        }

                        if (dtDoctorChem.Rows[ch]["CustomerCode"] != DBNull.Value)
                        {
                            aChemberDetails.CustomerCode = dtDoctorChem.Rows[ch]["CustomerCode"].ToString();
                        }
                        if (dtDoctorChem.Rows[ch]["CustomerName"] != DBNull.Value)
                        {
                            aChemberDetails.CustomerName = dtDoctorChem.Rows[ch]["CustomerName"].ToString();
                        }
                        aDetailsListDD.Add(aChemberDetails);
                    }
                    aDetails.aCustomerMasterList = aDetailsListDD;


                    aDetailsList.Add(aDetails);
                }
                return aDetailsList;


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




        public IList<TourPlanViewModel> Get_TourPlanDataDetailNew(string month, string year, int empId, string Date)
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> aSqlParameterlist = new List<SqlParameter>();
                aSqlParameterlist.Add(new SqlParameter("@month", month));
                aSqlParameterlist.Add(new SqlParameter("@year", year));
                aSqlParameterlist.Add(new SqlParameter("@empId", empId));
                aSqlParameterlist.Add(new SqlParameter("@Date", Date));
                DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_TourPlanInfoDetail_new", aSqlParameterlist);



                //List<MessageDao> aList = new List<MessageDao>();
                //MessageDao aInfo = new MessageDao();

                //    aInfo.isSuccess = "200";


                List<TourPlanViewModel> aDetailsList = new List<TourPlanViewModel>();
                TourPlanViewModel aDetails;
                for (var kk = 0; kk < dt.Rows.Count; kk++)

                {

                    aDetails = new TourPlanViewModel();
                    try
                    {
                        if (dt.Rows[kk]["TourPlanId"] != DBNull.Value)
                        {
                            aDetails.TourPlanId = Convert.ToInt32(dt.Rows[kk]["TourPlanId"].ToString());
                        }
                    }
                    catch (Exception) { }

                    try
                    {
                        if (dt.Rows[kk]["TPMaster"] != DBNull.Value)
                        {
                            aDetails.TPMaster = Convert.ToInt32(dt.Rows[kk]["TPMaster"].ToString());
                        }
                    }
                    catch (Exception) { }
                    try
                    {
                        if (dt.Rows[kk]["TourTypeId"] != DBNull.Value)
                        {
                            aDetails.TourTypeId = Convert.ToInt32(dt.Rows[kk]["TourTypeId"].ToString());
                        }
                    }
                    catch (Exception) { }
                    try
                    {
                        if (dt.Rows[kk]["TPId"] != DBNull.Value)
                        {
                            aDetails.TPId = Convert.ToInt32(dt.Rows[kk]["TPId"].ToString());
                        }
                    }
                    catch (Exception) { }
                    try
                    {
                        if (dt.Rows[kk]["EmpInfoId"] != DBNull.Value)
                        {
                            aDetails.EmpInfoId = Convert.ToInt32(dt.Rows[kk]["EmpInfoId"].ToString());
                        }
                    }
                    catch (Exception) { }
                    try
                    {
                        if (dt.Rows[kk]["TPMaster"] != DBNull.Value)
                        {
                            aDetails.TPMaster = Convert.ToInt32(dt.Rows[kk]["TPMaster"].ToString());
                        }
                    }
                    catch (Exception) { }


                    try
                    {
                        if (dt.Rows[kk]["GroupId"] != DBNull.Value)
                        {
                            aDetails.GroupId = Convert.ToInt32(dt.Rows[kk]["GroupId"].ToString());
                        }
                    }
                    catch (Exception) { }
                    try
                    {
                        if (dt.Rows[kk]["GroupName"] != DBNull.Value)
                        {
                            aDetails.GroupName = (dt.Rows[kk]["GroupName"].ToString());
                        }
                    }
                    catch (Exception) { }

                    try
                    {

                        if (dt.Rows[kk]["RegionId"] != DBNull.Value)
                        {
                            aDetails.RegionId = Convert.ToInt32(dt.Rows[kk]["RegionId"].ToString());
                        }
                    }
                    catch (Exception) { }
                    try
                    {
                        if (dt.Rows[kk]["RegionName"] != DBNull.Value)
                        {
                            aDetails.RegionName = (dt.Rows[kk]["RegionName"].ToString());
                        }
                    }
                    catch (Exception) { }
                    try
                    {


                        if (dt.Rows[kk]["AreaId"] != DBNull.Value)
                        {
                            aDetails.AreaId = Convert.ToInt32(dt.Rows[kk]["AreaId"].ToString());
                        }
                    }
                    catch (Exception) { }
                    try
                    {
                        if (dt.Rows[kk]["AreaName"] != DBNull.Value)
                        {
                            aDetails.AreaName = (dt.Rows[kk]["AreaName"].ToString());
                        }
                    }
                    catch (Exception) { }


                    try
                    {
                        if (dt.Rows[kk]["TerritoryId"] != DBNull.Value)
                        {
                            aDetails.TerritoryId = Convert.ToInt32(dt.Rows[kk]["TerritoryId"].ToString());
                        }
                    }
                    catch (Exception) { }
                    try
                    {
                        if (dt.Rows[kk]["TerritoryName"] != DBNull.Value)
                        {
                            aDetails.TerritoryName = (dt.Rows[kk]["TerritoryName"].ToString());
                        }
                    }
                    catch (Exception) { }


                    try
                    {
                        if (dt.Rows[kk]["SubTerritoryId"] != DBNull.Value)
                        {
                            aDetails.SubTerritoryId = Convert.ToInt32(dt.Rows[kk]["SubTerritoryId"].ToString());
                        }
                    }
                    catch (Exception) { }
                    try
                    {
                        if (dt.Rows[kk]["SubTerritoryName"] != DBNull.Value)
                        {
                            aDetails.SubTerritoryName = (dt.Rows[kk]["SubTerritoryName"].ToString());
                        }
                    }
                    catch (Exception) { }




                    try
                    {
                        if (dt.Rows[kk]["MarketIdEnd"] != DBNull.Value)
                        {
                            aDetails.MarketIdEnd = Convert.ToInt32(dt.Rows[kk]["MarketIdEnd"].ToString());
                        }
                    }
                    catch (Exception) { } try
                    {
                        if (dt.Rows[kk]["MarketId"] != DBNull.Value)
                        {
                            aDetails.MarketId = Convert.ToInt32(dt.Rows[kk]["MarketId"].ToString());
                        }
                    }
                    catch (Exception) { }
                    try
                    {
                        if (dt.Rows[kk]["MarketName"] != DBNull.Value)
                        {
                            aDetails.MarketName = (dt.Rows[kk]["MarketName"].ToString());
                        }
                    }
                    catch (Exception) { }
                    try
                    {
                        if (dt.Rows[kk]["Comment"] != DBNull.Value)
                        {
                            aDetails.Comment = (dt.Rows[kk]["Comment"].ToString());
                        }
                    }
                    catch (Exception) { }
                      try
                    {
                        if (dt.Rows[kk]["Objective"] != DBNull.Value)
                        {
                            aDetails.Objective = (dt.Rows[kk]["Objective"].ToString());
                        }
                    }
                    catch (Exception) { }


                    try
                    {
                        if (dt.Rows[kk]["TourTypeName"] != DBNull.Value)
                        {
                            aDetails.TourTypeName = (dt.Rows[kk]["TourTypeName"].ToString());
                        }
                    }
                    catch (Exception) { }

                    try
                    {
                        if (dt.Rows[kk]["TPName"] != DBNull.Value)
                        {
                            aDetails.TPName = (dt.Rows[kk]["TPName"].ToString());
                        }
                    }
                    catch (Exception) { }
                    try
                    {
                        if (dt.Rows[kk]["MonthValue"] != DBNull.Value)
                        {
                            aDetails.MonthValue = Convert.ToInt32(dt.Rows[kk]["MonthValue"].ToString());
                        }
                    }
                    catch (Exception) { }


                    try
                    {
                        if (dt.Rows[kk]["YearValue"] != DBNull.Value)
                        {
                            aDetails.YearValue = Convert.ToInt32(dt.Rows[kk]["YearValue"].ToString());
                        }
                    }
                    catch (Exception) { }

                    try
                    {
                        if (dt.Rows[kk]["DayValue"] != DBNull.Value)
                        {
                            aDetails.DayValue = Convert.ToInt32(dt.Rows[kk]["DayValue"].ToString());
                        }
                    }
                    catch (Exception) { }

                    try
                    {
                        if (dt.Rows[kk]["TourPlanDate"] != DBNull.Value)
                        {
                            aDetails.TourPlanDate = (dt.Rows[kk]["TourPlanDate"].ToString());
                        }
                    }
                    catch (Exception) { }


                    try
                    {
                        if (dt.Rows[kk]["IsMorning"] != DBNull.Value)
                        {
                            aDetails.IsMorning = (dt.Rows[kk]["IsMorning"].ToString());
                        }
                    }
                    catch (Exception) { }
                    try
                    {
                        if (dt.Rows[kk]["IsEvening"] != DBNull.Value)
                        {
                            aDetails.IsEvening = (dt.Rows[kk]["IsEvening"].ToString());
                        }
                    }
                    catch (Exception) { }
                    try
                    {
                        if (dt.Rows[kk]["IsStartTime"] != DBNull.Value)
                        {
                            aDetails.IsStartTime = (dt.Rows[kk]["IsStartTime"].ToString());
                        }
                    }
                    catch (Exception) { }
                    try
                    {
                        if (dt.Rows[kk]["Starttime"] != DBNull.Value)
                        {
                            aDetails.Starttime = (dt.Rows[kk]["Starttime"].ToString());
                        }
                    }
                    catch (Exception) { }
                    try
                    {
                        if (dt.Rows[kk]["IsEndtime"] != DBNull.Value)
                        {
                            aDetails.IsEndtime = (dt.Rows[kk]["IsEndtime"].ToString());
                        }
                    }
                    catch (Exception) { }
                    try
                    {
                        if (dt.Rows[kk]["Endtime"] != DBNull.Value)
                        {
                            aDetails.Endtime = (dt.Rows[kk]["Endtime"].ToString());
                        }
                    }
                    catch (Exception) { }
                    try
                    {
                        if (dt.Rows[kk]["IsFinalSubmit"] != DBNull.Value)
                        {
                            aDetails.IsFinalSubmit = Convert.ToBoolean(dt.Rows[kk]["IsFinalSubmit"].ToString());
                        }
                    }
                    catch (Exception) { }
                    try
                    {
                        if (dt.Rows[kk]["SerialNo"] != DBNull.Value)
                        {
                            aDetails.SerialNo = Convert.ToInt32(dt.Rows[kk]["SerialNo"].ToString());
                        }
                    }
                    catch (Exception) { }
                    try
                    {
                        if (dt.Rows[kk]["ApprovalStatus"] != DBNull.Value)
                        {
                            aDetails.ApprovalStatus = (dt.Rows[kk]["ApprovalStatus"].ToString());
                        }
                    }
                    catch (Exception) { }
                    try
                    {
                        if (dt.Rows[kk]["VisitedWithEmpInfoId"] != DBNull.Value)
                        {
                            aDetails.VisitedWithEmpInfoId = Convert.ToInt32(dt.Rows[kk]["VisitedWithEmpInfoId"].ToString());
                        }
                    }
                    catch (Exception) { }
                    try
                    {
                        if (dt.Rows[kk]["VisitedWithEmpName"] != DBNull.Value)
                        {
                            aDetails.VisitedWithEmpName = (dt.Rows[kk]["VisitedWithEmpName"].ToString());
                        }
                    }
                    catch (Exception) { }


                    try
                    {
                        if (dt.Rows[kk]["IsMarketVisit"] != DBNull.Value)
                        {
                            aDetails.IsMarketVisit = (dt.Rows[kk]["IsMarketVisit"].ToString());
                        }
                    }
                    catch (Exception) { }

                    try
                    {
                        if (dt.Rows[kk]["IsOtherVisit"] != DBNull.Value)
                        {
                            aDetails.IsOtherVisit = (dt.Rows[kk]["IsOtherVisit"].ToString());
                        }
                    }
                    catch (Exception) { }
                    try
                    {
                        if (dt.Rows[kk]["IsMorning"] != DBNull.Value)
                        {
                            aDetails.IsMorning = (dt.Rows[kk]["IsMorning"].ToString());
                        }
                    }
                    catch (Exception) { }
                    try
                    {
                        if (dt.Rows[kk]["IsEvening"] != DBNull.Value)
                        {
                            aDetails.IsEvening = (dt.Rows[kk]["IsEvening"].ToString());
                        }
                    }
                    catch (Exception) { }

                    try
                    {
                        aDetails.MarketName = (dt.Rows[kk]["MarketName"].ToString());
                    }
                    catch (Exception) { }
                    try
                    {
                        aDetails.Starttime = (dt.Rows[kk]["Starttime"].ToString());
                    }
                    catch (Exception) { }
                    try
                    {
                        aDetails.MarketNameEnd = (dt.Rows[kk]["MarketNameEnd"].ToString());
                    }
                    catch (Exception) { }
                    try
                    {
                        aDetails.Endtime = (dt.Rows[kk]["Endtime"].ToString());
                    }
                    catch (Exception) { }


                    List<SqlParameter> asqChem = new List<SqlParameter>();

                    asqChem.Add(new SqlParameter("@TourPlanId", dt.Rows[kk]["TourPlanId"]));


                    DataTable dtDoctorChem = accessManager.GetDataTable("sp_Webapi_Get_TPCustomerDetailList", asqChem);
                    List<Customer> aDetailsListDD = new List<Customer>();
                    Customer aChemberDetails;
                    for (var ch = 0; ch < dtDoctorChem.Rows.Count; ch++)

                    {

                        aChemberDetails = new Customer();
                        if (dtDoctorChem.Rows[ch]["CustomerMasterId"] != DBNull.Value)
                        {
                            aChemberDetails.CustomerMasterId = Convert.ToInt32(dtDoctorChem.Rows[ch]["CustomerMasterId"].ToString());
                        }

                        if (dtDoctorChem.Rows[ch]["CustomerCode"] != DBNull.Value)
                        {
                            aChemberDetails.CustomerCode = dtDoctorChem.Rows[ch]["CustomerCode"].ToString();
                        }
                        if (dtDoctorChem.Rows[ch]["CustomerName"] != DBNull.Value)
                        {
                            aChemberDetails.CustomerName = dtDoctorChem.Rows[ch]["CustomerName"].ToString();
                        }
                        aDetailsListDD.Add(aChemberDetails);
                    }
                    aDetails.aCustomerMasterList = aDetailsListDD;

                    DataTable dtMarket = accessManager.GetDataTable("sp_Webapi_Get_TPMarketDetailList", asqChem);
                    List<Market> aMarsListDD = new List<Market>();
                    Market aMarketDetails;
                    for (var ch = 0; ch < dtMarket.Rows.Count; ch++)

                    {

                        aMarketDetails = new Market();
                        if (dtMarket.Rows[ch]["MarketId"] != DBNull.Value)
                        {
                            aMarketDetails.MarketId = Convert.ToInt32(dtMarket.Rows[ch]["MarketId"].ToString());
                        }

                        if (dtMarket.Rows[ch]["MarketName"] != DBNull.Value)
                        {
                            aMarketDetails.MarketName = dtMarket.Rows[ch]["MarketName"].ToString();
                        }
                        aMarsListDD.Add(aMarketDetails);
                    }
                    aDetails.aVisitedMarketList = aMarsListDD;


                    aDetailsList.Add(aDetails);
                }
                return aDetailsList;


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

        public ResultInfo DeleteTourPlan(int id)
        {


            ResultInfo aInformation = new ResultInfo();
            try

            {

                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> gSqlParameterList = new List<SqlParameter>();
                gSqlParameterList.Add(new SqlParameter("@id", id));
                aInformation.isSuccess = accessManager.DeleteData("sp_Webapi_delete_TourPlanData", gSqlParameterList);

            }
            catch (Exception exception)
            {
                accessManager.SqlConnectionClose(true);
                aInformation.isSuccess = false;
                aInformation.ErrorMessage = exception.Message;
                throw exception;
            }
            finally
            {
                accessManager.SqlConnectionClose();
            }

            return aInformation;



        }


        public ResultInfo FinalSubmitTour(int empId,int month,int year,String remarks)
        {

            ResultInfo aInformation = new ResultInfo();
            try

            {

                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> gSqlParameterList = new List<SqlParameter>();
                gSqlParameterList.Add(new SqlParameter("@empId", empId));
                gSqlParameterList.Add(new SqlParameter("@month", month));
                gSqlParameterList.Add(new SqlParameter("@year", year));
                gSqlParameterList.Add(new SqlParameter("@remarks", remarks));
                aInformation.isSuccess = accessManager.UpdateData("sp_Webapi_FinalSubmitSend_TourPlan", gSqlParameterList);

            }
            catch (Exception exception)
            {
                accessManager.SqlConnectionClose(true);
                aInformation.isSuccess = false;
                aInformation.ErrorMessage = exception.Message;
                throw exception;
            }
            finally
            {
                accessManager.SqlConnectionClose();
            }

            return aInformation;



        }


        public ResultInfo FinalSubmitDWSP(int empId, int month, int year, String remarks)
        {

            ResultInfo aInformation = new ResultInfo();
            try

            {

                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> gSqlParameterList = new List<SqlParameter>();
                gSqlParameterList.Add(new SqlParameter("@empId", empId));
                gSqlParameterList.Add(new SqlParameter("@month", month));
                gSqlParameterList.Add(new SqlParameter("@year", year));
                gSqlParameterList.Add(new SqlParameter("@remarks", remarks));


                DataTable dtDoctorChem = accessManager.GetDataTable("sp_Webapi_FinalSubmitSend_Check", gSqlParameterList);

                if (dtDoctorChem.Rows.Count == 2)
                {
                    aInformation.isSuccess = accessManager.UpdateData("sp_Webapi_FinalSubmitSend_DWSP", gSqlParameterList);
                }
                else
                {
                    aInformation.isSuccess = false;
                    aInformation.Msd = dtDoctorChem.Rows[0]["col"].ToString();
                }
              

               

            }
            catch (Exception exception)
            {
                accessManager.SqlConnectionClose(true);
                aInformation.isSuccess = false;
                aInformation.ErrorMessage = exception.Message;
                throw exception;
            }
            finally
            {
                accessManager.SqlConnectionClose();
            }

            return aInformation;



        }


        public IList<tpStatusDAO> Get_TourPlanMasterData(int month, int year, int empId)
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> aSqlParameterlist = new List<SqlParameter>();
                aSqlParameterlist.Add(new SqlParameter("@empId", empId));
                aSqlParameterlist.Add(new SqlParameter("@month", month));
                aSqlParameterlist.Add(new SqlParameter("@year", year));
                DataTable dt = accessManager.GetDataTable("sp_webapi_GetTourPlanStatus", aSqlParameterlist);

                List<tpStatusDAO> aDetailsList = new List<tpStatusDAO>();
                tpStatusDAO aDetails;
                for (var kk = 0; kk < dt.Rows.Count; kk++)

                {

                    aDetails = new tpStatusDAO();
                    if (dt.Rows[kk]["TPMaster"] != DBNull.Value)
                    {
                        aDetails.TPMaster = Convert.ToInt32(dt.Rows[kk]["TPMaster"].ToString());
                    }

                    if (dt.Rows[kk]["MonthValue"] != DBNull.Value)
                    {
                        aDetails.MonthValue = (dt.Rows[kk]["MonthValue"].ToString());
                    }

                    if (dt.Rows[kk]["YearValue"] != DBNull.Value)
                    {
                        aDetails.YearValue = (dt.Rows[kk]["YearValue"].ToString());
                    }

                    if (dt.Rows[kk]["EmpInfoId"] != DBNull.Value)
                    {
                        aDetails.EmpInfoId = Convert.ToInt32(dt.Rows[kk]["EmpInfoId"].ToString());
                    }


                    if (dt.Rows[kk]["IsFinalSubmit"] != DBNull.Value)
                    {
                        aDetails.IsFinalSubmit = (dt.Rows[kk]["IsFinalSubmit"].ToString());
                    }

                    if (dt.Rows[kk]["ApprovalStatus"] != DBNull.Value)
                    {
                        aDetails.ApprovalStatus = (dt.Rows[kk]["ApprovalStatus"].ToString());
                    }
                    if (dt.Rows[kk]["ApprovedBy"] != DBNull.Value)
                    {
                        aDetails.ApprovedBy = Convert.ToInt32(dt.Rows[kk]["ApprovedBy"].ToString());
                    }


                    if (dt.Rows[kk]["ApprovedDate"] != DBNull.Value)
                    {
                        aDetails.ApprovedDate = (dt.Rows[kk]["ApprovedDate"].ToString());
                    }


                    if (dt.Rows[kk]["FinalSubmitRemarks"] != DBNull.Value)
                    {
                        aDetails.FinalSubmitRemarks = (dt.Rows[kk]["FinalSubmitRemarks"].ToString());
                    }




                    if (dt.Rows[kk]["ApprovalRemarks"] != DBNull.Value)
                    {
                        aDetails.ApprovalRemarks = (dt.Rows[kk]["ApprovalRemarks"].ToString());
                    }


                    

                    List<SqlParameter> asqChem = new List<SqlParameter>();

                    asqChem.Add(new SqlParameter("@EmpInfoId", empId));
                    asqChem.Add(new SqlParameter("@Month", month));
                    asqChem.Add(new SqlParameter("@year", year));

                    DataTable dtDoctorChem = accessManager.GetDataTable("sp_Webapi_Get_TourPlanBalance", asqChem);
                    List<TourTypeDAO> aDetailsListDD = new List<TourTypeDAO>();
                    TourTypeDAO aChemberDetails;
                    for (var ch = 0; ch < dtDoctorChem.Rows.Count; ch++)

                    {

                        aChemberDetails = new TourTypeDAO();
                       
                        if (dtDoctorChem.Rows[ch]["StationTypeName"] != DBNull.Value)
                        {
                            aChemberDetails.TourTypeName = dtDoctorChem.Rows[ch]["StationTypeName"].ToString();
                        }
                        if (dtDoctorChem.Rows[ch]["Balance"] != DBNull.Value)
                        {
                            aChemberDetails.Balance = dtDoctorChem.Rows[ch]["Balance"].ToString();
                        }
                        aDetailsListDD.Add(aChemberDetails);
                    }
                    aDetails.aTourTypeDAOList = aDetailsListDD;


                    aDetailsList.Add(aDetails);
                }
                return aDetailsList;
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

        public ResultInfo SaveTourPlan_ApplogDAL(SaveTourPlan_Applog_DAO atten)
        {
            ResultInfo aInformation = new ResultInfo();
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);

                List<SqlParameter> gSqlParameterList = new List<SqlParameter>();
                DateTime entryDtae = DateTime.Now;

                gSqlParameterList.Add(new SqlParameter("@TourPlanApprovalId", atten.TourPlanApprovalId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@Date", atten.Date ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@FromEmpId", atten.FromEmpId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@ToEmpId", atten.ToEmpId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@TableId", atten.TableId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@Status", atten.Status ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@Comments", atten.Comments ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@Type", atten.Type ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@Step", atten.Step ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@GroupId", atten.GroupId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@RegionId", atten.RegionId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@AreaId", atten.AreaId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@TerritoryId", atten.TerritoryId ?? (object)DBNull.Value ?? (object)DBNull.Value));

                gSqlParameterList.Add(new SqlParameter("@ToGroupId", atten.ToGroupId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@ToRegionId", atten.ToRegionId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@ToAreaId", atten.ToAreaId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@ToTerritoryId", atten.ToTerritoryId ?? (object)DBNull.Value ?? (object)DBNull.Value));

                gSqlParameterList.Add(new SqlParameter("@EntryByS", atten.EntryByS ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@EntryDateS", atten.EntryDateS ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@EntryTimeS", atten.EntryTimeS ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@ApproveByS", atten.ApproveByS ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@ApproveDateS", atten.ApproveDateS ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@ApproveTimeS", atten.ApproveTimeS ?? (object)DBNull.Value ?? (object)DBNull.Value));

                gSqlParameterList.Add(new SqlParameter("@EntryByApp", atten.EntryByApp ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@EntryDateApp", atten.EntryDateApp ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@EntryTimeApp", atten.EntryTimeApp ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@ApproveByApp", atten.ApproveByApp ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@ApproveDateApp", atten.ApproveDateApp ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@ApproveTimeApp", atten.ApproveTimeApp ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@MenuId", atten.MenuId ?? (object)DBNull.Value ?? (object)DBNull.Value));

                aInformation.isSuccess = accessManager.SaveData("sp_webapi_SaveTourPlanAppLog", gSqlParameterList);

            }
            catch (Exception exception)
            {
                accessManager.SqlConnectionClose(true);
                aInformation.isSuccess = false;
                aInformation.ErrorMessage = exception.Message;

                throw exception;
            }
            finally
            {
                accessManager.SqlConnectionClose();
            }

            return aInformation;
        }


        public ResultInfo SaveDWSP_ApplogDAL(SaveDWSP_Applog_DAO atten)
        {
            ResultInfo aInformation = new ResultInfo();
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);

                List<SqlParameter> gSqlParameterList = new List<SqlParameter>();
                DateTime entryDtae = DateTime.Now;

                gSqlParameterList.Add(new SqlParameter("@DWSPApprovalId", atten.DWSPApprovalId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@Date", atten.Date ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@FromEmpId", atten.FromEmpId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@ToEmpId", atten.ToEmpId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@TableId", atten.TableId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@Status", atten.Status ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@Comments", atten.Comments ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@Type", atten.Type ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@Step", atten.Step ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@GroupId", atten.GroupId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@RegionId", atten.RegionId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@AreaId", atten.AreaId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@TerritoryId", atten.TerritoryId ?? (object)DBNull.Value ?? (object)DBNull.Value));

                gSqlParameterList.Add(new SqlParameter("@ToGroupId", atten.ToGroupId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@ToRegionId", atten.ToRegionId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@ToAreaId", atten.ToAreaId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@ToTerritoryId", atten.ToTerritoryId ?? (object)DBNull.Value ?? (object)DBNull.Value));

                gSqlParameterList.Add(new SqlParameter("@EntryByS", atten.EntryByS ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@EntryDateS", atten.EntryDateS ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@EntryTimeS", atten.EntryTimeS ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@ApproveByS", atten.ApproveByS ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@ApproveDateS", atten.ApproveDateS ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@ApproveTimeS", atten.ApproveTimeS ?? (object)DBNull.Value ?? (object)DBNull.Value));

                gSqlParameterList.Add(new SqlParameter("@EntryByApp", atten.EntryByApp ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@EntryDateApp", atten.EntryDateApp ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@EntryTimeApp", atten.EntryTimeApp ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@ApproveByApp", atten.ApproveByApp ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@ApproveDateApp", atten.ApproveDateApp ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@ApproveTimeApp", atten.ApproveTimeApp ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@MenuId", atten.MenuId ?? (object)DBNull.Value ?? (object)DBNull.Value));

                aInformation.isSuccess = accessManager.SaveData("sp_webapi_SaveDWSPLog", gSqlParameterList);

            }
            catch (Exception exception)
            {
                accessManager.SqlConnectionClose(true);
                aInformation.isSuccess = false;
                aInformation.ErrorMessage = exception.Message;

                throw exception;
            }
            finally
            {
                accessManager.SqlConnectionClose();
            }

            return aInformation;
        }
        
        public IList<TourPlanInfo_Applog_DAO> GetTourPlan_ApplogDAL(string pram, string Role, string AppStatus, string FromDt, string ToDt, string EmpId, int? MonthValue, int? YearValue)
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> gSqlParameterList = new List<SqlParameter>(); 
                gSqlParameterList.Add(new SqlParameter("@Role", Role ?? (object)DBNull.Value ?? (object)DBNull.Value));
                pram = NewMethod(pram, Role, gSqlParameterList);
                gSqlParameterList.Add(new SqlParameter("@AppStatus", AppStatus ?? (object)DBNull.Value ?? (object)DBNull.Value));

                gSqlParameterList.Add(new SqlParameter("@FromDt", FromDt ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@ToDt", ToDt ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@EmpId", EmpId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@MonthValue", MonthValue ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@YearValue", YearValue ?? (object)DBNull.Value ?? (object)DBNull.Value));


                DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_TourPlanApp", gSqlParameterList);
                //SqlDataReader dr = accessManager.GetSqlDataReader("sp_Webapi_Get_DoctorClaimApp", gSqlParameterList);
                List<TourPlanInfo_Applog_DAO> aList = new List<TourPlanInfo_Applog_DAO>();
                for (var i = 0; i < dt.Rows.Count; i++)

                {
                    TourPlanInfo_Applog_DAO aInfo = new TourPlanInfo_Applog_DAO();


                    if (dt.Rows[i]["TPMaster"] != DBNull.Value)
                    {
                        aInfo.TPMaster = Convert.ToInt32(dt.Rows[i]["TPMaster"].ToString());
                    }

                    if (dt.Rows[i]["MonthValue"] != DBNull.Value)
                    {
                        aInfo.MonthValue = Convert.ToInt32(dt.Rows[i]["MonthValue"]);
                    }

                    if (dt.Rows[i]["YearValue"] != DBNull.Value)
                    {
                        aInfo.YearValue = Convert.ToInt32(dt.Rows[i]["YearValue"]);
                    }

                    if (dt.Rows[i]["TourPlanApprovalId"] != DBNull.Value)
                    {
                        aInfo.TourPlanApprovalId = Convert.ToInt32(dt.Rows[i]["TourPlanApprovalId"]);
                    }


                    if (dt.Rows[i]["EmpInfoId"] != DBNull.Value)
                    {
                        aInfo.EmpInfoId = Convert.ToInt32(dt.Rows[i]["EmpInfoId"].ToString());

                    }

                    if (dt.Rows[i]["Date"] != DBNull.Value)
                    {
                        aInfo.Date = (dt.Rows[i]["Date"].ToString());
                    }


                    if (dt.Rows[i]["FromEmpId"] != DBNull.Value)
                    {
                        aInfo.FromEmpId = Convert.ToInt32(dt.Rows[i]["FromEmpId"].ToString());
                    }



                    if (dt.Rows[i]["ToEmpId"] != DBNull.Value)
                    {
                        aInfo.ToEmpId = Convert.ToInt32(dt.Rows[i]["ToEmpId"].ToString());
                    }
                    if (dt.Rows[i]["TableId"] != DBNull.Value)
                    {
                        aInfo.TableId = Convert.ToInt32(dt.Rows[i]["TableId"].ToString());
                    }
                    if (dt.Rows[i]["Status"] != DBNull.Value)
                    {
                        aInfo.Status = dt.Rows[i]["Status"].ToString();
                    }
                    if (dt.Rows[i]["Comments"] != DBNull.Value)
                    {
                        aInfo.Comments = dt.Rows[i]["Comments"].ToString();
                    }

                    if (dt.Rows[i]["Type"] != DBNull.Value)
                    {
                        aInfo.Type = (dt.Rows[i]["Type"].ToString());
                    }


                    if (dt.Rows[i]["Step"] != DBNull.Value)
                    {
                        aInfo.Step = Convert.ToInt32(dt.Rows[i]["Step"].ToString());
                    }



                    if (dt.Rows[i]["ApprovalStatus"] != DBNull.Value)
                    {
                        aInfo.ApprovalStatus = dt.Rows[i]["ApprovalStatus"].ToString();
                    }











                    if (dt.Rows[i]["GroupId"] != DBNull.Value)
                    {
                        aInfo.GroupId = Convert.ToInt32(dt.Rows[i]["GroupId"].ToString());
                    }

                    if (dt.Rows[i]["RegionId"] != DBNull.Value)
                    {
                        aInfo.RegionId = Convert.ToInt32(dt.Rows[i]["RegionId"].ToString());
                    }


                    if (dt.Rows[i]["AreaId"] != DBNull.Value)
                    {
                        aInfo.AreaId = Convert.ToInt32(dt.Rows[i]["AreaId"].ToString());
                    }


                    if (dt.Rows[i]["TerritoryId"] != DBNull.Value)
                    {
                        aInfo.TerritoryId = Convert.ToInt32(dt.Rows[i]["TerritoryId"].ToString());
                    }


                    if (dt.Rows[i]["RoleTypeId"] != DBNull.Value)
                    {
                        aInfo.RoleTypeId = Convert.ToInt32(dt.Rows[i]["RoleTypeId"].ToString());
                    }


                    if (dt.Rows[i]["EmpMasterCode"] != DBNull.Value)
                    {
                        aInfo.EmpMasterCode = dt.Rows[i]["EmpMasterCode"].ToString();
                    }


                    if (dt.Rows[i]["EmpName"] != DBNull.Value)
                    {
                        aInfo.EmpName = dt.Rows[i]["EmpName"].ToString();
                    }


                    if (dt.Rows[i]["TerritoryName"] != DBNull.Value)
                    {
                        aInfo.TerritoryName = dt.Rows[i]["TerritoryName"].ToString();
                    }

                    if (dt.Rows[i]["TerritoryCode"] != DBNull.Value)
                    {
                        aInfo.TerritoryCode = dt.Rows[i]["TerritoryCode"].ToString();
                    }

                    if (dt.Rows[i]["AreaCode"] != DBNull.Value)
                    {
                        aInfo.AreaCode = dt.Rows[i]["AreaCode"].ToString();
                    }


                    if (dt.Rows[i]["ToRoleTypeId"] != DBNull.Value)
                    {
                        aInfo.ToRoleTypeId = Convert.ToInt32(dt.Rows[i]["ToRoleTypeId"].ToString());
                    }

                    if (dt.Rows[i]["MIOEmpId"] != DBNull.Value)
                    {
                        aInfo.MIOEmpId = Convert.ToInt32(dt.Rows[i]["MIOEmpId"].ToString());
                    }

                    if (dt.Rows[i]["ASMEMPId"] != DBNull.Value)
                    {
                        aInfo.ASMEMPId = Convert.ToInt32(dt.Rows[i]["ASMEMPId"].ToString());
                    }

                    if (dt.Rows[i]["RSMEMPId"] != DBNull.Value)
                    {
                        aInfo.RSMEMPId = Convert.ToInt32(dt.Rows[i]["RSMEMPId"].ToString());
                    }
                    if (dt.Rows[i]["NSMEMPId"] != DBNull.Value)
                    {
                        aInfo.NSMEMPId = Convert.ToInt32(dt.Rows[i]["NSMEMPId"].ToString());
                    }


                    List<SqlParameter> aSqlParameterListDetals = new List<SqlParameter>();

                    aSqlParameterListDetals.Add(new SqlParameter("@id", aInfo.TableId));
                    DataTable dtDoctor = accessManager.GetDataTable("sp_webapi_GetTourPlanMasterById", aSqlParameterListDetals);


                    TourPlanMasterDAO aDetails;


                    aDetails = new TourPlanMasterDAO();

                    if (dtDoctor.Rows[0]["TPMaster"] != DBNull.Value)
                    {
                        aDetails.TPMaster = Convert.ToInt32(dtDoctor.Rows[0]["TPMaster"].ToString());
                    }
                    if (dtDoctor.Rows[0]["EmpInfoId"] != DBNull.Value)
                    {
                        aDetails.EmpInfoId = Convert.ToInt32(dtDoctor.Rows[0]["EmpInfoId"].ToString());
                    }

                    if (dtDoctor.Rows[0]["MonthValue"] != DBNull.Value)
                    {
                        aDetails.MonthValue =  (dtDoctor.Rows[0]["MonthValue"].ToString());
                    }

                    if (dtDoctor.Rows[0]["YearValue"] != DBNull.Value)
                    {
                        aDetails.YearValue = dtDoctor.Rows[0]["YearValue"].ToString();
                    }


                    if (dtDoctor.Rows[0]["MonthName"] != DBNull.Value)
                    {
                        aDetails.MonthName = dtDoctor.Rows[0]["MonthName"].ToString();
                    }


                    if (dtDoctor.Rows[0]["IsFinalSubmit"] != DBNull.Value)
                    {
                        aDetails.IsFinalSubmit = Convert.ToBoolean(dtDoctor.Rows[0]["IsFinalSubmit"].ToString());
                    }


                    if (dtDoctor.Rows[0]["ApprovalStatus"] != DBNull.Value)
                    {
                        aDetails.ApprovalStatus = dtDoctor.Rows[0]["ApprovalStatus"].ToString();
                    }


                    List<SqlParameter> aSqlParameters = new List<SqlParameter>();
                    aSqlParameters.Add(new SqlParameter("@EmpInfoId", aDetails.EmpInfoId ));
                     
                    aSqlParameters.Add(new SqlParameter("@Month", MonthValue));
                    aSqlParameters.Add(new SqlParameter("@year", YearValue));
                    DataTable dtst = accessManager.GetDataTable("sp_Webapi_Get_TourPlanBalance", aSqlParameters);
                    for (var ch = 0; ch < dtst.Rows.Count; ch++)

                    {
                        if (dtst.Rows[ch]["StationTypeId"] != DBNull.Value)
                        {
                            if (dtst.Rows[ch]["StationTypeId"].ToString() == "2")
                            {
                                aDetails.ExHQ = dtst.Rows[ch]["Balance"].ToString();

                            }

                            if (dtst.Rows[ch]["StationTypeId"].ToString() == "1")
                            {
                                aDetails.HQ = dtst.Rows[ch]["Balance"].ToString();

                            }

                            if (dtst.Rows[ch]["StationTypeId"].ToString() == "3")
                            {
                                aDetails.OS = dtst.Rows[ch]["Balance"].ToString();

                            }
                        }

                    }

                    if (aDetails.OS == null)
                    {
                        aDetails.OS = "0";
                    }
                    if (aDetails.HQ == null)
                    {
                        aDetails.HQ = "0";
                    }

                    if (aDetails.ExHQ == null)
                    {
                        aDetails.ExHQ = "0";
                    }


                    List<SqlParameter> asqChem = new List<SqlParameter>();

                    asqChem.Add(new SqlParameter("@id", aInfo.TableId));
                    DataTable dtDoctorChem = accessManager.GetDataTable("sp_webapi_GetTourPlanDateById", asqChem);
                    List<TourPlanDtlsDAODate> aDetailsList = new List<TourPlanDtlsDAODate>();
                    TourPlanDtlsDAODate aChemberDetails;
                    for (var ch = 0; ch < dtDoctorChem.Rows.Count; ch++)

                    {



                        aChemberDetails = new TourPlanDtlsDAODate();
                     


                        if (dtDoctorChem.Rows[ch]["TourPlanDate"] != DBNull.Value)
                        {
                         string   TourPlanDate = dtDoctorChem.Rows[ch]["TourPlanDate"].ToString();


                            if (dtDoctorChem.Rows[ch]["DayValue"] != DBNull.Value)
                            {
                                aChemberDetails.DayValue = dtDoctorChem.Rows[ch]["DayValue"].ToString();

                            }
                            if (dtDoctorChem.Rows[ch]["DayName"] != DBNull.Value)
                            {
                                aChemberDetails.DayName = dtDoctorChem.Rows[ch]["DayName"].ToString();

                            }
                            List<SqlParameter> asqDetls = new List<SqlParameter>();

                            asqDetls.Add(new SqlParameter("@id", aInfo.TableId));
                            asqDetls.Add(new SqlParameter("@TourPlanDate", TourPlanDate));
                            DataTable dtDtl = accessManager.GetDataTable("sp_webapi_GetTourPlanDetailById_new", asqDetls);
                            List<TourPlanDtlsDAO> amarketList = new List<TourPlanDtlsDAO>();
                            TourPlanDtlsDAO aMrkDtls;
                            for (var chmr = 0; chmr < dtDtl.Rows.Count; chmr++)

                            {

                                aMrkDtls = new TourPlanDtlsDAO();

                                if (dtDtl.Rows[chmr]["CustomerName"] != DBNull.Value)
                                {
                                    aMrkDtls.CustomerName = dtDtl.Rows[chmr]["CustomerName"].ToString();
                                }
                                if (dtDtl.Rows[chmr]["MarketName"] != DBNull.Value)
                                {
                                    aMrkDtls.MarketName = dtDtl.Rows[chmr]["MarketName"].ToString();
                                }
                                   if (dtDtl.Rows[chmr]["MarketNameEnd"] != DBNull.Value)
                                {
                                    aMrkDtls.MarketNameEnd = dtDtl.Rows[chmr]["MarketNameEnd"].ToString();
                                }
                                        if (dtDtl.Rows[chmr]["OtherMarketName"] != DBNull.Value)
                                {
                                    aMrkDtls.OtherMarketName = dtDtl.Rows[chmr]["OtherMarketName"].ToString();
                                }    if (dtDtl.Rows[chmr]["VisitType"] != DBNull.Value)
                                {
                                    aMrkDtls.VisitType = dtDtl.Rows[chmr]["VisitType"].ToString();
                                }

                                if (dtDtl.Rows[chmr]["TourPlanDate"] != DBNull.Value)
                                {
                                    aMrkDtls.TourPlanDate = dtDtl.Rows[chmr]["TourPlanDate"].ToString();
                                }
                                    if (dtDtl.Rows[chmr]["TPId"] != DBNull.Value)
                                {
                                    aMrkDtls.TPId = Convert.ToInt32(dtDtl.Rows[chmr]["TPId"].ToString());
                                }

                                if (dtDtl.Rows[chmr]["TPName"] != DBNull.Value)
                                {
                                    aMrkDtls.TPName = dtDtl.Rows[chmr]["TPName"].ToString();
                                }


                                amarketList.Add(aMrkDtls);
                            }



                            aChemberDetails.aTourPlanDtlsDAO = amarketList;


                        }




                        aDetailsList.Add(aChemberDetails);
                    }

                    aDetails.aTourPlanDate = aDetailsList;



                    aInfo.aTourPlanMaster = aDetails;




                    aList.Add(aInfo);

                }
                return aList;
                //DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_AttendanceInformation", gSqlParameterList);
                //return dt;
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


        public DWSP_Applog_DAO GetDWSP_ApplogDAL(string pram, string Role, string AppStatus, string FromDt, string ToDt, string EmpId, int? MonthValue, int? YearValue)
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> gSqlParameterList = new List<SqlParameter>();
                gSqlParameterList.Add(new SqlParameter("@param", pram ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@Role", Role ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@AppStatus", AppStatus ?? (object)DBNull.Value ?? (object)DBNull.Value));

                gSqlParameterList.Add(new SqlParameter("@FromDt", FromDt ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@ToDt", ToDt ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@EmpId", EmpId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@MonthValue", MonthValue ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@YearValue", YearValue ?? (object)DBNull.Value ?? (object)DBNull.Value));


                DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_DWSPApp", gSqlParameterList);
                //SqlDataReader dr = accessManager.GetSqlDataReader("sp_Webapi_Get_DoctorClaimApp", gSqlParameterList);
                DWSP_Applog_DAO aInfo = new DWSP_Applog_DAO();
                

                // for (var i = 0; i < dt.Rows.Count; i++)
                int i = 0;
                {
                    aInfo = new DWSP_Applog_DAO();
                  

                    if (dt.Rows[i]["DWSPMasterId"] != DBNull.Value)
                    {
                        aInfo.DWSPMasterId = Convert.ToInt32(dt.Rows[i]["DWSPMasterId"].ToString());
                    }

                    if (dt.Rows[i]["MonthValue"] != DBNull.Value)
                    {
                        aInfo.MonthValue = Convert.ToInt32(dt.Rows[i]["MonthValue"]);
                    }

                    if (dt.Rows[i]["YearValue"] != DBNull.Value)
                    {
                        aInfo.YearValue = Convert.ToInt32(dt.Rows[i]["YearValue"]);
                    }

                    if (dt.Rows[i]["DWSPApprovalId"] != DBNull.Value)
                    {
                        aInfo.DWSPApprovalId = Convert.ToInt32(dt.Rows[i]["DWSPApprovalId"]);
                    }


                    if (dt.Rows[i]["EmpInfoId"] != DBNull.Value)
                    {
                        aInfo.EmpInfoId = Convert.ToInt32(dt.Rows[i]["EmpInfoId"].ToString());

                    }

                    if (dt.Rows[i]["Date"] != DBNull.Value)
                    {
                        aInfo.Date = (dt.Rows[i]["Date"].ToString());
                    }


                    if (dt.Rows[i]["FromEmpId"] != DBNull.Value)
                    {
                        aInfo.FromEmpId = Convert.ToInt32(dt.Rows[i]["FromEmpId"].ToString());
                    }



                    if (dt.Rows[i]["ToEmpId"] != DBNull.Value)
                    {
                        aInfo.ToEmpId = Convert.ToInt32(dt.Rows[i]["ToEmpId"].ToString());
                    }
                    if (dt.Rows[i]["TableId"] != DBNull.Value)
                    {
                        aInfo.TableId = Convert.ToInt32(dt.Rows[i]["TableId"].ToString());
                    }
                    if (dt.Rows[i]["Status"] != DBNull.Value)
                    {
                        aInfo.Status = dt.Rows[i]["Status"].ToString();
                    }
                    if (dt.Rows[i]["Comments"] != DBNull.Value)
                    {
                        aInfo.Comments = dt.Rows[i]["Comments"].ToString();
                    }

                    if (dt.Rows[i]["Type"] != DBNull.Value)
                    {
                        aInfo.Type = (dt.Rows[i]["Type"].ToString());
                    }


                    if (dt.Rows[i]["Step"] != DBNull.Value)
                    {
                        aInfo.Step = Convert.ToInt32(dt.Rows[i]["Step"].ToString());
                    }



                    if (dt.Rows[i]["ApprovalStatus"] != DBNull.Value)
                    {
                        aInfo.ApprovalStatus = dt.Rows[i]["ApprovalStatus"].ToString();
                    }











                    if (dt.Rows[i]["GroupId"] != DBNull.Value)
                    {
                        aInfo.GroupId = Convert.ToInt32(dt.Rows[i]["GroupId"].ToString());
                    }

                    if (dt.Rows[i]["RegionId"] != DBNull.Value)
                    {
                        aInfo.RegionId = Convert.ToInt32(dt.Rows[i]["RegionId"].ToString());
                    }


                    if (dt.Rows[i]["AreaId"] != DBNull.Value)
                    {
                        aInfo.AreaId = Convert.ToInt32(dt.Rows[i]["AreaId"].ToString());
                    }


                    if (dt.Rows[i]["TerritoryId"] != DBNull.Value)
                    {
                        aInfo.TerritoryId = Convert.ToInt32(dt.Rows[i]["TerritoryId"].ToString());
                    }


                    if (dt.Rows[i]["RoleTypeId"] != DBNull.Value)
                    {
                        aInfo.RoleTypeId = Convert.ToInt32(dt.Rows[i]["RoleTypeId"].ToString());
                    }


                    if (dt.Rows[i]["EmpMasterCode"] != DBNull.Value)
                    {
                        aInfo.EmpMasterCode = dt.Rows[i]["EmpMasterCode"].ToString();
                    }


                    if (dt.Rows[i]["EmpName"] != DBNull.Value)
                    {
                        aInfo.EmpName = dt.Rows[i]["EmpName"].ToString();
                    }


                    if (dt.Rows[i]["TerritoryName"] != DBNull.Value)
                    {
                        aInfo.TerritoryName = dt.Rows[i]["TerritoryName"].ToString();
                    }

                    if (dt.Rows[i]["TerritoryCode"] != DBNull.Value)
                    {
                        aInfo.TerritoryCode = dt.Rows[i]["TerritoryCode"].ToString();
                    }

                    if (dt.Rows[i]["AreaCode"] != DBNull.Value)
                    {
                        aInfo.AreaCode = dt.Rows[i]["AreaCode"].ToString();
                    }


                    if (dt.Rows[i]["ToRoleTypeId"] != DBNull.Value)
                    {
                        aInfo.ToRoleTypeId = Convert.ToInt32(dt.Rows[i]["ToRoleTypeId"].ToString());
                    }

                    if (dt.Rows[i]["MIOEmpId"] != DBNull.Value)
                    {
                        aInfo.MIOEmpId = Convert.ToInt32(dt.Rows[i]["MIOEmpId"].ToString());
                    }

                    if (dt.Rows[i]["ASMEMPId"] != DBNull.Value)
                    {
                        aInfo.ASMEMPId = Convert.ToInt32(dt.Rows[i]["ASMEMPId"].ToString());
                    }

                    if (dt.Rows[i]["RSMEMPId"] != DBNull.Value)
                    {
                        aInfo.RSMEMPId = Convert.ToInt32(dt.Rows[i]["RSMEMPId"].ToString());
                    }
                    if (dt.Rows[i]["NSMEMPId"] != DBNull.Value)
                    {
                        aInfo.NSMEMPId = Convert.ToInt32(dt.Rows[i]["NSMEMPId"].ToString());
                    }


                    List<SqlParameter> aSqlParameterListDetals = new List<SqlParameter>();

                    aSqlParameterListDetals.Add(new SqlParameter("@id", aInfo.TableId));
                    DataTable dtDoctor = accessManager.GetDataTable("sp_webapi_GetDWSPMasterById", aSqlParameterListDetals);


                    DWSPMasterDAO aDetails;


                    aDetails = new DWSPMasterDAO();

                    if (dtDoctor.Rows[0]["DWSPMasterId"] != DBNull.Value)
                    {
                        aDetails.DWSPMasterId = Convert.ToInt32(dtDoctor.Rows[0]["DWSPMasterId"].ToString());
                    }
                    if (dtDoctor.Rows[0]["EmpInfoId"] != DBNull.Value)
                    {
                        aDetails.EmpInfoId = Convert.ToInt32(dtDoctor.Rows[0]["EmpInfoId"].ToString());
                    }

                    if (dtDoctor.Rows[0]["MonthValue"] != DBNull.Value)
                    {
                        aDetails.MonthValue = Convert.ToInt32(dtDoctor.Rows[0]["MonthValue"].ToString());
                    }

                    if (dtDoctor.Rows[0]["YearValue"] != DBNull.Value)
                    {
                        aDetails.YearValue = Convert.ToInt32(dtDoctor.Rows[0]["YearValue"].ToString());
                    }


                    if (dtDoctor.Rows[0]["MonthName"] != DBNull.Value)
                    {
                        aDetails.MonthName = dtDoctor.Rows[0]["MonthName"].ToString();
                    }


                    if (dtDoctor.Rows[0]["IsFinalSubmit"] != DBNull.Value)
                    {
                        aDetails.IsFinalSubmit = Convert.ToBoolean(dtDoctor.Rows[0]["IsFinalSubmit"].ToString());
                    }


                    if (dtDoctor.Rows[0]["ApprovalStatus"] != DBNull.Value)
                    {
                        aDetails.ApprovalStatus = dtDoctor.Rows[0]["ApprovalStatus"].ToString();
                    }



                    if (dtDoctor.Rows[0]["TargetAmount"] != DBNull.Value)
                    {
                        aDetails.TargetAmount = dtDoctor.Rows[0]["TargetAmount"].ToString();

                    }

                    if (dtDoctor.Rows[0]["CampaignAmount"] != DBNull.Value)
                    {
                        aDetails.CampaignAmount = Convert.ToDecimal(dtDoctor.Rows[0]["CampaignAmount"].ToString());

                    }

                    if (dtDoctor.Rows[0]["GeneralAmount"] != DBNull.Value)
                    {
                        aDetails.GeneralAmount = Convert.ToDecimal(dtDoctor.Rows[0]["GeneralAmount"].ToString());

                    }
                    if (dtDoctor.Rows[0]["FCBAmount"] != DBNull.Value)
                    {
                        aDetails.FCBAmount = Convert.ToDecimal(dtDoctor.Rows[0]["FCBAmount"].ToString());

                    }

                    List<SqlParameter> asqChem = new List<SqlParameter>();

                    asqChem.Add(new SqlParameter("@id", aInfo.TableId));
                    DataTable dtDoctorChem = accessManager.GetDataTable("sp_webapi_GetDWSPDateById", asqChem);
                    List<DWSPDtlsDAODate> aDetailsList = new List<DWSPDtlsDAODate>();
                    DWSPDtlsDAODate aChemberDetails;
                    for (var ch = 0; ch < dtDoctorChem.Rows.Count; ch++)

                    {



                        aChemberDetails = new DWSPDtlsDAODate();



                        if (dtDoctorChem.Rows[ch]["DWSPDate"] != DBNull.Value)
                        {
                            string DWSPDate = dtDoctorChem.Rows[ch]["DWSPDate"].ToString();


                            if (dtDoctorChem.Rows[ch]["DayValue"] != DBNull.Value)
                            {
                                aChemberDetails.DayValue = dtDoctorChem.Rows[ch]["DayValue"].ToString();

                            }
                            if (dtDoctorChem.Rows[ch]["DayName"] != DBNull.Value)
                            {
                                aChemberDetails.DayName = dtDoctorChem.Rows[ch]["DayName"].ToString();

                            }

                            if (dtDoctorChem.Rows[ch]["CampaignAmount"] != DBNull.Value)
                            {
                                aChemberDetails.CampaignAmount = dtDoctorChem.Rows[ch]["CampaignAmount"].ToString();

                            }

                            if (dtDoctorChem.Rows[ch]["FCBAmount"] != DBNull.Value)
                            {
                                aChemberDetails.FCBAmount = dtDoctorChem.Rows[ch]["FCBAmount"].ToString();

                            }
                            if (dtDoctorChem.Rows[ch]["GeneralAmount"] != DBNull.Value)
                            {
                                aChemberDetails.GeneralAmount = dtDoctorChem.Rows[ch]["GeneralAmount"].ToString();

                            }




                        }




                        aDetailsList.Add(aChemberDetails);
                    }

                    aDetails.aDWSPDate = aDetailsList;



                  aInfo.aDWSPMasterDAO = aDetails;




                   // return aList;

                }
                return aInfo;
                //DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_AttendanceInformation", gSqlParameterList);
                //return dt;
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

        public ResultInfo SaveDayWiseTourPlan(DayWiseTourPlanDTO req)
        {
            ResultInfo result = new ResultInfo();
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> gSqlParameterList = new List<SqlParameter>();
                gSqlParameterList.Add(new SqlParameter("@EmpInfoId", req.EmpInfoId));
                gSqlParameterList.Add(new SqlParameter("@TourPlanDate", req.TourPlanDate));
                gSqlParameterList.Add(new SqlParameter("@IsFinalSubmit", req.IsFinalSubmit));
                gSqlParameterList.Add(new SqlParameter("@CustomerIds", req.CustomerIds != null ? string.Join(",", req.CustomerIds) : ""));
                gSqlParameterList.Add(new SqlParameter("@DoctorIds", req.DoctorIds != null ? string.Join(",", req.DoctorIds) : ""));

                DataTable dt = accessManager.GetDataTable("sp_Webapi_Save_DayWiseTourPlan", gSqlParameterList);
                if (dt != null && dt.Rows.Count > 0)
                {
                    result.isSuccess = Convert.ToInt32(dt.Rows[0]["IsSuccess"]) == 1;
                    result.ErrorMessage = dt.Rows[0]["Message"].ToString();
                }
                else
                {
                    result.isSuccess = true;
                    result.ErrorMessage = "Day-wise Tour Plan saved.";
                }
            }
            catch (Exception ex)
            {
                accessManager.SqlConnectionClose(true);
                result.isSuccess = false;
                result.ErrorMessage = ex.Message;
            }
            finally
            {
                accessManager.SqlConnectionClose();
            }
            return result;
        }


    }
}
