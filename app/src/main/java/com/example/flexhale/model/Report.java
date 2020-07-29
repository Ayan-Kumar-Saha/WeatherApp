package com.example.flexhale.model;

import org.json.JSONException;
import org.json.JSONObject;

public class Report {

    private String weather;
    private String temperature;
    private String temperatureFeelsLike;
    private String temperatureMax;
    private String temperatureMin;
    private String windSpeed;
    private String windDirection;
    private String location;
    private String humidity;
    private String icon;

    public Report(JSONObject jsonObject) throws JSONException {

        double tempInKelvin = jsonObject.getJSONObject("main").getDouble("temp");
        this.temperature = Integer.toString(convertToCelcius(tempInKelvin));

        double tempFeelsLike = jsonObject.getJSONObject("main").getDouble("feels_like");
        this.temperatureFeelsLike = Integer.toString(convertToCelcius(tempFeelsLike));

        double tempMinInKelvin = jsonObject.getJSONObject("main").getDouble("temp_min");
        this.temperatureMin = Integer.toString(convertToCelcius(tempMinInKelvin));

        double tempMaxInKelvin = jsonObject.getJSONObject("main").getDouble("temp_max");
        this.temperatureMax = Integer.toString(convertToCelcius(tempMaxInKelvin));

        this.location = jsonObject.getString("name");
        this.humidity = Double.toString(jsonObject.getJSONObject("main").getDouble("humidity"));
        this.windSpeed = Double.toString(jsonObject.getJSONObject("wind").getDouble("speed"));
        this.windDirection = convertToDirection(jsonObject.getJSONObject("wind").getDouble("deg"));
        this.icon = jsonObject.getJSONArray("weather").getJSONObject(0).getString("icon");
        this.weather = jsonObject.getJSONArray("weather").getJSONObject(0).getString("main");
    }

    public String getTemperature() { return temperature; }

    public String getTemperatureMax() { return temperatureMax; }

    public String getTemperatureMin() { return temperatureMin; }

    public String getWindSpeed() { return windSpeed; }

    public String getWindDirection() { return windDirection; }

    public String getLocation() { return location; }

    public String getHumidity() { return humidity; }

    public String getTemperatureFeelsLike() { return temperatureFeelsLike; }

    public String getIcon() { return icon; }

    public String getWeather() { return weather; }

    private static int convertToCelcius(double temperatureInKelvin) { return (int) Math.floor(temperatureInKelvin - 273); }

    private static String convertToDirection(double directionInDegrees) {

        int val = (int) ((directionInDegrees / 22.5) + 0.5);

        String[] arr = {"N","NNE","NE","ENE","E","ESE", "SE", "SSE","S","SSW","SW","WSW","W","WNW","NW","NNW"};

        return arr[val % 16];
    }
}
