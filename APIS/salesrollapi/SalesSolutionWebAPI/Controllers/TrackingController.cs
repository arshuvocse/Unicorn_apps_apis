using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using SalesSolutionWebAPI.Models;
using SalesSolutionWebAPI.Repository;
using System;
using System.Collections.Generic;
using System.Configuration;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading.Tasks;
using System.Web.Http;

namespace SalesSolutionWebAPI.Controllers
{
    public class TrackingController : ApiController
    {
        private static readonly HttpClient http = new HttpClient { Timeout = TimeSpan.FromSeconds(10) };
        private static readonly Regex macRegex = new Regex("^([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}$");
        private static readonly string[] radioTypes = { "gsm", "wcdma", "lte", "nr" };
        /// <summary>A fix coarser than this cannot teach a tower position; it would only blur the map.</summary>
        private const double MaxLearnAccuracyMeters = 100;

        static TrackingController()
        {
            ServicePointManager.SecurityProtocol |= SecurityProtocolType.Tls12;
            // BeaconDB (and the other MLS-compatible services) reject requests without a User-Agent.
            http.DefaultRequestHeaders.Add("User-Agent", "UnicornSalesRoll/1.0");
        }

        [System.Web.Http.HttpPost]
        [System.Web.Http.Route("api/Tracking/ResolveLocation")]
        public async Task<IHttpActionResult> ResolveLocation(ResolveLocationRequest request)
        {
            string error = Validate(request);
            if (error != null)
                return Json(Fail(error));

            var cells = request.CellTowers ?? new List<GeoCellTower>();

            // Our own map first: free, fast, and the only source that knows the towers the public
            // databases have never seen.
            // shortcut: only the serving cell (first in the list) is looked up, so the fix is that
            // tower's radius; average the known neighbours too if that proves too coarse.
            if (cells.Count > 0)
            {
                try
                {
                    ResolveLocationResponse known = new CellTowerDAL().Get_CellTowerLocation(cells[0]);
                    if (known != null)
                        return Json(known);
                }
                catch (Exception)
                {
                    // The map is an optimisation, never a hard dependency: fall through to the service.
                }
            }

            // Google ignores a single AP and a lone stale one can skew the cell answer.
            var wifis = request.WifiAccessPoints != null && request.WifiAccessPoints.Count >= 2 ? request.WifiAccessPoints : new List<GeoWifiAccessPoint>();
            // Google reads a 5G cell id from newRadioCellId, not cellId.
            var googleCells = cells.Select(c => c.RadioType == "nr"
                ? (object)new { radioType = c.RadioType, mobileCountryCode = c.MobileCountryCode, mobileNetworkCode = c.MobileNetworkCode, locationAreaCode = c.LocationAreaCode, newRadioCellId = c.CellId, signalStrength = c.SignalStrength }
                : c).ToList();
            string body = JsonConvert.SerializeObject(new { considerIp = false, cellTowers = googleCells, wifiAccessPoints = wifis },
                new JsonSerializerSettings { NullValueHandling = NullValueHandling.Ignore });

            try
            {
                using (var content = new StringContent(body, Encoding.UTF8, "application/json"))
                using (var resp = await http.PostAsync(GeolocationUrl(), content))
                {
                    if (resp.StatusCode == HttpStatusCode.NotFound)
                        return Json(Fail("location not found"));
                    if (!resp.IsSuccessStatusCode)
                        return Json(Fail("geolocation failed (" + (int)resp.StatusCode + ")"));

                    JObject json = JObject.Parse(await resp.Content.ReadAsStringAsync());
                    return Json(new ResolveLocationResponse
                    {
                        Success = true,
                        Latitude = (double)json["location"]["lat"],
                        Longitude = (double)json["location"]["lng"],
                        Accuracy = (double)json["accuracy"],
                        Source = wifis.Count >= 2 ? "wifi" : "cell",
                        Message = "ok"
                    });
                }
            }
            catch (Exception)
            {
                // Never surface exception text: it could carry the request URL with the key.
                return Json(Fail("geolocation failed"));
            }
        }

        /// <summary>
        /// BeaconDB by default: it speaks the same request/response format as Google Geolocation and
        /// needs no key. Set GoogleGeolocationApiKey to use Google instead, or GeolocationUrl for
        /// another MLS-compatible service.
        /// </summary>
        private static string GeolocationUrl()
        {
            string key = ConfigurationManager.AppSettings["GoogleGeolocationApiKey"];
            string url = ConfigurationManager.AppSettings["GeolocationUrl"];
            if (string.IsNullOrWhiteSpace(url))
                url = string.IsNullOrWhiteSpace(key)
                    ? "https://api.beacondb.net/v1/geolocate"
                    : "https://www.googleapis.com/geolocation/v1/geolocate";
            if (!string.IsNullOrWhiteSpace(key))
                url += (url.Contains("?") ? "&" : "?") + "key=" + Uri.EscapeDataString(key);
            return url;
        }

        /// <summary>
        /// The device pairs its serving tower with a GPS fix it already has; that is how the tower map
        /// gets built, so an indoor device with no GPS can be placed later. A coarse fix is refused:
        /// it would move the tower instead of confirming it.
        /// </summary>
        [System.Web.Http.HttpPost]
        [System.Web.Http.Route("api/Tracking/LearnCell")]
        public IHttpActionResult LearnCell(LearnCellRequest request)
        {
            if (request == null)
                return Json(Fail("request body required"));
            if (!radioTypes.Contains(request.RadioType) || request.MobileCountryCode < 0
                    || request.MobileNetworkCode < 0 || request.LocationAreaCode < 0 || request.CellId < 0)
                return Json(Fail("invalid cell tower"));
            if (request.Latitude < -90 || request.Latitude > 90
                    || request.Longitude < -180 || request.Longitude > 180
                    || (request.Latitude == 0 && request.Longitude == 0))
                return Json(Fail("invalid position"));
            if (request.Accuracy <= 0 || request.Accuracy > MaxLearnAccuracyMeters)
                return Json(Fail("fix not accurate enough"));

            try
            {
                new CellTowerDAL().Save_CellTowerLocation(request);
                return Json(new ResolveLocationResponse { Success = true, Message = "ok" });
            }
            catch (Exception)
            {
                return Json(Fail("could not store tower"));
            }
        }

        private static string Validate(ResolveLocationRequest r)
        {
            if (r == null) return "request body required";
            int cellCount = r.CellTowers == null ? 0 : r.CellTowers.Count;
            int wifiCount = r.WifiAccessPoints == null ? 0 : r.WifiAccessPoints.Count;
            if (cellCount > 10) return "too many cellTowers (max 10)";
            if (wifiCount > 20) return "too many wifiAccessPoints (max 20)";
            if (cellCount == 0 && wifiCount < 2) return "need at least one cell tower or two wifi access points";
            if (cellCount > 0 && r.CellTowers.Any(c => c == null || !radioTypes.Contains(c.RadioType)
                    || c.MobileCountryCode < 0 || c.MobileNetworkCode < 0 || c.LocationAreaCode < 0 || c.CellId < 0))
                return "invalid cell tower";
            if (wifiCount > 0 && r.WifiAccessPoints.Any(w => w == null || w.MacAddress == null || !macRegex.IsMatch(w.MacAddress)))
                return "invalid wifi macAddress";
            return null;
        }

        private static ResolveLocationResponse Fail(string message)
        {
            return new ResolveLocationResponse { Success = false, Message = message };
        }
    }
}
