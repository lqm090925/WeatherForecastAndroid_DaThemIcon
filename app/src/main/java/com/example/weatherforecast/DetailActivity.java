package com.example.weatherforecast;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class DetailActivity extends Activity {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state); setContentView(R.layout.activity_detail);
        String city = getIntent().getStringExtra("city"), daily = getIntent().getStringExtra("daily");
        ((TextView)findViewById(R.id.title)).setText("Dự báo 7 ngày: " + (city == null ? "" : city));
        ((TextView)findViewById(R.id.forecast)).setText(daily == null ? "Không có dữ liệu" : daily);
        findViewById(R.id.backBtn).setOnClickListener(v -> finish());
    }
}
