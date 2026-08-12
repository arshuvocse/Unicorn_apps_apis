using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace SalesSolutionWebAPI.Models
{
    public class TourPlanInfo
    {
        public int TourPlanId { get; set; }

        //public int? TerritoryId { get; set; }

        public int? MarketId { get; set; }

        //public int? SMId { get; set; }

        

        //public int? ShiftId { get; set; }

        //public int? TourTypeId { get; set; }

        public int? TPId { get; set; }

        //public string Comment { get; set; }

        public DateTime? TourPlanDate { get; set; }

        public int? EmpInfoId { get; set; }
        public int? VisitedWithEmpInfoId { get; set; }
        public int? SerialNo { get; set; }


        public string ApprovedBy { get; set; }
        public string IsMorning { get; set; }
        public string IsEvening { get; set; }
        public string IsStartTime { get; set; }
        public string Starttime { get; set; }
        public string IsEndtime { get; set; }
        public string Endtime { get; set; }
 

        //public bool? IsMarketWise { get; set; }

        //public bool? IsApproved { get; set; }

        public string CreatedBy { get; set; }

        public DateTime? CreatedDate { get; set; }

        public string UpdateBy { get; set; }
        public DateTime? UpdateDate { get; set; }
      
        public DateTime? ApprovedDate { get; set; }

        public List<CustomerMasterDAO> aCustomerMasterList { get; set; }

    }


    public class TourPlanInfoMasterDAO
    {
        public List<TourPlanInfoNewDAO> aTourPlanInfo { get; set; }
    }

    public class TourPlanInfoNewDAO
    {
        public int TourPlanId { get; set; }

        //public int? TerritoryId { get; set; }

        public int? MarketId { get; set; }
        public int? MarketIdEnd { get; set; } 

        public int? TPId { get; set; } 

        public DateTime? TourPlanDate { get; set; }

        public int? EmpInfoId { get; set; }
        public int? VisitedWithEmpInfoId { get; set; }
        public int? SerialNo { get; set; }


        public string ApprovedBy { get; set; }
        public string IsMorning { get; set; } = "0";
        public string IsEvening { get; set; } = "0";

        public string Starttime { get; set; }
   
        public string Endtime { get; set; }

        public string IsMarketVisit { get; set; } = "0";
        public string IsOtherVisit { get; set; } = "0";
        public string CreatedBy { get; set; }
        public string Objective { get; set; }

        public DateTime? CreatedDate { get; set; }

        public string UpdateBy { get; set; }
        public DateTime? UpdateDate { get; set; }
      
        public DateTime? ApprovedDate { get; set; }

        public List<VisitedMarketDao> aVisitedMarketList { get; set; }

    }

    public class CustomerMasterDAO
    {
        public int CustomerMasterId { get; set; }
    } public class VisitedMarketDao
    {
        public int marketId { get; set; }
    }
        public class TourPlanInfoMaster
    {
        public List<TourPlanInfo> aTourPlanInfo { get; set; }
    }

     

public class TourPlanViewModel
    {
        public int TourPlanId { get; set; } = 0;
        public int TPMaster { get; set; } = 0;
        public int TourTypeId { get; set; } = 0;
        public int DocTPDetailsId { get; set; } = 0;
        public string Comment { get; set; } = "";
        public string Objective { get; set; } = "";
        public bool IsMarketWise { get; set; } = false;
        public bool IsApproved { get; set; } = false;
        public int SMId { get; set; } = 0;
        public int GroupId { get; set; } = 0;
        public int RegionId { get; set; } = 0;
        public int AreaId { get; set; } = 0;
        public int TerritoryId { get; set; } = 0;
        public int SubTerritoryId { get; set; } = 0;
        public int MarketId { get; set; } = 0;
        public int MarketIdEnd { get; set; } = 0;
        public string GroupName { get; set; } = "";
        public string RegionName { get; set; } = "";
        public string AreaName { get; set; } = "";
        public string TerritoryName { get; set; } = "";
        public string SubTerritoryName { get; set; } = "";
        public string MarketName { get; set; } = "";
        public string MarketNameEnd { get; set; } = "";
        public int CustomerMasterId { get; set; } = 0;
        public string CustomerName { get; set; } = "";
        public string DoctorName { get; set; } = "";
        public string ShiftText { get; set; } = "";
        public string TourTypeName { get; set; } = "";
        public int TPId { get; set; } = 0;
        public string TPName { get; set; } = "";
        public int MonthValue { get; set; } = 0;
        public int YearValue { get; set; } = 0;
        public int DayValue { get; set; } = 0;
        public int EmpInfoId { get; set; }
        public string TourPlanDate { get; set; }
        public bool IsFinalSubmit { get; set; }
        public int SerialNo { get; set; } = 0;
        public List<Customer> aCustomerMasterList { get; set; }
        public string CreatedBy { get; set; } = "";
        public string CreatedDate { get; set; } = "";
        public string UpdateBy { get; set; } = "";
        public string UpdateDate { get; set; } = "";
        public string ApprovedBy { get; set; } = "";
        public string ApprovedDate { get; set; } = "";
        public string details { get; set; } = "";
        public string ApprovalStatus { get; set; } = "";
        public string IsMorning { get; set; } = "0";
        public string IsEvening { get; set; } = "0";
        public string IsStartTime { get; set; } = "";
        public string Starttime { get; set; } = "";
        public string IsEndtime { get; set; } = "0";
        public string Endtime { get; set; } = "";
        public string IsMarketVisit { get; set; } = "0";
        public string IsOtherVisit { get; set; } = "0";
        public string VisitedWithEmpName { get; set; } = "";
        public int VisitedWithEmpInfoId { get; set; } = 0;
        public List<Market> aVisitedMarketList { get; set; }
    }

    public class Customer
    {

        public int CustomerMasterId;
       
        public string CustomerName;
        public string CustomerCode;
    }

    public class Market
    {
        public int MarketId;
        public string MarketName;
    }


    public class TourPlanInfo_Applog_DAO
    {

        public int TPMaster { get; set; }
        public int? MonthValue { get; set; }
        public int? YearValue { get; set; }
        public int? TourPlanApprovalId { get; set; }
         
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
        public TourPlanMasterDAO aTourPlanMaster { get; set; }

    }


    public class DWSP_Applog_DAO
    {

        public int DWSPMasterId { get; set; }
        public int? MonthValue { get; set; }
        public int? YearValue { get; set; }
        public int? DWSPApprovalId { get; set; }

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
        public DWSPMasterDAO aDWSPMasterDAO { get; set; }

    }

    public class DoctorVisitPlan_Applog_DAO
    {

        public int DocTPMaster { get; set; }
        public int? MonthValue { get; set; }
        public int? YearValue { get; set; }
        public int? VisitPlanApprovalId { get; set; }

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
        public DoctorVisitPlanMasterDAO aDoctorVisitPlanMaster { get; set; }

    }
    public class TourPlanMasterDAO
    {
        public int? TPMaster { get; set; }
        public int EmpInfoId { get; set; }
        public string MonthValue { get; set; }
        public string YearValue { get; set; }
        public string MonthName { get; set; }
     
       
        public string ApprovalStatus { get; set; }
        public string ExHQ { get; set; }
        public string  HQ { get; set; }
        public string OS { get; set; }
        public bool IsFinalSubmit { get; set; }
        public List<TourPlanDtlsDAODate> aTourPlanDate { get; set; }
     
        

    }


  

    public class DoctorVisitPlanMasterDAO
    {
        public int? DocTPMaster { get; set; }
        public int EmpInfoId { get; set; }
        public string MonthValue { get; set; }
        public string YearValue { get; set; }
        public string MonthName { get; set; }


        public string ApprovalStatus { get; set; }
        
        
        public bool IsFinalSubmit { get; set; }

        public List<dcvisDtlsDAODate> aDoctorVisitPlanDetail{ get; set; }


    }


    public class TourPlanInfoMasterDao
    {
        public int? TourPlanId { get; set; } = 0;
        public int? TPMaster { get; set; } = 0;
        public int? GroupId { get; set; } = 0;
        public int? RegionId { get; set; } = 0;

        public int? AreaId { get; set; } = 0;
        public int? TerritoryId { get; set; } = 0;
        public int? SubTerritoryId { get; set; } = 0;
        public int? MarketId { get; set; } = 0;
        public int? TourTypeId { get; set; } = 0;
        public int? TPId { get; set; } = 0;
        public int? EmpInfoId { get; set; } = 0;
        public string Comment { get; set; } = "";
        public bool IsMarketWise { get; set; } = false;
        public bool IsApproved { get; set; } = false;
        public string GroupName { get; set; } = "";
        public string RegionName { get; set; } = "";
        public string AreaName { get; set; } = "";
        public string TerritoryName { get; set; } = "";
        public string SubTerritoryName { get; set; } = "";
        public string MarketName { get; set; } = "";
        public string CustomerName { get; set; } = "";
        public string ShiftText { get; set; } = "";
        public string TourTypeName { get; set; } = "";
        public string TPName { get; set; } = "";
        public string MonthValue { get; set; } = "";
        public string YearValue { get; set; } = "";
        public string DayValue { get; set; } = "";
        public string TourPlanDate { get; set; } = "";

        public string IsMorning { get; set; } = "0";
        public string IsEvening { get; set; } = "0";
        public string IsStartTime { get; set; } = "0";
        public string Starttime { get; set; } = "0";
        public string IsEndtime { get; set; } = "0";
        public string Endtime { get; set; } = "";

        public string SerialNo { get; set; } = "0";
        public string ApprovalStatus { get; set; } = "";
        public string VisitedWithEmpInfoId { get; set; } = "";
        public string VisitedWithEmpName { get; set; } = "";
        public string IsFinalSubmit { get; set; } = "0";
        public List<CustomerDAO> aCustomerMasterList { get; set; }

    }

    public class tpStatusDAO
    {
        public int? TPMaster { get; set; }
        public string MonthValue { get; set; }
        public string YearValue { get; set; }
        public int EmpInfoId { get; set; }
        public string IsFinalSubmit { get; set; }
        public string ApprovalStatus { get; set; }
        public int ApprovedBy { get; set; }
        public string ApprovedDate { get; set; }
        public string FinalSubmitRemarks { get; set; }
        public string ApprovalRemarks { get; set; }
        
        public List<TourTypeDAO> aTourTypeDAOList { get; set; }

    }


    public class TourTypeDAO
    {
       
        public string TourTypeName { get; set; }
        public string Balance { get; set; }

    }


    public class CustomerDAO
    {
        public int CustomerMasterId { get; set; } = 0;
        public string CustomerCode { get; set; } = "";
        public string CustomerName { get; set; } = "";

    }

    public class SaveTourPlan_Applog_DAO
    {
        public int? TourPlanApprovalId { get; set; }

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

    public class SaveDWSP_Applog_DAO
    {
        public int? DWSPApprovalId { get; set; }

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
    public class SaveDoctorVisit_Applog_DAO
    {
        public int? VisitPlanApprovalId { get; set; }

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
    public class TourPlanDtlsDAO
    {
        public int CustomerMasterId { get; set; }
        public int MarketId { get; set; }
      
        public string TourPlanDate { get; set; }
        public string CustomerName { get; set; }
        public string MarketName { get; set; }
        public string MarketNameEnd { get; set; }
        public string OtherMarketName { get; set; }
        public string VisitType { get; set; }
        public int TPId { get; set; }
        public string TPName { get; set; }

       
    }


    public class DWSPDtlsDAO
    {
 
      

        public string FCBAmount { get; set; }
        public string GeneralAmount { get; set; }
        public string CampaignAmount { get; set; }
      


    }
    public class TourPlanDtlsDAODate
    {
        
        public string DayValue { get; set; }
        public string DayName { get; set; }

        public List<TourPlanDtlsDAO> aTourPlanDtlsDAO { get; set; }

    }

    public class DWSPDtlsDAODate
    {

        public string DayValue { get; set; }
        public string DayName { get; set; }

        public string FCBAmount { get; set; }
        public string GeneralAmount { get; set; }
        public string CampaignAmount { get; set; }

    }
    public class VisitPlanDtlsDAO
    { 
         
        public string DoctorName { get; set; }
         


    }
    public class dcvisDtlsDAODate
    {
        public string DayValue { get; set; }
        public string DayName { get; set; }
        public string VisitPlanDate { get; set; }
        public List<VisitPlanDtlsDAO> aVisitDtlsDAO { get; set; }
         

    }
    public class DoctrVisitDtlsDAODate
    {

        public string DayValue { get; set; }
        public string DayName { get; set; }

        public List<TourPlanDtlsDAODate> aVisitPlanDtlsDAO { get; set; }

    }
    public class MessageDao
    {
        public string isSuccess { get; set; }

        public List<TourPlanInfoMasterDao> aTourPlanInfo { get; set; }
    }

    public class DayWiseTourPlanDTO
    {
        public int EmpInfoId { get; set; }
        public string TourPlanDate { get; set; }
        public bool IsFinalSubmit { get; set; } = true;
        public List<int> CustomerIds { get; set; } = new List<int>();
        public List<int> DoctorIds { get; set; } = new List<int>();
    }
}