using SalesSolutionWebAPI.Models;

using System;
using System.Data;

namespace SalesSolutionWebAPI.Interface
{
    interface iUser
    {
        User doLogin(string UserName, string password, string Imei, string DeviceToken, string Device, string AppVersion, string OS, string OS_Versio);

        void AddUser(User aUser);
        void RemoveUser(string userId);

        ResultInfo SaveTracking(UserTracking tracking);
        DataTable GetLiveTracking(int empId, DateTime date);
    }
}
