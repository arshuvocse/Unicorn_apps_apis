using Newtonsoft.Json.Linq;
using SalesSolutionWebAPI.DataManager;
using SalesSolutionWebAPI.Models;
using System;
using System.Data;
using System.Data.SqlClient;
using System.Collections.Generic;

namespace SalesSolutionWebAPI.Repository
{
    public class EPharmaIntrigrationDAL
    {
        private readonly DataAccessManager accessManager = new DataAccessManager();

        public EPharmaPersonInfoResponse SavePersonInfo(EPharmaPersonInfoRequest request)
        {
            EPharmaPersonInfoResponse response = new EPharmaPersonInfoResponse
            {
                BSPCode = request.BSPCode
            };

            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);

                List<SqlParameter> parameters = new List<SqlParameter>
                {
                    BuildStringParameter("@BSPCode", request.BSPCode),
                    BuildStringParameter("@Name", request.Name),
                    BuildStringParameter("@OwnerName", request.OwnerName),
                    BuildStringParameter("@Address", request.Address),
                    BuildStringParameter("@Mobile", request.Mobile),
                    BuildStringParameter("@Division", request.Division),
                    BuildStringParameter("@District", request.District),
                    BuildStringParameter("@Upazila", request.Upazila),
                    BuildStringParameter("@ProviderType", request.ProviderType, 50),
                    BuildVariantParameter("@UpsertMode", request.UpsertMode)
                };

                DataTable result = accessManager.GetDataTable("sp_WebApi_SaveEPharmaPersonInfo", parameters);
                if (result != null && result.Rows.Count > 0)
                {
                    DataRow row = result.Rows[0];
                    response.isSuccess = row["IsSuccess"] != DBNull.Value && Convert.ToBoolean(row["IsSuccess"]);
                    response.Action = row["Action"] != DBNull.Value ? row["Action"].ToString() : null;
                    response.Msd = row["Message"] != DBNull.Value ? row["Message"].ToString() : null;
                    response.ErrorMessage = row["ErrorMessage"] != DBNull.Value ? row["ErrorMessage"].ToString() : null;
                    response.isError = !response.isSuccess;
                }
                else
                {
                    response.isSuccess = false;
                    response.isError = true;
                    response.ErrorMessage = "Stored procedure returned no result.";
                    response.Msd = "Person info save failed.";
                }
            }
            catch (Exception exception)
            {
                accessManager.SqlConnectionClose(true);
                response.isSuccess = false;
                response.isError = true;
                response.ErrorMessage = exception.Message;
                response.Msd = "Person info save failed.";
            }
            finally
            {
                accessManager.SqlConnectionClose();
            }

            return response;
        }

        public EPharmaProviderDropoutResponse SaveProviderDropoutInfo(EPharmaProviderDropoutRequest request)
        {
            EPharmaProviderDropoutResponse response = new EPharmaProviderDropoutResponse();

            try
            {
                accessManager.SqlConnectionOpen(DataBase.SalesDisDB);

                List<SqlParameter> parameters = new List<SqlParameter>
                {
                    BuildStringParameter("@programName", request.ProgramName, 50),
                    BuildStringParameter("@providerCode", request.ProviderCode, 50),
                    BuildStringParameter("@providerName", request.ProviderName, 200),
                    BuildStringParameter("@mobileNo", request.MobileNo, 30),
                    BuildStringParameter("@nid", request.Nid, 30),
                    BuildStringParameter("@email", request.Email, 255),
                    BuildStringParameter("@outlet", request.Outlet, 200),
                    BuildStringParameter("@dropoutReason", request.DropoutReason, 500),
                    BuildDateTimeParameter("@insertedAt", request.InsertedAt)
                };

                DataTable result = accessManager.GetDataTable("sp_WebApi_SaveProviderDropoutIntrigration", parameters);
                if (result != null && result.Rows.Count > 0)
                {
                    DataRow row = result.Rows[0];
                    response.isSuccess = row["IsSuccess"] != DBNull.Value && Convert.ToBoolean(row["IsSuccess"]);
                    response.Msd = row["Message"] != DBNull.Value ? row["Message"].ToString() : null;
                    response.ErrorMessage = row["ErrorMessage"] != DBNull.Value ? row["ErrorMessage"].ToString() : null;
                    response.isError = !response.isSuccess;
                }
                else
                {
                    response.isSuccess = false;
                    response.isError = true;
                    response.ErrorMessage = "Stored procedure returned no result.";
                    response.Msd = "Provider dropout info save failed.";
                }
            }
            catch (Exception exception)
            {
                accessManager.SqlConnectionClose(true);
                response.isSuccess = false;
                response.isError = true;
                response.ErrorMessage = exception.Message;
                response.Msd = "Provider dropout info save failed.";
            }
            finally
            {
                accessManager.SqlConnectionClose();
            }

            return response;
        }

        private static SqlParameter BuildStringParameter(string parameterName, string value, int size = -1)
        {
            SqlParameter parameter = new SqlParameter(parameterName, SqlDbType.NVarChar);
            parameter.Size = size > 0 ? size : -1;
            parameter.Value = string.IsNullOrWhiteSpace(value) ? (object)DBNull.Value : value.Trim();
            return parameter;
        }

        private static SqlParameter BuildDateTimeParameter(string parameterName, DateTime? value)
        {
            return new SqlParameter(parameterName, SqlDbType.DateTime)
            {
                Value = value.HasValue ? (object)value.Value : DBNull.Value
            };
        }

        private static SqlParameter BuildVariantParameter(string parameterName, JToken value)
        {
            return new SqlParameter(parameterName, SqlDbType.Variant)
            {
                Value = ToDbValue(value)
            };
        }

        private static object ToDbValue(JToken value)
        {
            if (value == null || value.Type == JTokenType.Null || value.Type == JTokenType.Undefined)
            {
                return DBNull.Value;
            }

            if (value.Type == JTokenType.Integer)
            {
                long number = value.ToObject<long>();
                if (number >= int.MinValue && number <= int.MaxValue)
                {
                    return Convert.ToInt32(number);
                }

                return number;
            }

            if (value.Type == JTokenType.Float)
            {
                return value.ToObject<decimal>();
            }

            if (value.Type == JTokenType.Boolean)
            {
                return value.ToObject<bool>();
            }

            if (value.Type == JTokenType.Date)
            {
                return value.ToObject<DateTime>();
            }

            string text = value.ToString().Trim();
            return string.IsNullOrWhiteSpace(text) ? (object)DBNull.Value : text;
        }
    }
}
