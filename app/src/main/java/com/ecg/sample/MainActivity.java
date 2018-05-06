package com.ecg.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.clj.ecgview.EcgView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EcgView ecgView = findViewById(R.id.ecg_view);

        List<Integer> list = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 4800; i++) {
            list.add(random.nextInt(30));
        }
        ecgView.setDataList(list);
    }
}
