-- Reports fixes (App Reports menu). Re-runnable: CREATE OR ALTER.
-- 1) Sales Summary > Customer Wise was a copy of the Order Summary SP (orders by SubmissionDate),
--    so "Sales" showed ordered values. Now counts only invoiced orders, filtered by InvoiceDate,
--    same as sp_Webapi_GetProductWiseSalesReport_L (Sales Summary > Product Wise).
-- 2) DCR Report returned rows in no particular order (oldest first in practice); now newest first.

CREATE OR ALTER PROCEDURE [dbo].[sp_Webapi_GetCustWiseSalesReport_L]
    @empId int = null,
    @Role  nvarchar(max)=null,
    @Month  int=null,
    @Year   int=null,
    @ProviderType  int=null,
    @CustomerType  int=null
AS
BEGIN
    DECLARE @params NVARCHAR(max)='  '

    IF(@Month IS NOT NULL)
        SET @params=@params+ ' AND MONTH(convert(Date, A.InvoiceDate))='''+CAST(@Month AS NVARCHAR(max))+''''
    IF(@Year IS NOT NULL )
        SET @params=@params+ ' AND year(convert(Date, A.InvoiceDate))= '''+CAST(@Year AS NVARCHAR(max))+''''
    IF(@ProviderType IS NOT NULL )
        SET @params=@params+ ' AND  D.ProgramTypeId='''+CAST(@ProviderType AS NVARCHAR(max))+''''
    IF(@CustomerType IS NOT NULL )
        SET @params=@params+ ' AND  D.CustTypeId='''+CAST(@CustomerType AS NVARCHAR(max))+''''

    DECLARE @Q NVARCHAR(MAX)
    SET @Q='SELECT ROW_NUMBER() Over (Order by D.CustomerCode) As [SN],  ISNULL(Count(D.CustomerMasterId),0) ordered_qty, D.CustomerCode+ '' : '' + D.CustomerName customer_name,
    convert(decimal(18,2),iSNULL(SUM(D.GrossValue-D.TotalDiscount),0)) as ordered_value
FROM    dbo.tblOrder D with (nolock)
inner join dbo.tblInvoice A with (nolock) on A.OrderId=D.OrderId
left join tblUser u with (nolock) on D.EntryBy=u.UserId
left join tblEmpGeneralInfo emp with (nolock) on emp.EmpInfoId=u.EmpInfoId
inner join View_Webapi_EmployeeFieldForceInfo fs on emp.EmpInfoId=fs.EmpInfoId
WHERE   IsFromApp = 1  and (fs.MIOEmpId='+convert(nvarchar(max),@empId)+' or fs.ASMEMPId='+convert(nvarchar(max),@empId)+' or fs.RSMEMPId='+convert(nvarchar(max),@empId)+' or fs.NSMEMPId='+convert(nvarchar(max),@empId)+')
  '  +@params+'
group by D.CustomerCode, D.CustomerName
'
    EXEC sys.sp_executesql @Q
END
GO

CREATE OR ALTER PROCEDURE [dbo].[sp_Webapi_Get_DcrListById]
    @empId NVARCHAR(50),
    @FromDt NVARCHAR(MAX) =NULL,
    @ToDt NVARCHAR(MAX) =NULL,
    @providertype NVARCHAR(MAX) =NULL,
    @pharmatype NVARCHAR(MAX) =NULL,
    @doctortype NVARCHAR(MAX) =NULL
AS
BEGIN
    DECLARE @userId NVARCHAR(max), @params NVARCHAR(max) =''
    SELECT @userId = UserId FROM dbo.tblUser WHERE EmpInfoId = @empId

    IF(@FromDt IS NOT NULL AND @ToDt IS NULL)
        SET @params=@params+ ' AND convert(Date, A.DcrDate)='''+CAST(CONVERT(DATE,@FromDt) AS NVARCHAR(max))+''''
    IF(@FromDt IS NOT NULL AND @ToDt IS NOT NULL)
        SET @params=@params+ ' AND convert(Date,A.DcrDate) between '''+CAST(CONVERT(DATE,@FromDt) AS NVARCHAR(max))+''' AND '''+CAST(CONVERT(DATE,@ToDt) AS NVARCHAR(max))+''' '
    IF(@providertype<>0 )
        SET @params=@params+ ' AND  convert(Int,A.DoctorProgramypeId)='''+CAST(CONVERT(Int,@providertype) AS NVARCHAR(max))+''''
    IF(@pharmatype<>0 )
        SET @params=@params+ ' AND  convert(Int,A.SmcTypeId_DCR)='''+CAST(CONVERT(Int,@pharmatype) AS NVARCHAR(max))+''''
    IF(@doctortype<>0 )
        SET @params=@params+ ' AND  convert(Int,M.Doctortypeid)='''+CAST(CONVERT(Int,@doctortype) AS NVARCHAR(max))+''''

    DECLARE @Q NVARCHAR(MAX)
    SET @Q='
        SELECT  A.DcrId ,
                FORMAT(A.DcrDate, ''MMMM dd, yyyy hh:mm tt'') AS DcrDate ,
                B.TourTypeName ,
                C.Name AS ChamberAddress ,
                D.EmpName ,
                A.IsApproved ,
                M.DoctorName AS Name, A.ApprovalStatus
        FROM    dbo.tbl_DCRInfo A
                LEFT JOIN dbo.tbl_TourPlanType B ON B.TourTypeId = A.TourTypeId
                LEFT JOIN dbo.tblDoctorChemberDetail C ON C.ChemberId = A.ChemberId
                LEFT JOIN dbo.tblUser us ON us.UserId = A.EntryBy
                LEFT JOIN dbo.tblEmpGeneralInfo D ON D.EmpInfoId = us.EmpInfoId
                LEFT JOIN dbo.tblDoctorMaster M ON M.DoctorId = A.DoctorId
        WHERE A.EntryBy='+@userId +@params + '
        ORDER BY A.DcrDate DESC, A.DcrId DESC'
    EXEC sys.sp_executesql @Q
END
GO
