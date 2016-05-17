package ru.redandspring.knocked;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import ru.redandspring.models.AndroidDatabaseManager;
import ru.redandspring.models.OrderModel;

public class MainActivity extends AppCompatActivity {

    public  final static int    STATUS_SUCCESS = 200;
    public  final static int    STATUS_FAIL = 500;
    public  final static String BROADCAST_ACTION = "ru.redandspring.knocked";
    public  final static String TASK_NAME = "newOrderTask";
    private final static String LOG_TAG = "TAG-Main-activity";

    private BroadcastReceiver br;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if ( savedInstanceState == null ){
            if (br == null) {
                broadcast();
            }
            runService(CheckService.ResultMode.CREATE_ACTIVITY);
        }

        showOrders();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (br == null) {
                    broadcast();
                }
                runService(CheckService.ResultMode.BUTTON);
                /*
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                        */
            }
        });

    }

    private void broadcast(){

        // создаем BroadcastReceiver
        br = new BroadcastReceiver() {
            // действия при получении сообщений
            public void onReceive(Context context, Intent intent) {
                int task = intent.getIntExtra(TASK_NAME, 0);

                if (task == STATUS_SUCCESS){
                    showOrders();
                }
            }
        };
        // создаем фильтр для BroadcastReceiver
        IntentFilter intFilt = new IntentFilter(BROADCAST_ACTION);
        // регистрируем (включаем) BroadcastReceiver
        registerReceiver(br, intFilt);
    }

    private void runService(CheckService.ResultMode action){
        Intent serviceIntent = new Intent(this, CheckService.class);
        serviceIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        serviceIntent.setAction(action.toString());
        this.startService(serviceIntent);
    }

    private void showOrders(){

        ListView listView = (ListView)findViewById(R.id.listOrderView);
        OrderModel orderModel = new OrderModel(this);
        ArrayList<OrderModel.RowOrderModel> orders = orderModel.findOrdersLastAll();
        if (orders == null) return;
        int ordersSize =  orders.size();
        String[] listOrder = new String[ordersSize];
        for (int i = 0; i < ordersSize; i++) {
            listOrder[i] = orders.get(i).orderID + ": " + orders.get(i).message;
        }

        // используем адаптер данных
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,	android.R.layout.simple_list_item_1, listOrder);

        listView.setAdapter(adapter);

        adapter.notifyDataSetChanged();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View itemClicked, int position, long id) {

                TextView textView = (TextView) itemClicked;
                String strText = textView.getText().toString();
                //TextView infoOrderView = (TextView) findViewById(R.id.infoOrder);
                //infoOrderView.setText(strText);

                Intent intent = new Intent(MainActivity.this, OrderActivity.class);
                intent.putExtra("order", strText);
                startActivity(intent);


            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("is", 1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        else if (id == R.id.action_about){
            Intent intent = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(intent);
            return true;
        }
        /*
        else if (id == R.id.action_db){
            Intent intent = new Intent(MainActivity.this, AndroidDatabaseManager.class);
            startActivity(intent);
            return true;
        }
        */

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        if (br != null) {
            unregisterReceiver(br);
        }
        super.onStop();
    }
}
