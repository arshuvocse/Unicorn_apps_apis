using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using System;
using System.ComponentModel.DataAnnotations;

namespace SalesSolutionWebAPI.Models
{
    public class EPharmaPersonInfoRequest
    {
        [Required]
        public string BSPCode { get; set; }

        [Required]
        public string Name { get; set; }

        public string OwnerName { get; set; }

        public string Address { get; set; }

        public string Mobile { get; set; }

        public string Division { get; set; }

        public string District { get; set; }

        public string Upazila { get; set; }

        [StringLength(50)]
        public string ProviderType { get; set; }

        public JToken UpsertMode { get; set; }
    }

    public class EPharmaPersonInfoResponse : ResultInfo
    {
        public string BSPCode { get; set; }

        public string Action { get; set; }
    }

    public class EPharmaProviderDropoutRequest
    {
        [JsonProperty("programName")]
        [StringLength(50)]
        public string ProgramName { get; set; }

        [JsonProperty("providerCode")]
        [StringLength(50)]
        public string ProviderCode { get; set; }

        [JsonProperty("providerName")]
        [StringLength(200)]
        public string ProviderName { get; set; }

        [JsonProperty("mobileNo")]
        [StringLength(30)]
        public string MobileNo { get; set; }

        [JsonProperty("nid")]
        [StringLength(30)]
        public string Nid { get; set; }

        [JsonProperty("email")]
        [StringLength(255)]
        public string Email { get; set; }

        [JsonProperty("outlet")]
        [StringLength(200)]
        public string Outlet { get; set; }

        [JsonProperty("dropoutReason")]
        [StringLength(500)]
        public string DropoutReason { get; set; }

        [JsonProperty("insertedAt")]
        public DateTime? InsertedAt { get; set; }
    }

    public class EPharmaProviderDropoutResponse : ResultInfo
    {
    }
}
