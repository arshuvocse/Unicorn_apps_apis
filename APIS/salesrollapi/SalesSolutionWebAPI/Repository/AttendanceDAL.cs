using SalesSolutionWebAPI.DataManager;
using SalesSolutionWebAPI.Interface;
using System;
using System.Collections.Generic;
using System.Data;
using System.Data.SqlClient;
using System.IO;
using SalesSolutionWebAPI.Repository;
using SalesSolutionWebAPI.Models;
using System.Globalization;
using System.Linq;

namespace SalesSolutionWebAPI.Repository
{
    public class AttendanceDAL : iAttendance
    {
        private DataAccessManager accessManager = new DataAccessManager();

        public IList<AttendanceDAO> GetAttendanceInformation(string pram, string emprole)
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> gSqlParameterList = new List<SqlParameter>();
                gSqlParameterList.Add(new SqlParameter("@param", pram));
                gSqlParameterList.Add(new SqlParameter("@Role", emprole));


                SqlDataReader dr = accessManager.GetSqlDataReader("sp_Webapi_Get_AttendanceInformation", gSqlParameterList);
                List<AttendanceDAO> aList = new List<AttendanceDAO>();

                while (dr.Read())
                {
                    AttendanceDAO aInfo = new AttendanceDAO();

                    if (dr["AttendanceId"] != DBNull.Value)
                    {
                        aInfo.AttendanceId = Convert.ToInt32(dr["AttendanceId"]);
                    }
                    if (dr["MIOId"] != DBNull.Value)
                    {
                        aInfo.MIOId = Convert.ToInt32(dr["MIOId"].ToString());
                    }
                    if (dr["EmpInfoId"] != DBNull.Value)
                    {
                        aInfo.EmpInfoId = Convert.ToInt32(dr["EmpInfoId"].ToString());

                    }
                    if (dr["PunchInTime"] != DBNull.Value)
                    {
                        aInfo.PunchInTime = dr["PunchInTime"].ToString();
                    }
                    if (dr["PInLat"] != DBNull.Value)
                    {
                        aInfo.PInLat = dr["PInLat"].ToString();
                    }

                    if (dr["PInLog"] != DBNull.Value)
                    {
                        aInfo.PInLog =  (dr["PInLog"].ToString());
                    }



                    if (dr["PunchOutTime"] != DBNull.Value)
                    {
                        aInfo.PunchOutTime = dr["PunchOutTime"].ToString();
                    }





                    if (dr["POutLat"] != DBNull.Value)
                    {
                        aInfo.POutLat = dr["POutLat"].ToString();
                    }


                    if (dr["POutLong"] != DBNull.Value)
                    {
                        aInfo.POutLong = dr["POutLong"].ToString();
                    }

                    if (dr["EntryDate"] != DBNull.Value)
                    {
                        aInfo.EntryDate = dr["EntryDate"].ToString();
                    }



                    if (dr["POutRemarks"] != DBNull.Value)
                    {
                        aInfo.POutRemarks = dr["POutRemarks"].ToString();
                    }
                    if (dr["AttendanceDate"] != DBNull.Value)
                    {
                        aInfo.AttendanceDate = dr["AttendanceDate"].ToString();
                    }
                    if (dr["PINCreatedDateTime"] != DBNull.Value)
                    {
                        aInfo.PINCreatedDateTime = dr["PINCreatedDateTime"].ToString();
                    }
                    if (dr["POUTCreatedDateTime"] != DBNull.Value)
                    {
                        aInfo.POUTCreatedDateTime = dr["POUTCreatedDateTime"].ToString();
                    }
                    if (dr["Status"] != DBNull.Value)
                    {
                        aInfo.Status = dr["Status"].ToString();
                    }
                    if (dr["ShiftId"] != DBNull.Value)
                    {
                        aInfo.ShiftId = Convert.ToInt32(dr["ShiftId"].ToString());
                    }


                    if (dr["ApprovalId"] != DBNull.Value)
                    {
                        aInfo.ShiftId = Convert.ToInt32(dr["ApprovalId"].ToString());
                    }


                    if (dr["Date"] != DBNull.Value)
                    {
                        aInfo.Status = dr["Date"].ToString();
                    }


                    if (dr["FromEmpId"] != DBNull.Value)
                    {
                        aInfo.FromEmpId = Convert.ToInt32(dr["FromEmpId"].ToString());
                    }
                    if (dr["ToEmpId"] != DBNull.Value)
                    {
                        aInfo.ToEmpId = Convert.ToInt32(dr["ToEmpId"].ToString());
                    }


                    if (dr["TableId"] != DBNull.Value)
                    {
                        aInfo.TableId = Convert.ToInt32(dr["TableId"].ToString());
                    }

                    if (dr["Comments"] != DBNull.Value)
                    {
                        aInfo.Comments = dr["Comments"].ToString();
                    }

                    if (dr["Type"] != DBNull.Value)
                    {
                        aInfo.Type = dr["Type"].ToString();
                    }

                    if (dr["Step"] != DBNull.Value)
                    {
                        aInfo.Step = dr["Step"].ToString();
                    }



                    if (dr["GroupId"] != DBNull.Value)
                    {
                        aInfo.GroupId = Convert.ToInt32(dr["GroupId"].ToString());
                    }

                    if (dr["RegionId"] != DBNull.Value)
                    {
                        aInfo.RegionId = Convert.ToInt32(dr["RegionId"].ToString());
                    }


                    if (dr["AreaId"] != DBNull.Value)
                    {
                        aInfo.AreaId = Convert.ToInt32(dr["AreaId"].ToString());
                    }


                    if (dr["TerritoryId"] != DBNull.Value)
                    {
                        aInfo.TerritoryId = Convert.ToInt32(dr["TerritoryId"].ToString());
                    }


                    if (dr["RoleTypeId"] != DBNull.Value)
                    {
                        aInfo.RoleTypeId = Convert.ToInt32(dr["RoleTypeId"].ToString());
                    }


                    if (dr["EmpMasterCode"] != DBNull.Value)
                    {
                        aInfo.EmpMasterCode = dr["EmpMasterCode"].ToString();
                    }


                    if (dr["EmpName"] != DBNull.Value)
                    {
                        aInfo.EmpName = dr["EmpName"].ToString();
                    }


                    if (dr["TerritoryName"] != DBNull.Value)
                    {
                        aInfo.TerritoryName = dr["TerritoryName"].ToString();
                    }


                    if (dr["TerritoryCode"] != DBNull.Value)
                    {
                        aInfo.TerritoryCode = dr["TerritoryCode"].ToString();
                    }

                    if (dr["AreaCode"] != DBNull.Value)
                    {
                        aInfo.AreaCode = dr["AreaCode"].ToString();
                    }




                    if (dr["RoleTypeId"] != DBNull.Value)
                    {
                        aInfo.AttType = Convert.ToInt32(dr["AttType"].ToString());
                    }
                    if (dr["ToRoleTypeId"] != DBNull.Value)
                    {
                        aInfo.ToRoleTypeId = Convert.ToInt32(dr["ToRoleTypeId"].ToString());
                    }





                    if (dr["MIOEmpId"] != DBNull.Value)
                    {
                        aInfo.MIOEmpId = Convert.ToInt32(dr["MIOEmpId"].ToString());
                    }

                    if (dr["ASMEMPId"] != DBNull.Value)
                    {
                        aInfo.ASMEMPId = Convert.ToInt32(dr["ASMEMPId"].ToString());
                    }

                    if (dr["RSMEMPId"] != DBNull.Value)
                    {
                        aInfo.RSMEMPId = Convert.ToInt32(dr["RSMEMPId"].ToString());
                    }
                    if (dr["NSMEMPId"] != DBNull.Value)
                    {
                        aInfo.NSMEMPId = Convert.ToInt32(dr["NSMEMPId"].ToString());
                    }

                    if (dr["ImageString"] != DBNull.Value)
                    {
                        aInfo.ImageString = dr["ImageString"].ToString();
                    }
                  
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

        public IList<AttendanceDAO> GetAttendanceInformation_New(string pram, string Role, string AppStatus, string AttType, string FromDt, string ToDt, string EmpId)
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> gSqlParameterList = new List<SqlParameter>();
                 gSqlParameterList.Add(new SqlParameter("@Role", Role ?? (object)DBNull.Value ?? (object)DBNull.Value));
                pram = NewMethod(pram, Role, gSqlParameterList);

                gSqlParameterList.Add(new SqlParameter("@AppStatus", AppStatus ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@AttType", AttType ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@FromDt", FromDt ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@ToDt", ToDt ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@EmpId", EmpId ?? (object)DBNull.Value ?? (object)DBNull.Value));



                SqlDataReader dr = accessManager.GetSqlDataReader("sp_Webapi_Get_AttendanceInformation2", gSqlParameterList);
                List<AttendanceDAO> aList = new List<AttendanceDAO>();

                while (dr.Read())
                {
                    AttendanceDAO aInfo = new AttendanceDAO();

                    if (dr["AttendanceId"] != DBNull.Value)
                    {
                        aInfo.AttendanceId = Convert.ToInt32(dr["AttendanceId"]);
                    }
                    //if (dr["MIOId"] != DBNull.Value)
                    //{
                    //    aInfo.MIOId = Convert.ToInt32(dr["MIOId"].ToString());
                    //}
                    if (dr["EmpInfoId"] != DBNull.Value)
                    {
                        aInfo.EmpInfoId = Convert.ToInt32(dr["EmpInfoId"].ToString());

                    }
                    if (dr["PunchInTime"] != DBNull.Value)
                    {
                        aInfo.PunchInTime = dr["PunchInTime"].ToString();
                    }
                    if (dr["PInLat"] != DBNull.Value)
                    {
                        aInfo.PInLat = dr["PInLat"].ToString();
                    }

                    if (dr["PInLog"] != DBNull.Value)
                    {
                        aInfo.PInLog = (dr["PInLog"].ToString());
                    }

                    if (dr["ApprovalStatus"] != DBNull.Value)
                    {
                        aInfo.ApprovalStatus = (dr["ApprovalStatus"].ToString());
                    }

                    if (dr["AttAddress"] != DBNull.Value)
                    {
                        aInfo.AttAddress = (dr["AttAddress"].ToString());
                    }

                    //if (dr["PunchOutTime"] != DBNull.Value)
                    //{
                    //    aInfo.PunchOutTime = dr["PunchOutTime"].ToString();
                    //}





                    //if (dr["POutLat"] != DBNull.Value)
                    //{
                    //    aInfo.POutLat = dr["POutLat"].ToString();
                    //}


                    //if (dr["POutLong"] != DBNull.Value)
                    //{
                    //    aInfo.POutLong = dr["POutLong"].ToString();
                    //}

                    //if (dr["EntryDate"] != DBNull.Value)
                    //{
                    //    aInfo.EntryDate = dr["EntryDate"].ToString();
                    //}



                    if (dr["POutRemarks"] != DBNull.Value)
                    {
                        aInfo.POutRemarks = dr["POutRemarks"].ToString();
                    }
                    if (dr["AttendanceDate"] != DBNull.Value)
                    {
                        aInfo.AttendanceDate = dr["AttendanceDate"].ToString();
                    }
                    if (dr["PINCreatedDateTime"] != DBNull.Value)
                    {
                        aInfo.PINCreatedDateTime = dr["PINCreatedDateTime"].ToString();
                    }
                    if (dr["POUTCreatedDateTime"] != DBNull.Value)
                    {
                        aInfo.POUTCreatedDateTime = dr["POUTCreatedDateTime"].ToString();
                    }
                    if (dr["Status"] != DBNull.Value)
                    {
                        aInfo.Status = dr["Status"].ToString();
                    }
                    if (dr["ShiftId"] != DBNull.Value)
                    {
                        aInfo.ShiftId = Convert.ToInt32(dr["ShiftId"].ToString());
                    }


                    if (dr["ApprovalId"] != DBNull.Value)
                    {
                        aInfo.ApprovalId = Convert.ToInt32(dr["ApprovalId"].ToString());
                    }


                    if (dr["Date"] != DBNull.Value)
                    {
                        aInfo.Date = dr["Date"].ToString();
                    }


                    if (dr["FromEmpId"] != DBNull.Value)
                    {
                        aInfo.FromEmpId = Convert.ToInt32(dr["FromEmpId"].ToString());
                    }
                    if (dr["ToEmpId"] != DBNull.Value)
                    {
                        aInfo.ToEmpId = Convert.ToInt32(dr["ToEmpId"].ToString());
                    }


                    if (dr["TableId"] != DBNull.Value)
                    {
                        aInfo.TableId = Convert.ToInt32(dr["TableId"].ToString());
                    }

                    if (dr["Comments"] != DBNull.Value)
                    {
                        aInfo.Comments = dr["Comments"].ToString();
                    }

                    if (dr["Type"] != DBNull.Value)
                    {
                        aInfo.Type = dr["Type"].ToString();
                    }

                    if (dr["Step"] != DBNull.Value)
                    {
                        aInfo.Step = dr["Step"].ToString();
                    }



                    if (dr["GroupId"] != DBNull.Value)
                    {
                        aInfo.GroupId = Convert.ToInt32(dr["GroupId"].ToString());
                    }

                    if (dr["RegionId"] != DBNull.Value)
                    {
                        aInfo.RegionId = Convert.ToInt32(dr["RegionId"].ToString());
                    }


                    if (dr["AreaId"] != DBNull.Value)
                    {
                        aInfo.AreaId = Convert.ToInt32(dr["AreaId"].ToString());
                    }


                    if (dr["TerritoryId"] != DBNull.Value)
                    {
                        aInfo.TerritoryId = Convert.ToInt32(dr["TerritoryId"].ToString());
                    }


                    if (dr["RoleTypeId"] != DBNull.Value)
                    {
                        aInfo.RoleTypeId = Convert.ToInt32(dr["RoleTypeId"].ToString());
                    }


                    if (dr["EmpMasterCode"] != DBNull.Value)
                    {
                        aInfo.EmpMasterCode = dr["EmpMasterCode"].ToString();
                    }


                    if (dr["EmpName"] != DBNull.Value)
                    {
                        aInfo.EmpName = dr["EmpName"].ToString();
                    }


                    if (dr["TerritoryName"] != DBNull.Value)
                    {
                        aInfo.TerritoryName = dr["TerritoryName"].ToString();
                    }
                     
                    if (dr["TerritoryCode"] != DBNull.Value)
                    {
                        aInfo.TerritoryCode = dr["TerritoryCode"].ToString();
                    }

                    if (dr["AreaCode"] != DBNull.Value)
                    {
                        aInfo.AreaCode = dr["AreaCode"].ToString();
                    }
                      
                    if (dr["RoleTypeId"] != DBNull.Value)
                    {
                        aInfo.AttType = Convert.ToInt32(dr["AttType"].ToString());
                    }
                    if (dr["ToRoleTypeId"] != DBNull.Value)
                    {
                        aInfo.ToRoleTypeId = Convert.ToInt32(dr["ToRoleTypeId"].ToString());
                    }
                     
                    if (dr["MIOEmpId"] != DBNull.Value)
                    {
                        aInfo.MIOEmpId = Convert.ToInt32(dr["MIOEmpId"].ToString());
                    }

                    if (dr["ASMEMPId"] != DBNull.Value)
                    {
                        aInfo.ASMEMPId = Convert.ToInt32(dr["ASMEMPId"].ToString());
                    }

                    if (dr["RSMEMPId"] != DBNull.Value)
                    {
                        aInfo.RSMEMPId = Convert.ToInt32(dr["RSMEMPId"].ToString());
                    }
                    if (dr["NSMEMPId"] != DBNull.Value)
                    {
                        aInfo.NSMEMPId = Convert.ToInt32(dr["NSMEMPId"].ToString());
                    }


                    if (dr["ImageString"] != DBNull.Value)
                    {
                        aInfo.ImageString = dr["ImageString"].ToString();
                    }
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

        public ResultInfo SaveAttandaceAppLog(AttendanceLog atten)
        {
            ResultInfo aInformation = new ResultInfo();
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                
                List<SqlParameter> gSqlParameterList = new List<SqlParameter>();
                DateTime entryDtae = DateTime.Now;
               
                gSqlParameterList.Add(new SqlParameter("@ApprovalId",  atten.ApprovalId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@Date",  atten.Date ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@FromEmpId",  atten.FromEmpId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@ToEmpId",  atten.ToEmpId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@TableId",  atten.TableId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@Status",  atten.Status ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@Comments",  atten.Comments ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@Type",  atten.Type ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@Step",  atten.Step ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@GroupId",  atten.GroupId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@RegionId",  atten.RegionId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@AreaId",  atten.AreaId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@TerritoryId",  atten.TerritoryId ?? (object)DBNull.Value ?? (object)DBNull.Value));

                gSqlParameterList.Add(new SqlParameter("@ToGroupId",  atten.ToGroupId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@ToRegionId",  atten.ToRegionId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@ToAreaId",  atten.ToAreaId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@ToTerritoryId",  atten.ToTerritoryId ?? (object)DBNull.Value ?? (object)DBNull.Value));

                gSqlParameterList.Add(new SqlParameter("@EntryByS",  atten.EntryByS ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@EntryDateS",  atten.EntryDateS ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@EntryTimeS",  atten.EntryTimeS ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@ApproveByS",    atten.ApproveByS ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@ApproveDateS",  atten.ApproveDateS ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@ApproveTimeS",  atten.ApproveTimeS ?? (object)DBNull.Value ?? (object)DBNull.Value));

                gSqlParameterList.Add(new SqlParameter("@EntryByApp",  atten.EntryByApp ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@EntryDateApp",  atten.EntryDateApp ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@EntryTimeApp",  atten.EntryTimeApp ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@ApproveByApp",  atten.ApproveByApp ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@ApproveDateApp",  atten.ApproveDateApp ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@ApproveTimeApp",  atten.ApproveTimeApp ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@MenuId",  atten.MenuId ?? (object)DBNull.Value ?? (object)DBNull.Value));

                aInformation.isSuccess = accessManager.SaveData("sp_webapi_SaveAppLog", gSqlParameterList);

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

       

        public ResultInfo SavePunchIn(Attendance atten)
        {
            ResultInfo aInformation = new ResultInfo();
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> gSqlParameterList = new List<SqlParameter>();
                DateTime entryDtae = DateTime.Now;
                gSqlParameterList.Add(new SqlParameter("@attendaceDate", atten.AttendanceDate));
                gSqlParameterList.Add(new SqlParameter("@pInTime", atten.PunchInTime));
                gSqlParameterList.Add(new SqlParameter("@pInLat", atten.PInLat));
                gSqlParameterList.Add(new SqlParameter("@pInLong", atten.PInLog));
                gSqlParameterList.Add(new SqlParameter("@empId", atten.EmpInfoId));
                gSqlParameterList.Add(new SqlParameter("@EntryDate", entryDtae));
                aInformation.isSuccess = accessManager.SaveData("@sp_webapi_SavePunchInInfo", gSqlParameterList);

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


        public ResultInfo SavePunchIn_PunchOut(Attendance atten)
        {
            ResultInfo aInformation = new ResultInfo();
            try
            {

                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);

                // Punch-in time restriction (punch-out is never restricted).
                if (atten.AttType == 1)
                {
                    // Own connection: a failed SP closes and poisons its DataAccessManager for the save below.
                    DataTable dtWindow = null;
                    DataAccessManager windowDb = new DataAccessManager();
                    try
                    {
                        windowDb.SqlConnectionOpen(DataBase.SalesDisDB);
                        dtWindow = windowDb.GetDataTable("sp_Webapi_Get_AttendancePunchWindow",
                            new List<SqlParameter> { new SqlParameter("@empId", atten.EmpInfoId) });
                    }
                    catch (SqlException)
                    {
                        // attendance_punch_window.sql not run on this DB yet: no window, never block punch-in
                    }
                    finally
                    {
                        windowDb.SqlConnectionClose();
                    }
                    if (dtWindow != null && dtWindow.Rows.Count > 0 && !Convert.ToBoolean(dtWindow.Rows[0]["IsAllowedNow"]))
                    {
                        aInformation.Msd = "Punch in is allowed only between " + dtWindow.Rows[0]["PunchInFrom"] + " and " + dtWindow.Rows[0]["PunchInTo"] + ".";
                        aInformation.isSuccess = false;
                        return aInformation;
                    }
                }

                List<SqlParameter> gSqlParameterList = new List<SqlParameter>();
                DateTime entryDtae = DateTime.Now;
                gSqlParameterList.Add(new SqlParameter("@attendaceDate", atten.AttendanceDate));
                gSqlParameterList.Add(new SqlParameter("@pInTime", atten.PunchInTime));
                gSqlParameterList.Add(new SqlParameter("@pInLat", atten.PInLat));
                gSqlParameterList.Add(new SqlParameter("@pInLong", atten.PInLog));

                gSqlParameterList.Add(new SqlParameter("@remarks", atten.POutRemarks));
                gSqlParameterList.Add(new SqlParameter("@empId", atten.EmpInfoId));
                gSqlParameterList.Add(new SqlParameter("@EntryDate", entryDtae));
                gSqlParameterList.Add(new SqlParameter("@AttType", atten.AttType));
                gSqlParameterList.Add(new SqlParameter("@AttAddress", atten.AttAddress));
                gSqlParameterList.Add(new SqlParameter(
                    "@ShiftId",
                    atten.ShiftId.HasValue ? (object)atten.ShiftId.Value : DBNull.Value));


                int pk = 0;

                pk = accessManager.SaveDataReturnPrimaryKey("sp_webapi_SavePunchTotalInfo", gSqlParameterList);
                


                if (pk>0)
                {
                     
                        try
                        {
                            AppPrimaryDAL app = new AppPrimaryDAL();
                            ImagePath _path = new ImagePath();
                            string type = "Attendance";
                            _path = app.GetImagePath(type);



                            string targetPath = @"" + _path.ImagePathValue + "";


                            if (!System.IO.Directory.Exists(targetPath))
                            {
                                System.IO.Directory.CreateDirectory(targetPath);
                            }

                            string filePath = targetPath + "/" + _path.ImagePreName + pk + "." + "jpg";
                            File.WriteAllBytes(filePath, Convert.FromBase64String(atten.AttImg));
                        }
                        catch(Exception ex) { }
                    
                    if (atten.AttType == 1)
                    {
                        TimeSpan timespan;
                        TimeSpan? shiftTime = null;
                        List<SqlParameter> gSqlPunch = new List<SqlParameter>();
                        gSqlPunch.Add(new SqlParameter("@empId", atten.EmpInfoId));
                        gSqlPunch.Add(new SqlParameter("@AttType", atten.AttType));
                        gSqlPunch.Add(new SqlParameter("@pInTime", atten.PunchInTime));
                        try
                        {
                            DataTable dtPunch = accessManager.GetDataTable("sp_Webapi_Get_PunchInOutStatus", gSqlPunch);
                            if (dtPunch.Rows.Count > 0)
                            {
                                if (dtPunch.Rows[0]["TextField"] != DBNull.Value)
                                {
                                    aInformation.Msd = "Your punch in successful.  You are " + dtPunch.Rows[0]["TextField"]+".";
                                }
                            }
                        }
                        catch(Exception ex)
                        {
                            aInformation.Msd = "Your punch in successful.  You are Early IN." ;
                            aInformation.isSuccess = true;
                        }
                        //timespan = shiftTime - atten.AttType;

             //           DateTime dt1 = DateTime.ParseExact(dtPunch.Rows[0]["ShiftInTime"].ToString(), "HH:mm:ss", new DateTimeFormatInfo());
                        //DateTime dt2 = DateTime.ParseExact(atten.AttendanceDate.ToString(), "HH:mm:ss", new DateTimeFormatInfo());
                        //TimeSpan ts = dt1.Subtract(dt2);
                    }
                    if (atten.AttType ==2)
                    {
                        List<SqlParameter> gSqlPunch = new List<SqlParameter>();
                        gSqlPunch.Add(new SqlParameter("@empId", atten.EmpInfoId));
                        gSqlPunch.Add(new SqlParameter("@AttType", atten.AttType));
                        gSqlPunch.Add(new SqlParameter("@pInTime", atten.PunchInTime));
                        try
                        {
                            DataTable dtPunch = accessManager.GetDataTable("sp_Webapi_Get_PunchInOutStatus", gSqlPunch);
                            if (dtPunch.Rows.Count > 0)
                            {
                                if (dtPunch.Rows[0]["TextField"] != DBNull.Value)
                                {
                                    aInformation.Msd = "Your punch out successful.  You are " + dtPunch.Rows[0]["TextField"] + ".";
                                }
                            }
                        }
                        catch (Exception ex)
            {
                aInformation.Msd = "Your punch out successful.  You are Early OUT.";

            }

        }


                    aInformation.isSuccess = true;
                   

                    
                }
                else if (pk == -1)
                {
                    aInformation.Msd = "You have already provided your attendance. Please wait 10 minutes before submitting your next attendance.";
                    aInformation.isSuccess = false;
                }
                else if (pk == -2)
                {
                    aInformation.Msd = "Invalid punch action for the current shift.";
                    aInformation.isSuccess = false;
                }
                else if (pk == -3)
                {
                    aInformation.Msd = "Attendance already submitted for this shift.";
                    aInformation.isSuccess = false;
                }
                else if (pk == -4)
                {
                    aInformation.Msd = "Attendance completed for all shifts.";
                    aInformation.isSuccess = false;
                }
                else
                {
                    aInformation.Msd = "No active shift is assigned.";
                    aInformation.isSuccess = false;

                }



            }
            catch (Exception exception)
            {
                accessManager.SqlConnectionClose(true);
                aInformation.isSuccess = false;
                aInformation.ErrorMessage = exception.Message;
                // Returned, not rethrown: a rethrow became HTTP 500 with no body, shown in the app as "Attendance Null".
                aInformation.Msd = "Attendance could not be saved. Please try again. (" + exception.Message + ")";
            }
            finally
            {
                accessManager.SqlConnectionClose();
            }

            return aInformation;
        }
        public ResultInfoNew GetInOutInfo(int EmpInfoId)
        {
            ResultInfoNew aInformation = new ResultInfoNew();
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);

                List<SqlParameter> gsql = new List<SqlParameter>();
                gsql.Add(new SqlParameter("@empId", EmpInfoId));
                DataTable dt = accessManager.GetDataTable(
                    "sp_Webapi_Get_AttendanceInfo",
                    gsql);

                if (dt.Rows.Count == 0)
                {
                    aInformation.PunchInBtn = "ON";
                    aInformation.PunchOUTBtn = "OFF";
                    return aInformation;
                }

                DataRow result = dt.Rows[0];
                aInformation.PunchInBtn = result["PunchInBtn"].ToString();
                aInformation.PunchOUTBtn = result["PunchOUTBtn"].ToString();
                aInformation.ShiftId = result["ShiftId"] == DBNull.Value
                    ? (int?)null
                    : Convert.ToInt32(result["ShiftId"]);
                aInformation.ShiftInTime = result["ShiftInTime"].ToString();
                aInformation.ShiftOutTime = result["ShiftOutTime"].ToString();
                aInformation.ShiftText = result["ShiftText"].ToString();
            }
            catch (Exception exception)
            {
                accessManager.SqlConnectionClose(true);

                throw exception;
            }
            finally
            {
                accessManager.SqlConnectionClose();
            }

            return aInformation;
        }

        public DataTable GetShiftCategoryList(int empId)
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> gsql = new List<SqlParameter>();
                gsql.Add(new SqlParameter("@empId", empId));
                DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_ShiftCategoryList", gsql);
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

        public DataTable GetAttendancePunchWindow(int empId)
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> gsql = new List<SqlParameter>();
                gsql.Add(new SqlParameter("@empId", empId));
                return accessManager.GetDataTable("sp_Webapi_Get_AttendancePunchWindow", gsql);
            }
            finally
            {
                accessManager.SqlConnectionClose();
            }
        }

        public ResultInfo SaveAttendancePunchWindow(AttendancePunchWindowRequest request)
        {
            ResultInfo aInformation = new ResultInfo();
            TimeSpan from, to;
            if (request == null || request.EmpId <= 0
                || !TimeSpan.TryParse(request.PunchInFrom, out from) || !TimeSpan.TryParse(request.PunchInTo, out to)
                || from < TimeSpan.Zero || from.Days > 0 || to < TimeSpan.Zero || to.Days > 0)
            {
                aInformation.isSuccess = false;
                aInformation.Msd = "Invalid time. Use HH:mm.";
                return aInformation;
            }
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> gSqlParameterList = new List<SqlParameter>();
                gSqlParameterList.Add(new SqlParameter("@empId", request.EmpId));
                gSqlParameterList.Add(new SqlParameter("@PunchInFrom", from));
                gSqlParameterList.Add(new SqlParameter("@PunchInTo", to));
                gSqlParameterList.Add(new SqlParameter("@EntryBy", request.EntryBy ?? (object)DBNull.Value));
                aInformation.isSuccess = accessManager.SaveData("sp_Webapi_Save_AttendancePunchWindow", gSqlParameterList);
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

        public ResultInfo RemoveAttendancePunchWindow(AttendancePunchWindowRequest request)
        {
            ResultInfo aInformation = new ResultInfo();
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> gSqlParameterList = new List<SqlParameter>();
                gSqlParameterList.Add(new SqlParameter("@empId", request.EmpId));
                gSqlParameterList.Add(new SqlParameter("@EntryBy", request.EntryBy ?? (object)DBNull.Value));
                aInformation.isSuccess = accessManager.SaveData("sp_Webapi_Remove_AttendancePunchWindow", gSqlParameterList);
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

        public ResultInfo SaveShiftCategoryList(EmpShiftCategoryRequest request)
        {
            ResultInfo aInformation = new ResultInfo();
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> gSqlParameterList = new List<SqlParameter>();
                gSqlParameterList.Add(new SqlParameter("@empId", request.EmpId));
                gSqlParameterList.Add(new SqlParameter("@ShiftCategory", request.ShiftCategory ?? (object)DBNull.Value));
                aInformation.isSuccess = accessManager.SaveData("sp_Webapi_Save_ShiftCategoryList", gSqlParameterList);
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

        //public ResultInfo SavePunchOut(Attendance atten)
        //{
        //    ResultInfo aInformation = new ResultInfo();
        //    try
        //    {

        //        accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
        //        List<SqlParameter> gSqlParameterList = new List<SqlParameter>();
        //        DateTime entryDtae = DateTime.Now;
        //        gSqlParameterList.Add(new SqlParameter("@attendaceDate", atten.AttendanceDate));
        //        gSqlParameterList.Add(new SqlParameter("@pOutTime", atten.PunchOutTime));
        //        gSqlParameterList.Add(new SqlParameter("@pOutLat", atten.POutLat));
        //        gSqlParameterList.Add(new SqlParameter("@pOutLong", atten.POutLong));
        //        gSqlParameterList.Add(new SqlParameter("@remarks", atten.POutRemarks));
        //        gSqlParameterList.Add(new SqlParameter("@empId", atten.EmpInfoId));
        //        gSqlParameterList.Add(new SqlParameter("@EntryDate", entryDtae));

        //        aInformation.isSuccess = accessManager.SaveData("@sp_webapi_SavePunchoutInfo", gSqlParameterList);

        //    }
        //    catch (Exception exception)
        //    {
        //        accessManager.SqlConnectionClose(true);
        //        aInformation.isSuccess = false;
        //        aInformation.ErrorMessage = exception.Message;

        //        throw exception;
        //    }
        //    finally
        //    {
        //        accessManager.SqlConnectionClose();
        //    }

        //    return aInformation;

        //}

    }
}
