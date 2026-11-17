package com.example.weatherforecast;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class WeatherApi {
    public static class WeatherData {
        public String city, current, daily, condition, updated;
        public double temperature;
        public int weatherCode;
    }
    private static JSONObject get(String address) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(address).openConnection();
        c.setConnectTimeout(12000); c.setReadTimeout(12000); c.setRequestProperty("Accept", "application/json");
        try {
            if (c.getResponseCode() != 200) throw new Exception("Máy chủ trả về mã " + c.getResponseCode());
            BufferedReader r = new BufferedReader(new InputStreamReader(c.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder b = new StringBuilder(); String line;
            while ((line = r.readLine()) != null) b.append(line);
            return new JSONObject(b.toString());
        } finally { c.disconnect(); }
    }
    public static WeatherData load(String city) throws Exception {
        String query = URLEncoder.encode(city.trim(), "UTF-8");
        JSONObject places = get("https://geocoding-api.open-meteo.com/v1/search?name=" + query + "&count=1&language=vi&format=json");
        JSONArray found = places.optJSONArray("results");
        if (found == null || found.length() == 0) throw new Exception("Không tìm thấy thành phố.");
        JSONObject place = found.getJSONObject(0);
        double lat = place.getDouble("latitude"), lon = place.getDouble("longitude");
        String url = "https://api.open-meteo.com/v1/forecast?latitude=" + lat + "&longitude=" + lon +
                "&current=temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,weather_code,wind_speed_10m" +
                "&daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max&timezone=auto&forecast_days=7";
        JSONObject weather = get(url), now = weather.getJSONObject("current"), days = weather.getJSONObject("daily");
        WeatherData data = new WeatherData();
        data.city = place.getString("name") + ", " + place.optString("country", "");
        data.weatherCode = now.optInt("weather_code");
        data.condition = describe(data.weatherCode);
        data.temperature = now.optDouble("temperature_2m");
        data.updated = now.optString("time");
        data.current = data.city + "\n" + describe(now.optInt("weather_code")) +
                "\nNhiệt độ: " + now.optDouble("temperature_2m") + " °C" +
                "\nCảm giác như: " + now.optDouble("apparent_temperature") + " °C" +
                "\nĐộ ẩm: " + now.optInt("relative_humidity_2m") + " %" +
                "\nGió: " + now.optDouble("wind_speed_10m") + " km/h" +
                "\nLượng mưa: " + now.optDouble("precipitation") + " mm" +
                "\nCập nhật: " + now.optString("time");
        JSONArray dates = days.getJSONArray("time"), max = days.getJSONArray("temperature_2m_max"), min = days.getJSONArray("temperature_2m_min"), codes = days.getJSONArray("weather_code"), rain = days.getJSONArray("precipitation_probability_max");
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < dates.length(); i++) {
            b.append(dates.getString(i)).append("  •  ").append(describe(codes.optInt(i))).append("\n")
             .append("Nhiệt độ: ").append(min.optDouble(i)).append("–").append(max.optDouble(i)).append(" °C")
             .append("  |  Khả năng mưa: ").append(rain.isNull(i) ? "N/A" : rain.optInt(i) + "%").append("\n\n");
        }
        data.daily = b.toString(); return data;
    }
    private static String describe(int code) {
        if (code == 0) return "Trời quang";
        if (code <= 3) return "Có mây";
        if (code == 45 || code == 48) return "Sương mù";
        if (code >= 51 && code <= 67) return "Mưa nhỏ / mưa";
        if (code >= 71 && code <= 77) return "Tuyết";
        if (code >= 80 && code <= 82) return "Mưa rào";
        if (code >= 95) return "Dông";
        return "Thời tiết thay đổi";
    }
}
