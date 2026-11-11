package com.creatrix.unicorn.UtilityHelper;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Intelligent reverse-geocoder that resolves high-accuracy, human-readable place names.
 *
 * Solves the common Bangladesh issues where Google Geocoder returns:
 * - Plus Codes ("RMWW+8MJ, Bangladesh")
 * - Unnamed Road ("Unnamed Road, Bangladesh")
 *
 * Combines Google Geocoder components (subLocality, locality, subAdminArea, adminArea)
 * and falls back to fast online reverse geocoding if Google Geocoder provides poor or missing data.
 */
public final class SmartAddressResolver {
    private static final String TAG = "SmartAddressResolver";
    private static final Pattern PLUS_CODE_PATTERN = Pattern.compile("^[A-Z0-9]{2,8}\\+[A-Z0-9]{2,}(\\s*,\\s*)?", Pattern.CASE_INSENSITIVE);
    private static final int HTTP_TIMEOUT_MS = 2500;

    private SmartAddressResolver() {}

    // "Road 5, Dhanmondi, Dhaka" = 3; "Dhaka, Bangladesh" = 1 and is not specific enough to locate anyone
    private static final int GOOD_DETAIL = 3;

    @NonNull
    public static String resolveAddress(@NonNull Context context, double lat, double lon) {
        String best = "";
        // Step 1: Android Geocoder, keeping the most detailed of its results. In Bangladesh it often
        // gives "PLUS+CODE, Dhaka, Bangladesh", so the first non-empty answer is usually too vague.
        try {
            if (Geocoder.isPresent()) {
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(lat, lon, 3);
                if (addresses != null) {
                    for (Address addr : addresses) {
                        best = richer(best, formatAddress(addr));
                        best = richer(best, assembleComponents(addr));
                    }
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Geocoder failed: " + e.getMessage());
        }
        if (detailLevel(best) >= GOOD_DETAIL) {
            return best;
        }

        // Step 2: OpenStreetMap knows roads and neighbourhoods; BigDataCloud only knows the city,
        // so it is tried only when there is nothing at all.
        best = richer(best, fetchNominatimAddress(lat, lon));
        if (best.isEmpty()) {
            best = richer(best, fetchBigDataCloudAddress(lat, lon));
        }
        return best;
    }

    /** The more detailed of the two; a poor candidate never wins. */
    @NonNull
    private static String richer(@NonNull String current, @Nullable String candidate) {
        if (candidate == null || isPoorAddress(candidate)) {
            return current;
        }
        return detailLevel(candidate.trim()) > detailLevel(current) ? candidate.trim() : current;
    }

    /** Number of address parts below the district level, e.g. house, road, area, thana, city. */
    static int detailLevel(@Nullable String address) {
        if (address == null || address.trim().isEmpty()) {
            return 0;
        }
        int level = 0;
        for (String part : address.split(",")) {
            String p = part.trim().toLowerCase(Locale.ROOT);
            if (p.isEmpty() || p.equals("bangladesh") || p.endsWith("division") || p.matches("\\d{4}")) {
                continue; // country, division and bare post code do not narrow the place down
            }
            level++;
        }
        return level;
    }

    /**
     * Formats an address from AddressLine, stripping unwanted prefixes (like Plus Codes)
     * and enriching with locality/district if missing.
     */
    @Nullable
    private static String formatAddress(@NonNull Address address) {
        String line = address.getAddressLine(0);
        if (line == null || line.trim().isEmpty()) {
            return assembleComponents(address);
        }

        // Clean trailing newlines/spaces
        line = line.trim();

        // Strip leading Plus Code if present (e.g. "RMWW+8MJ, Mohadevpur, Bangladesh" -> "Mohadevpur, Bangladesh")
        line = PLUS_CODE_PATTERN.matcher(line).replaceFirst("").trim();

        // If after stripping it became empty or just ", Bangladesh"
        if (line.startsWith(",")) {
            line = line.substring(1).trim();
        }

        if (isPoorAddress(line)) {
            // Try assembling from structured fields
            String assembled = assembleComponents(address);
            if (assembled != null && !isPoorAddress(assembled)) {
                return assembled;
            }
            return null;
        }

        return line;
    }

    /**
     * Assembles human-readable address from structured components:
     * FeatureName, Thoroughfare, SubLocality (Village/Ward), Locality (Upazila/Thana),
     * SubAdminArea (District), AdminArea (Division), Country.
     */
    @Nullable
    private static String assembleComponents(@NonNull Address address) {
        List<String> parts = new ArrayList<>();

        String feature = address.getFeatureName();
        if (feature != null && !isPlusCode(feature) && !isCoordinates(feature) && !feature.toLowerCase(Locale.ROOT).contains("unnamed")) {
            parts.add(feature.trim());
        }

        String thoroughfare = address.getThoroughfare();
        if (thoroughfare != null && !thoroughfare.toLowerCase(Locale.ROOT).contains("unnamed") && !parts.contains(thoroughfare.trim())) {
            parts.add(thoroughfare.trim());
        }

        String subLocality = address.getSubLocality();
        if (subLocality != null && !parts.contains(subLocality.trim())) {
            parts.add(subLocality.trim());
        }

        String locality = address.getLocality(); // Upazila / Thana / City
        if (locality != null && !parts.contains(locality.trim())) {
            parts.add(locality.trim());
        }

        String subAdmin = address.getSubAdminArea(); // District
        if (subAdmin != null && !parts.contains(subAdmin.trim())) {
            parts.add(subAdmin.trim());
        }

        String admin = address.getAdminArea(); // Division
        if (admin != null && !parts.contains(admin.trim())) {
            parts.add(admin.trim());
        }

        String country = address.getCountryName();
        if (country != null && !parts.contains(country.trim())) {
            parts.add(country.trim());
        }

        if (parts.isEmpty()) {
            return null;
        }

        return String.join(", ", parts);
    }

    /**
     * Checks if an address string is too vague (e.g. only Plus Code, only "Unnamed Road", or only country).
     */
    public static boolean isPoorAddress(@Nullable String address) {
        if (address == null || address.trim().isEmpty()) {
            return true;
        }
        String clean = address.trim();

        // Just country name
        if (clean.equalsIgnoreCase("Bangladesh") || clean.equalsIgnoreCase("BD")) {
            return true;
        }

        // Entire string is a plus code e.g. "RMWW+8MJ" or "RMWW+8MJ, Bangladesh"
        if (clean.matches("^[A-Z0-9]{2,8}\\+[A-Z0-9]{2,}(\\s*,\\s*(Bangladesh)?)?$")) {
            return true;
        }

        // Just "Unnamed Road" or "Unnamed Road, Bangladesh"
        if (clean.toLowerCase(Locale.ROOT).startsWith("unnamed road") && clean.split(",").length <= 2) {
            return true;
        }

        return false;
    }

    private static boolean isPlusCode(String str) {
        return str != null && str.matches("^[A-Z0-9]{2,8}\\+[A-Z0-9]{2,}$");
    }

    private static boolean isCoordinates(String str) {
        return str != null && str.matches("^[+-]?\\d+(\\.\\d+)?$");
    }

    /** BigDataCloud client API: fast, but city/Upazila level only. */
    @Nullable
    private static String fetchBigDataCloudAddress(double lat, double lon) {
        try {
            String urlStr = "https://api.bigdatacloud.net/data/reverse-geocode-client?latitude=" + lat + "&longitude=" + lon + "&localityLanguage=en";
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(HTTP_TIMEOUT_MS);
            conn.setReadTimeout(HTTP_TIMEOUT_MS);
            conn.setRequestProperty("User-Agent", "UnicornApp/1.0");

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject json = new JSONObject(response.toString());
                List<String> parts = new ArrayList<>();
                String locality = json.optString("locality", "");
                String city = json.optString("city", "");
                String principalSubdivision = json.optString("principalSubdivision", "");
                String countryName = json.optString("countryName", "");

                if (!locality.isEmpty()) parts.add(locality);
                if (!city.isEmpty() && !parts.contains(city)) parts.add(city);
                if (!principalSubdivision.isEmpty() && !parts.contains(principalSubdivision)) parts.add(principalSubdivision);
                if (!countryName.isEmpty() && !parts.contains(countryName)) parts.add(countryName);

                if (!parts.isEmpty()) {
                    return String.join(", ", parts);
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /** OSM Nominatim (zoom 18 = building/road level). Usage policy: max 1 request/second per app. */
    @Nullable
    private static String fetchNominatimAddress(double lat, double lon) {
        try {
            String urlStr = "https://nominatim.openstreetmap.org/reverse?format=json&lat=" + lat + "&lon=" + lon + "&zoom=18&addressdetails=1&accept-language=en";
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(HTTP_TIMEOUT_MS);
            conn.setReadTimeout(HTTP_TIMEOUT_MS);
            conn.setRequestProperty("User-Agent", "UnicornTrackingApp/1.0 (support@creatrixbd.com)");

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject json = new JSONObject(response.toString());
                JSONObject addr = json.optJSONObject("address");
                if (addr != null) {
                    List<String> parts = new ArrayList<>();
                    String house = addr.optString("house_number", "");
                    if (!house.isEmpty()) parts.add("House " + house);
                    // most specific first: place name, road, neighbourhood, area, thana/city, district, division
                    String[] keys = {"amenity", "shop", "office", "building", "road", "neighbourhood", "quarter",
                            "suburb", "hamlet", "village", "town", "city_district", "city", "county",
                            "state_district", "state"};
                    for (String k : keys) {
                        String v = addr.optString(k, "");
                        if (!v.isEmpty() && !parts.contains(v) && !v.toLowerCase(Locale.ROOT).contains("unnamed")) {
                            parts.add(v);
                        }
                    }
                    parts.add("Bangladesh");
                    return String.join(", ", parts);
                }
            }
        } catch (Exception ignored) {
        }

        return null;
    }
}
