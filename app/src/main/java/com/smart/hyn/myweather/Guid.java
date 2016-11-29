package com.smart.hyn.myweather;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hyn on 16-11-29.
 */
public class Guid extends Activity implements ViewPager.OnPageChangeListener {
    private GuidAdapter guidAdapter;
    private ViewPager guidPage;
    private List<View> views;

    private ImageView[] dots;
    private int ids[] = {R.id.guid_index_image_1, R.id.guid_index_image_2, R.id.guid_index_image_3};

    private Button startBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guid_once);
        initViews();
        initDots();

        startBtn = (Button)views.get(2).findViewById(R.id.start_btn);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startApp();
            }
        });
    }

    private void initDots(){
        dots = new ImageView[views.size()];
        for(int i = 0; i < views.size(); ++i){
            dots[i] = (ImageView)findViewById(ids[i]);
        }
    }

    private void initViews(){
        LayoutInflater inflater = LayoutInflater.from(this);
        views = new ArrayList<View>();
        views.add(inflater.inflate(R.layout.intro_1,null));
        views.add(inflater.inflate(R.layout.intro_2,null));
        views.add(inflater.inflate(R.layout.intro_3,null));
        guidAdapter = new GuidAdapter(views, this);
        guidPage = (ViewPager)findViewById(R.id.guid_pager);
        guidPage.setAdapter(guidAdapter);
        guidPage.setOnPageChangeListener(this);
    }

    private void startApp(){
        Intent i = new Intent(Guid.this, MainActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        for(int i = 0; i < ids.length; ++i) {
            if(i == position){
                dots[i].setImageResource(R.drawable.page_indicator_focused);
            }else{
                dots[i].setImageResource(R.drawable.page_indicator_unfocused);
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
