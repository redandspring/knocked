package ru.redandspring.libs;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Created by Alexander on 29.03.2016.
 */
public class MakeAsyncRequest extends AsyncTask<String, Void, String> {

    private Context mContext;

    private String uService;
    private String modeAction;

    public MakeAsyncRequest(Context context) {
        mContext = context;
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            // String service = params[0];
            uService = params[0];
            modeAction = params[1];
            return InternetRequest.main(mContext, uService, modeAction);
        }
        catch (IndexOutOfBoundsException e){
            Log.d("TAG", "Error params: " + e.getMessage());
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
        // [... Обновите индикатор хода выполнения, уведомления или другой
        // элемент пользовательского интерфейса ...]
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        // [... Сообщите о результате через обновление пользовательского
        // интерфейса, диалоговое окно или уведомление ...]
        if (s != null){
            InternetRequest.post(mContext, uService, s);
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }
}

