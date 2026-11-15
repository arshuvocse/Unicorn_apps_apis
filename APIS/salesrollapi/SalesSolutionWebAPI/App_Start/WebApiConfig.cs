using Microsoft.Owin.Security.OAuth;
using System;
using System.IO;
using System.Net;
using System.Net.Http;
using System.Web.Hosting;
using System.Web.Http;
using System.Web.Http.Filters;

namespace SalesSolutionWebAPI
{
    public static class WebApiConfig
    {
        public static void Register(HttpConfiguration config)
        {
            // Web API configuration and services
            // Configure Web API to use only bearer token authentication.
            config.SuppressDefaultHostAuthentication();
            config.Filters.Add(new HostAuthenticationFilter(OAuthDefaults.AuthenticationType));
            config.Filters.Add(new ApiExceptionFilter());

            // Web API routes
            config.MapHttpAttributeRoutes();

            config.Routes.MapHttpRoute(
                name: "DefaultApi",
                routeTemplate: "api/{controller}/{id}",
                defaults: new { id = RouteParameter.Optional }
            );
        }
    }

    // Status stays 500 so apps keep treating it as a failure; the cause is logged and sent in the body.
    public class ApiExceptionFilter : ExceptionFilterAttribute
    {
        private static readonly object logLock = new object();

        public override void OnException(HttpActionExecutedContext context)
        {
            try
            {
                string dir = HostingEnvironment.MapPath("~/App_Data/Logs");
                Directory.CreateDirectory(dir);
                // Path only: query strings can carry credentials.
                string entry = DateTime.Now.ToString("HH:mm:ss") + " " + context.Request.Method + " "
                    + context.Request.RequestUri.AbsolutePath + Environment.NewLine
                    + context.Exception + Environment.NewLine + Environment.NewLine;
                lock (logLock)
                {
                    File.AppendAllText(Path.Combine(dir, "api-errors-" + DateTime.Now.ToString("yyyy-MM-dd") + ".log"), entry);
                }
            }
            catch (Exception)
            {
                // logging must never replace the real error
            }

            context.Response = context.Request.CreateResponse(
                HttpStatusCode.InternalServerError,
                new { isSuccess = false, Msd = "Server error. Please try again.", ErrorMessage = context.Exception.Message },
                context.ActionContext.ControllerContext.Configuration.Formatters.JsonFormatter);
        }
    }
}
