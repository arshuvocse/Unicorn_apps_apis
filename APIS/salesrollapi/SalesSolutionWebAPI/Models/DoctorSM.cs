using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using Newtonsoft.Json;

namespace SalesSolutionWebAPI.Models
{
    public class DoctorSM
    {
        public int  DoctorId { get; set; }
        public string DoctorName { get; set; }
        public string Remarks { get; set; }
        //public int? DivisionId { get; set; }
        //public int? DistrictId { get; set; }
        //public int? ThanaId { get; set; }

        public string DegreeStr { get; set; }
        public string BrandStr { get; set; }
        public string SpecialityStr { get; set; }
        public string DoctorTypeStr { get; set; }
        public string InstitutionSTr { get; set; }
        public int? ContactTypeId { get; set; }
        public int? SpecialDayId { get; set; }

        public int? DoctorTypeId { get; set; }

        public int? SessionUser { get; set; }
        public int? EmpId { get; set; }
        public int? SubMarketId { get; set; }
        public int? MarketId { get; set; }
        public int? TerritoryId { get; set; }
        public int? SubTerritoryId { get; set; }
        public string UnionName { get; set; }
        public string DoctorAddress { get; set; }
        public string Latitude { get; set; }
        public string Longitude { get; set; }

        public int? DoctorCategoryId { get; set; }
        public int? StationTypeId { get; set; }
        public int? SMCTypeId { get; set; }
        public int? ProgramTypeId { get; set; }
    
        public int? DesignationId { get; set; }

        public List<DoctorChemberDetailDAO> aChemberListDAO { get; set; }
        public List<DoctorContactDAO> aDoctorContactDAO { get; set; }
        public List<DoctorSpecialDAO> aDoctorSpecialDAO { get; set; }


    }

    public class CordinateUpdate
    {
        [JsonProperty("id")]
        public int Id { get; set; }

        [JsonProperty("empId")]
        public int? EmpId { get; set; }

        [JsonProperty("latitudeValue")]
        public string LatitudeValue { get; set; }

        [JsonProperty("longitudeValue")]
        public string LongitudeValue { get; set; }

        [JsonProperty("streetaddress")]
        public string StreetAddress { get; set; }
    }

    public class DoctorChemberDetailDAO
    {
        public int ChamberTypeId { get; set; }
        public string Name { get; set; }
        public string Shift { get; set; }
        public string SittingTime { get; set; }
        public int? InstitutionId { get; set; }
        public string InstitutionName { get; set; }
        public string Address { get; set; }
    }


    public class DoctorContactDAO
    {
        public int? ContactTypeId { get; set; }

        public string Contact { get; set; }
        public string ContactType { get; set; }


    }

    public class DoctorContactDAONew
    {
        public int? ContactTypeId { get; set; }

        public string Contact { get; set; }
        public string ContactType { get; set; }


    }
    public class DoctorSpecialDAO
    {
        public int? pk { get; set; }
        public int? SpecialDayId { get; set; }
        public string SpecialDay { get; set; }
        public string SpeciaDateStr { get; set; }


    }

    public class DoctorSpecialDAONew
    {
        public int? SpecialDayId { get; set; }
        public string SpeciaDateStr { get; set; }
        public string SpecialDay { get; set; }


    }

    public class DoctorProductBrandMasterDAO
    {
        public int MaxValue { get; set; }

        public List<DoctorProductBrandDao> aBrandListDAO { get; set; }

    }

    public class DoctorProductBrandDao
    {
        public int ProductBrandId { get; set; }
        public string ProductSQName { get; set; }

    }



    public class DoctorAppLog_DAO
    {

        public int? DoctorApprovalId { get; set; }
        public int? DoctorId { get; set; }
        public int? EmpInfoId { get; set; }

        public string Date { get; set; }

        public int? FromEmpId { get; set; }

        public int? ToEmpId { get; set; }

        public int? TableId { get; set; }

        public string Status { get; set; }

        public string Comments { get; set; }
        public string WaitingRole { get; set; }
        public string WatingEmployee { get; set; }

        public string Type { get; set; }

        public int? Step { get; set; }

        public int? GroupId { get; set; }

        public int? RegionId { get; set; }

        public int? AreaId { get; set; }

        public int? TerritoryId { get; set; }
  

        
        public int? RoleTypeId { get; set; }

        public int? ToRoleTypeId { get; set; }

        public int? MenuId { get; set; }

        public string TerritoryName { get; set; }
        public string TerritoryCode { get; set; }
        public string AreaCode { get; set; }
       
      

      
        public string ApprovalStatus { get; set; }
        public string EmpMasterCode { get; set; }
        public string EmpName { get; set; }



        public int MIOEmpId { get; set; }
        public int ASMEMPId { get; set; }
        public int RSMEMPId { get; set; }
        public int NSMEMPId { get; set; }
        public   DoctorSMDao DoctorMasterDao { get; set; }

    }

    public class DoctorSMDao
    {
        public int DoctorId { get; set; }
        public string DoctorName { get; set; }
        public string DoctorCode { get; set; }
        public string DesignationName { get; set; }
       
        public string CategoryName { get; set; }
        public string ProgramTypeName { get; set; }
        public string SpecialDay { get; set; }
        public string AreaName { get; set; }
        public string TerritoryName { get; set; }
        public string SubTerritoryName { get; set; }
        public string MarketName { get; set; }
        public string RegionName { get; set; }
        public string Remarks { get; set; }
        //public int? DivisionId { get; set; }
        //public int? DistrictId { get; set; }
        //public int? ThanaId { get; set; }

        public string DegreeStr { get; set; }
        public string BrandStr { get; set; }
        public string SpecialityStr { get; set; }
        public string DoctorTypeStr { get; set; }
        public string InstitutionSTr { get; set; }


        public int? ContactTypeId { get; set; }
        public string Contact { get; set; }
        public string ContactType { get; set; }

        public int? SessionUser { get; set; }
        public int? SubMarketId { get; set; }
        public int? DoctorTypeId { get; set; }
        public int? MarketId { get; set; }
        public int? TerritoryId { get; set; }
        public int? SubTerritoryId { get; set; }
        public string UnionName { get; set; }
        public int? SpecialDayId { get; set; }
        public string SpeciaDateStr { get; set; }
        public int? DoctorCategoryId { get; set; }
        public int? StationTypeId { get; set; }
        public int? ProgramTypeId { get; set; }
        public string SMCTypeName { get; set; }
        public int SMCTypeId { get; set; }
        public string DoctorAddress { get; set; }

        public int? DesignationId { get; set; }
    

        public List<DoctorChemberDetailDAOApp> aChemberListDAO { get; set; }

        public List<DoctorContactDAONew> aDoctorContactDAO { get; set; }
        public List<DoctorSpecialDAONew> aDoctorSpecialDAO { get; set; }
    }

    public class DoctorChemberDetailDAOApp
    {
        public int ChamberTypeId { get; set; }
        public string ChamberTypeName { get; set; }
        public string ChamberName { get; set; }
        public string Shift { get; set; }
        public string SittingTime { get; set; }
        public int? InstitutionId { get; set; }
        public string InstitutionName { get; set; }
        public string Address { get; set; }
    }


    


    public class SaveDoctorAppLog_DAO
    {
        public int? DoctorApprovalId { get; set; }

        public DateTime? Date { get; set; }

        public int? FromEmpId { get; set; }

        public int? ToEmpId { get; set; }

        public int? TableId { get; set; }

        public string Status { get; set; }

        public string Comments { get; set; }

        public string Type { get; set; }

        public int? Step { get; set; }

        public int? GroupId { get; set; }

        public int? RegionId { get; set; }

        public int? AreaId { get; set; }

        public int? TerritoryId { get; set; }

        public int? ToGroupId { get; set; }

        public int? ToRegionId { get; set; }

        public int? ToAreaId { get; set; }

        public int? ToTerritoryId { get; set; }

        public int? EntryByS { get; set; }

        public DateTime? EntryDateS { get; set; }

        public TimeSpan? EntryTimeS { get; set; }

        public int? ApproveByS { get; set; }

        public DateTime? ApproveDateS { get; set; }

        public TimeSpan? ApproveTimeS { get; set; }

        public int? EntryByApp { get; set; }

        public DateTime? EntryDateApp { get; set; }

        public TimeSpan? EntryTimeApp { get; set; }

        public int? ApproveByApp { get; set; }

        public DateTime? ApproveDateApp { get; set; }

        public TimeSpan? ApproveTimeApp { get; set; }

        public int? RoleTypeId { get; set; }

        public int? ToRoleTypeId { get; set; }
        public int? MenuId { get; set; }

    }



    public class SaveDCRAppLog_DAO
    {
        public int? DCRApprovalId { get; set; }

        public DateTime? Date { get; set; }

        public int? FromEmpId { get; set; }

        public int? ToEmpId { get; set; }

        public int? TableId { get; set; }

        public string Status { get; set; }

        public string Comments { get; set; }

        public string Type { get; set; }

        public int? Step { get; set; }

        public int? GroupId { get; set; }

        public int? RegionId { get; set; }

        public int? AreaId { get; set; }

        public int? TerritoryId { get; set; }

        public int? ToGroupId { get; set; }

        public int? ToRegionId { get; set; }

        public int? ToAreaId { get; set; }

        public int? ToTerritoryId { get; set; }

        public int? EntryByS { get; set; }

        public DateTime? EntryDateS { get; set; }

        public TimeSpan? EntryTimeS { get; set; }

        public int? ApproveByS { get; set; }

        public DateTime? ApproveDateS { get; set; }

        public TimeSpan? ApproveTimeS { get; set; }

        public int? EntryByApp { get; set; }

        public DateTime? EntryDateApp { get; set; }

        public TimeSpan? EntryTimeApp { get; set; }

        public int? ApproveByApp { get; set; }

        public DateTime? ApproveDateApp { get; set; }

        public TimeSpan? ApproveTimeApp { get; set; }

        public int? RoleTypeId { get; set; }

        public int? ToRoleTypeId { get; set; }
        public int? MenuId { get; set; }

    }


    public class DCRAppLog_DAO
    {

        public int DCRApprovalId { get; set; }
        public int? DcrId { get; set; }
        public int? EmpInfoId { get; set; }

        public string Date { get; set; }

        public int? FromEmpId { get; set; }

        public int? ToEmpId { get; set; }

        public int? TableId { get; set; }

        public string Status { get; set; }

        public string Comments { get; set; }

        public string Type { get; set; }

        public int? Step { get; set; }

        public int? GroupId { get; set; }

        public int? RegionId { get; set; }

        public int? AreaId { get; set; }

        public int? TerritoryId { get; set; }



        public int? RoleTypeId { get; set; }

        public int? ToRoleTypeId { get; set; }

        public int? MenuId { get; set; }

        public string TerritoryName { get; set; }
        public string TerritoryCode { get; set; }
        public string AreaCode { get; set; }




        public string ApprovalStatus { get; set; }
        public string EmpMasterCode { get; set; }
        public string EmpName { get; set; }



        public int MIOEmpId { get; set; }
        public int ASMEMPId { get; set; }
        public int RSMEMPId { get; set; }
        public int NSMEMPId { get; set; }
        public DCRMasterDAO aDCRMasterDAO { get; set; }

    }


    public class DoctorList_DAO
    {

        public int DoctorId { get; set; }
        public string DoctorName { get; set; }
        public string DoctorCode { get; set; }
        public string MarketCode { get; set; }
        public string DocContact { get; set; }
        public string DoctorTypeName { get; set; }
        public string ProgramTypeName { get; set; }
        
        public string ChemberName { get; set; }
        public string GroupId { get; set; }
        public string RegionId { get; set; }
        public string AreaId { get; set; }
        public string TerritoryId { get; set; }
        public string SubTerritoryId { get; set; }
        public string MarketId { get; set; }
        public string MarketName { get; set; }
        public int? ProgramTypeId { get; set; }
        public int? DoctorTypeId { get; set; }

        public int?  SMCTypeId { get; set; }
        public string SMCType { get; set; }
        public string Latitude { get; set; }
        public string Longitude { get; set; }
        public string StreetAddress { get; set; }



        public List<doctorBrandDAO> doctorBrand { get; set; }


    }

    public class doctorBrandDAO
    {
        public int DoctorId { get; set; }
        public string BrandName { get; set; }
        public int BrandId { get; set; }

    }
    public class DCRMasterDAO
    {
        public int DcrId { get; set; }

        public  string  DcrDate { get; set; }
        public  string DoctorName { get; set; }
        public  string VisitTypeName { get; set; }
        public  string ChemberName { get; set; }
   

        public int? TourTypeId { get; set; }

        public int? ChemberId { get; set; }

        public string EntryBy { get; set; }

        public DateTime? EntryDate { get; set; }

        public string UpdateBy { get; set; }

        public DateTime? UpdateDate { get; set; }

        public bool? IsApproved { get; set; }

        public string Remarks { get; set; }
        public string NonEffectiveReason { get; set; }
        public string ReasonName { get; set; }
        public string GroupName { get; set; }
        public string RegionName { get; set; }
        public string AreaName { get; set; }
        public string TerritoryName { get; set; }
        public string SubTerritoryName { get; set; }
        public string MarketName { get; set; }
        public string Latitude { get; set; }
        public string Longitude { get; set; }
        public string StreetAddress { get; set; }


        public int? DoctorId { get; set; }

        public int? DocTPDetailsId { get; set; }

        public int? GroupId { get; set; }

        public int? RegionId { get; set; }

        public int? AreaId { get; set; }

        public int? SubTerritoryId { get; set; }

        public int? TerritoryId { get; set; }

        public int? MarketId { get; set; }

        public bool? IsNonEffectiveReason { get; set; }

        public int? ReasonId { get; set; }

        public DateTime? EntryDate_Apps { get; set; }

        public string ApprovalStatus { get; set; }

        public List<DCRProductDAO> aDCRProductDAOList{ get; set; }
        public List<doctorBrandDAONew> doctorBrand { get; set; }
        public List<VisitedWithDAO> VisitedWithDAO { get; set; }


    }

    public class DCRProductDAO
    {
        public int ProductId { get; set; }
        public string ProductName { get; set; }
        public int ProductQty { get; set; }

    }

    public class doctorBrandDAONew
    {
        public int BrandId { get; set; }
        public string BrandName { get; set; }
       

    }
    public class VisitedWithDAO 
    {
        public int EmpInfoId { get; set; }
        public string EmpName { get; set; }


    }

    public class SavePrescription_Applog_DAO
    {
        public int? PrescriptionApprovalId { get; set; }

        public DateTime? Date { get; set; }

        public int? FromEmpId { get; set; }

        public int? ToEmpId { get; set; }

        public int? TableId { get; set; }

        public string Status { get; set; }

        public string Comments { get; set; }

        public string Type { get; set; }

        public int? Step { get; set; }

        public int? GroupId { get; set; }

        public int? RegionId { get; set; }

        public int? AreaId { get; set; }

        public int? TerritoryId { get; set; }

        public int? ToGroupId { get; set; }

        public int? ToRegionId { get; set; }

        public int? ToAreaId { get; set; }

        public int? ToTerritoryId { get; set; }

        public int? EntryByS { get; set; }

        public DateTime? EntryDateS { get; set; }

        public TimeSpan? EntryTimeS { get; set; }

        public int? ApproveByS { get; set; }

        public DateTime? ApproveDateS { get; set; }

        public TimeSpan? ApproveTimeS { get; set; }

        public int? EntryByApp { get; set; }

        public DateTime? EntryDateApp { get; set; }

        public TimeSpan? EntryTimeApp { get; set; }

        public int? ApproveByApp { get; set; }

        public DateTime? ApproveDateApp { get; set; }

        public TimeSpan? ApproveTimeApp { get; set; }

        public int? RoleTypeId { get; set; }

        public int? ToRoleTypeId { get; set; }
        public int? MenuId { get; set; }

    }


    public class Prescription_Applog_DAO
    {

        public int PrescriptionApprovalId { get; set; }
        public int? PrescriptionId { get; set; }
        public int? EmpInfoId { get; set; }

        public string Date { get; set; }

        public int? FromEmpId { get; set; }

        public int? ToEmpId { get; set; }

        public int? TableId { get; set; }

        public string Status { get; set; }

        public string Comments { get; set; }

        public string Type { get; set; }

        public int? Step { get; set; }

        public int? GroupId { get; set; }

        public int? RegionId { get; set; }

        public int? AreaId { get; set; }

        public int? TerritoryId { get; set; }



        public int? RoleTypeId { get; set; }

        public int? ToRoleTypeId { get; set; }

        public int? MenuId { get; set; }

        public string TerritoryName { get; set; }
        public string TerritoryCode { get; set; }
        public string AreaCode { get; set; }




        public string ApprovalStatus { get; set; }
        public string EmpMasterCode { get; set; }
        public string EmpName { get; set; }



        public int MIOEmpId { get; set; }
        public int ASMEMPId { get; set; }
        public int RSMEMPId { get; set; }
        public int NSMEMPId { get; set; }
        public PrescriptionRMasterDAO aPrescriptionRMasterDAO { get; set; }

    }

    public class PrescriptionRMasterDAO
    {
        public int PrescriptionId { get; set; }

        public string PrescriptionDate { get; set; }
        public string PrescriptionType { get; set; }
        public string DoctorName { get; set; }
        public string ChemberName { get; set; }
        public string DcrDate { get; set; }

        public int? TourTypeId { get; set; }

        public int? ChemberId { get; set; }

        public string EntryBy { get; set; }

        public DateTime? EntryDate { get; set; }

        public string UpdateBy { get; set; }

        public DateTime? UpdateDate { get; set; }

        public bool? IsApproved { get; set; }

     
        public string GroupName { get; set; }
        public string RegionName { get; set; }
        public string AreaName { get; set; }
        public string TerritoryName { get; set; }
        public string SubTerritoryName { get; set; }
        public string MarketName { get; set; }
        public string MyImg { get; set; }
        public string ImageString { get; set; }
        public string ImagePreName { get; set; }

        public int? DoctorId { get; set; }

        public int? DocTPDetailsId { get; set; }

        public int? GroupId { get; set; }

        public int? RegionId { get; set; }

        public int? AreaId { get; set; }

        public int? SubTerritoryId { get; set; }

        public int? TerritoryId { get; set; }

        public int? MarketId { get; set; }

        public bool? IsNonEffectiveReason { get; set; }

        public int? ReasonId { get; set; }

        public DateTime? EntryDate_Apps { get; set; }

        public string ApprovalStatus { get; set; }
        public List<PrescriptionDtlsDAO> aPrescriptionDtlsDAO { get; set; }

    }


    public class PrescriptionDtlsDAO
    {
       
        public string ProductName { get; set; }
        public int ProductId { get; set; }

    }
}
