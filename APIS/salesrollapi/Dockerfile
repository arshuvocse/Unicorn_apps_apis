# escape=`
# Windows container for the .NET Framework 4.7.2 Web API (needs Docker Desktop in "Windows containers" mode).
# Build the project first (MSBuild, Debug or Release) so SalesSolutionWebAPI\bin is populated, then from this folder:
#   docker compose up --build
FROM mcr.microsoft.com/dotnet/framework/aspnet:4.8-windowsservercore-ltsc2022
WORKDIR /inetpub/wwwroot
# the site runs straight from the project folder (bin + Web.config + content), like IIS Express does
COPY SalesSolutionWebAPI/ ./
# SQL Server to use; SqlUserAccess reads UNICORN_SQL_DATASOURCE, override it in docker-compose.yml
ENV UNICORN_SQL_DATASOURCE=host.docker.internal,1433
EXPOSE 80
