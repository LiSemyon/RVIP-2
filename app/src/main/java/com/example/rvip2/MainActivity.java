package com.example.rvip2;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Looper;
import android.support.annotation.MainThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    TextView status_text;
    Button button;

    ArrayList<String> urls;

    View header;

    RecyclerView recyclerView;
    ImageAdapter imageAdapter;


    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        header = getLayoutInflater().inflate(R.layout.header_layout, null, false);
        status_text = (TextView) header.findViewById(R.id.status_textView);
        button = (Button) header.findViewById(R.id.button);
        urls = new ArrayList<>();
        imageAdapter = new ImageAdapter(this, urls);
        imageAdapter.addHeader(header);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(imageAdapter);
        button.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        try {
            backgr("http://mentallandscape.com/C_Catalog.htm");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void backgr(final String url_string) throws InterruptedException {

        count++;

        Thread newThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Document doc;
                try {

                    doc = Jsoup.connect(url_string).get();

                    Elements img = doc.getElementsByTag("img");

                    for (Element el : img){
                        urls.add("http://mentallandscape.com/" + el.attr("src"));
                    }

                    Elements href = doc.getElementsByTag("a");

                    for (Element el : href) {
                        if (count < 100) {
                            backgr("http://mentallandscape.com/" + el.attr("href"));
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        newThread.setName("super thread number " + String.valueOf(count));
        updateUINameThread(newThread.getName());

        try{
            Thread.sleep(2000);
            newThread.start();
        }catch(InterruptedException e){}
    }

    private void updateUINameThread(final String name) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageAdapter.notifyDataSetChanged();
                status_text.append("START " + name + "\n");
            }
        });
    }
}
