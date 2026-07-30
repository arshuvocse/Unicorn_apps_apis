<%@ Application Codebehind="Global.asax.cs" Inherits="SalesSolutionWebAPI.WebApiApplication" Language="C#" %>
<%@ Import Namespace="System" %>
<%@ Import Namespace="System.Security.Cryptography" %>
<%@ Import Namespace="System.Text" %>

<script runat="server">
    private const string SwaggerAuthSalt = "SalesRollSwaggerAuth:v1:";
    private const string SwaggerUserHash = "0f99a56eeda1e49a98123a3ef0c5b79138f48a916f64a89000b43b010938f437";
    private const string SwaggerPasswordHash = "e332d03de22488b6d6aaf948d47a91b5e0fd73cd040d96a682daad5ca5e97f4d";

    protected void Application_BeginRequest(object sender, EventArgs e)
    {
        string applicationPath = Request.ApplicationPath == "/" ? string.Empty : Request.ApplicationPath;
        string requestPath = Request.Path.Substring(applicationPath.Length);

        if (!requestPath.Equals("/swagger", StringComparison.OrdinalIgnoreCase) &&
            !requestPath.StartsWith("/swagger/", StringComparison.OrdinalIgnoreCase))
        {
            return;
        }

        if (!IsValidSwaggerCredential(Request.Headers["Authorization"]))
        {
            Response.StatusCode = 401;
            Response.StatusDescription = "Unauthorized";
            Response.Headers["WWW-Authenticate"] = "Basic realm=\"Sales Roll Swagger\"";
            Response.ContentType = "text/plain";
            Response.Write("Swagger authentication is required.");
            CompleteRequest();
        }
    }

    private static bool IsValidSwaggerCredential(string authorization)
    {
        if (string.IsNullOrWhiteSpace(authorization) ||
            !authorization.StartsWith("Basic ", StringComparison.OrdinalIgnoreCase))
        {
            return false;
        }

        try
        {
            string encodedCredential = authorization.Substring(6).Trim();
            string decodedCredential = Encoding.UTF8.GetString(Convert.FromBase64String(encodedCredential));
            int separatorIndex = decodedCredential.IndexOf(':');

            if (separatorIndex < 0)
            {
                return false;
            }

            string userName = decodedCredential.Substring(0, separatorIndex);
            string password = decodedCredential.Substring(separatorIndex + 1);

            return SlowEquals(ComputeHash(userName), SwaggerUserHash) &&
                   SlowEquals(ComputeHash(password), SwaggerPasswordHash);
        }
        catch (FormatException)
        {
            return false;
        }
    }

    private static string ComputeHash(string value)
    {
        using (SHA256 sha256 = SHA256.Create())
        {
            byte[] hash = sha256.ComputeHash(Encoding.UTF8.GetBytes(SwaggerAuthSalt + value));
            StringBuilder result = new StringBuilder(hash.Length * 2);

            for (int i = 0; i < hash.Length; i++)
            {
                result.Append(hash[i].ToString("x2"));
            }

            return result.ToString();
        }
    }

    private static bool SlowEquals(string left, string right)
    {
        if (left == null || right == null || left.Length != right.Length)
        {
            return false;
        }

        int difference = 0;
        for (int i = 0; i < left.Length; i++)
        {
            difference |= left[i] ^ right[i];
        }

        return difference == 0;
    }
</script>
