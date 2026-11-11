using SalesSolutionWebAPI.DataManager;
using SalesSolutionWebAPI.Models;
using System;
using System.Collections.Generic;
using System.Data;
using System.Data.SqlClient;
using System.Globalization;
using System.IO;
using System.Linq;
using System.Web;

namespace SalesSolutionWebAPI.Repository
{
    public class DoctorDAL
    {
        private TourPlanDAL aTourPlanDAL = new TourPlanDAL();

        private DataAccessManager accessManager = new DataAccessManager();

        public DataTable GetDoctorDegree()
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> aSqlParameterlist = new List<SqlParameter>();
                DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_DoctorDegree");
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

        public DataTable GetDoctorSpecialDayDAL()
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> aSqlParameterlist = new List<SqlParameter>();
                DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_DoctorSpecialDay");
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

        public DataTable GetDoctorSpeciality()
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> aSqlParameterlist = new List<SqlParameter>();
                DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_DoctorSpeciality");
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

        public DataTable GetDoctorType()
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> aSqlParameterlist = new List<SqlParameter>();
                DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_DoctorType");
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


        public DataTable GetProgramType()
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> aSqlParameterlist = new List<SqlParameter>();
                DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_DoctorProgramType");
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

        public DataTable GetProviderType()
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> aSqlParameterlist = new List<SqlParameter>();
                DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_ProviderType");
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


        public DataTable GetSMCType()
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> aSqlParameterlist = new List<SqlParameter>();
                DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_SMCType");
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
        public DataTable GetNonEffectiveReason()
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> aSqlParameterlist = new List<SqlParameter>();
                DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_NonEffectiveReason");
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

        public DataTable GetDoctorCategory()
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> aSqlParameterlist = new List<SqlParameter>();
                DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_DoctorCategory");
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


        //public IList<DoctorProductBrandMasterDAO> GetDoctorBrand()
        //{
        //    try
        //    {
        //        accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
        //        List<SqlParameter> aSqlParameterlist = new List<SqlParameter>();
        //        DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_DoctorBrand");
        //        List<DoctorProductBrandMasterDAO> aList = new List<DoctorProductBrandMasterDAO>();
        //        DoctorProductBrandMasterDAO aInfo = new DoctorProductBrandMasterDAO();
        //        if (dt.Rows[0]["MaxValue"] != DBNull.Value)
        //        {
        //            aInfo.MaxValue = Convert.ToInt32(dt.Rows[0]["MaxValue"].ToString());
        //        }

        //        List<DoctorProductBrandDao> aDetailsList = new List<DoctorProductBrandDao>();
        //        DoctorProductBrandDao aDetails;
        //        for (var kk = 0; kk < dt.Rows.Count; kk++)

        //        {

        //            aDetails = new DoctorProductBrandDao();
        //            if (dt.Rows[kk]["ProductBrandId"] != DBNull.Value)
        //            {
        //                aDetails.ProductBrandId = Convert.ToInt32(dt.Rows[kk]["ProductBrandId"].ToString());
        //            }
        //            if (dt.Rows[kk]["ProductSQName"] != DBNull.Value)
        //            {
        //                aDetails.ProductSQName = dt.Rows[kk]["ProductSQName"].ToString();
        //            }
        //            aDetailsList.Add(aDetails);
        //        }
        //        aInfo.aBrandListDAO = aDetailsList;
        //        aList.Add(aInfo);

        //        return aList;
        //    }
        //    catch (Exception e)
        //    {
        //        throw;
        //    }
        //    finally
        //    {
        //        accessManager.SqlConnectionClose();
        //    }
        //}

        public DataTable GetDoctorBrand()
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> aSqlParameterlist = new List<SqlParameter>();
                DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_DoctorBrand");

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
        public DataTable GetDoctorInstitution()
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> aSqlParameterlist = new List<SqlParameter>();
                DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_DoctorInstitute");
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
        public DataTable GetDoctorChemberType()
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> aSqlParameterlist = new List<SqlParameter>();
                DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_DoctorChamber");
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

        public DataTable GetDoctorChemberName(int empId)
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> aSqlParameterlist = new List<SqlParameter>();
                aSqlParameterlist.Add(new SqlParameter("@empid", empId));
                DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_ChamberName", aSqlParameterlist);
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

        // empId = 0 -> every employee with a DCR area lock setup row
        public DataTable GetDcrAreaLock(int empId)
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> aSqlParameterlist = new List<SqlParameter>();
                aSqlParameterlist.Add(new SqlParameter("@EmpInfoId", empId));
                return accessManager.GetDataTable("sp_Webapi_Get_DcrAreaLock", aSqlParameterlist);
            }
            finally
            {
                accessManager.SqlConnectionClose();
            }
        }

        public ResultInfo SaveDcrAreaLock(DcrAreaLockDTO req)
        {
            ResultInfo result = new ResultInfo();
            if (req == null || req.EmpInfoId <= 0)
            {
                result.ErrorMessage = "EmpInfoId is required.";
                return result;
            }
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> gSqlParameterList = new List<SqlParameter>();
                gSqlParameterList.Add(new SqlParameter("@EmpInfoId", req.EmpInfoId));
                gSqlParameterList.Add(new SqlParameter("@IsLockOff", req.IsLockOff));
                gSqlParameterList.Add(new SqlParameter("@Remarks", (object)req.Remarks ?? DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@EntryBy", (object)req.EntryBy ?? DBNull.Value));
                DataTable dt = accessManager.GetDataTable("sp_Webapi_Save_DcrAreaLock", gSqlParameterList);
                result.isSuccess = dt.Rows.Count > 0 && Convert.ToInt32(dt.Rows[0]["IsSuccess"]) == 1;
                result.Msd = dt.Rows.Count > 0 ? dt.Rows[0]["Message"].ToString() : "";
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
        // IsLock = false lifts the chamber's DCR area lock (geofence), true puts it back
        public ResultInfo SaveChamberLock(ChamberLockDTO req)
        {
            ResultInfo result = new ResultInfo();
            if (req == null || req.ChamberId <= 0)
            {
                result.ErrorMessage = "ChamberId is required.";
                return result;
            }
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> gSqlParameterList = new List<SqlParameter>();
                gSqlParameterList.Add(new SqlParameter("@ChamberId", req.ChamberId));
                gSqlParameterList.Add(new SqlParameter("@IsLock", req.IsLock));
                DataTable dt = accessManager.GetDataTable("sp_Webapi_Save_ChamberLock", gSqlParameterList);
                result.isSuccess = dt.Rows.Count > 0 && Convert.ToInt32(dt.Rows[0]["IsSuccess"]) == 1;
                result.Msd = dt.Rows.Count > 0 ? dt.Rows[0]["Message"].ToString() : "";
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

        // Fills the chamber's Lat/Lon only when it has none yet (the SP enforces that rule)
        public ResultInfo SaveChamberLocation(ChamberLocationDTO req)
        {
            ResultInfo result = new ResultInfo();
            if (req == null || req.ChamberId <= 0)
            {
                result.ErrorMessage = "ChamberId is required.";
                return result;
            }
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> gSqlParameterList = new List<SqlParameter>();
                gSqlParameterList.Add(new SqlParameter("@chamberId", req.ChamberId));
                gSqlParameterList.Add(new SqlParameter("@Latitude", (object)req.Latitude ?? DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@Longitude", (object)req.Longitude ?? DBNull.Value));
                DataTable dt = accessManager.GetDataTable("sp_Webapi_Set_ChamberLocationIfEmpty", gSqlParameterList);
                bool updated = dt.Rows.Count > 0 && Convert.ToBoolean(dt.Rows[0]["Updated"]);
                result.isSuccess = updated;
                result.Msd = updated ? "Chamber location saved." : "Chamber already has a location.";
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

        public DataTable GetDoctorContactType()
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> aSqlParameterlist = new List<SqlParameter>();

                DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_DoctorContactType");
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



        public ResultInfo SaveDoctor(DoctorSM aDoctor)
        {
            ResultInfo aInformation = new ResultInfo();
            if (aDoctor == null)
            {
                aInformation.ErrorMessage = "Doctor information is required.";
                return aInformation;
            }

            if (string.IsNullOrWhiteSpace(aDoctor.DoctorName))
            {
                aInformation.ErrorMessage = "DoctorName is required.";
                return aInformation;
            }

            if (string.IsNullOrWhiteSpace(aDoctor.DoctorAddress))
            {
                aInformation.ErrorMessage = "Doctor Address is required.";
                return aInformation;
            }

            if (string.IsNullOrWhiteSpace(aDoctor.InstitutionSTr) || aDoctor.InstitutionSTr.Trim() == "----")
            {
                aInformation.ErrorMessage = "Doctor Institution is required.";
                return aInformation;
            }

            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> gSqlParameterList = new List<SqlParameter>();
                DateTime? entryDtae = DateTime.Now;
                int? entryBy = aDoctor.EmpId > 0 ? aDoctor.EmpId : aDoctor.SessionUser;
                aDoctor.SessionUser = entryBy;
                gSqlParameterList.Add(new SqlParameter("@DoctorId", aDoctor.DoctorId));
                gSqlParameterList.Add(new SqlParameter("@DoctorName", aDoctor.DoctorName.Trim()));
                gSqlParameterList.Add(new SqlParameter("@DoctorAddress", aDoctor.DoctorAddress ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@DegreeStr", aDoctor.DegreeStr ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@BrandStr", aDoctor.BrandStr ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@SpecialityStr", aDoctor.SpecialityStr ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@DoctorTypeStr", aDoctor.DoctorTypeStr ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@InstitutionSTr", aDoctor.InstitutionSTr ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@SessionUser", aDoctor.SessionUser ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@EmpId", aDoctor.EmpId ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@Latitude", aDoctor.Latitude ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@Longitude", aDoctor.Longitude ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@ContactTypeId", aDoctor.ContactTypeId ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@SpecialDayId", aDoctor.SpecialDayId ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@DoctorCategoryId", aDoctor.DoctorCategoryId ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@StationTypeId", aDoctor.StationTypeId ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@ProgramTypeId", aDoctor.ProgramTypeId ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@SMCTypeId", aDoctor.SMCTypeId ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@DoctorTypeId", aDoctor.DoctorTypeId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@DesignationId", aDoctor.DesignationId ?? (object)DBNull.Value));


                int pk = 0;

                if(aDoctor.DoctorId>0)
                {
                    aInformation.isSuccess = accessManager.UpdateData("sp_Webapi_UD_DoctorEntry", gSqlParameterList);
                    if (aInformation.isSuccess)
                    {
                        pk = aDoctor.DoctorId;
                    }
                }
                else
                {
                    pk = accessManager.SaveDataReturnPrimaryKey("sp_Webapi_Save_DoctorEntry", gSqlParameterList);

                    if (aDoctor.SessionUser > 0)
                    {

                      


                        DoctorAppLog_DAO aInfoApp = new DoctorAppLog_DAO();
                        List<SqlParameter> gSqlEmp = new List<SqlParameter>();

                        gSqlEmp.Add(new SqlParameter("@EmployeeId", (aDoctor.SessionUser)));
                        DataTable dtEmp = accessManager.GetDataTable("sp_Webapi_Get_RSM_DZSMcheck", gSqlEmp);
                        aInfoApp.EmpInfoId = aDoctor.SessionUser;



                        if (dtEmp.Rows.Count > 0)
                        {
                            string NewEmpId = dtEmp.Rows[0]["ASMEMPId"].ToString();
                            if (NewEmpId != "nai")
                            {

                                List<SqlParameter> gSqlNewEmp = new List<SqlParameter>();
                                gSqlNewEmp.Add(new SqlParameter("@EmployeeId", aDoctor.SessionUser));
                                DataTable dtNewEmp = accessManager.GetDataTable("sp_Webapi_Get_DICcheck", gSqlNewEmp);

                                if (dtNewEmp.Rows.Count > 0 && dtNewEmp.Rows[0]["UserType"].ToString() != "DZSM")
                                {



                                    List<SqlParameter> gSqlGet = new List<SqlParameter>();
                                    gSqlGet.Add(new SqlParameter("@DoctorId", (pk)));
                                    DataTable dt = accessManager.GetDataTable("sp_Get_DoctorAppDataForDuplicate", gSqlGet);
                                    List<SqlParameter> gSql = new List<SqlParameter>();


                                    int i = 0;

                                    if (dt.Rows[i]["DoctorApprovalId"] != DBNull.Value)
                                    {
                                        aInfoApp.DoctorApprovalId = Convert.ToInt32(dt.Rows[i]["DoctorApprovalId"].ToString());
                                    }

                                    if (dt.Rows[i]["DoctorId"] != DBNull.Value)
                                    {
                                        aInfoApp.DoctorId = Convert.ToInt32(dt.Rows[i]["DoctorId"]);
                                    }

                                    if (dt.Rows[i]["EmpInfoId"] != DBNull.Value)
                                    {
                                        aInfoApp.EmpInfoId = Convert.ToInt32(dt.Rows[i]["EmpInfoId"].ToString());

                                    }

                                    if (dt.Rows[i]["Date"] != DBNull.Value)
                                    {
                                        aInfoApp.Date = (dt.Rows[i]["Date"].ToString());
                                    }


                                    if (dt.Rows[i]["FromEmpId"] != DBNull.Value)
                                    {
                                        aInfoApp.FromEmpId = Convert.ToInt32(dt.Rows[i]["FromEmpId"].ToString());
                                    }



                                    if (dt.Rows[i]["ToEmpId"] != DBNull.Value)
                                    {
                                        aInfoApp.ToEmpId = Convert.ToInt32(dt.Rows[i]["ToEmpId"].ToString());
                                    }
                                    if (dt.Rows[i]["TableId"] != DBNull.Value)
                                    {
                                        aInfoApp.TableId = Convert.ToInt32(dt.Rows[i]["TableId"].ToString());
                                    }
                                    if (dt.Rows[i]["Status"] != DBNull.Value)
                                    {
                                        aInfoApp.Status = dt.Rows[i]["Status"].ToString();
                                    }
                                    if (dt.Rows[i]["Comments"] != DBNull.Value)
                                    {
                                        aInfoApp.Comments = dt.Rows[i]["Comments"].ToString();
                                    }


                                    if (dt.Rows[i]["WaitingRole"] != DBNull.Value)
                                    {
                                        aInfoApp.WaitingRole = dt.Rows[i]["WaitingRole"].ToString();
                                    }


                                    if (dt.Rows[i]["WatingEmployee"] != DBNull.Value)
                                    {
                                        aInfoApp.WatingEmployee = dt.Rows[i]["WatingEmployee"].ToString();
                                    }

                                    if (dt.Rows[i]["Type"] != DBNull.Value)
                                    {
                                        aInfoApp.Type = (dt.Rows[i]["Type"].ToString());
                                    }


                                    if (dt.Rows[i]["Step"] != DBNull.Value)
                                    {
                                        aInfoApp.Step = Convert.ToInt32(dt.Rows[i]["Step"].ToString());
                                    }



                                    if (dt.Rows[i]["ApprovalStatus"] != DBNull.Value)
                                    {
                                        aInfoApp.ApprovalStatus = dt.Rows[i]["ApprovalStatus"].ToString();
                                    }











                                    if (dt.Rows[i]["GroupId"] != DBNull.Value)
                                    {
                                        aInfoApp.GroupId = Convert.ToInt32(dt.Rows[i]["GroupId"].ToString());
                                    }

                                    if (dt.Rows[i]["RegionId"] != DBNull.Value)
                                    {
                                        aInfoApp.RegionId = Convert.ToInt32(dt.Rows[i]["RegionId"].ToString());
                                    }


                                    if (dt.Rows[i]["AreaId"] != DBNull.Value)
                                    {
                                        aInfoApp.AreaId = Convert.ToInt32(dt.Rows[i]["AreaId"].ToString());
                                    }


                                    if (dt.Rows[i]["TerritoryId"] != DBNull.Value)
                                    {
                                        aInfoApp.TerritoryId = Convert.ToInt32(dt.Rows[i]["TerritoryId"].ToString());
                                    }


                                    if (dt.Rows[i]["RoleTypeId"] != DBNull.Value)
                                    {
                                        aInfoApp.RoleTypeId = Convert.ToInt32(dt.Rows[i]["RoleTypeId"].ToString());
                                    }


                                    if (dt.Rows[i]["EmpMasterCode"] != DBNull.Value)
                                    {
                                        aInfoApp.EmpMasterCode = dt.Rows[i]["EmpMasterCode"].ToString();
                                    }


                                    if (dt.Rows[i]["EmpName"] != DBNull.Value)
                                    {
                                        aInfoApp.EmpName = dt.Rows[i]["EmpName"].ToString();
                                    }


                                    if (dt.Rows[i]["TerritoryName"] != DBNull.Value)
                                    {
                                        aInfoApp.TerritoryName = dt.Rows[i]["TerritoryName"].ToString();
                                    }

                                    if (dt.Rows[i]["TerritoryCode"] != DBNull.Value)
                                    {
                                        aInfoApp.TerritoryCode = dt.Rows[i]["TerritoryCode"].ToString();
                                    }

                                    if (dt.Rows[i]["AreaCode"] != DBNull.Value)
                                    {
                                        aInfoApp.AreaCode = dt.Rows[i]["AreaCode"].ToString();
                                    }


                                    if (dt.Rows[i]["ToRoleTypeId"] != DBNull.Value)
                                    {
                                        aInfoApp.ToRoleTypeId = Convert.ToInt32(dt.Rows[i]["ToRoleTypeId"].ToString());
                                    }

                                    if (dt.Rows[i]["MIOEmpId"] != DBNull.Value)
                                    {
                                        aInfoApp.MIOEmpId = Convert.ToInt32(dt.Rows[i]["MIOEmpId"].ToString());
                                    }

                                    if (dt.Rows[i]["ASMEMPId"] != DBNull.Value)
                                    {
                                        aInfoApp.ASMEMPId = Convert.ToInt32(dt.Rows[i]["ASMEMPId"].ToString());
                                    }

                                    if (dt.Rows[i]["RSMEMPId"] != DBNull.Value)
                                    {
                                        aInfoApp.RSMEMPId = Convert.ToInt32(dt.Rows[i]["RSMEMPId"].ToString());
                                    }
                                    if (dt.Rows[i]["NSMEMPId"] != DBNull.Value)
                                    {
                                        aInfoApp.NSMEMPId = Convert.ToInt32(dt.Rows[i]["NSMEMPId"].ToString());
                                    }






                                    gSql.Add(new SqlParameter("@DoctorApprovalId", aInfoApp.DoctorApprovalId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                                    gSql.Add(new SqlParameter("@Date", DateTime.Now));
                                    gSql.Add(new SqlParameter("@FromEmpId", aInfoApp.FromEmpId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                                    gSql.Add(new SqlParameter("@ToEmpId", aInfoApp.ToEmpId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                                    gSql.Add(new SqlParameter("@TableId", aInfoApp.TableId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                                    gSql.Add(new SqlParameter("@Status", "Verified" ?? (object)DBNull.Value ?? (object)DBNull.Value));
                                    gSql.Add(new SqlParameter("@Comments", "ABC" ?? (object)DBNull.Value ?? (object)DBNull.Value));
                                    gSql.Add(new SqlParameter("@Type", aInfoApp.Type ?? (object)DBNull.Value ?? (object)DBNull.Value));
                                    gSql.Add(new SqlParameter("@Step", aInfoApp.Step + 1 ?? (object)DBNull.Value ?? (object)DBNull.Value));
                                    gSql.Add(new SqlParameter("@GroupId", aInfoApp.GroupId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                                    gSql.Add(new SqlParameter("@RegionId", aInfoApp.RegionId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                                    gSql.Add(new SqlParameter("@AreaId", aInfoApp.AreaId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                                    gSql.Add(new SqlParameter("@TerritoryId", aInfoApp.TerritoryId ?? (object)DBNull.Value ?? (object)DBNull.Value));

                                    gSql.Add(new SqlParameter("@ToGroupId", null ?? (object)DBNull.Value ?? (object)DBNull.Value));
                                    gSql.Add(new SqlParameter("@ToRegionId", null ?? (object)DBNull.Value ?? (object)DBNull.Value));
                                    gSql.Add(new SqlParameter("@ToAreaId", null ?? (object)DBNull.Value ?? (object)DBNull.Value));
                                    gSql.Add(new SqlParameter("@ToTerritoryId", null ?? (object)DBNull.Value ?? (object)DBNull.Value));

                                    gSql.Add(new SqlParameter("@EntryByS", aInfoApp.EmpInfoId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                                    gSql.Add(new SqlParameter("@EntryDateS", entryDtae));
                                    gSql.Add(new SqlParameter("@EntryTimeS", null ?? (object)DBNull.Value ?? (object)DBNull.Value));
                                    gSql.Add(new SqlParameter("@ApproveByS", aInfoApp.EmpInfoId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                                    gSql.Add(new SqlParameter("@ApproveDateS", entryDtae));
                                    gSql.Add(new SqlParameter("@ApproveTimeS", null ?? (object)DBNull.Value ?? (object)DBNull.Value));

                                    gSql.Add(new SqlParameter("@EntryByApp", aInfoApp.EmpInfoId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                                    gSql.Add(new SqlParameter("@EntryDateApp", entryDtae));
                                    gSql.Add(new SqlParameter("@EntryTimeApp", null ?? (object)DBNull.Value ?? (object)DBNull.Value));
                                    gSql.Add(new SqlParameter("@ApproveByApp", aInfoApp.EmpInfoId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                                    gSql.Add(new SqlParameter("@ApproveDateApp", entryDtae));
                                    gSql.Add(new SqlParameter("@ApproveTimeApp", null ?? (object)DBNull.Value ?? (object)DBNull.Value));
                                    gSql.Add(new SqlParameter("@MenuId", 303));

                                    aInformation.isSuccess = accessManager.SaveData("sp_webapi_SaveDoctorAppLog", gSql);

                                }
                            }


                        }


                    }


                }
                if (pk > 0)
                {
                    aInformation.isSuccess = true;
                }
                else
                {
                    throw new InvalidOperationException("Doctor could not be saved.");
                }

                string[] degreeList = SplitDoctorValues(aDoctor.DegreeStr);
                string[] BrandList = SplitDoctorValues(aDoctor.BrandStr);
                string[] spList = SplitDoctorValues(aDoctor.SpecialityStr);
                string[] docTypeList = SplitDoctorValues(aDoctor.DoctorTypeStr);
                string[] instituteList = SplitDoctorValues(aDoctor.InstitutionSTr);


                if (degreeList.Length > 0)
                {
                    foreach (String item in degreeList)
                    {
                        if (item != "")
                        {
                            List<SqlParameter> aSqlParameterList = new List<SqlParameter>();
                            aSqlParameterList.Add(new SqlParameter("@masterId", pk));
                            aSqlParameterList.Add(new SqlParameter("@itemName", item));
                            aInformation.isSuccess = accessManager.SaveData("sp_Webapi_Save_DoctorDegreeDetail", aSqlParameterList);
                        }

                    }

                }

                if (spList.Length > 0)
                {
                    foreach (String item in spList)
                        if (item != "")
                        {
                            {
                                List<SqlParameter> aSqlParameterList = new List<SqlParameter>();
                                aSqlParameterList.Add(new SqlParameter("@masterId", pk));
                                aSqlParameterList.Add(new SqlParameter("@itemName", item));
                                aInformation.isSuccess = accessManager.SaveData("sp_Webapi_Save_DoctorSpecialityDetail", aSqlParameterList);
                            }
                        }

                }

                if (docTypeList.Length > 0)
                {
                    foreach (String item in docTypeList)
                    {
                        if (item != "")
                        {
                            List<SqlParameter> aSqlParameterList = new List<SqlParameter>();
                            aSqlParameterList.Add(new SqlParameter("@masterId", pk));
                            aSqlParameterList.Add(new SqlParameter("@itemName", item));
                            aInformation.isSuccess = accessManager.SaveData("sp_Webapi_Save_DoctorTypeDetail", aSqlParameterList);
                        }
                    }

                }


                if (instituteList.Length > 0)
                {
                    foreach (String item in instituteList)
                    {
                        if (item != "")
                        {
                            List<SqlParameter> aSqlParameterList = new List<SqlParameter>();
                            aSqlParameterList.Add(new SqlParameter("@masterId", pk));
                            aSqlParameterList.Add(new SqlParameter("@itemName", item));
                            aInformation.isSuccess = accessManager.SaveData("sp_Webapi_Save_DoctorInstitutionDetail", aSqlParameterList);
                        }
                    }

                }


                if (BrandList.Length > 0)
                {
                    foreach (String item in BrandList)
                    {
                        if (item != "")
                        {
                            List<SqlParameter> aSqlParameterList = new List<SqlParameter>();
                            aSqlParameterList.Add(new SqlParameter("@masterId", pk));
                            aSqlParameterList.Add(new SqlParameter("@itemName", item));
                            aInformation.isSuccess = accessManager.SaveData("sp_Webapi_Save_DoctorBrandDetail", aSqlParameterList);
                        }
                    }

                }

                if (aDoctor.aChemberListDAO != null)
                {
                    foreach (DoctorChemberDetailDAO item in aDoctor.aChemberListDAO)
                    {
                        List<SqlParameter> aSqlParameterList = new List<SqlParameter>();

                        aSqlParameterList.Add(new SqlParameter("@pk", pk));
                        aSqlParameterList.Add(new SqlParameter("@ChamberTypeId", item.ChamberTypeId));
                        aSqlParameterList.Add(new SqlParameter("@Name", item.Name ?? (object)DBNull.Value));
                        aSqlParameterList.Add(new SqlParameter("@Shift", item.Shift ?? (object)DBNull.Value));
                        aSqlParameterList.Add(new SqlParameter("@SittingTime", item.SittingTime ?? (object)DBNull.Value));
                        aSqlParameterList.Add(new SqlParameter("@InstitutionId", item.InstitutionId ?? (object)DBNull.Value));
                        aSqlParameterList.Add(new SqlParameter("@Address", item.Address ?? (object)DBNull.Value));
                        aInformation.isSuccess = accessManager.SaveData("sp_Webapi_Save_DoctorChemberDetail", aSqlParameterList);

                    }
            }



                if (aDoctor.aDoctorContactDAO != null)
                {
                    foreach ( DoctorContactDAO item in aDoctor.aDoctorContactDAO)
                    {
                        List<SqlParameter> aSqlParameterList = new List<SqlParameter>();

                        aSqlParameterList.Add(new SqlParameter("@pk", pk));
                        aSqlParameterList.Add(new SqlParameter("@Contact", item.Contact ?? (object)DBNull.Value ?? (object)DBNull.Value));
                        aSqlParameterList.Add(new SqlParameter("@ContactTypeId", item.ContactTypeId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                        aInformation.isSuccess = accessManager.SaveData("sp_Webapi_Save_DoctorContactDetail", aSqlParameterList);

                    }
                }


                if (aDoctor.aDoctorSpecialDAO != null)
                {
                    foreach (DoctorSpecialDAO item in aDoctor.aDoctorSpecialDAO)
                    {
                        List<SqlParameter> aSqlParameterList = new List<SqlParameter>();

                        aSqlParameterList.Add(new SqlParameter("@pk", pk));
                        DateTime specialDate;
                        object specialDateValue = TryParseSpecialDate(item.SpeciaDateStr, out specialDate)
                            ? (object)specialDate
                            : DBNull.Value;
                        aSqlParameterList.Add(new SqlParameter("@SpeciaDateStr", specialDateValue));
                        aSqlParameterList.Add(new SqlParameter("@SpecialDayId", item.SpecialDayId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                        aInformation.isSuccess = accessManager.SaveData("sp_Webapi_Save_DoctorSpecialDayDetail", aSqlParameterList);

                    }
                }


            }
            catch (Exception exception)
            {
                accessManager.SqlConnectionClose(true);
                aInformation.isSuccess = false;
                aInformation.ErrorMessage = exception.Message;
            }
            finally
            {
                    accessManager.SqlConnectionClose();
            }

            return aInformation;



        }

        private static string[] SplitDoctorValues(string value)
        {
            return (value ?? string.Empty)
                .Split(new[] { ',' }, StringSplitOptions.RemoveEmptyEntries)
                .Select(item => item.Trim())
                .Where(item => item.Length > 0)
                .Distinct(StringComparer.OrdinalIgnoreCase)
                .ToArray();
        }

        private static bool TryParseSpecialDate(string value, out DateTime date)
        {
            string[] formats = { "dd-MMM-yyyy", "d-MMM-yyyy", "yyyy-MM-dd", "dd/MM/yyyy", "d/M/yyyy" };
            return DateTime.TryParseExact(
                value,
                formats,
                CultureInfo.InvariantCulture,
                DateTimeStyles.None,
                out date);
        }


        public ResultInfo UpdateDoctorProviderDAL(string empId, string DoctorId, String ProviderTypeId)
        {

            ResultInfo aInformation = new ResultInfo();
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> gSqlParameterList = new List<SqlParameter>();
                gSqlParameterList.Add(new SqlParameter("@empId", empId));
                gSqlParameterList.Add(new SqlParameter("@DoctorId", DoctorId));
                gSqlParameterList.Add(new SqlParameter("@ProgramTypeId", ProviderTypeId));

                aInformation.isSuccess = accessManager.UpdateData("sp_Webapi_UpdateDoctorProvider", gSqlParameterList);



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

        public ResultInfo UpdateDoctorLocationDAL(CordinateUpdate cordinateUpdate)
        {
            ResultInfo aInformation = new ResultInfo();
            if (cordinateUpdate == null)
            {
                aInformation.isSuccess = false;
                aInformation.ErrorMessage = "Doctor location information is required.";
                return aInformation;
            }

            if (cordinateUpdate.Id <= 0)
            {
                aInformation.isSuccess = false;
                aInformation.ErrorMessage = "Doctor id is required.";
                return aInformation;
            }

            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> gSqlParameterList = new List<SqlParameter>();
                gSqlParameterList.Add(new SqlParameter("@DoctorId", cordinateUpdate.Id));
                gSqlParameterList.Add(new SqlParameter("@EmpId", cordinateUpdate.EmpId ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@Latitude", cordinateUpdate.LatitudeValue ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@Longitude", cordinateUpdate.LongitudeValue ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@StreetAddress", cordinateUpdate.StreetAddress ?? (object)DBNull.Value));

                aInformation.isSuccess = accessManager.UpdateData("sp_Webapi_UpdateDoctorLocation", gSqlParameterList);
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


        public ResultInfo UpdateDoctorMarketDAL(DoctorMArket_DAO _DAO)
        {

            ResultInfo aInformation = new ResultInfo();
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> gSqlParameterList = new List<SqlParameter>();
                List<SqlParameter> sqlCheck = new List<SqlParameter>();


                gSqlParameterList.Add(new SqlParameter("@empId", _DAO.empId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@DoctorId", _DAO.DoctorId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@TGroupId", _DAO.GroupId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@TZoneId", _DAO.ZoneId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@TAreaId", _DAO.AreaId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@TTeritoryId", _DAO.TerritoryId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@TSTeritoryId", _DAO.STeritoryId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@TMarketId", _DAO.MarketId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                sqlCheck.Add(new SqlParameter("@Marketd", _DAO.MarketId ?? (object)DBNull.Value ?? (object)DBNull.Value));

                DataTable dtMS = accessManager.GetDataTable("sp_Webapi_CheckFakeMarket", sqlCheck);

                if (dtMS.Rows.Count == 0)
                {

                    aInformation.isSuccess = accessManager.UpdateData("sp_Webapi_UpdateDoctorMarket", gSqlParameterList);
                }
                else
                {
                    aInformation.Msd = "This Market is not valid. Please select another market.";

                    aInformation.isValiCheck = true;
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


        public IList<DoctorList_DAO> GetDoctorList(int empId)
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> gSqlParameterList = new List<SqlParameter>();
                gSqlParameterList.Add(new SqlParameter("@empid", empId));

                DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_DoctorList", gSqlParameterList);
                List<DoctorList_DAO> aList = new List<DoctorList_DAO>();
                for (var i = 0; i < dt.Rows.Count; i++)

                {
                    DoctorList_DAO aInfo = new DoctorList_DAO();

                    if (dt.Rows[i]["DoctorId"] != DBNull.Value)
                    {
                        aInfo.DoctorId = Convert.ToInt32(dt.Rows[i]["DoctorId"].ToString());
                    }
                    if (dt.Rows[i]["DoctorName"] != DBNull.Value)
                    {
                        aInfo.DoctorName = (dt.Rows[i]["DoctorName"].ToString());
                    }

                    if (dt.Rows[i]["DoctorCode"] != DBNull.Value)
                    {
                        aInfo.DoctorCode = (dt.Rows[i]["DoctorCode"].ToString());
                    }

                    if (dt.Rows[i]["MarketCode"] != DBNull.Value)
                    {
                        aInfo.MarketCode = dt.Rows[i]["MarketCode"].ToString();

                    }
                    if (dt.Rows[i]["DocContact"] != DBNull.Value)
                    {
                        aInfo.DocContact = (dt.Rows[i]["DocContact"].ToString());
                    }



                    if (dt.Rows[i]["DoctorTypeName"] != DBNull.Value)
                    {
                        aInfo.DoctorTypeName = dt.Rows[i]["DoctorTypeName"].ToString();
                    }


                    if (dt.Rows[i]["ProgramTypeName"] != DBNull.Value)
                    {
                        aInfo.ProgramTypeName = dt.Rows[i]["ProgramTypeName"].ToString();
                    }

                    if (dt.Rows[i]["ChemberName"] != DBNull.Value)
                    {
                        aInfo.ChemberName = dt.Rows[i]["ChemberName"].ToString();
                    }


                    aInfo.GroupId = dt.Rows[i]["GroupId"] != DBNull.Value ? dt.Rows[i]["GroupId"].ToString() : "";
                    aInfo.RegionId = dt.Rows[i]["RegionId"] != DBNull.Value ? dt.Rows[i]["RegionId"].ToString() : "";
                    aInfo.AreaId = dt.Rows[i]["AreaId"] != DBNull.Value ? dt.Rows[i]["AreaId"].ToString() : "";
                    aInfo.TerritoryId = dt.Rows[i]["TerritoryId"] != DBNull.Value ? dt.Rows[i]["TerritoryId"].ToString() : "";
                    aInfo.SubTerritoryId = dt.Rows[i]["SubTerritoryId"] != DBNull.Value ? dt.Rows[i]["SubTerritoryId"].ToString() : "";
                    aInfo.MarketId = dt.Rows[i]["MarketId"] != DBNull.Value ? dt.Rows[i]["MarketId"].ToString() : "";
                    aInfo.MarketName = dt.Rows[i]["MarketName"] != DBNull.Value ? dt.Rows[i]["MarketName"].ToString() : "";

                    if (dt.Rows[i]["ProgramTypeId"] != DBNull.Value)
                    {
                        aInfo.ProgramTypeId = Convert.ToInt32(dt.Rows[i]["ProgramTypeId"].ToString());
                    }

                    if (dt.Rows[i]["DoctorTypeId"] != DBNull.Value)
                    {
                        aInfo.DoctorTypeId = Convert.ToInt32(dt.Rows[i]["DoctorTypeId"].ToString());
                    }

                    if (dt.Rows[i]["SMCTypeId"] != DBNull.Value)
                    {
                        aInfo.SMCTypeId = Convert.ToInt32(dt.Rows[i]["SMCTypeId"].ToString());
                    }

                    aInfo.SMCType = dt.Rows[i]["SMCType"] != DBNull.Value ? dt.Rows[i]["SMCType"].ToString() : "";
                    aInfo.Latitude = dt.Columns.Contains("Latitude") && dt.Rows[i]["Latitude"] != DBNull.Value ? dt.Rows[i]["Latitude"].ToString() : "";
                    aInfo.Longitude = dt.Columns.Contains("Longitude") && dt.Rows[i]["Longitude"] != DBNull.Value ? dt.Rows[i]["Longitude"].ToString() : "";
                    aInfo.StreetAddress = dt.Columns.Contains("StreetAddress") && dt.Rows[i]["StreetAddress"] != DBNull.Value ? dt.Rows[i]["StreetAddress"].ToString() : "";



                    List<SqlParameter> aSqlParameterListDetals = new List<SqlParameter>();

                    aSqlParameterListDetals.Add(new SqlParameter("@DoctorId", dt.Rows[i]["DoctorId"]));
                    DataTable dtDoctorChem = accessManager.GetDataTable("sp_Webapi_Get_DoctorBrandByDoctorId", aSqlParameterListDetals);



                     
                    List<doctorBrandDAO> aDetailsList = new List<doctorBrandDAO>();
                    doctorBrandDAO aChemberDetails;
                    for (var ch = 0; ch < dtDoctorChem.Rows.Count; ch++)

                    {

                        aChemberDetails = new doctorBrandDAO();
                        if (dtDoctorChem.Rows[ch]["DoctorId"] != DBNull.Value)
                        {
                            aChemberDetails.DoctorId = Convert.ToInt32(dtDoctorChem.Rows[ch]["DoctorId"].ToString());
                        }

                        if (dtDoctorChem.Rows[ch]["BrandId"] != DBNull.Value)
                        {
                            aChemberDetails.BrandId = Convert.ToInt32(dtDoctorChem.Rows[ch]["BrandId"].ToString());
                        }


                        if (dtDoctorChem.Rows[ch]["BrandName"] != DBNull.Value)
                        {
                            aChemberDetails.BrandName = dtDoctorChem.Rows[ch]["BrandName"].ToString();
                        }

                        aDetailsList.Add(aChemberDetails);
                    }
                    
               
                    
                    
                    aInfo.doctorBrand = aDetailsList;

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

        // Lightweight count used by the app to decide whether a full GetDoctorList sync is needed,
        // so a mobile client isn't forced to download and re-parse the whole list just to check for changes.
        public int GetDoctorCountByUser(int empId)
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> gSqlParameterList = new List<SqlParameter>();
                gSqlParameterList.Add(new SqlParameter("@empid", empId));

                DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_DoctorList", gSqlParameterList);
                return dt.Rows.Count;
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

        //public DataTable GetDoctorList(int empId)
        //{
        //    try
        //    {
        //        accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
        //        List<SqlParameter> aSqlParameterlist = new List<SqlParameter>();
        //        aSqlParameterlist.Add(new SqlParameter("@empid", empId));

        //        DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_DoctorList", aSqlParameterlist);
        //        return dt;
        //    }
        //    catch (Exception e)
        //    {
        //        throw;
        //    }
        //    finally
        //    {
        //        accessManager.SqlConnectionClose();
        //    }
        //}


        public IList<DoctorAppLog_DAO> GetDoctor_ApplogDAL(string pram, string Role, string AppStatus, string FromDt, string ToDt, string EmpId, string ProgramTypeId, string DoctorTypeId, string pharmatype)
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

                //gSqlParameterList.Add(new SqlParameter("@StationTypeId", StationTypeId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@ProgramTypeId", ProgramTypeId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@DoctorTypeId", DoctorTypeId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@pharmatype", pharmatype ?? (object)DBNull.Value ?? (object)DBNull.Value));



                DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_DoctorClaimApp", gSqlParameterList);
                //SqlDataReader dr = accessManager.GetSqlDataReader("sp_Webapi_Get_DoctorClaimApp", gSqlParameterList);
                List<DoctorAppLog_DAO> aList = new List<DoctorAppLog_DAO>();
                for (var i = 0; i < dt.Rows.Count; i++)

                           {
                    DoctorAppLog_DAO aInfo = new DoctorAppLog_DAO();


                    if (dt.Rows[i]["DoctorApprovalId"] != DBNull.Value)
                    {
                        aInfo.DoctorApprovalId = Convert.ToInt32(dt.Rows[i]["DoctorApprovalId"].ToString());
                    }

                    if (dt.Rows[i]["DoctorId"] != DBNull.Value)
                    {
                        aInfo.DoctorId = Convert.ToInt32(dt.Rows[i]["DoctorId"]);
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


                    if (dt.Rows[i]["WaitingRole"] != DBNull.Value)
                    {
                        aInfo.WaitingRole = dt.Rows[i]["WaitingRole"].ToString();
                    }


                    if (dt.Rows[i]["WatingEmployee"] != DBNull.Value)
                    {
                        aInfo.WatingEmployee = dt.Rows[i]["WatingEmployee"].ToString();
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

                    aSqlParameterListDetals.Add(new SqlParameter("@DoctorId", aInfo.TableId));
                    DataTable dtDoctor = accessManager.GetDataTable("sp_Webapi_Get_DoctorMaster_AppLog", aSqlParameterListDetals);

                   
                    DoctorSMDao aDetails;
                  

                        aDetails = new DoctorSMDao();
                        if (dtDoctor.Rows[0]["DoctorId"] != DBNull.Value)
                        {
                            aDetails.DoctorId = Convert.ToInt32(dtDoctor.Rows[0]["DoctorId"].ToString());
                        }
                        if (dtDoctor.Rows[0]["DoctorName"] != DBNull.Value)
                        {
                            aDetails.DoctorName = dtDoctor.Rows[0]["DoctorName"].ToString();
                        }

                        if (dtDoctor.Rows[0]["DoctorCode"] != DBNull.Value)
                        {
                            aDetails.DoctorCode = dtDoctor.Rows[0]["DoctorCode"].ToString();
                        }

                        if (dtDoctor.Rows[0]["ContactTypeId"] != DBNull.Value)
                        {
                            aDetails.ContactTypeId = Convert.ToInt32(dtDoctor.Rows[0]["ContactTypeId"].ToString());
                        }

                    if (dtDoctor.Rows[0]["ContactType"] != DBNull.Value)
                    {
                        aDetails.ContactType = dtDoctor.Rows[0]["ContactType"].ToString();
                    }

                    if (dtDoctor.Rows[0]["Contact"] != DBNull.Value)
                    {
                        aDetails.Contact = dtDoctor.Rows[0]["Contact"].ToString();
                    }

                    if (dtDoctor.Rows[0]["DesignationName"] != DBNull.Value)
                        {
                            aDetails.DesignationName = dtDoctor.Rows[0]["DesignationName"].ToString();
                        }


                        if (dtDoctor.Rows[0]["DoctorTypeId"] != DBNull.Value)
                        {
                            aDetails.DoctorTypeId = Convert.ToInt32(dtDoctor.Rows[0]["DoctorTypeId"].ToString());
                        }

                  

                    if (dtDoctor.Rows[0]["DoctorTypeName"] != DBNull.Value)
                        {
                            aDetails.DoctorTypeStr = dtDoctor.Rows[0]["DoctorTypeName"].ToString();
                        }


                        if (dtDoctor.Rows[0]["DegreeName"] != DBNull.Value)
                        {
                            aDetails.DegreeStr = dtDoctor.Rows[0]["DegreeName"].ToString();
                        }

                        if (dtDoctor.Rows[0]["DoctorSpeciality"] != DBNull.Value)
                        {
                            aDetails.SpecialityStr = dtDoctor.Rows[0]["DoctorSpeciality"].ToString();
                        }

                        if (dtDoctor.Rows[0]["DoctorInstitution"] != DBNull.Value)
                        {
                            aDetails.InstitutionSTr = dtDoctor.Rows[0]["DoctorInstitution"].ToString();
                        }
                        if (dtDoctor.Rows[0]["DoctorBrand"] != DBNull.Value)
                        {
                            aDetails.BrandStr = dtDoctor.Rows[0]["DoctorBrand"].ToString();
                        }

                        if (dtDoctor.Rows[0]["DoctorCategoryId"] != DBNull.Value)
                        {
                            aDetails.DoctorCategoryId = Convert.ToInt32(dtDoctor.Rows[0]["DoctorCategoryId"].ToString());
                        }

                        if (dtDoctor.Rows[0]["CategoryName"] != DBNull.Value)
                        {
                            aDetails.CategoryName = dtDoctor.Rows[0]["CategoryName"].ToString();
                        }

                        if (dtDoctor.Rows[0]["ProgramTypeId"] != DBNull.Value)
                        {
                            aDetails.ProgramTypeId = Convert.ToInt32(dtDoctor.Rows[0]["ProgramTypeId"].ToString());
                        }


                    if (dtDoctor.Rows[0]["DesignationId"] != DBNull.Value)
                    {
                        aDetails.DesignationId = Convert.ToInt32(dtDoctor.Rows[0]["DesignationId"].ToString());
                    }

                    if (dtDoctor.Rows[0]["ProgramTypeName"] != DBNull.Value)
                        {
                            aDetails.ProgramTypeName = dtDoctor.Rows[0]["ProgramTypeName"].ToString();
                        }

                    if (dtDoctor.Rows[0]["StationTypeId"] != DBNull.Value)
                    {
                        aDetails.StationTypeId = Convert.ToInt32(dtDoctor.Rows[0]["StationTypeId"].ToString());
                    }

                    if (dtDoctor.Rows[0]["SpecialDayId"] != DBNull.Value)
                        {
                            aDetails.SpecialDayId = Convert.ToInt32(dtDoctor.Rows[0]["SpecialDayId"].ToString());
                        }

                        if (dtDoctor.Rows[0]["SpecialDay"] != DBNull.Value)
                        {
                            aDetails.SpecialDay = dtDoctor.Rows[0]["SpecialDay"].ToString();
                        }

                        if (dtDoctor.Rows[0]["AreaName"] != DBNull.Value)
                        {
                            aDetails.AreaName = dtDoctor.Rows[0]["AreaName"].ToString();
                        }

                        if (dtDoctor.Rows[0]["TerritoryName"] != DBNull.Value)
                        {
                            aDetails.TerritoryName = dtDoctor.Rows[0]["TerritoryName"].ToString();
                        }

                        if (dtDoctor.Rows[0]["SubTerritoryName"] != DBNull.Value)
                        {
                            aDetails.SubTerritoryName = dtDoctor.Rows[0]["SubTerritoryName"].ToString();
                        }


                        if (dtDoctor.Rows[0]["MarketName"] != DBNull.Value)
                        {
                            aDetails.MarketName = dtDoctor.Rows[0]["MarketName"].ToString();
                        }

                    if (dtDoctor.Rows[0]["RegionName"] != DBNull.Value)
                    {
                        aDetails.RegionName = dtDoctor.Rows[0]["RegionName"].ToString();
                    }


                    if (dtDoctor.Rows[0]["TerritoryId"] != DBNull.Value)
                    {
                        aDetails.TerritoryId = Convert.ToInt32(dtDoctor.Rows[0]["TerritoryId"].ToString());
                    }

                    if (dtDoctor.Rows[0]["SubTerritoryId"] != DBNull.Value)
                    {
                        aDetails.SubTerritoryId = Convert.ToInt32(dtDoctor.Rows[0]["SubTerritoryId"].ToString());
                    }
                    if (dtDoctor.Rows[0]["MarketId"] != DBNull.Value)
                        {
                            aDetails.MarketId = Convert.ToInt32(dtDoctor.Rows[0]["MarketId"].ToString());
                        }


                        if (dtDoctor.Rows[0]["SpeciaDateStr"] != DBNull.Value)
                        {
                            aDetails.SpeciaDateStr = dtDoctor.Rows[0]["SpeciaDateStr"].ToString();
                        }

                        if (dtDoctor.Rows[0]["Reamrks"] != DBNull.Value)
                        {
                            aDetails.Remarks = dtDoctor.Rows[0]["Reamrks"].ToString();
                        }


                    if (dtDoctor.Rows[0]["SMCType"] != DBNull.Value)
                    {
                        aDetails.SMCTypeName = (dtDoctor.Rows[0]["SMCType"].ToString());
                    }


                    if (dtDoctor.Rows[0]["SMCTypeId"] != DBNull.Value)
                    {
                        aDetails.SMCTypeId = Convert.ToInt32(dtDoctor.Rows[0]["SMCTypeId"].ToString());
                    }
                    if (dtDoctor.Rows[0]["DoctorAddress"] != DBNull.Value)
                    {
                        aDetails.DoctorAddress = (dtDoctor.Rows[0]["DoctorAddress"].ToString());
                    }
                    //aDoctorMasterList.Add(aDetails);
                    List<SqlParameter> asqChem = new List<SqlParameter>();

                        asqChem.Add(new SqlParameter("@DoctorId", aInfo.TableId));
                        DataTable dtDoctorChem = accessManager.GetDataTable("sp_Webapi_Get_DoctorChember_AppLog", asqChem);
                        List<DoctorChemberDetailDAOApp> aDetailsList = new List<DoctorChemberDetailDAOApp>();
                        DoctorChemberDetailDAOApp aChemberDetails;
                        for (var ch = 0; ch < dtDoctorChem.Rows.Count; ch++)

                        {

                            aChemberDetails = new DoctorChemberDetailDAOApp();
                            if (dtDoctorChem.Rows[ch]["ChamberTypeId"] != DBNull.Value)
                            {
                                aChemberDetails.ChamberTypeId = Convert.ToInt32(dtDoctorChem.Rows[ch]["ChamberTypeId"].ToString());
                            }

                            if (dtDoctorChem.Rows[ch]["ChamberTypeName"] != DBNull.Value)
                            {
                                aChemberDetails.ChamberTypeName = dtDoctorChem.Rows[ch]["ChamberTypeName"].ToString();
                            }
                            if (dtDoctorChem.Rows[ch]["ChamberName"] != DBNull.Value)
                            {
                                aChemberDetails.ChamberName = dtDoctorChem.Rows[ch]["ChamberName"].ToString();
                            }
                            if (dtDoctorChem.Columns.Contains("Shift") && dtDoctorChem.Rows[ch]["Shift"] != DBNull.Value)
                            {
                                aChemberDetails.Shift = dtDoctorChem.Rows[ch]["Shift"].ToString();
                            }
                            if (dtDoctorChem.Columns.Contains("SittingTime") && dtDoctorChem.Rows[ch]["SittingTime"] != DBNull.Value)
                            {
                                aChemberDetails.SittingTime = dtDoctorChem.Rows[ch]["SittingTime"].ToString();
                            }
                            if (dtDoctorChem.Columns.Contains("InstitutionId") && dtDoctorChem.Rows[ch]["InstitutionId"] != DBNull.Value)
                            {
                                aChemberDetails.InstitutionId = Convert.ToInt32(dtDoctorChem.Rows[ch]["InstitutionId"].ToString());
                            }
                            if (dtDoctorChem.Columns.Contains("Address") && dtDoctorChem.Rows[ch]["Address"] != DBNull.Value)
                            {
                                aChemberDetails.Address = dtDoctorChem.Rows[ch]["Address"].ToString();
                            }
                            aDetailsList.Add(aChemberDetails);
                        }
                        aDetails.aChemberListDAO = aDetailsList;




                    List<SqlParameter> asqCon = new List<SqlParameter>();

                    asqCon.Add(new SqlParameter("@DoctorId", aInfo.TableId));
                    DataTable dtCon = accessManager.GetDataTable("sp_Webapi_Get_DoctorContact_AppLog", asqCon);
                    List<DoctorContactDAONew> aConList = new List<DoctorContactDAONew>();
                    DoctorContactDAONew aConDetails;
                    for (var ch = 0; ch < dtCon.Rows.Count; ch++)

                    {

                        aConDetails = new DoctorContactDAONew();
                        if (dtCon.Rows[ch]["ContactTypeId"] != DBNull.Value)
                        {
                            aConDetails.ContactTypeId = Convert.ToInt32(dtCon.Rows[ch]["ContactTypeId"].ToString());
                        }

                        if (dtCon.Rows[ch]["Contact"] != DBNull.Value)
                        {
                            aConDetails.Contact = dtCon.Rows[ch]["Contact"].ToString();
                        }

                        if (dtCon.Rows[ch]["ContactType"] != DBNull.Value)
                        {
                            aConDetails.ContactType = dtCon.Rows[ch]["ContactType"].ToString();
                        }

                        aConList.Add(aConDetails);
                    }
                    aDetails.aDoctorContactDAO = aConList;


                    List<SqlParameter> asqday = new List<SqlParameter>();

                    asqday.Add(new SqlParameter("@DoctorId", aInfo.TableId));
                    DataTable dtday = accessManager.GetDataTable("sp_Webapi_Get_DoctorSpecialDay_AppLog", asqday);
                    List<DoctorSpecialDAONew> aDayList = new List<DoctorSpecialDAONew>();
                    DoctorSpecialDAONew aDayDetails;
                    for (var ch = 0; ch < dtday.Rows.Count; ch++)

                    {

                        aDayDetails = new DoctorSpecialDAONew();
                        if (dtday.Rows[ch]["SpecialDayId"] != DBNull.Value)
                        {
                            aDayDetails.SpecialDayId = Convert.ToInt32(dtday.Rows[ch]["SpecialDayId"].ToString());
                        }

                        if (dtday.Rows[ch]["SpecialDate"] != DBNull.Value)
                        {
                            aDayDetails.SpeciaDateStr = dtday.Rows[ch]["SpecialDate"].ToString();
                        }

                        if (dtday.Rows[ch]["SpecialDay"] != DBNull.Value)
                        {
                            aDayDetails.SpecialDay = dtday.Rows[ch]["SpecialDay"].ToString();
                        }

                        aDayList.Add(aDayDetails);
                    }
                    aDetails.aDoctorSpecialDAO = aDayList;




                    aInfo.DoctorMasterDao = aDetails;




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


        public ResultInfo SaveDoctor_ApplogDAL(SaveDoctorAppLog_DAO atten)
        {
            ResultInfo aInformation = new ResultInfo();
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);

                List<SqlParameter> gSqlParameterList = new List<SqlParameter>();
                DateTime entryDtae = DateTime.Now;

                gSqlParameterList.Add(new SqlParameter("@DoctorApprovalId", atten.DoctorApprovalId ?? (object)DBNull.Value ?? (object)DBNull.Value));
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

                aInformation.isSuccess = accessManager.SaveData("sp_webapi_SaveDoctorAppLog", gSqlParameterList);

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



        public ResultInfo SaveDCR_ApplogDAL(SaveDCRAppLog_DAO atten)
        {
            ResultInfo aInformation = new ResultInfo();
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);

                List<SqlParameter> gSqlParameterList = new List<SqlParameter>();
                DateTime entryDtae = DateTime.Now;

                gSqlParameterList.Add(new SqlParameter("@DCRApprovalId", atten.DCRApprovalId ?? (object)DBNull.Value ?? (object)DBNull.Value));
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

                aInformation.isSuccess = accessManager.SaveData("sp_webapi_SaveDCRAppLog", gSqlParameterList);

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


    

        public DataTable GetPrescriptionTYpe(int empId)
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> aSqlParameterlist = new List<SqlParameter>();
                aSqlParameterlist.Add(new SqlParameter("@empid", empId));

                DataTable dt = accessManager.GetDataTable("sp_WebAPi_Get_PrescriptionType", aSqlParameterlist);
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


        public ResultInfo SavePrescritpion(PrescriptionSM aInfo)
        {
            ResultInfo aInformation = new ResultInfo();
            try
            {

                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> gSqlParameterList = new List<SqlParameter>();
                DateTime entryDtae = DateTime.Now;

                gSqlParameterList.Add(new SqlParameter("@doctorId", aInfo.DoctorId));
                gSqlParameterList.Add(new SqlParameter("@presTypeId", aInfo.PrescriptionTypeId));
                gSqlParameterList.Add(new SqlParameter("@ChemberId", aInfo.ChemberId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@presDate", aInfo.PrescriptionDate));
                gSqlParameterList.Add(new SqlParameter("@sessionUser", aInfo.SessionUser));


                int pk = 0;
                pk = accessManager.SaveDataReturnPrimaryKey("sp_Webapi_Save_Prescription", gSqlParameterList);
                if (pk > 0)
                {
                    aInformation.isSuccess = true;
                    foreach (mProduct item in aInfo.aProList)
                    {
                        List<SqlParameter> aSqlParameterList = new List<SqlParameter>();
                        aSqlParameterList.Add(new SqlParameter("@productId", item.ProductId));
                        aSqlParameterList.Add(new SqlParameter("@pk", pk));
                        aInformation.isSuccess = accessManager.SaveData("sp_Webapi_Save_PrescriptionDetail", aSqlParameterList);

                    }


                }

                if (aInformation.isSuccess)
                {
                    try
                    {
                        AppPrimaryDAL app = new AppPrimaryDAL();
                        ImagePath _path = new ImagePath();
                        string type = "Prescription";
                        _path = app.GetImagePath(type);



                        string targetPath = @"" + _path.ImagePathValue + "";


                        if (!System.IO.Directory.Exists(targetPath))
                        {
                            System.IO.Directory.CreateDirectory(targetPath);
                        }

                        string filePath = targetPath + "/" + _path.ImagePreName + pk + "." + "jpg";
                        File.WriteAllBytes(filePath, Convert.FromBase64String(aInfo.ImageString));
                    }
                    catch { }
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

        public DataTable GetPrescriptionList(int empId, string FromDt, string ToDt, string providertype, string pharmatype, string doctortype)
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> aSqlParameterlist = new List<SqlParameter>();
                aSqlParameterlist.Add(new SqlParameter("@empid", empId));
                aSqlParameterlist.Add(new SqlParameter("@FromDt", FromDt ?? (object)DBNull.Value ?? (object)DBNull.Value));
                aSqlParameterlist.Add(new SqlParameter("@ToDt", ToDt ?? (object)DBNull.Value ?? (object)DBNull.Value));


                aSqlParameterlist.Add(new SqlParameter("@providertype", providertype ?? (object)DBNull.Value ?? (object)DBNull.Value));
                aSqlParameterlist.Add(new SqlParameter("@pharmatype", pharmatype ?? (object)DBNull.Value ?? (object)DBNull.Value));
                aSqlParameterlist.Add(new SqlParameter("@doctortype", doctortype ?? (object)DBNull.Value ?? (object)DBNull.Value));

                DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_PrescriptionList", aSqlParameterlist);
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

        public ResultInfo SavePrescription_ApplogDAL(SavePrescription_Applog_DAO atten)
        {
            ResultInfo aInformation = new ResultInfo();
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);

                List<SqlParameter> gSqlParameterList = new List<SqlParameter>();
                DateTime entryDtae = DateTime.Now;

                gSqlParameterList.Add(new SqlParameter("@PrescriptionApprovalId", atten.PrescriptionApprovalId ?? (object)DBNull.Value ?? (object)DBNull.Value));
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

                aInformation.isSuccess = accessManager.SaveData("sp_webapi_SavePrescriptionAppLog", gSqlParameterList);

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
        public IList<Prescription_Applog_DAO> GetPrescription_ApplogDAL(string pram, string Role, string AppStatus, string FromDt, string ToDt, string EmpId, string GroupId, string ZoneId, string AreaId, string TerritoryId, string providertype, string pharmatype, string doctortype)
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> gSqlParameterList = new List<SqlParameter>();
                gSqlParameterList.Add(new SqlParameter("@Role", Role ?? (object)DBNull.Value ?? (object)DBNull.Value));

                pram =  NewMethod(pram, Role, gSqlParameterList);


                gSqlParameterList.Add(new SqlParameter("@AppStatus", AppStatus ?? (object)DBNull.Value ?? (object)DBNull.Value));

                gSqlParameterList.Add(new SqlParameter("@FromDt", FromDt ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@ToDt", ToDt ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@EmpId", EmpId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@GroupId", GroupId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@ZoneId", ZoneId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@AreaId", AreaId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@TerritoryId", TerritoryId ?? (object)DBNull.Value ?? (object)DBNull.Value));

                gSqlParameterList.Add(new SqlParameter("@providertype", providertype ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@pharmatype", pharmatype ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@doctortype", doctortype ?? (object)DBNull.Value ?? (object)DBNull.Value));


                DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_PrescriptionApp", gSqlParameterList);
                //SqlDataReader dr = accessManager.GetSqlDataReader("sp_Webapi_Get_DoctorClaimApp", gSqlParameterList);
                List<Prescription_Applog_DAO> aList = new List<Prescription_Applog_DAO>();
                for (var i = 0; i < dt.Rows.Count; i++)

                {
                    Prescription_Applog_DAO aInfo = new Prescription_Applog_DAO();


                    if (dt.Rows[i]["PrescriptionApprovalId"] != DBNull.Value)
                    {
                        aInfo.PrescriptionApprovalId = Convert.ToInt32(dt.Rows[i]["PrescriptionApprovalId"].ToString());
                    }

                    if (dt.Rows[i]["PrescriptionId"] != DBNull.Value)
                    {
                        aInfo.PrescriptionId = Convert.ToInt32(dt.Rows[i]["PrescriptionId"]);
                    }

                    if (dt.Rows[i]["EmpInfoId"] != DBNull.Value)
                    {
                        aInfo.EmpInfoId = Convert.ToInt32(dt.Rows[i]["EmpInfoId"].ToString());

                    }

                    if (dt.Rows[i]["EntryDate"] != DBNull.Value)
                    {
                        aInfo.Date = (dt.Rows[i]["EntryDate"].ToString());
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
                    DataTable dtDoctor = accessManager.GetDataTable("sp_Webapi_Get_PrescriptionByPrescriptionIdId", aSqlParameterListDetals);


                    PrescriptionRMasterDAO aDetails;


                    aDetails = new PrescriptionRMasterDAO();

                    if (dtDoctor.Rows[0]["PrescriptionId"] != DBNull.Value)
                    {
                        aDetails.PrescriptionId = Convert.ToInt32(dtDoctor.Rows[0]["PrescriptionId"].ToString());
                    }

                    if (dtDoctor.Rows[0]["DoctorId"] != DBNull.Value)
                    {
                        aDetails.DoctorId = Convert.ToInt32(dtDoctor.Rows[0]["DoctorId"].ToString());
                    }

                    if (dtDoctor.Rows[0]["PrescriptionDate"] != DBNull.Value)
                    {
                        aDetails.PrescriptionDate = dtDoctor.Rows[0]["PrescriptionDate"].ToString();
                    }


                    if (dtDoctor.Rows[0]["PrescriptionType"] != DBNull.Value)
                    {
                        aDetails.PrescriptionType = dtDoctor.Rows[0]["PrescriptionType"].ToString();
                    }


                    if (dtDoctor.Rows[0]["DoctorName"] != DBNull.Value)
                    {
                        aDetails.DoctorName = dtDoctor.Rows[0]["DoctorName"].ToString();
                    }


                    if (dtDoctor.Rows[0]["ChemberName"] != DBNull.Value)
                    {
                        aDetails.ChemberName = dtDoctor.Rows[0]["ChemberName"].ToString();
                    }



                    if (dtDoctor.Rows[0]["GroupName"] != DBNull.Value)
                    {
                        aDetails.GroupName = dtDoctor.Rows[0]["GroupName"].ToString();
                    }


                    if (dtDoctor.Rows[0]["AreaName"] != DBNull.Value)
                    {
                        aDetails.AreaName = dtDoctor.Rows[0]["AreaName"].ToString();
                    }

                    if (dtDoctor.Rows[0]["TerritoryName"] != DBNull.Value)
                    {
                        aDetails.TerritoryName = dtDoctor.Rows[0]["TerritoryName"].ToString();
                    }


                    if (dtDoctor.Rows[0]["SubTerritoryName"] != DBNull.Value)
                    {
                        aDetails.SubTerritoryName = dtDoctor.Rows[0]["SubTerritoryName"].ToString();
                    }

                    if (dtDoctor.Rows[0]["MarketName"] != DBNull.Value)
                    {
                        aDetails.MarketName = dtDoctor.Rows[0]["MarketName"].ToString();
                    }


                    if (dtDoctor.Rows[0]["ImageString"] != DBNull.Value)
                    {
                        aDetails.ImageString = dtDoctor.Rows[0]["ImageString"].ToString();
                    }

                    List<SqlParameter> asqChem = new List<SqlParameter>();

                    asqChem.Add(new SqlParameter("@id", aInfo.TableId));
                    DataTable dtDoctorChem = accessManager.GetDataTable("sp_Webapi_Get_PresCripProductbyId", asqChem);
                    List<PrescriptionDtlsDAO> aDetailsList = new List<PrescriptionDtlsDAO>();
                    PrescriptionDtlsDAO aChemberDetails;
                    for (var ch = 0; ch < dtDoctorChem.Rows.Count; ch++)

                    {

                        aChemberDetails = new PrescriptionDtlsDAO();


                        if (dtDoctorChem.Rows[ch]["ProductId"] != DBNull.Value)
                        {
                            aChemberDetails.ProductId = Convert.ToInt32(dtDoctorChem.Rows[ch]["ProductId"].ToString());
                        }


                        if (dtDoctorChem.Rows[ch]["ProductName"] != DBNull.Value)
                        {
                            aChemberDetails.ProductName = dtDoctorChem.Rows[ch]["ProductName"].ToString();
                        }

                        aDetailsList.Add(aChemberDetails);
                    }
                    aDetails.aPrescriptionDtlsDAO = aDetailsList;



                    aInfo.aPrescriptionRMasterDAO = aDetails;




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
         

        public IList<DCRAppLog_DAO> GetDCR_ApplogDAL(string pram, string Role, string AppStatus, string FromDt, string ToDt, string EmpId, string GroupId, string ZoneId, string AreaId, string TerritoryId, string providertype, string pharmatype, string doctortype)
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

                gSqlParameterList.Add(new SqlParameter("@GroupId", GroupId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@ZoneId", ZoneId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@AreaId", AreaId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@TerritoryId", TerritoryId ?? (object)DBNull.Value ?? (object)DBNull.Value));


                gSqlParameterList.Add(new SqlParameter("@providertype", providertype ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@pharmatype", pharmatype ?? (object)DBNull.Value ?? (object)DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@doctortype", doctortype ?? (object)DBNull.Value ?? (object)DBNull.Value));


                DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_DCRApp", gSqlParameterList);
                //SqlDataReader dr = accessManager.GetSqlDataReader("sp_Webapi_Get_DoctorClaimApp", gSqlParameterList);
                List<DCRAppLog_DAO> aList = new List<DCRAppLog_DAO>();
                for (var i = 0; i < dt.Rows.Count; i++)

                {
                    DCRAppLog_DAO aInfo = new DCRAppLog_DAO();


                    if (dt.Rows[i]["DCRApprovalId"] != DBNull.Value)
                    {
                        aInfo.DCRApprovalId = Convert.ToInt32(dt.Rows[i]["DCRApprovalId"].ToString());
                    }

                    if (dt.Rows[i]["DcrId"] != DBNull.Value)
                    {
                        aInfo.DcrId = Convert.ToInt32(dt.Rows[i]["DcrId"]);
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

                    aSqlParameterListDetals.Add(new SqlParameter("@DcrId", aInfo.TableId));
                    DataTable dtDoctor = accessManager.GetDataTable("sp_Webapi_Get_DCRInfoListbyDcrId", aSqlParameterListDetals);


                    DCRMasterDAO aDetails;


                    aDetails = new DCRMasterDAO();

                    if (dtDoctor.Rows[0]["DcrId"] != DBNull.Value)
                    {
                        aDetails.DcrId = Convert.ToInt32(dtDoctor.Rows[0]["DcrId"].ToString());
                    }

                    if (dtDoctor.Rows[0]["DoctorId"] != DBNull.Value)
                    {
                        aDetails.DoctorId = Convert.ToInt32(dtDoctor.Rows[0]["DoctorId"].ToString());
                    }

                    if (dtDoctor.Rows[0]["DcrDate"] != DBNull.Value)
                    {
                        aDetails.DcrDate = dtDoctor.Rows[0]["DcrDate"].ToString();
                    }
                    if (dtDoctor.Rows[0]["DoctorName"] != DBNull.Value)
                    {
                        aDetails.DoctorName = dtDoctor.Rows[0]["DoctorName"].ToString();
                    }

                    if (dtDoctor.Rows[0]["VisitTypeName"] != DBNull.Value)
                    {
                        aDetails.VisitTypeName = dtDoctor.Rows[0]["VisitTypeName"].ToString();
                    }


                    if (dtDoctor.Rows[0]["ChemberName"] != DBNull.Value)
                    {
                        aDetails.ChemberName = dtDoctor.Rows[0]["ChemberName"].ToString();
                    }

                    if (dtDoctor.Rows[0]["Remarks"] != DBNull.Value)
                    {
                        aDetails.Remarks = dtDoctor.Rows[0]["Remarks"].ToString();
                    }


                    if (dtDoctor.Rows[0]["GroupName"] != DBNull.Value)
                    {
                        aDetails.GroupName = dtDoctor.Rows[0]["GroupName"].ToString();
                    }

                    if (dtDoctor.Rows[0]["ReasonName"] != DBNull.Value)
                    {
                        aDetails.ReasonName = dtDoctor.Rows[0]["ReasonName"].ToString();
                    }
                    if (dtDoctor.Rows[0]["AreaName"] != DBNull.Value)
                    {
                        aDetails.AreaName = dtDoctor.Rows[0]["AreaName"].ToString();
                    }

                    if (dtDoctor.Rows[0]["TerritoryName"] != DBNull.Value)
                    {
                        aDetails.TerritoryName = dtDoctor.Rows[0]["TerritoryName"].ToString();
                    }


                    if (dtDoctor.Rows[0]["SubTerritoryName"] != DBNull.Value)
                    {
                        aDetails.SubTerritoryName = dtDoctor.Rows[0]["SubTerritoryName"].ToString();
                    }

                    if (dtDoctor.Rows[0]["MarketName"] != DBNull.Value)
                    {
                        aDetails.MarketName = dtDoctor.Rows[0]["MarketName"].ToString();
                    }

                    if (dtDoctor.Rows[0]["Latitude"] != DBNull.Value)
                    {
                        aDetails.Latitude = dtDoctor.Rows[0]["Latitude"].ToString();
                    }

                    if (dtDoctor.Rows[0]["Longitude"] != DBNull.Value)
                    {
                        aDetails.Longitude = dtDoctor.Rows[0]["Longitude"].ToString();
                    }

                    if (dtDoctor.Rows[0]["StreetAddress"] != DBNull.Value)
                    {
                        aDetails.StreetAddress = dtDoctor.Rows[0]["StreetAddress"].ToString();
                    }

                    List<SqlParameter> asqChem = new List<SqlParameter>();

                    asqChem.Add(new SqlParameter("@DcrId", aInfo.TableId));
                    DataTable dtDoctorChem = accessManager.GetDataTable("sp_Webapi_Get_DCRProductList", asqChem);
                    List<DCRProductDAO> aDetailsList = new List<DCRProductDAO>();
                    DCRProductDAO aChemberDetails;
                    for (var ch = 0; ch < dtDoctorChem.Rows.Count; ch++)

                    {

                        aChemberDetails = new DCRProductDAO();
                        if (dtDoctorChem.Rows[ch]["ProductId"] != DBNull.Value)
                        {
                            aChemberDetails.ProductId = Convert.ToInt32(dtDoctorChem.Rows[ch]["ProductId"].ToString());
                        }

                        if (dtDoctorChem.Rows[ch]["ProductName"] != DBNull.Value)
                        {
                            aChemberDetails.ProductName = dtDoctorChem.Rows[ch]["ProductName"].ToString();
                        }
                        if (dtDoctorChem.Rows[ch]["ProductQty"] != DBNull.Value)
                        {
                            aChemberDetails.ProductQty = Convert.ToInt32(dtDoctorChem.Rows[ch]["ProductQty"].ToString());
                        }
                        aDetailsList.Add(aChemberDetails);
                    }
                    aDetails.aDCRProductDAOList = aDetailsList;


                    List<SqlParameter> asqBrand = new List<SqlParameter>();

                    asqBrand.Add(new SqlParameter("@DcrId", aInfo.TableId));
                    DataTable dtBrnd = accessManager.GetDataTable("sp_Webapi_Get_DCRBrandList", asqBrand);
                    List<doctorBrandDAONew> aBrndList = new List<doctorBrandDAONew>();
                    doctorBrandDAONew aBrndDetails;
                    for (var ch = 0; ch < dtBrnd.Rows.Count; ch++)

                    {

                        aBrndDetails = new doctorBrandDAONew();
                        if (dtBrnd.Rows[ch]["BrandId"] != DBNull.Value)
                        {
                            aBrndDetails.BrandId = Convert.ToInt32(dtBrnd.Rows[ch]["BrandId"].ToString());
                        }

                        if (dtBrnd.Rows[ch]["BrandName"] != DBNull.Value)
                        {
                            aBrndDetails.BrandName = dtBrnd.Rows[ch]["BrandName"].ToString();
                        }

                        aBrndList.Add(aBrndDetails);
                    }


                    List<SqlParameter> asqVW = new List<SqlParameter>();

                    asqVW.Add(new SqlParameter("@DcrId", aInfo.TableId));
                    DataTable dtVW = accessManager.GetDataTable("sp_Webapi_Get_DCRVisitedWithList", asqVW);
                    List<VisitedWithDAO> aVWList = new List<VisitedWithDAO>();
                    VisitedWithDAO aVWDetails;
                    for (var ch = 0; ch < dtVW.Rows.Count; ch++)

                    {

                        aVWDetails = new VisitedWithDAO();
                        if (dtVW.Rows[ch]["EmpInfoId"] != DBNull.Value)
                        {
                            aVWDetails.EmpInfoId = Convert.ToInt32(dtVW.Rows[ch]["EmpInfoId"].ToString());
                        }

                        if (dtVW.Rows[ch]["EmpName"] != DBNull.Value)
                        {
                            aVWDetails.EmpName = dtVW.Rows[ch]["EmpName"].ToString();
                        }

                        aVWList.Add(aVWDetails);
                    }
                    aDetails.VisitedWithDAO = aVWList;

                    aDetails.doctorBrand = aBrndList;

                    aInfo.aDCRMasterDAO = aDetails;




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

        public DataTable GetVisitTYpe()
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> aSqlParameterlist = new List<SqlParameter>();
                DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_DoctoreVisitTypeForDcr");
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

        public DataTable GetDoctorChamberForDcr(int doctorId)
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> aSqlParameterlist = new List<SqlParameter>
                {
                    new SqlParameter("@doctorId",doctorId)
                };
                DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_DoctorChamberByDocId", aSqlParameterlist);
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

        public DataTable GetProductForDcrByType(int empId, string type)
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> aSqlParameterlist = new List<SqlParameter>
                {
                    new SqlParameter("@empId",empId),
                    new SqlParameter("@type",type)
                };
                DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_DcrProductByType", aSqlParameterlist);
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



        public ResultInfo SaveDcr(DcrMaster aInfo)
        {
            ResultInfo aInformation = new ResultInfo();
            try
            {




                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);


                int pk = 0;
                DataTable dtProduct = new DataTable();
                bool insuf = false;

                // DCR area lock + tour plan gate: return the reason to the app (the save SP also enforces it).
                List<SqlParameter> gateParams = new List<SqlParameter>();
                gateParams.Add(new SqlParameter("@doctorId", aInfo.DoctorId));
                gateParams.Add(new SqlParameter("@chamberId", (object)aInfo.ChamberId ?? DBNull.Value));
                gateParams.Add(new SqlParameter("@sessionUser", (object)aInfo.SessionUser ?? DBNull.Value));
                gateParams.Add(new SqlParameter("@dcrDate", (object)aInfo.DcrDate ?? DBNull.Value));
                gateParams.Add(new SqlParameter("@Type", (object)aInfo.Type ?? DBNull.Value));
                gateParams.Add(new SqlParameter("@Latitude", (object)aInfo.Latitude ?? DBNull.Value));
                gateParams.Add(new SqlParameter("@Longitude", (object)aInfo.Longitude ?? DBNull.Value));
                DataTable dtGate = accessManager.GetDataTable("sp_Webapi_Check_DcrGate", gateParams);
                string gateMsg = dtGate.Rows.Count > 0 ? dtGate.Rows[0]["Msg"].ToString() : "";
                if (gateMsg != "")
                {
                    aInformation.isValiCheck = true;
                    aInformation.Msd = gateMsg;
                    return aInformation;
                }


                if (aInfo.giftList != null)
                {
                    foreach (DCRProduct item in aInfo.giftList)
                    {
                        List<SqlParameter> gList = new List<SqlParameter>();
                        gList.Add(new SqlParameter("@ProductId", item.ProductId));
                        gList.Add(new SqlParameter("@Qty", item.Quantity));
                        gList.Add(new SqlParameter("@EmpInfoId", aInfo.SessionUser));
                        dtProduct = accessManager.GetDataTable("sp_Webapi_CHK_PROMOProductQty", gList);
                        if (dtProduct.Rows.Count == 0)
                        {
                            insuf = true;
                        }

                    }
                }

                if (aInfo.sampleList != null)
                {
                    foreach (DCRProduct item in aInfo.sampleList)
                    {
                        List<SqlParameter> gList = new List<SqlParameter>();
                        gList.Add(new SqlParameter("@ProductId", item.ProductId));
                        gList.Add(new SqlParameter("@Qty", item.Quantity));
                        gList.Add(new SqlParameter("@EmpInfoId", aInfo.SessionUser));
                        dtProduct = accessManager.GetDataTable("sp_Webapi_CHK_PROMOProductQty", gList);
                        if (dtProduct.Rows.Count == 0)
                        {
                            insuf = true;
                        }

                    }
                }
                List<SqlParameter> gSqlParameterList = new List<SqlParameter>();
                DateTime entryDtae = DateTime.Now;

                gSqlParameterList.Add(new SqlParameter("@doctorId", aInfo.DoctorId));
                gSqlParameterList.Add(new SqlParameter("@dcrDate", (object)aInfo.DcrDate ?? DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@EntryTime", (object)aInfo.EntryTime ?? DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@visitTypeId", (object)aInfo.TourPlanTypeId ?? DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@chamberId", (object)aInfo.ChamberId ?? DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@EntryDate_Apps", (object)aInfo.EntryDate_Apps ?? DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@Type", (object)aInfo.Type ?? DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@sessionUser", (object)aInfo.SessionUser ?? DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@remarks", (object)aInfo.Remarks ?? DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@DocTPDetailsId", (object)aInfo.DocTPDetailsId ?? DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@Latitude", (object)aInfo.Latitude ?? DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@Longitude", (object)aInfo.Longitude ?? DBNull.Value));
                gSqlParameterList.Add(new SqlParameter("@StreetAddress", (object)aInfo.StreetAddress ?? DBNull.Value));


                //prdId = prdId.TrimEnd(',');

                //if (aInfo.sampleList != null)
                //{
                //    foreach (DCRProduct item in aInfo.sampleList)
                //    {

                //        prdId = prdId + item.ProductId + ",";

                //    }
                //}
                //prdId = prdId.TrimEnd(',');
                //List<SqlParameter> asqChem = new List<SqlParameter>();

                //asqChem.Add(new SqlParameter("@prdIdStr", prdId));
                //asqChem.Add(new SqlParameter("@sessionUser", aInfo.SessionUser));

                //DataTable dtDoctorChem = accessManager.GetDataTable("sp_Webapi_Check_DcrProduct", asqChem);
                if (insuf ==false)
                {
                    pk = accessManager.SaveDataReturnPrimaryKey("sp_Webapi_Save_DcrInfo", gSqlParameterList);
                    if (pk > 0)
                    {
                        aInformation.isSuccess = true;
                        // first DCR at a chamber stores its GPS position; an existing chamber location is never overwritten.
                        // CVR excluded: there ChamberId is the customer id (the app sends Type "ccrAdapter").
                        if (!string.Equals(aInfo.Type, "ccrAdapter", StringComparison.OrdinalIgnoreCase)
                            && aInfo.ChamberId.GetValueOrDefault() > 0)
                        {
                            List<SqlParameter> locParams = new List<SqlParameter>();
                            locParams.Add(new SqlParameter("@chamberId", aInfo.ChamberId.Value));
                            locParams.Add(new SqlParameter("@Latitude", (object)aInfo.Latitude ?? DBNull.Value));
                            locParams.Add(new SqlParameter("@Longitude", (object)aInfo.Longitude ?? DBNull.Value));
                            accessManager.GetDataTable("sp_Webapi_Set_ChamberLocationIfEmpty", locParams);
                        }
                        //if (aInfo.productList != null)
                        //{
                        //    foreach (mProduct item in aInfo.productList)
                        //    {
                        //        List<SqlParameter> aSqlParameterList = new List<SqlParameter>();
                        //        aSqlParameterList.Add(new SqlParameter("@productId", item.ProductId));
                        //        aSqlParameterList.Add(new SqlParameter("@pk", pk));
                        //        aSqlParameterList.Add(new SqlParameter("@type", "Product"));
                        //        aInformation.isSuccess = accessManager.SaveData("sp_Webapi_Save_DcrProduct", aSqlParameterList);

                        //    }
                        //}

                        if (aInfo.giftList != null)
                        {
                            foreach (DCRProduct item in aInfo.giftList)
                            {
                                List<SqlParameter> agf = new List<SqlParameter>();
                                agf.Add(new SqlParameter("@productId", item.ProductId));
                                agf.Add(new SqlParameter("@qty", item.Quantity));
                                agf.Add(new SqlParameter("@empid", aInfo.SessionUser));

                                agf.Add(new SqlParameter("@pk", pk));
                                agf.Add(new SqlParameter("@type", "Gift"));
                                aInformation.isSuccess = accessManager.SaveData("sp_Webapi_Save_DcrProduct", agf);

                            }
                        }

                        if (aInfo.sampleList != null)
                        {
                            foreach (DCRProduct item in aInfo.sampleList)
                            {
                                List<SqlParameter> smp = new List<SqlParameter>();
                                smp.Add(new SqlParameter("@productId", item.ProductId));
                                smp.Add(new SqlParameter("@pk", pk));
                                smp.Add(new SqlParameter("@type", "Sample"));
                                smp.Add(new SqlParameter("@qty", item.Quantity));
                                smp.Add(new SqlParameter("@empid", aInfo.SessionUser));

                                aInformation.isSuccess = accessManager.SaveData("sp_Webapi_Save_DcrProduct", smp);

                            }
                        }

                        if (aInfo.aempList != null)
                        {
                            foreach (EmpInfo item in aInfo.aempList)
                            {
                                List<SqlParameter> smp = new List<SqlParameter>();
                                smp.Add(new SqlParameter("@empId", item.EmpInfoId));
                                smp.Add(new SqlParameter("@pk", pk));
                                aInformation.isSuccess = accessManager.SaveData("sp_Webapi_Save_DcrVisitedWith", smp);

                            }
                        }


                        if (aInfo.doctorBrand != null)
                        {
                            foreach (doctorBrandDA item in aInfo.doctorBrand)
                            {
                                List<SqlParameter> smp = new List<SqlParameter>();
                                smp.Add(new SqlParameter("@BrandId", item.BrandId));
                                smp.Add(new SqlParameter("@pk", pk));
                                aInformation.isSuccess = accessManager.SaveData("sp_Webapi_Save_DcrBrand", smp);

                            }
                        }

                    }
                }
                else
                {
                    aInformation.isValiCheck = true;
                    //if (dtDoctorChem.Rows[0]["Qty"] != DBNull.Value)
                    //{
                    aInformation.Msd = "Insufficient Quantity";
                    //}
                    //aInformation.Msd = "";
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


        public DataTable GetDcrList(string empId, string FromDt, string ToDt, string providertype, string pharmatype, string doctortype, int doctorId = 0, bool allTeam = false)
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> aSqlParameterlist = new List<SqlParameter>();
                aSqlParameterlist.Add(new SqlParameter("@empid", empId));
                aSqlParameterlist.Add(new SqlParameter("@FromDt", FromDt ?? (object)DBNull.Value ?? (object)DBNull.Value));
                aSqlParameterlist.Add(new SqlParameter("@ToDt", ToDt ?? (object)DBNull.Value ?? (object)DBNull.Value));
                aSqlParameterlist.Add(new SqlParameter("@providertype", providertype ?? (object)DBNull.Value ?? (object)DBNull.Value));
                aSqlParameterlist.Add(new SqlParameter("@pharmatype", pharmatype ?? (object)DBNull.Value ?? (object)DBNull.Value));
                aSqlParameterlist.Add(new SqlParameter("@doctortype", doctortype ?? (object)DBNull.Value ?? (object)DBNull.Value));
                aSqlParameterlist.Add(new SqlParameter("@doctorId", doctorId));
                aSqlParameterlist.Add(new SqlParameter("@allTeam", allTeam));

                DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_DcrListById", aSqlParameterlist);
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



        public ResultInfo DeleteDCR(int id)
        {


            ResultInfo aInformation = new ResultInfo();
            try

            {

                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> gSqlParameterList = new List<SqlParameter>();
                gSqlParameterList.Add(new SqlParameter("@id", id));
                aInformation.isSuccess = accessManager.DeleteData("sp_Webapi_Delete_Dcr", gSqlParameterList);

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


        public DataTable GetDoctorDesignation()
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> aSqlParameterlist = new List<SqlParameter>();
                DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_DoctorDesignation");
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

        public DataTable GetDWSPTotalsDAL(int month, int year, int empId)
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> gSqlParameterList = new List<SqlParameter>();
                gSqlParameterList.Add(new SqlParameter("@month", month));
                gSqlParameterList.Add(new SqlParameter("@year", year));
                gSqlParameterList.Add(new SqlParameter("@empId", empId));
                DataTable dt = accessManager.GetDataTable("sp_Get_GetDWSPTotalsByEmpId", gSqlParameterList);
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


        public ResultInfo SaveDocTourPlan(List<DocTourPlanMaster> aInfo)
        {
            ResultInfo aInformation = new ResultInfo();
            try
            {

                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);

                DateTime entryDtae = DateTime.Now;
                if (aInfo != null)
                {
                    foreach (DocTourPlanMaster item in aInfo)
                    {
                        List<SqlParameter> gSqlParameterList = new List<SqlParameter>();
                        gSqlParameterList.Add(new SqlParameter("@TourDate", item.TourPlanDate));
                        gSqlParameterList.Add(new SqlParameter("@DoctorName", item.DoctorName));
                        gSqlParameterList.Add(new SqlParameter("@doctorId", item.DoctorId));
                        gSqlParameterList.Add(new SqlParameter("@empId", item.EmpInfoId));
                        aInformation.isSuccess = accessManager.SaveData("sp_Webapi_Save_TourPlanInfo_Doctor_New", gSqlParameterList);

                    }


                }
                else
                {
                    aInformation.isSuccess = false;
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



        public ResultInfo SaveDWSPDAL(DWSPMasterDAO item)
        {
            ResultInfo aInformation = new ResultInfo();
            try
            {

                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);

                DateTime entryDtae = DateTime.Now;
                 
                     
                        List<SqlParameter> gSqlParameterList = new List<SqlParameter>();
                        gSqlParameterList.Add(new SqlParameter("@DWSPDate", item.DWSPDate ?? (object)DBNull.Value ?? (object)DBNull.Value));
                        gSqlParameterList.Add(new SqlParameter("@empId", item.EmpInfoId ?? (object)DBNull.Value ?? (object)DBNull.Value));
                        gSqlParameterList.Add(new SqlParameter("@FCBAmount", item.FCBAmount ?? (object)DBNull.Value ?? (object)DBNull.Value));
                        gSqlParameterList.Add(new SqlParameter("@CampaignAmount", item.CampaignAmount ?? (object)DBNull.Value ?? (object)DBNull.Value));
                        gSqlParameterList.Add(new SqlParameter("@GeneralAmount", item.GeneralAmount ?? (object)DBNull.Value ?? (object)DBNull.Value));

                        aInformation.isSuccess = accessManager.SaveData("sp_Webapi_Save_DWSPMaster", gSqlParameterList);

                  
                if (aInformation.isSuccess == false)
                {
                    aInformation.Msd = "Already Submitted!";
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

        public DataTable Get_DoctorTourPLanDetails(int month, int year, int empId)
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> aSqlParameterlist = new List<SqlParameter>();
                aSqlParameterlist.Add(new SqlParameter("@month", month));
                aSqlParameterlist.Add(new SqlParameter("@year", year));
                aSqlParameterlist.Add(new SqlParameter("@empId", empId));
                DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_TourTourPlanDetails", aSqlParameterlist);
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


        public DataTable GetDWSPDetailDAL(int month, int year, int empId)
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> aSqlParameterlist = new List<SqlParameter>();
                aSqlParameterlist.Add(new SqlParameter("@month", month));
                aSqlParameterlist.Add(new SqlParameter("@year", year));
                aSqlParameterlist.Add(new SqlParameter("@empId", empId));
                DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_DWSPDetails", aSqlParameterlist);
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
        public ResultInfo DeleteDoctorTourPlan(int id)
        {


            ResultInfo aInformation = new ResultInfo();
            try

            {

                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> gSqlParameterList = new List<SqlParameter>();
                gSqlParameterList.Add(new SqlParameter("@id", id));
                aInformation.isSuccess = accessManager.DeleteData("sp_Webapi_Delete_DoctorTourPlan", gSqlParameterList);

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


        public ResultInfo FinalSubmitDoctorTour(int empId, int month, int year, String remarks)
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
                aInformation.isSuccess = accessManager.UpdateData("sp_Webapi_Update_DoctorFinalSubmit", gSqlParameterList);
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

        public DataTable Get_TourPlanMasterData(int month, int year, int empId)
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> aSqlParameterlist = new List<SqlParameter>();
                aSqlParameterlist.Add(new SqlParameter("@empId", empId));
                aSqlParameterlist.Add(new SqlParameter("@month", month));
                aSqlParameterlist.Add(new SqlParameter("@year", year));
                DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_DoctorVisitPlanMasterData", aSqlParameterlist);
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


        public DataTable GetTodaysDoctorPlan(DateTime cDate, int empId)
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> aSqlParameterlist = new List<SqlParameter>();
                aSqlParameterlist.Add(new SqlParameter("@cDate", cDate));
                aSqlParameterlist.Add(new SqlParameter("@empId", empId));
           
                DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_DoctorPlanByDate", aSqlParameterlist);
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
        public DataTable GetDWSPByDateDAL(DateTime cDate, int empId)
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> aSqlParameterlist = new List<SqlParameter>();
                aSqlParameterlist.Add(new SqlParameter("@cDate", cDate));
                aSqlParameterlist.Add(new SqlParameter("@empId", empId));

                DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_DWSPByDate", aSqlParameterlist);
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
        public DataTable GetTodaysTask( int empId)
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> aSqlParameterlist = new List<SqlParameter>();
            
                aSqlParameterlist.Add(new SqlParameter("@empId", empId));

                DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_TodaysTask", aSqlParameterlist);
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
        public DataTable GetTodaysTaskDCPCCP( int empId)
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> aSqlParameterlist = new List<SqlParameter>();
            
                aSqlParameterlist.Add(new SqlParameter("@empId", empId));

                DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_TodaysTaskforDCPCCP", aSqlParameterlist);
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
        public DataTable GetUserRole()
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_Userrole");
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


        public DataTable GetEmpInfoRoleID(int empId)
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> aSqlParameterlist = new List<SqlParameter>();

                aSqlParameterlist.Add(new SqlParameter("@empId", empId));

                DataTable dt = accessManager.GetDataTable("sp_Webapi_GetEmpInfoRoleID", aSqlParameterlist);
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

        public DataTable GetUserByRoleId(int id,int empId)
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> aSqlParameterlist = new List<SqlParameter>();
                aSqlParameterlist.Add(new SqlParameter("@id", id));
                aSqlParameterlist.Add(new SqlParameter("@empId", empId));
                DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_UserByRoleId", aSqlParameterlist);
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


        public DataTable GetDoctorVisitTYpe()
        {
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);
                List<SqlParameter> aSqlParameterlist = new List<SqlParameter>();
                DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_VisitTypeForDcr");
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


        public ResultInfo SaveDoctorVisitPlan_ApplogDAL(SaveDoctorVisit_Applog_DAO atten)
        {
            ResultInfo aInformation = new ResultInfo();
            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);

                List<SqlParameter> gSqlParameterList = new List<SqlParameter>();
                DateTime entryDtae = DateTime.Now;

                gSqlParameterList.Add(new SqlParameter("@VisitPlanApprovalId", atten.VisitPlanApprovalId ?? (object)DBNull.Value ?? (object)DBNull.Value));
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

                aInformation.isSuccess = accessManager.SaveData("sp_webapi_SaveVisitPlanAppLog", gSqlParameterList);

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

        public IList<DoctorVisitPlan_Applog_DAO> GetDoctorVisit_ApplogDAL(string pram, string Role, string AppStatus, string FromDt, string ToDt, string EmpId, int? MonthValue, int? YearValue)
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


                DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_VisitPlanApp", gSqlParameterList);
                //SqlDataReader dr = accessManager.GetSqlDataReader("sp_Webapi_Get_DoctorClaimApp", gSqlParameterList);
                List<DoctorVisitPlan_Applog_DAO> aList = new List<DoctorVisitPlan_Applog_DAO>();
                for (var i = 0; i < dt.Rows.Count; i++)

                {
                    DoctorVisitPlan_Applog_DAO aInfo = new DoctorVisitPlan_Applog_DAO();


                    if (dt.Rows[i]["DocTPMaster"] != DBNull.Value)
                    {
                        aInfo.DocTPMaster = Convert.ToInt32(dt.Rows[i]["DocTPMaster"].ToString());
                    }

                    if (dt.Rows[i]["MonthValue"] != DBNull.Value)
                    {
                        aInfo.MonthValue = Convert.ToInt32(dt.Rows[i]["MonthValue"]);
                    }

                    if (dt.Rows[i]["YearValue"] != DBNull.Value)
                    {
                        aInfo.YearValue = Convert.ToInt32(dt.Rows[i]["YearValue"]);
                    }

                    if (dt.Rows[i]["VisitPlanApprovalId"] != DBNull.Value)
                    {
                        aInfo.VisitPlanApprovalId = Convert.ToInt32(dt.Rows[i]["VisitPlanApprovalId"]);
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
                    DataTable dtDoctor = accessManager.GetDataTable("sp_webapi_GetDoctorVisitPlanMasterById", aSqlParameterListDetals);


                    DoctorVisitPlanMasterDAO aDetails;


                    aDetails = new DoctorVisitPlanMasterDAO();

                    if (dtDoctor.Rows[0]["DocTPMaster"] != DBNull.Value)
                    {
                        aDetails.DocTPMaster = Convert.ToInt32(dtDoctor.Rows[0]["DocTPMaster"].ToString());
                    }
                    if (dtDoctor.Rows[0]["EmpInfoId"] != DBNull.Value)
                    {
                        aDetails.EmpInfoId = Convert.ToInt32(dtDoctor.Rows[0]["EmpInfoId"].ToString());
                    }

                    if (dtDoctor.Rows[0]["MonthValue"] != DBNull.Value)
                    {
                        aDetails.MonthValue = (dtDoctor.Rows[0]["MonthValue"].ToString());
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


                    List<SqlParameter> asqChem = new List<SqlParameter>();

                    asqChem.Add(new SqlParameter("@id", aInfo.TableId));
                    DataTable dtDoctorChem = accessManager.GetDataTable("sp_webapi_GetDoctorVisitDateById", asqChem);
                    List<dcvisDtlsDAODate> aDetailsList = new List<dcvisDtlsDAODate>();
                    dcvisDtlsDAODate aChemberDetails;
                    for (var ch = 0; ch < dtDoctorChem.Rows.Count; ch++)

                    {



                        aChemberDetails = new dcvisDtlsDAODate();



                        if (dtDoctorChem.Rows[ch]["TourPlanDate"] != DBNull.Value)
                        {
                            aChemberDetails.VisitPlanDate = dtDoctorChem.Rows[ch]["TourPlanDate"].ToString();

                         
                        string TourPlanDate = dtDoctorChem.Rows[ch]["TourPlanDate"].ToString();


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
                            DataTable dtDtl = accessManager.GetDataTable("sp_webapi_GetvisitPlanDetailById_new", asqDetls);
                            List<VisitPlanDtlsDAO> amarketList = new List<VisitPlanDtlsDAO>();
                            VisitPlanDtlsDAO aMrkDtls;
                            for (var chmr = 0; chmr < dtDtl.Rows.Count; chmr++)

                            {

                                aMrkDtls = new VisitPlanDtlsDAO();


                                
                                if (dtDtl.Rows[chmr]["DoctorName"] != DBNull.Value)
                                {
                                    aMrkDtls.DoctorName = dtDtl.Rows[chmr]["DoctorName"].ToString();

                                }

                                amarketList.Add(aMrkDtls);
                            }



                            aChemberDetails.aVisitDtlsDAO = amarketList;


                        }




                        aDetailsList.Add(aChemberDetails);
                    }

                    aDetails.aDoctorVisitPlanDetail = aDetailsList;


                    aInfo.aDoctorVisitPlanMaster = aDetails;



                    //if (dtDoctor.Rows[0]["DoctorName"] != DBNull.Value)
                    //{
                    //    aDetails.DoctorName = dtDoctor.Rows[0]["DoctorName"].ToString();
                    //}

                  





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


    }
}
