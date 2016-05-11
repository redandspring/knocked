package ru.redandspring.knocked;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class OrderActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        String order = getIntent().getStringExtra("order");

        setContentView(R.layout.activity_order);
        TextView textView = (TextView) findViewById(R.id.infoOrder);
        textView.setText(order);
    }
}
