using DataLayer.DataManager;
using DataLayer.Models;
using DataLayer.Repository.Interface;
using DataLayer.ResponseModel;
using DataLayer.ViewModels;
using System;
using System.Collections.Generic;
using System.Data;
using System.Data.SqlClient;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DataLayer.Repository.Implementation
{
    public class OrderProcessRepository : IOrderProcess
    {
        private DataAccessManager accessManager = new DataAccessManager();
        public ResultInfo MakeOrder(OrderMaster aOrder)
        {
            ResultInfo aInformation = new ResultInfo();
            string operationStage = "campaign processing";
            try
            {
                CampaignRepository aCampaignRepository = new CampaignRepository();
                List<OrderDetails> aOrderDetailses = aCampaignRepository.GetOrderProductWiseCampaign(aOrder);

                if (aOrderDetailses == null)
                {
                    aInformation.isSuccess = false;
                    aInformation.errormessagenew = "Duplicate Campaign Exist";
                    return aInformation;
                }

                string inactiveProductName = string.Empty;
                bool hasInactiveProduct = false;
                operationStage = "product validation";

                // Open SQL Connection once for the entire order process to avoid N+1 connection overhead
                accessManager.SqlConnectionOpen(DataBase.SalesDB);

                foreach (OrderDetails item in aOrderDetailses)
                {
                    int productGroupId = 0;
                    string activeStatus = string.Empty;
                    DataTable dtallProduct = null;

                    try
                    {
                        List<SqlParameter> p1 = new List<SqlParameter> { new SqlParameter("@ProductId", (object)item.ProductId ?? DBNull.Value) };
                        dtallProduct = accessManager.GetDataTable("sp_Webapi_Get_ProductGiftInactivePro", p1);
                        if (dtallProduct != null && dtallProduct.Rows.Count > 0)
                        {
                            DataRow row = dtallProduct.Rows[0];
                            productGroupId = Convert.ToInt32(row["ProductGroupId"]);
                            activeStatus = row["AtiveStatus"].ToString();

                            if (activeStatus.Equals("Inactive", StringComparison.OrdinalIgnoreCase))
                            {
                                hasInactiveProduct = true;
                                inactiveProductName = row["ProductName"].ToString();
                            }
                        }
                    }
                    catch
                    {
                        // Safely ignore lookup exceptions per legacy behavior
                    }

                    if (hasInactiveProduct) break;

                    if (productGroupId == 1)
                    {
                        List<SqlParameter> p2 = new List<SqlParameter> { 
                            new SqlParameter("@ProductId", (object)item.ProductId ?? DBNull.Value),
                            new SqlParameter("@empId", (object)aOrder.EmpId ?? DBNull.Value)
                        };
                        DataTable dtProduct = accessManager.GetDataTable("sp_Webapi_Check_ProductActiveorNotEmpId", p2);
                        if (dtProduct == null || dtProduct.Rows.Count == 0)
                        {
                            hasInactiveProduct = true;
                            inactiveProductName = (dtallProduct != null && dtallProduct.Rows.Count > 0) ? dtallProduct.Rows[0]["ProductName"].ToString() : "Unknown Product";
                        }
                    }
                    else
                    {
                        List<SqlParameter> p3 = new List<SqlParameter> { 
                            new SqlParameter("@ProductId", (object)item.ProductId ?? DBNull.Value),
                            new SqlParameter("@empId", (object)aOrder.EmpId ?? DBNull.Value)
                        };
                        DataTable dtProduct = accessManager.GetDataTable("sp_Webapi_Check_ProductGiftActiveorNotEmpId", p3);
                        if (dtProduct == null || dtProduct.Rows.Count == 0)
                        {
                            hasInactiveProduct = true;
                            inactiveProductName = (dtallProduct != null && dtallProduct.Rows.Count > 0) ? dtallProduct.Rows[0]["ProductName"].ToString() : "Unknown Product";
                        }
                    }

                    if (hasInactiveProduct) break;
                }

                if (hasInactiveProduct)
                {
                    accessManager.SqlConnectionClose();
                    aInformation.isSuccess = false;
                    aInformation.errormessagenew = $"{inactiveProductName} is an inactive product!";
                    return aInformation;
                }

                List<SqlParameter> gSqlParameterList = new List<SqlParameter>
                {
                    new SqlParameter("@empId", aOrder.EmpId),
                    new SqlParameter("@comUnitId", aOrder.ComunitId),
                    new SqlParameter("@CustomerCode", aOrder.CustomerCode),
                    new SqlParameter("@SubmittedDate", DBNull.Value),
                    new SqlParameter("@CollectionDate", (object)aOrder.CollectionDate ?? DBNull.Value),
                    new SqlParameter("@EntryDate", DateTime.Now),
                    new SqlParameter("@Remarks", (object)aOrder.Remarks ?? DBNull.Value),
                    new SqlParameter("@orderType", (object)aOrder.OrderType ?? DBNull.Value),
                    new SqlParameter("@tpDiscount", aOrder.TpPercentage),
                    new SqlParameter("@PaymentType", (object)aOrder.PaymentType ?? DBNull.Value),
                    new SqlParameter("@PaymentDate", (object)aOrder.PaymentDate ?? DBNull.Value),
                    new SqlParameter("@DeliveryDate", (object)aOrder.DeliveryDate ?? DBNull.Value)
                };

                int pk = 0;
                if (aOrder.OrderId > 0)
                {
                    operationStage = $"existing order update (OrderId: {aOrder.OrderId})";
                    List<SqlParameter> delParams = new List<SqlParameter> { new SqlParameter("@OrderId", aOrder.OrderId.ToString()) };
                    accessManager.SaveDataReturnPrimaryKey("sp_webapi_DeleteOrderInfo", delParams);
                    pk = aOrder.OrderId;
                }
                else
                {
                    operationStage = "order master save";
                    // Pass IsBigData = true to prevent timeouts during large saves
                    pk = accessManager.SaveDataReturnPrimaryKey("sp_webapi_SaveOrderMaster", gSqlParameterList, true);
                }

                if (pk > 0)
                {
                    aInformation.isSuccess = true;

                    foreach (OrderDetails item in aOrderDetailses)
                    {
                        operationStage = $"order detail save (ProductId: {item.ProductId})";
                        List<SqlParameter> aSqlParameterList = new List<SqlParameter>
                        {
                            new SqlParameter("@orderid", pk),
                            new SqlParameter("@productid", item.ProductId),
                            new SqlParameter("@quantity", item.Quantity),
                            new SqlParameter("@unitprice", item.UnitPrice),
                            new SqlParameter("@unitvatPercentage", item.VatPercentage),
                            new SqlParameter("@IsGiftProduct", item.IsGiftProduct),
                            new SqlParameter("@IsCampaignProduct", item.IsCampaignProduct),
                            new SqlParameter("@DiscountPercentage", item.DiscountPercentage),
                            new SqlParameter("@CampaingName", (object)item.CampaingName ?? DBNull.Value),
                            new SqlParameter("@CampaignType", (object)item.CampaignType ?? DBNull.Value),
                            new SqlParameter("@DiscountValue", item.DiscountValue)
                        };

                        // Pass IsBigData = true to prevent timeouts during large saves
                        accessManager.SaveData("sp_webapi_SaveOrderDetail", aSqlParameterList, true);
                    }
                }
            }
            catch (Exception ex)
            {
                accessManager.SqlConnectionClose(true);
                aInformation.isSuccess = false;
                aInformation.isError = true;
                aInformation.ErrorMessage = ex.ToString();
                aInformation.errormessagenew = $"{operationStage} failed: {ex.GetBaseException().Message}";
            }
            finally
            {
                accessManager.SqlConnectionClose();
            }


            return aInformation;
        }

        private   (bool isValid, string message) ValidateForGhorSajaiType(OrderMaster aOrder, int custtypeid)
        {
            // Check if the customer type ID is 18 or 19 and payment type is "NCOD"
            if ((custtypeid == 18 || custtypeid == 19|| custtypeid == 20 || custtypeid == 21|| custtypeid == 24 || custtypeid == 26 ) && aOrder.PaymentType == "NCOD")
            {
                return (false, "Must be Select COD for this Customer type.");
            }

            if (custtypeid == 33 && aOrder.PaymentType != "NCOD")
            {
                return (false, "Must select NCOD for this customer type.");
            }

            //if ((custtypeid == 25) )
            //{
            //    return (false, "Must be Select COD for this Customer type.");
            //}

            // Additional check for customer type ID 19
            if (custtypeid == 19)
            {
                foreach (OrderDetails item in aOrder.OrderDetails)
                {
                    DataTable dtPro = CheckGhorShajai2RestrictProductsSubmitOrder(item.ProductId);
                    if (dtPro.Rows.Count>0) // Example condition for ProductId
                    {
                        return (false, "This Customer type is not allowed for Product: " + item.ProductName);
                    }
                }
            }
            if (custtypeid == 20)
            {
                foreach (OrderDetails item in aOrder.OrderDetails)
                {
                    DataTable dtPro = CheckGhorShajai3RestrictProductsSubmitOrder(item.ProductId);
                    if (dtPro.Rows.Count>0) // Example condition for ProductId
                    {
                        return (false, "This Customer type is not allowed for Product: " + item.ProductName);
                    }
                }
            }
            
            if (custtypeid == 21)
            {
                foreach (OrderDetails item in aOrder.OrderDetails)
                {
                    DataTable dtPro = CheckGhorShajai3RestrictProductsSubmitOrder(item.ProductId);
                    if (dtPro.Rows.Count>0) // Example condition for ProductId
                    {
                        return (false, "This Customer type is not allowed for Product: " + item.ProductName);
                    }
                }
            }
      //      if (custtypeid == 22)
            {
                foreach (OrderDetails item in aOrder.OrderDetails)
                {
                    DataTable dtPro = CheckRestrictProductsWithTypeSubmitOrder(item.ProductId, custtypeid);
                    if (dtPro.Rows.Count>0) // Example condition for ProductId
                    {
                        return (false, "This Customer type is not allowed for Product: " + item.ProductName);
                    }
                }
            }

            // Return true with a success message for all other cases
            return (true, "Validation passed successfully.");
        }


        public string Splitcode(string dateString)
        {
            string returnstring = "";

            string[] dateParts = dateString.Split('-');

            if (dateParts[1].Length > 0 && dateParts[1].Length > 3)
            {
                returnstring = dateParts[0] + '-' + dateParts[1].Substring(0, dateParts[1].Length - 1) + '-' + dateParts[2];
            }
            else
            {
                returnstring = dateString;
            }
            return returnstring;
        }
        public DataTable GetCampaignCheck(int customerId, string param)
        {
            accessManager.SqlConnectionOpen(DataBase.SalesDB);
            List<SqlParameter> aSqlParameterList = new List<SqlParameter>();
            aSqlParameterList.Add(new SqlParameter("@CustomerId", (object)customerId ?? DBNull.Value));
            aSqlParameterList.Add(new SqlParameter("@param", (object)param ?? DBNull.Value));


            DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_CampaignMasterCheck", aSqlParameterList);

            accessManager.SqlConnectionClose();


            return dt;
        }


        public DataTable GetCampaignNCOD(int customerId, string param)
        {
            accessManager.SqlConnectionOpen(DataBase.SalesDB);
            List<SqlParameter> aSqlParameterList = new List<SqlParameter>();
            aSqlParameterList.Add(new SqlParameter("@CustomerId", (object)customerId ?? DBNull.Value));
            aSqlParameterList.Add(new SqlParameter("@param", (object)param ?? DBNull.Value));


            DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_CampaignNCOD", aSqlParameterList);

            accessManager.SqlConnectionClose();


            return dt;
        }

        public bool DeleteOrder(string id)
        {
            List<SqlParameter> aSqlParameterList = new List<SqlParameter>();

            aSqlParameterList.Add(new SqlParameter("@OrderId", id));
            return accessManager.DeleteData("sp_Webapi_Delete_OrderDetail", aSqlParameterList, true);

        }
        public DataTable GetProductWiseCampaign(int productid, string param)
        {
            accessManager.SqlConnectionOpen(DataBase.SalesDB);
            List<SqlParameter> aSqlParameterList = new List<SqlParameter>();

            aSqlParameterList.Add(new SqlParameter("@OrderProductId", (object)productid ?? DBNull.Value));
            aSqlParameterList.Add(new SqlParameter("@param", (object)param ?? DBNull.Value));


            DataTable dt = accessManager.GetDataTable("sp_Webapi_GetCampaignData", aSqlParameterList);

            accessManager.SqlConnectionClose();


            return dt;
        }

        public DataTable GetCampaignTradePolicyProductPerc(decimal amount,int custid,int typeid)
        {
            accessManager.SqlConnectionOpen(DataBase.SalesDB);
            List<SqlParameter> aSqlParameterList = new List<SqlParameter>();
            aSqlParameterList.Add(new SqlParameter("@RemTotalAmount", (object)amount ?? DBNull.Value));
            aSqlParameterList.Add(new SqlParameter("@customerId", (object)custid ?? DBNull.Value));
            aSqlParameterList.Add(new SqlParameter("@cuttypeId", (object)typeid ?? DBNull.Value));



            DataTable dt = accessManager.GetDataTable("sp_Webapi_GetCampaignTradePolicyPerc", aSqlParameterList);

            accessManager.SqlConnectionClose();


            return dt;
        }
        public DataTable GetPrice(int? id)
        {
            accessManager.SqlConnectionOpen(DataBase.SalesDB);
            List<SqlParameter> aSqlParameterList = new List<SqlParameter>();
            aSqlParameterList.Add(new SqlParameter("@ProductId", (object)id ?? DBNull.Value));



            DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_ProductPrice", aSqlParameterList);

            accessManager.SqlConnectionClose();


            return dt;
        }

        public DataTable CheckProductInActive(int? id)
        {
            accessManager.SqlConnectionOpen(DataBase.SalesDB);
            List<SqlParameter> aSqlParameterList = new List<SqlParameter>();
            aSqlParameterList.Add(new SqlParameter("@ProductId", (object)id ?? DBNull.Value));



            DataTable dt = accessManager.GetDataTable("sp_Webapi_Check_ProductActiveorGift", aSqlParameterList);

            accessManager.SqlConnectionClose();


            return dt;
        }

        

        public DataTable CheckWhocanSubmitOrder(int? EmpId, string CustomerCode)
        {
            accessManager.SqlConnectionOpen(DataBase.SalesDB);
            List<SqlParameter> aSqlParameterList = new List<SqlParameter>();
            aSqlParameterList.Add(new SqlParameter("@EmpId", (object)EmpId ?? DBNull.Value));
            aSqlParameterList.Add(new SqlParameter("@CustomerCode", (object)CustomerCode ?? DBNull.Value));



            DataTable dt = accessManager.GetDataTable("sp_Webapi_CheckWhocanSubmitOrder", aSqlParameterList);

            accessManager.SqlConnectionClose();


            return dt;
        }
           public   DataTable CheckGhorShajai2RestrictProductsSubmitOrder(int  ProductID )
        {
            accessManager.SqlConnectionOpen(DataBase.SalesDB);
            List<SqlParameter> aSqlParameterList = new List<SqlParameter>();
            aSqlParameterList.Add(new SqlParameter("@ProductID", (object)ProductID ?? DBNull.Value)); 



            DataTable dt = accessManager.GetDataTable("sp_Webapi_CheckGhorShajai2RestrictProducts", aSqlParameterList);

            accessManager.SqlConnectionClose();


            return dt;
        }

         public   DataTable CheckGhorShajai3RestrictProductsSubmitOrder(int  ProductID )
        {
            accessManager.SqlConnectionOpen(DataBase.SalesDB);
            List<SqlParameter> aSqlParameterList = new List<SqlParameter>();
            aSqlParameterList.Add(new SqlParameter("@ProductID", (object)ProductID ?? DBNull.Value)); 



            DataTable dt = accessManager.GetDataTable("sp_Webapi_CheckGhorShajai3RestrictProducts", aSqlParameterList);

            accessManager.SqlConnectionClose();


            return dt;
        }

        
         public   DataTable CheckRestrictProductsWithTypeSubmitOrder(int  ProductID, int custtypeid)
        {
            accessManager.SqlConnectionOpen(DataBase.SalesDB);
            List<SqlParameter> aSqlParameterList = new List<SqlParameter>();
            aSqlParameterList.Add(new SqlParameter("@ProductID", (object)ProductID ?? DBNull.Value)); 
            aSqlParameterList.Add(new SqlParameter("@custtypeid", (object)custtypeid ?? DBNull.Value)); 



            DataTable dt = accessManager.GetDataTable("sp_Webapi_CheckGRestrictProductsByCustYpeID", aSqlParameterList);

            accessManager.SqlConnectionClose();


            return dt;
        }


        public DataTable CheckProductInActive_New(int? id, int EmpId)
        {
            accessManager.SqlConnectionOpen(DataBase.SalesDB);
            List<SqlParameter> aSqlParameterList = new List<SqlParameter>();
            aSqlParameterList.Add(new SqlParameter("@ProductId", (object)id ?? DBNull.Value));
            aSqlParameterList.Add(new SqlParameter("@empId", (object)EmpId ?? DBNull.Value));



            DataTable dt = accessManager.GetDataTable("sp_Webapi_Check_ProductActiveorNotEmpId", aSqlParameterList);

            accessManager.SqlConnectionClose();


            return dt;
        }

        
        public DataTable CheckGiftProductInActive_New(int? id, int EmpId)
        {
            accessManager.SqlConnectionOpen(DataBase.SalesDB);
            List<SqlParameter> aSqlParameterList = new List<SqlParameter>();
            aSqlParameterList.Add(new SqlParameter("@ProductId", (object)id ?? DBNull.Value));
            aSqlParameterList.Add(new SqlParameter("@empId", (object)EmpId ?? DBNull.Value));



            DataTable dt = accessManager.GetDataTable("sp_Webapi_Check_ProductGiftActiveorNotEmpId", aSqlParameterList);

            accessManager.SqlConnectionClose();


            return dt;
        }



        public DataTable  ProductInActive_New(int? id )
        {
            accessManager.SqlConnectionOpen(DataBase.SalesDB);
            List<SqlParameter> aSqlParameterList = new List<SqlParameter>();
            aSqlParameterList.Add(new SqlParameter("@ProductId", (object)id ?? DBNull.Value));
           



            DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_ProductInactiveName", aSqlParameterList);

            accessManager.SqlConnectionClose();


            return dt;
        }


        public DataTable  ProductInActive_NewGiftProduct(int? id )
        {
            accessManager.SqlConnectionOpen(DataBase.SalesDB);
            List<SqlParameter> aSqlParameterList = new List<SqlParameter>();
            aSqlParameterList.Add(new SqlParameter("@ProductId", (object)id ?? DBNull.Value));
           



            DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_ProductGiftInactivePro", aSqlParameterList);

            accessManager.SqlConnectionClose();


            return dt;
        }
        public DataTable GetCustomerInfo(int? id)
        {
            accessManager.SqlConnectionOpen(DataBase.SalesDB);
            List<SqlParameter> aSqlParameterList = new List<SqlParameter>();
            aSqlParameterList.Add(new SqlParameter("@CustomerMasterId", (object)id ?? DBNull.Value));



            DataTable dt = accessManager.GetDataTable("sp_Webapi_Get_CustomerInfos", aSqlParameterList);

            accessManager.SqlConnectionClose();


            return dt;
        }
        public DataTable GetCampaignListCheck(int? id, int? CustomerMasterId)
        {
            accessManager.SqlConnectionOpen(DataBase.SalesDB);
            List<SqlParameter> aSqlParameterList = new List<SqlParameter>();
            aSqlParameterList.Add(new SqlParameter("@CustomerTypeId", (object)id ?? DBNull.Value));
            aSqlParameterList.Add(new SqlParameter("@CustomerMasterId", (object)id ?? DBNull.Value));



            DataTable dt = accessManager.GetDataTable("usp_CheckCampaignListByCustomerMasterId", aSqlParameterList);

            accessManager.SqlConnectionClose();


            return dt;
        }
        public DataTable GetCampaignDetail(int camid, string param)
        {
            accessManager.SqlConnectionOpen(DataBase.SalesDB);
            List<SqlParameter> aSqlParameterList = new List<SqlParameter>();
            aSqlParameterList.Add(new SqlParameter("@CampaignMasterId", (object)camid ?? DBNull.Value));
            aSqlParameterList.Add(new SqlParameter("@param", (object)param ?? DBNull.Value));


            DataTable dt = accessManager.GetDataTable("sp_Webapi_GetCampaignDetail", aSqlParameterList);

            accessManager.SqlConnectionClose();


            return dt;
        }
        public List<OrderDetails> GetOrderProductWiseCampaign(OrderMaster orderMasters)
        {
            Response result = new Response();
            List<OrderDetails> aList = new List<OrderDetails>();
            int customerId = 0;
            int custtypeid = 0;
            string campaignmasterId = "";
            foreach (var campaignInfoOrderWise in orderMasters.OrderDetails)
            {
                try
                {

                    DataTable dtprice = GetPrice(campaignInfoOrderWise.ProductId);
                    if (dtprice.Rows.Count > 0)
                    {
                        campaignInfoOrderWise.UnitPrice = Convert.ToDecimal(dtprice.Rows[0]["UnitPrice"].ToString());
                        campaignInfoOrderWise.VatPercentage = Convert.ToDecimal(dtprice.Rows[0]["VATPercentage"].ToString());
                        campaignInfoOrderWise.UnitVatAmount = Convert.ToDecimal(dtprice.Rows[0]["VATAmountPerUnit"].ToString());
                        campaignInfoOrderWise.TotalPrice =
                            campaignInfoOrderWise.UnitPrice * campaignInfoOrderWise.Quantity;
                        campaignInfoOrderWise.TotalVatAmount =
                            campaignInfoOrderWise.UnitVatAmount * campaignInfoOrderWise.Quantity;
                        campaignInfoOrderWise.NetAmount =
                            campaignInfoOrderWise.TotalPrice + campaignInfoOrderWise.TotalVatAmount;
                    }

                    string bonustypeId = "";

                    DataTable dtcustomerinfo = GetCustomerInfo(campaignInfoOrderWise.CustomerId);
                    if (dtcustomerinfo.Rows.Count > 0)
                    {
                        customerId = Convert.ToInt32(dtcustomerinfo.Rows[0]["CustomerMasterId"].ToString());
                        custtypeid = Convert.ToInt32(dtcustomerinfo.Rows[0]["CustomerTypeId"].ToString());
                    }
                    int count = 0;
                    DataTable dtdata = null;



                    foreach (var campaignMaster in orderMasters.CampaignMasters)
                    {
                        string param = "";
                        if (campaignMaster.CampgainMasterId != 0)
                        {
                            param = " AND CampaignMasterId='" + campaignMaster.CampgainMasterId + "'";
                        }
                        else
                        {
                            param = " AND CampaignMasterId IN (SELECT CampMasId FROM dbo.GetCampaignCustomer('" +
                                    custtypeid + "') WHERE CustomerId='" + customerId + "') ";
                        }
                        DataTable dtdata1 = GetProductWiseCampaign(
                            campaignInfoOrderWise.ProductId, param);


                        if (dtdata1.Rows.Count > 0)
                        {
                            dtdata = dtdata1;
                            campaignmasterId = dtdata.Rows[0]["CampaignMasterId"].ToString();
                            count++;
                        }
                    }

                    if (count < 2)
                    {
                        if (dtdata != null)
                        {



                            if (dtdata.Rows.Count > 0)
                            {
                                bonustypeId = dtdata.Rows[0]["BonusTypeId"].ToString();
                                //decimal qtycompare = string.IsNullOrEmpty(dtdata.Rows[0]["Quantity"].ToString())
                                //    ? 0
                                //    : Convert.ToDecimal(dtdata.Rows[0]["Quantity"].ToString());
                                //if (qtycompare <= campaignInfoOrderWise.Quantity)
                                //{


                                //    bonustypeId = dtdata.Rows[0]["BonusTypeId"].ToString();
                                //    campaignInfoOrderWise.IsCamp = true;
                                //    if (bonustypeId == "5")
                                //    {


                                //        decimal camqty = 0;
                                //        decimal bonusqty = 0;
                                //        decimal campaignproqty = 0;
                                //        decimal orderqty = campaignInfoOrderWise.Quantity;
                                //        bonusqty = string.IsNullOrEmpty(dtdata.Rows[0]["BonusQuantity"].ToString())
                                //            ? 0
                                //            : Convert.ToDecimal(dtdata.Rows[0]["BonusQuantity"].ToString());
                                //        campaignproqty = string.IsNullOrEmpty(dtdata.Rows[0]["Quantity"].ToString())
                                //            ? 0
                                //            : Convert.ToDecimal(dtdata.Rows[0]["Quantity"].ToString());
                                //        camqty = Math.Floor(Convert.ToDecimal((orderqty / campaignproqty)));
                                //        camqty = camqty * bonusqty;


                                //        OrderDetails aOrderDetails1 = new OrderDetails();
                                //        aOrderDetails1.ProductId = campaignInfoOrderWise.ProductId;
                                //        aOrderDetails1.CustomerId = campaignInfoOrderWise.CustomerId;
                                //        aOrderDetails1.Quantity = campaignInfoOrderWise.Quantity;
                                //        aOrderDetails1.DiscountPercentage = campaignInfoOrderWise.DiscountPercentage;
                                //        aOrderDetails1.DiscountValue = campaignInfoOrderWise.DiscountValue;
                                //        aOrderDetails1.UnitPrice = campaignInfoOrderWise.UnitPrice;
                                //        aOrderDetails1.TotalPrice = campaignInfoOrderWise.TotalPrice;
                                //        aOrderDetails1.TotalVatAmount = campaignInfoOrderWise.TotalVatAmount;
                                //        aOrderDetails1.VatPercentage = campaignInfoOrderWise.VatPercentage;
                                //        aOrderDetails1.UnitVatAmount = campaignInfoOrderWise.UnitVatAmount;
                                //        aOrderDetails1.NetAmount = campaignInfoOrderWise.NetAmount;
                                //        aOrderDetails1.CampaingName = dtdata.Rows[0]["CampaignName"].ToString();
                                //        aOrderDetails1.CampaignType = dtdata.Rows[0]["CampaignDetailId"].ToString();
                                //        aOrderDetails1.IsGiftProduct = false;
                                //        aOrderDetails1.IsCampaignProduct = true;
                                //        aOrderDetails1.ProductName = campaignInfoOrderWise.ProductName;

                                //        aList.Add(aOrderDetails1);




                                //    }

                                //}

                                if (bonustypeId == "1")
                                {
                                    decimal perctradeprice = 0;
                                    decimal perctotaltradeprice = 0;
                                    decimal dispercentage =
                                        string.IsNullOrEmpty(dtdata.Rows[0]["DiscountPercentage"].ToString())
                                            ? 0
                                            : Convert.ToDecimal(dtdata.Rows[0]["DiscountPercentage"].ToString());
                                    perctotaltradeprice = campaignInfoOrderWise.TotalPrice * (dispercentage / 100);

                                    OrderDetails aOrderDetails = new OrderDetails();
                                    aOrderDetails.ProductId = campaignInfoOrderWise.ProductId;
                                    aOrderDetails.CustomerId = campaignInfoOrderWise.CustomerId;
                                    aOrderDetails.Quantity = campaignInfoOrderWise.Quantity;
                                    aOrderDetails.DiscountPercentage = dispercentage;
                                    aOrderDetails.DiscountValue = perctotaltradeprice;
                                    aOrderDetails.UnitPrice = campaignInfoOrderWise.UnitPrice;
                                    aOrderDetails.TotalPrice = campaignInfoOrderWise.TotalPrice;
                                    aOrderDetails.TotalVatAmount = campaignInfoOrderWise.TotalVatAmount;
                                    aOrderDetails.VatPercentage = campaignInfoOrderWise.VatPercentage;
                                    aOrderDetails.UnitVatAmount = campaignInfoOrderWise.UnitVatAmount;
                                    aOrderDetails.NetAmount = campaignInfoOrderWise.NetAmount - perctotaltradeprice;
                                    aOrderDetails.CampaingName = dtdata.Rows[0]["CampaignName"].ToString();
                                    aOrderDetails.CampaignType = dtdata.Rows[0]["CampaignDetailId"].ToString();
                                    aOrderDetails.IsGiftProduct = false;
                                    aOrderDetails.IsCampaignProduct = true;
                                    aOrderDetails.ProductName = campaignInfoOrderWise.ProductName;

                                    campaignInfoOrderWise.IsCamp = true;

                                    aList.Add(aOrderDetails);


                                }

                                if (bonustypeId == "2")
                                {
                                    decimal perctradeprice = 0;
                                    //decimal perctotaltradeprice = 0;
                                    decimal perctotaltradeprice =
                                        string.IsNullOrEmpty(dtdata.Rows[0]["QuantityDteail"].ToString())
                                            ? 0
                                            : Convert.ToDecimal(dtdata.Rows[0]["QuantityDteail"].ToString());
                                    //perctotaltradeprice = campaignInfoOrderWise.TotalPrice * (dispercentage / 100);

                                    OrderDetails aOrderDetails = new OrderDetails();
                                    aOrderDetails.ProductId = campaignInfoOrderWise.ProductId;
                                    aOrderDetails.CustomerId = campaignInfoOrderWise.CustomerId;
                                    aOrderDetails.Quantity = campaignInfoOrderWise.Quantity;
                                    aOrderDetails.DiscountPercentage = 0;
                                    aOrderDetails.DiscountValue = perctotaltradeprice;
                                    aOrderDetails.UnitPrice = campaignInfoOrderWise.UnitPrice;
                                    aOrderDetails.TotalPrice = campaignInfoOrderWise.TotalPrice;
                                    aOrderDetails.TotalVatAmount = campaignInfoOrderWise.TotalVatAmount;
                                    aOrderDetails.VatPercentage = campaignInfoOrderWise.VatPercentage;
                                    aOrderDetails.UnitVatAmount = campaignInfoOrderWise.UnitVatAmount;
                                    aOrderDetails.NetAmount = campaignInfoOrderWise.NetAmount - perctotaltradeprice;
                                    aOrderDetails.CampaingName = dtdata.Rows[0]["CampaignName"].ToString();
                                    aOrderDetails.CampaignType = dtdata.Rows[0]["CampaignDetailId"].ToString();
                                    aOrderDetails.IsGiftProduct = false;
                                    aOrderDetails.ProductName = campaignInfoOrderWise.ProductName;
                                    aOrderDetails.IsCampaignProduct = true;
                                    aOrderDetails.IsCamp = true;
                                    campaignInfoOrderWise.IsCamp = true;
                                    aList.Add(aOrderDetails);


                                }
                                if (bonustypeId == "3")
                                {
                                    decimal perctradeprice = 0;
                                    //decimal perctotaltradeprice = 0;
                                    decimal perctotaltradeprice =
                                        string.IsNullOrEmpty(dtdata.Rows[0]["QuantityDteail"].ToString())
                                            ? 0
                                            : Convert.ToDecimal(dtdata.Rows[0]["QuantityDteail"].ToString());
                                    //perctotaltradeprice = campaignInfoOrderWise.TotalPrice * (dispercentage / 100);

                                    OrderDetails aOrderDetails = new OrderDetails();
                                    aOrderDetails.ProductId = campaignInfoOrderWise.ProductId;
                                    aOrderDetails.CustomerId = campaignInfoOrderWise.CustomerId;
                                    aOrderDetails.Quantity = campaignInfoOrderWise.Quantity;
                                    aOrderDetails.DiscountPercentage = 0;
                                    aOrderDetails.DiscountValue = perctotaltradeprice;
                                    aOrderDetails.UnitPrice = campaignInfoOrderWise.UnitPrice - perctotaltradeprice;
                                    aOrderDetails.TotalPrice = aOrderDetails.UnitPrice * aOrderDetails.Quantity;
                                    aOrderDetails.TotalVatAmount = campaignInfoOrderWise.TotalVatAmount;
                                    aOrderDetails.VatPercentage = campaignInfoOrderWise.VatPercentage;
                                    aOrderDetails.UnitVatAmount = campaignInfoOrderWise.UnitVatAmount;
                                    aOrderDetails.NetAmount = aOrderDetails.TotalPrice + aOrderDetails.TotalVatAmount - perctotaltradeprice;
                                    aOrderDetails.CampaingName = dtdata.Rows[0]["CampaignName"].ToString();
                                    aOrderDetails.CampaignType = dtdata.Rows[0]["CampaignDetailId"].ToString();
                                    aOrderDetails.IsGiftProduct = false;
                                    aOrderDetails.ProductName = campaignInfoOrderWise.ProductName;
                                    aOrderDetails.IsCampaignProduct = true;
                                    aOrderDetails.IsCamp = true;
                                    campaignInfoOrderWise.IsCamp = true;
                                    aList.Add(aOrderDetails);


                                }
                            }
                            else
                            {
                                campaignInfoOrderWise.IsCamp = false;
                            }
                        }
                        else
                        {
                            campaignInfoOrderWise.IsCamp = false;
                        }




                    }

                }
                catch (Exception ex)
                {
                    //result.Status = Status.BadRequest;
                    //result.Message = "Bad Request";
                    //result.ErrorMessage = ex.Message.ToString();
                }
            }



            foreach (var orderMastersCampaignMaster in orderMasters.CampaignMasters)
            {

                decimal camqty = 0;
                decimal bonusqty = 0;
                decimal campaignproqty = 0;
                decimal orderqty = 0;
                DataTable dtdata = null;
                if (orderMastersCampaignMaster.CampgainMasterId != 0)
                {
                    dtdata = GetCampaignDetail(orderMastersCampaignMaster.CampgainMasterId, "");
                }
                else
                {
                    campaignmasterId = string.IsNullOrEmpty(campaignmasterId) ? "0" : campaignmasterId;
                    dtdata = GetCampaignDetail(Convert.ToInt32(campaignmasterId), "");
                }

                if (dtdata.Rows.Count > 0)
                {
                    foreach (var orderMastersOrderDetail in orderMasters.OrderDetails)
                    {

                        {


                            if (orderMastersOrderDetail.ProductId.ToString() == dtdata.Rows[0]["ProductId"].ToString())
                            {
                                if (orderMastersOrderDetail.IsCamp == false)
                                {


                                    OrderDetails aOrderDetails = new OrderDetails();
                                    aOrderDetails.ProductId = orderMastersOrderDetail.ProductId;
                                    aOrderDetails.CustomerId = orderMastersOrderDetail.CustomerId;
                                    aOrderDetails.Quantity = orderMastersOrderDetail.Quantity;
                                    aOrderDetails.DiscountPercentage = orderMastersOrderDetail.DiscountPercentage;
                                    aOrderDetails.DiscountValue = orderMastersOrderDetail.DiscountValue;
                                    aOrderDetails.UnitPrice = orderMastersOrderDetail.UnitPrice;
                                    aOrderDetails.TotalPrice = orderMastersOrderDetail.TotalPrice;
                                    aOrderDetails.TotalVatAmount = orderMastersOrderDetail.TotalVatAmount;
                                    aOrderDetails.VatPercentage = orderMastersOrderDetail.VatPercentage;
                                    aOrderDetails.UnitVatAmount = orderMastersOrderDetail.UnitVatAmount;
                                    aOrderDetails.NetAmount = orderMastersOrderDetail.NetAmount;
                                    aOrderDetails.CampaingName = dtdata.Rows[0]["CampaignName"].ToString();
                                    aOrderDetails.CampaignType = dtdata.Rows[0]["CampaignDetailId"].ToString();
                                    aOrderDetails.IsGiftProduct = false;
                                    aOrderDetails.ProductName = orderMastersOrderDetail.ProductName;
                                    aOrderDetails.IsCampaignProduct = true;
                                    aOrderDetails.ProductName = orderMastersOrderDetail.ProductName;

                                    aList.Add(aOrderDetails);

                                    orderMastersOrderDetail.IsCamp = true;
                                }


                                orderqty = orderMastersOrderDetail.Quantity;
                                break;
                            }
                        }
                    }

                    decimal qtycompare = string.IsNullOrEmpty(dtdata.Rows[0]["Quantity"].ToString())
                        ? 0
                        : Convert.ToDecimal(dtdata.Rows[0]["Quantity"].ToString());
                    if (qtycompare <= orderqty)
                    {

                        foreach (DataRow dtdataRow in dtdata.Rows)
                        {

                            bonusqty = string.IsNullOrEmpty(dtdataRow["BonusQuantity"].ToString())
                                ? 0
                                : Convert.ToDecimal(dtdataRow["BonusQuantity"].ToString());
                            campaignproqty = string.IsNullOrEmpty(dtdata.Rows[0]["Quantity"].ToString())
                                ? 0
                                : Convert.ToDecimal(dtdataRow["Quantity"].ToString());
                            camqty = Math.Floor(Convert.ToDecimal((orderqty / campaignproqty)));
                            camqty = camqty * bonusqty;

                            OrderDetails aOrderDetails = new OrderDetails();
                            aOrderDetails.ProductId = Convert.ToInt32(dtdataRow["BonusProductId"].ToString());
                            aOrderDetails.CustomerId = customerId;
                            aOrderDetails.Quantity = (int)camqty;
                            aOrderDetails.DiscountPercentage = 0;
                            aOrderDetails.DiscountValue = 0;
                            aOrderDetails.UnitPrice = 0;
                            aOrderDetails.TotalPrice = 0;
                            aOrderDetails.TotalVatAmount = 0;
                            aOrderDetails.VatPercentage = 0;
                            aOrderDetails.UnitVatAmount = 0;
                            aOrderDetails.NetAmount = 0;
                            aOrderDetails.IsCampaignProduct = true;
                            aOrderDetails.ProductName = dtdataRow["ProductName"].ToString();
                            aOrderDetails.CampaingName = dtdataRow["CampaignName"].ToString();
                            aOrderDetails.CampaignType = dtdataRow["CampaignDetailId"].ToString();
                            aOrderDetails.IsGiftProduct = true;
                            aOrderDetails.IsCampaignProduct = true;

                            aList.Add(aOrderDetails);
                        }
                    }
                }
            }






            decimal remainingtotal = 0;
            foreach (var aOrderDetailse in orderMasters.OrderDetails)
            {
                if (aOrderDetailse.IsCamp == false)
                {
                    remainingtotal += aOrderDetailse.TotalPrice;
                }
            }

            decimal dispercampaigntrade = 0;
            DataTable dtdisperc = GetCampaignTradePolicyProductPerc(remainingtotal, customerId, custtypeid);
            if (dtdisperc.Rows.Count > 0)
            {
                dispercampaigntrade = Convert.ToDecimal(dtdisperc.Rows[0]["DiscountPercentage"].ToString());

                foreach (var detailse in orderMasters.OrderDetails)
                {
                    if (detailse.IsCamp == false)
                    {
                        OrderDetails aOrderDetails = new OrderDetails();
                        aOrderDetails.ProductId = detailse.ProductId;
                        aOrderDetails.CustomerId = detailse.CustomerId;
                        aOrderDetails.Quantity = detailse.Quantity;
                        aOrderDetails.DiscountPercentage = dispercampaigntrade;
                        aOrderDetails.DiscountValue =
                            detailse.TotalPrice * (dispercampaigntrade / 100);
                        aOrderDetails.UnitPrice = detailse.UnitPrice;
                        aOrderDetails.TotalPrice = detailse.TotalPrice;
                        aOrderDetails.TotalVatAmount = detailse.TotalVatAmount;
                        aOrderDetails.VatPercentage = detailse.VatPercentage;
                        aOrderDetails.UnitVatAmount = detailse.UnitVatAmount;
                        aOrderDetails.NetAmount =
                            detailse.NetAmount - detailse.TotalPrice *
                            (dispercampaigntrade / 100);
                        aOrderDetails.CampaingName = dtdisperc.Rows[0]["CampaignName"].ToString();
                        aOrderDetails.CampaignType = dtdisperc.Rows[0]["CampaignDetailId"].ToString();
                        aOrderDetails.IsGiftProduct = false;
                        aOrderDetails.IsCampaignProduct = true;
                        aOrderDetails.ProductName = detailse.ProductName;

                        aList.Add(aOrderDetails);

                        detailse.IsCamp = true;
                    }
                }


            }
            else
            {
                foreach (var detailse in orderMasters.OrderDetails)
                {

                    if (detailse.IsCamp == false)
                    {
                        OrderDetails aOrderDetails = new OrderDetails();
                        aOrderDetails.ProductId = detailse.ProductId;
                        aOrderDetails.CustomerId = detailse.CustomerId;
                        aOrderDetails.Quantity = detailse.Quantity;
                        aOrderDetails.DiscountPercentage = dispercampaigntrade;
                        aOrderDetails.DiscountValue = detailse.DiscountValue;
                        aOrderDetails.UnitPrice = detailse.UnitPrice;
                        aOrderDetails.TotalPrice = detailse.TotalPrice;
                        aOrderDetails.TotalVatAmount = detailse.TotalVatAmount;
                        aOrderDetails.VatPercentage = detailse.VatPercentage;
                        aOrderDetails.UnitVatAmount = detailse.UnitVatAmount;
                        aOrderDetails.NetAmount = detailse.NetAmount;
                        aOrderDetails.CampaingName = "";
                        aOrderDetails.CampaignType = "";
                        aOrderDetails.IsGiftProduct = false;
                        aOrderDetails.IsCampaignProduct = false;
                        aOrderDetails.ProductName = detailse.ProductName;

                        aList.Add(aOrderDetails);

                        detailse.IsCamp = true;
                    }
                }
            }





            return aList;
        }

        public bool SaveCampaign(string orderid,string campaignmasterid)
        {
            List<SqlParameter> aSqlParameterList = new List<SqlParameter>();

            aSqlParameterList.Add(new SqlParameter("@orderid", orderid));
            aSqlParameterList.Add(new SqlParameter("@campaignMasterId", campaignmasterid));


            return accessManager.SaveData("sp_webapi_SaveOrderCampaign", aSqlParameterList);

        }
    }
}
