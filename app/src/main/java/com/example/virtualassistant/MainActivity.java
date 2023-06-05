package com.example.virtualassistant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.textclassifier.TextLanguage;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    RecyclerView recyclerView ;
    TextView welcomeTextView ;
    EditText messageEditText ;
    ImageButton sendButton ;
    List<Message> messageList ;
    MessageAdapter messageAdapter ;

    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");

    OkHttpClient client = new OkHttpClient();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //message list :
        messageList=new ArrayList<>() ;

        //id link :
        recyclerView=findViewById(R.id.recycler_view) ;
        welcomeTextView=findViewById(R.id.welcome_text) ;
        messageEditText=findViewById(R.id.message_edit_text) ;
        sendButton=findViewById(R.id.send_btn) ;

        //setup recycler view :
        messageAdapter=new MessageAdapter(messageList) ;
        recyclerView.setAdapter(messageAdapter) ;
        LinearLayoutManager llm=new LinearLayoutManager(this) ;
        llm.setStackFromEnd(true) ;
        recyclerView.setLayoutManager(llm) ;

        //listening du message d'utilisateur :
        sendButton.setOnClickListener((v) -> {
            String question=messageEditText.getText().toString().trim() ;
            Toast.makeText(this, question, Toast.LENGTH_SHORT).show();
            addToChat(question,Message.SENT_BY_ME);
            messageEditText.setText("") ;
            callAPI(question);
            welcomeTextView.setVisibility(View.GONE);
        });
    }

    //affichage message :
    void addToChat(String message,String sentBy){
        runOnUiThread(new Runnable() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void run() {
                messageList.add(new Message(message,sentBy)) ;
                messageAdapter.notifyDataSetChanged() ;
                recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
            }
        });

    }

    //ajout reponse :
    void addResponse(String response) {
        runOnUiThread(new Runnable() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void run() {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("choices");
                    String result = jsonArray.getJSONObject(0).getString("text");
                    messageList.add(new Message(result.trim(), Message.SENT_BY_BOT));
                } catch (JSONException e) {
                    messageList.add(new Message("Oups" + e.getMessage(), Message.SENT_BY_BOT));
                    e.printStackTrace();
                }
                messageAdapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
            }
        });
    }

    //appel API :
    void callAPI(String question){
        //okHTTP
        JSONObject jsonBody=new JSONObject() ;
        try {
            jsonBody.put("model","text-davinci-003") ;
            jsonBody.put("prompt",question) ;
            jsonBody.put("max_tokens",4000) ;
            jsonBody.put("temperature",0) ;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        //output type JSON pour convertir à string :
        RequestBody body=RequestBody.create(jsonBody.toString(),MediaType.parse("application/json")) ;
        Request request=new Request.Builder()
                .url("https://api.openai.com/v1/completions")
                .header("Authorization","Bearer "+"API_KEY")
                .post(body)
                .build() ;

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                addResponse(e.getMessage()) ;
            }

            //generation reponse :
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.isSuccessful()){
                    JSONObject jsonObject=null ;
                    try {
                        jsonObject =new JSONObject(response.body().string()) ;
                        JSONArray jsonArray = jsonObject.getJSONArray("choices");
                        String result = jsonArray.getJSONObject(0).getString("text");
                        addResponse(result.trim());
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    //message d'erreur :
                }else {
                    addResponse(response.body().toString()) ;
                }
            }
        });

    }
}
