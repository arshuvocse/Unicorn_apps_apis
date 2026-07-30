using System.Linq;
using System.Web.Http;
using Swashbuckle.Application;

namespace SalesSolutionWebAPI
{
    public static class SwaggerConfig
    {
        public static void Register(HttpConfiguration config)
        {
            config
                .EnableSwagger(c =>
                {
                    c.SingleApiVersion("v1", "Sales Roll API")
                        .Description("API documentation for Sales Roll.");

                    c.ApiKey("Bearer")
                        .Description("Enter the token as: Bearer {token}")
                        .Name("Authorization")
                        .In("header");

                    c.ResolveConflictingActions(apiDescriptions => apiDescriptions.First());
                    c.UseFullTypeNameInSchemaIds();
                })
                .EnableSwaggerUi(c =>
                {
                    c.DocumentTitle("Sales Roll API");
                    c.EnableApiKeySupport("Authorization", "header");
                });
        }
    }
}
