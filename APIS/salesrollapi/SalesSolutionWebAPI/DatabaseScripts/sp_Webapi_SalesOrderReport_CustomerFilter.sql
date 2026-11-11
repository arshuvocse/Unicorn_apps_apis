USE [SalesDisDB_Unicorn_DB]
GO
/*
  Sales Summary / Order Summary reports (app Reports menu), Customer-wise and Product-wise tabs.
  - New optional @CustomerId (default NULL = all customers) on all four SPs: filters tblOrder.CustomerMasterId.
  - Product-wise SPs: the employee filter now matches @empId in any hierarchy column (same as the
    customer-wise SPs) instead of the caller's @Role, so a manager can pick one subordinate from the
    Sales Person dropdown. @Role is kept for compatibility but no longer used.
  Re-runnable (ALTER).
*/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
ALTER PROCEDURE [dbo].[sp_Webapi_GetCustWiseOrderReport_L]
    @empId int = null,
    @Role  nvarchar(max)=null,
    @Month  int=null,
    @Year   int=null,
    @ProviderType  int=null,
    @CustomerType  int=null,
    @CustomerId int=null
AS
BEGIN
    DECLARE @params NVARCHAR(max)='  '
    IF(@Month IS NOT NULL)
        SET @params=@params+ ' AND MONTH(convert(Date,  D.SubmissionDate))='''+CAST(@Month AS NVARCHAR(max))+''''
    IF(@Year IS NOT NULL )
        SET @params=@params+ ' AND   year(convert(Date,  D.SubmissionDate))= '''+CAST(@Year AS NVARCHAR(max))+''''
    IF(@ProviderType IS NOT NULL )
        SET @params=@params+ ' AND  D.ProgramTypeId='''+CAST(@ProviderType AS NVARCHAR(max))+''''
    IF(@CustomerType IS NOT NULL )
        SET @params=@params+ ' AND  D.CustTypeId='''+CAST(@CustomerType AS NVARCHAR(max))+''''
    IF(ISNULL(@CustomerId,0) > 0)
        SET @params=@params+ ' AND  D.CustomerMasterId='+CAST(@CustomerId AS NVARCHAR(max))
    DECLARE @Q NVARCHAR(MAX)
    SET @Q='SELECT ROW_NUMBER() Over (Order by D.CustomerCode) As [SN],  ISNULL(Count(D.CustomerMasterId),0) ordered_qty, D.CustomerCode+ '' : '' + D.CustomerName customer_name,
    convert(decimal(18,2),iSNULL(SUM(D.GrossValue-D.TotalDiscount),0)) as ordered_value
FROM    dbo.tblOrder D with (nolock)
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

ALTER PROCEDURE [dbo].[sp_Webapi_GetCustWiseSalesReport_L]
    @empId int = null,
    @Role  nvarchar(max)=null,
    @Month  int=null,
    @Year   int=null,
    @ProviderType  int=null,
    @CustomerType  int=null,
    @CustomerId int=null
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
    IF(ISNULL(@CustomerId,0) > 0)
        SET @params=@params+ ' AND  D.CustomerMasterId='+CAST(@CustomerId AS NVARCHAR(max))
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

ALTER PROCEDURE [dbo].[sp_Webapi_GetProductWiseOrderReport_L]
    @empId int = null,
    @Role  nvarchar(max)=null,
    @Month  int=null,
    @Year   int=null,
    @ProviderType  int=null,
    @CustomerType  int=null,
    @CustomerId int=null
AS
BEGIN
    DECLARE @params NVARCHAR(max)='  '
    IF(@Month IS NOT NULL)
        SET @params=@params+ ' AND MONTH(convert(Date,  D.SubmissionDate))='''+CAST(@Month AS NVARCHAR(max))+''''
    IF(@Year IS NOT NULL )
        SET @params=@params+ ' AND   year(convert(Date,  D.SubmissionDate))= '''+CAST(@Year AS NVARCHAR(max))+''''
    IF(@ProviderType IS NOT NULL )
        SET @params=@params+ ' AND  D.ProgramTypeId='''+CAST(@ProviderType AS NVARCHAR(max))+''''
    IF(@CustomerType IS NOT NULL )
        SET @params=@params+ ' AND  D.CustTypeId='''+CAST(@CustomerType AS NVARCHAR(max))+''''
    IF(ISNULL(@CustomerId,0) > 0)
        SET @params=@params+ ' AND  D.CustomerMasterId='+CAST(@CustomerId AS NVARCHAR(max))
    DECLARE @Q NVARCHAR(MAX)
    SET @Q='select  ROW_NUMBER() Over (Order by pro.ProductCode) As [SN], order_times,  pro.ProductCode+'' : ''+pro.ProductName product_name,   (tblDtl.Quantity) ordered_qty, ordered_value from tblProduct pro
inner join (select ISNULL(count(D.OrderId),0) order_times, convert(decimal(18,2),iSNULL(SUM(dtl.TotalTradePrice-dtl.DiscountAmount),0)) as ordered_value,  SUM(dtl.Quantity) Quantity, dtl.ProductId from tblOrderDetail dtl
inner join  dbo.tblOrder D  with (nolock) on dtl.OrderId=D.OrderId
left join tblUser u with (nolock) on D.EntryBy=u.UserId
left join tblEmpGeneralInfo emp with (nolock) on emp.EmpInfoId=u.EmpInfoId
inner join View_Webapi_EmployeeFieldForceInfo fs on emp.EmpInfoId=fs.EmpInfoId
WHERE  IsFromApp = 1  and (fs.MIOEmpId='+convert(nvarchar(max),@empId)+' or fs.ASMEMPId='+convert(nvarchar(max),@empId)+' or fs.RSMEMPId='+convert(nvarchar(max),@empId)+' or fs.NSMEMPId='+convert(nvarchar(max),@empId)+')
  '  +@params+'
 group by  ProductId) tblDtl on tblDtl.ProductId=pro.ProductId
'
    EXEC sys.sp_executesql @Q
END
GO

ALTER PROCEDURE [dbo].[sp_Webapi_GetProductWiseSalesReport_L]
    @empId int = null,
    @Role  nvarchar(max)=null,
    @Month  int=null,
    @Year   int=null,
    @ProviderType  int=null,
    @CustomerType  int=null,
    @CustomerId int=null
AS
BEGIN
    DECLARE @params NVARCHAR(max)='  '
    IF(@Month IS NOT NULL)
        SET @params=@params+ ' AND MONTH(convert(Date,A.InvoiceDate))='''+CAST(@Month AS NVARCHAR(max))+''''
    IF(@Year IS NOT NULL )
        SET @params=@params+ ' AND   year(convert(Date,A.InvoiceDate))= '''+CAST(@Year AS NVARCHAR(max))+''''
    IF(@ProviderType IS NOT NULL )
        SET @params=@params+ ' AND  D.ProgramTypeId='''+CAST(@ProviderType AS NVARCHAR(max))+''''
    IF(@CustomerType IS NOT NULL )
        SET @params=@params+ ' AND  D.CustTypeId='''+CAST(@CustomerType AS NVARCHAR(max))+''''
    IF(ISNULL(@CustomerId,0) > 0)
        SET @params=@params+ ' AND  D.CustomerMasterId='+CAST(@CustomerId AS NVARCHAR(max))
    DECLARE @Q NVARCHAR(MAX)
    SET @Q='select  ROW_NUMBER() Over (Order by pro.ProductCode) As [SN], order_times,  pro.ProductCode+'' : ''+pro.ProductName product_name,   (tblDtl.Quantity) ordered_qty, ordered_value from tblProduct pro
inner join (select ISNULL(count(D.OrderId),0) order_times,  convert(decimal(18,2),iSNULL(SUM(dtl.TotalTradePrice-dtl.DiscountAmount),0)) as ordered_value,  SUM(dtl.Quantity) Quantity, dtl.ProductId from tblOrderDetail dtl
inner join  dbo.tblOrder D  with (nolock) on dtl.OrderId=D.OrderId
inner join  dbo.tblInvoice A  with (nolock) on A.OrderId=D.OrderId
left join tblUser u with (nolock) on D.EntryBy=u.UserId
left join tblEmpGeneralInfo emp with (nolock) on emp.EmpInfoId=u.EmpInfoId
inner join View_Webapi_EmployeeFieldForceInfo fs on emp.EmpInfoId=fs.EmpInfoId
WHERE  IsFromApp = 1  and (fs.MIOEmpId='+convert(nvarchar(max),@empId)+' or fs.ASMEMPId='+convert(nvarchar(max),@empId)+' or fs.RSMEMPId='+convert(nvarchar(max),@empId)+' or fs.NSMEMPId='+convert(nvarchar(max),@empId)+')
  '  +@params+'
 group by  ProductId) tblDtl on tblDtl.ProductId=pro.ProductId
'
    EXEC sys.sp_executesql @Q
END
GO
