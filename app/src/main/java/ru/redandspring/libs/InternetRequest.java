package ru.redandspring.libs;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import ru.redandspring.knocked.CheckService;
import ru.redandspring.knocked.MainActivity;
import ru.redandspring.knocked.R;
import ru.redandspring.models.OrderModel;

/**
 * Created by Alexander on 29.03.2016.
 */
public class InternetRequest {

    private final static String LOG_TAG = "TAG-Internet-Request";

    private final static String F_PATH = "http://%s/";
    private final static String F_PARAMS = "key=%s&last=%s";
    private final static String KEY_API_SITE = "key_api_site";
    private final static String SERVER_SITE = "server_site";
    private final static String LAST_ORDER_ID = "last_order_id";

    private static CheckService.ResultMode mode;

    public static final String main(Context context, String service, String modeAction){

        mode = CheckService.ResultMode.valueOf(modeAction);

        switch (service){
            case "api-opencart-orders":
                return InternetRequest.getOrder(context);
        }

        return null;
    }

    public static final void post(Context context, String service, String result){
        switch (service){
            case "api-opencart-orders":
                InternetRequest.setOrder(context, result);
                break;
        }
    }

    private static final void setOrder(Context context, String result){

        if (result.isEmpty()){
            Log.w(LOG_TAG, "result isEmpty");
            return;
        }

        if (result == "no-settings"){
            if (mode == CheckService.ResultMode.BUTTON) {
                Toast.makeText(context, "Не заданы настройки", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        OrderModel orderModel = new OrderModel(context);

        try {
            JSONObject dataJsonObj =  new JSONObject(result);
            Integer code = dataJsonObj.getInt("code");
            if (code == 1){
                JSONArray orders = dataJsonObj.getJSONArray("orders");
                int lastID = 0;
                boolean isNew = false;
                for (int i = 0; i < orders.length(); i++) {
                    JSONObject order = orders.getJSONObject(i);
                    Integer ID = order.getInt("id");
                    lastID = (ID > lastID) ? ID : lastID;
                    String txt = order.getString("txt");

                    OrderModel.RowOrderModel row = orderModel.findOrderById(ID);

                    if (row == null) {
                        Log.d(LOG_TAG, ">> INSERT ORDER: " + ID + "<<");
                        orderModel.insert(ID, txt);
                        isNew = true;
                    }
                }
                boolean toastShow = false;
                if (lastID > 0){
                    prefs.edit().putString(LAST_ORDER_ID, String.valueOf(lastID)).commit();
                    if (isNew){
                        if (mode == CheckService.ResultMode.BACKGROUND){
                            note(context, lastID);
                        }
                        else {

                            Intent intent = new Intent(MainActivity.BROADCAST_ACTION);
                            intent.putExtra(MainActivity.TASK_NAME, MainActivity.STATUS_SUCCESS);
                            context.sendBroadcast(intent);

                            Toast.makeText(context, "Получен новый заказ!", Toast.LENGTH_SHORT).show();
                            toastShow = true;
                        }
                    }
                }

                if (mode == CheckService.ResultMode.BUTTON && !toastShow) {
                    Toast.makeText(context, "обновлено", Toast.LENGTH_SHORT).show();
                }
            }
            else if (code == 2 && mode == CheckService.ResultMode.BUTTON){
                Toast.makeText(context, "Новых заказов нет", Toast.LENGTH_SHORT).show();
            }
            else {
                if (mode == CheckService.ResultMode.BUTTON) {
                    Toast.makeText(context, "Ошибка при запросе, код: " + code, Toast.LENGTH_SHORT).show();
                }
                Log.w(LOG_TAG, "Error Json code: " + code);
            }
        } catch (JSONException e) {
            if (mode == CheckService.ResultMode.BUTTON) {
                Toast.makeText(context, "Ошибка", Toast.LENGTH_SHORT).show();
            }
            Log.e(LOG_TAG, "Error JSON: " + e.getMessage());
            return;
        }
    }

    private static final void note(Context context, int orderID){

        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context,
                0, notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        Resources res = context.getResources();
        Notification.Builder builder = new Notification.Builder(context);

        builder.setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentTitle(res.getString(R.string.notifytitle)) // Заголовок уведомления
                .setContentText(res.getString(R.string.notifytext) + " #" + orderID);

        Notification notification = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            notification = builder.build();
        }
        else {
            notification = builder.getNotification(); // до API 16
        }

        notification.defaults = Notification.DEFAULT_SOUND |
                Notification.DEFAULT_VIBRATE;

        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(orderID, notification);
    }

    private static final String getOrder(Context context){

        String result = "";
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String serverSite = prefs.getString(SERVER_SITE, null);
        String keyApiSite = prefs.getString(KEY_API_SITE, null);
        String lastOrderId = prefs.getString(LAST_ORDER_ID, null);

        if (serverSite == null || serverSite.isEmpty() || keyApiSite == null || keyApiSite.isEmpty() || lastOrderId == null){
            Log.w(LOG_TAG, "Empty settings");
            // Нельзя вызвать здесь Toast
            result = "no-settings";
            return result;
        }

        String path = String.format(F_PATH, serverSite); //"http://api.svet-lu.ru/";
        String method = "POST";
        String params = String.format(F_PARAMS, keyApiSite, lastOrderId);

        try {
            result = InternetRequest.connect(path, method, params);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error connect http: " + e.getMessage());
        }
        finally {
            return result;
        }
    }

    private static final String connect(String path, String method, String params) throws IOException {

        byte[] data;
        BufferedReader reader=null;

        try {
            URL url=new URL(path);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod(method);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setReadTimeout(10000);
            conn.setRequestProperty("Content-Length", "" + Integer.toString(params.getBytes().length));
            OutputStream os = conn.getOutputStream();
            data = params.getBytes("UTF-8");
            os.write(data);

            conn.connect();

            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder buf=new StringBuilder();
            String line;
            while ((line=reader.readLine()) != null) {
                buf.append(line + "\n");
            }
            return(buf.toString());
        }
        finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

}
