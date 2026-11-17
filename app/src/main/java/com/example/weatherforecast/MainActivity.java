package com.example.weatherforecast;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import java.util.LinkedHashSet;
import java.util.Set;

public class MainActivity extends Activity {
    private EditText cityInput;
    private TextView result, heroCity, heroIcon, heroTemp, heroCondition, heroUpdated;
    private Button detailBtn, saveBtn;
    private ProgressBar progress;
    private LinearLayout favorites;
    private WeatherApi.WeatherData data;
    private final android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); setContentView(R.layout.activity_main);
        cityInput = findViewById(R.id.cityInput); result = findViewById(R.id.result);
        heroCity = findViewById(R.id.heroCity); heroIcon = findViewById(R.id.heroIcon);
        heroTemp = findViewById(R.id.heroTemp); heroCondition = findViewById(R.id.heroCondition);
        heroUpdated = findViewById(R.id.heroUpdated);
        detailBtn = findViewById(R.id.detailBtn); saveBtn = findViewById(R.id.saveBtn);
        progress = findViewById(R.id.progress); favorites = findViewById(R.id.favorites);
        findViewById(R.id.searchBtn).setOnClickListener(v -> search(cityInput.getText().toString()));
        detailBtn.setOnClickListener(v -> {
            if (data == null) return;
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra("city", data.city); intent.putExtra("daily", data.daily);
            startActivity(intent);
        });
        saveBtn.setOnClickListener(v -> {
            String city = cityInput.getText().toString().trim();
            SharedPreferences pref = getSharedPreferences("weather", MODE_PRIVATE);
            Set<String> cities = new LinkedHashSet<>(pref.getStringSet("favorites", new LinkedHashSet<>()));
            cities.add(city); pref.edit().putStringSet("favorites", cities).apply(); showFavorites();
            Toast.makeText(this, "Đã lưu thành phố", Toast.LENGTH_SHORT).show();
        });
        showFavorites(); cityInput.setText("Hanoi"); search("Hanoi");
    }
    private void search(String city) {
        if (city.trim().isEmpty()) { cityInput.setError("Hãy nhập thành phố"); return; }
        progress.setVisibility(View.VISIBLE); detailBtn.setEnabled(false); saveBtn.setEnabled(false);
        result.setText("Đang tải dữ liệu..."); heroCondition.setText("Đang cập nhật..."); data = null;
        new Thread(() -> {
            try {
                WeatherApi.WeatherData loaded = WeatherApi.load(city);
                handler.post(() -> { data = loaded; result.setText(loaded.current.substring(loaded.current.indexOf("\nNhiệt độ:") + 1));
                    heroCity.setText(loaded.city); heroTemp.setText(Math.round(loaded.temperature) + "°");
                    heroCondition.setText(loaded.condition); heroUpdated.setText("Cập nhật: " + loaded.updated);
                    heroIcon.setText(weatherIcon(loaded.weatherCode));
                    detailBtn.setEnabled(true); saveBtn.setEnabled(true); progress.setVisibility(View.GONE); });
            } catch (Exception e) {
                handler.post(() -> { result.setText("Không thể lấy thời tiết: " + e.getMessage()); heroCondition.setText("Không có dữ liệu"); progress.setVisibility(View.GONE); });
            }
        }).start();
    }
    private String weatherIcon(int code) {
        if (code == 0) return "☀";
        if (code <= 3) return "⛅";
        if (code == 45 || code == 48) return "🌫";
        if (code >= 95) return "⛈";
        if (code >= 51 && code <= 82) return "🌧";
        return "☁";
    }
    private void showFavorites() {
        favorites.removeAllViews();
        Set<String> cities = getSharedPreferences("weather", MODE_PRIVATE).getStringSet("favorites", new LinkedHashSet<>());
        for (String city : cities) {
            Button button = new Button(this); button.setText("📍  " + city + "   →");
            button.setAllCaps(false); button.setTextColor(0xFF1556C0);
            button.setBackgroundResource(R.drawable.bg_secondary);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, (int)(52 * getResources().getDisplayMetrics().density));
            lp.bottomMargin = (int)(8 * getResources().getDisplayMetrics().density); button.setLayoutParams(lp);
            button.setOnClickListener(v -> { cityInput.setText(city); search(city); });
            button.setOnLongClickListener(v -> {
                Set<String> updated = new LinkedHashSet<>(getSharedPreferences("weather", MODE_PRIVATE).getStringSet("favorites", new LinkedHashSet<>()));
                updated.remove(city); getSharedPreferences("weather", MODE_PRIVATE).edit().putStringSet("favorites", updated).apply(); showFavorites();
                Toast.makeText(this, "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show(); return true;
            });
            favorites.addView(button);
        }
    }
}
