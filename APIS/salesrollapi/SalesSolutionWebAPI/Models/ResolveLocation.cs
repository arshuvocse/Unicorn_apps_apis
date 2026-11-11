using Newtonsoft.Json;
using System.Collections.Generic;

namespace SalesSolutionWebAPI.Models
{
    // Field names match Google Geolocation API, so the lists are forwarded as-is.
    public class GeoCellTower
    {
        [JsonProperty("radioType")]
        public string RadioType { get; set; }

        [JsonProperty("mobileCountryCode")]
        public int MobileCountryCode { get; set; }

        [JsonProperty("mobileNetworkCode")]
        public int MobileNetworkCode { get; set; }

        [JsonProperty("locationAreaCode")]
        public int LocationAreaCode { get; set; }

        [JsonProperty("cellId")]
        public long CellId { get; set; }

        [JsonProperty("signalStrength", NullValueHandling = NullValueHandling.Ignore)]
        public int? SignalStrength { get; set; }
    }

    public class GeoWifiAccessPoint
    {
        [JsonProperty("macAddress")]
        public string MacAddress { get; set; }

        [JsonProperty("signalStrength", NullValueHandling = NullValueHandling.Ignore)]
        public int? SignalStrength { get; set; }
    }

    public class ResolveLocationRequest
    {
        [JsonProperty("employeeId")]
        public string EmployeeId { get; set; }

        [JsonProperty("cellTowers")]
        public List<GeoCellTower> CellTowers { get; set; }

        [JsonProperty("wifiAccessPoints")]
        public List<GeoWifiAccessPoint> WifiAccessPoints { get; set; }
    }

    /// <summary>One tower the device saw while it still had a good GPS fix: that is what teaches the map.</summary>
    public class LearnCellRequest
    {
        [JsonProperty("employeeId")]
        public string EmployeeId { get; set; }

        [JsonProperty("radioType")]
        public string RadioType { get; set; }

        [JsonProperty("mobileCountryCode")]
        public int MobileCountryCode { get; set; }

        [JsonProperty("mobileNetworkCode")]
        public int MobileNetworkCode { get; set; }

        [JsonProperty("locationAreaCode")]
        public int LocationAreaCode { get; set; }

        [JsonProperty("cellId")]
        public long CellId { get; set; }

        [JsonProperty("latitude")]
        public double Latitude { get; set; }

        [JsonProperty("longitude")]
        public double Longitude { get; set; }

        /// <summary>Metres, from the GPS fix this tower was paired with.</summary>
        [JsonProperty("accuracy")]
        public double Accuracy { get; set; }
    }

    public class ResolveLocationResponse
    {
        [JsonProperty("success")]
        public bool Success { get; set; }

        [JsonProperty("latitude")]
        public double Latitude { get; set; }

        [JsonProperty("longitude")]
        public double Longitude { get; set; }

        [JsonProperty("accuracy")]
        public double Accuracy { get; set; }

        [JsonProperty("source")]
        public string Source { get; set; }

        [JsonProperty("message")]
        public string Message { get; set; }
    }
}
