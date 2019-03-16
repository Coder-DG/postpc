package com.dginzbourg.postpc.ex0;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerTextView;

import static com.dginzbourg.postpc.ex0.R.id.shimmer_tv;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Shimmer shimmer = new Shimmer();
        shimmer.start((ShimmerTextView) findViewById(shimmer_tv));
    }
}
