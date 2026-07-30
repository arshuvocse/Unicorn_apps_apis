using SalesSolutionWebAPI.Models;
using System;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Text;
using System.Web.Http;
using System.Web.Http.Controllers;
using System.Web.Http.Filters;

namespace SalesSolutionWebAPI.Utilities
{
    [AttributeUsage(AttributeTargets.Class | AttributeTargets.Method, AllowMultiple = false)]
    public sealed class EPharmaBasicAuthorizeAttribute : AuthorizationFilterAttribute
    {
        private const string UserName = "ePharma";
        private const string Password = "smc@!234";
        private const string Realm = "ePharmaIntrigrationAPI";

        public override void OnAuthorization(HttpActionContext actionContext)
        {
            if (SkipAuthorization(actionContext))
            {
                return;
            }

            AuthenticationHeaderValue authorization = actionContext.Request.Headers.Authorization;
            if (authorization == null ||
                !string.Equals(authorization.Scheme, "Basic", StringComparison.OrdinalIgnoreCase) ||
                string.IsNullOrWhiteSpace(authorization.Parameter))
            {
                Challenge(actionContext, "Basic authorization header is required.");
                return;
            }

            string decodedCredential;
            try
            {
                byte[] credentialBytes = Convert.FromBase64String(authorization.Parameter);
                decodedCredential = Encoding.UTF8.GetString(credentialBytes);
            }
            catch (FormatException)
            {
                Challenge(actionContext, "Invalid basic authorization header.");
                return;
            }

            string[] credentialParts = decodedCredential.Split(new[] { ':' }, 2);
            if (credentialParts.Length != 2 ||
                !string.Equals(credentialParts[0], UserName, StringComparison.Ordinal) ||
                !string.Equals(credentialParts[1], Password, StringComparison.Ordinal))
            {
                Challenge(actionContext, "Invalid username or password.");
            }
        }

        private static bool SkipAuthorization(HttpActionContext actionContext)
        {
            return actionContext.ActionDescriptor.GetCustomAttributes<AllowAnonymousAttribute>().Any() ||
                   actionContext.ControllerContext.ControllerDescriptor.GetCustomAttributes<AllowAnonymousAttribute>().Any();
        }

        private static void Challenge(HttpActionContext actionContext, string errorMessage)
        {
            HttpResponseMessage response = actionContext.Request.CreateResponse(
                HttpStatusCode.Unauthorized,
                new ResultInfo
                {
                    isSuccess = false,
                    isError = true,
                    ErrorMessage = errorMessage,
                    Msd = "Use Basic auth with the configured ePharma integration credentials."
                });

            response.Headers.WwwAuthenticate.Add(new AuthenticationHeaderValue("Basic", "realm=\"" + Realm + "\""));
            actionContext.Response = response;
        }
    }
}
