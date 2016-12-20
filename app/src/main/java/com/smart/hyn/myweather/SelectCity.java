package com.smart.hyn.myweather;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.smart.hyn.myweather.com.smart.hyn.bean.City;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hyn on 16-10-18.
 */
public class SelectCity extends Activity implements View.OnClickListener{

    private ImageView mBackBtn;
    private ListView cityListView;
    private List<City> cityList;
    private String[] cityNameList;
    private String[] cityNumberList;
    private String selectedCityCode;
    private EditText searchText;
    private TextWatcher searchTextWatcher;
    private TextView title;
    private SharedPreferences prefConfig;
    private SharedPreferences.Editor prefConfigEditor;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.select_city);
        mBackBtn = (ImageView)findViewById(R.id.title_back);
        mBackBtn.setOnClickListener((View.OnClickListener) this);

        title = (TextView)findViewById(R.id.title_name);
        Bundle bundle = this.getIntent().getExtras();
        String name = bundle.getString("city_name");
        title.setText(getString(R.string.current_city) + name);
        selectedCityCode = bundle.getString("city_code");

        InitCityList();

        searchText = (EditText)findViewById(R.id.search_edit);
        searchText.addTextChangedListener(searchTextWatcher);
        searchTextWatcher = new TextWatcher() {
            private CharSequence temp;
            private int editStart;
            private int editEnd;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };

    }

    private void InitCityList(){
        MyApplication myApp = (MyApplication)this.getApplication();
        cityList = myApp.getCityList();
        int l = cityList.size();
        List<String> cn = new ArrayList<String>();
        List<String> cn2 = new ArrayList<String>();
        for (int i = 0; i < l; ++i){
            cn.add(cityList.get(i).getCity());
            cn2.add(cityList.get(i).getNumber());
        }
        cityNameList = (String[])cn.toArray(new String[cn.size()]);
        cityNumberList = (String[])cn2.toArray(new String[cn2.size()]);

        cityListView = (ListView)findViewById(R.id.selection_list);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(SelectCity.this,android.R.layout.simple_list_item_1, cityNameList);
        cityListView.setAdapter(adapter);
        //click
        cityListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(SelectCity.this, "Clicked"+ i, Toast.LENGTH_SHORT).show();
                selectedCityCode = cityNumberList[i];
                back();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.title_back:
                back();
                break;
            default:
                break;
        }
    }

    private void back(){
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("cityCode", selectedCityCode);
        setResult(RESULT_OK, i);
        finish();
    }
}
