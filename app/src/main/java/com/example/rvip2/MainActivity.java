package com.example.rvip2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadFactory;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    TextView status_text;
    Button button;

    Queue<Thread> threadQueue;

    Queue<Thread> thread_abstract_queue;

    CopyOnWriteArrayList<String> urls;

    View header;

    RecyclerView recyclerView;
    ImageAdapter imageAdapter;

    private static int COUNT_THREADS = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*thread_abstract_queue = new ArrayBlockingQueue<Thread>(20);
        for (int i = 0; i < 100; i ++) {
            final Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    thread_abstract_queue.remove(thread_abstract_queue.peek());
                    updateUIWithThreadName("remove " + Thread.currentThread().getName());
                }
            });
            thread.setName(String.valueOf(i) + " thread");
            thread_abstract_queue.add(thread);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            thread_abstract_queue.peek().start();
        }*/
        threadQueue = new ArrayBlockingQueue<>(4);
        header = getLayoutInflater().inflate(R.layout.header_layout, null, false);
        status_text = (TextView) header.findViewById(R.id.status_textView);
        button = (Button) header.findViewById(R.id.button);
        urls = new CopyOnWriteArrayList<>();
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
            startParseNewPage(getString(R.string.url_to_start));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void startParseNewPage(final String url_string) throws InterruptedException {
        COUNT_THREADS++;
        Thread newThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Document doc;
                try {
                    doc = Jsoup.connect(url_string).get();
                    updateUIWithThreadName("now i parse images from " + Thread.currentThread().getName());
                    image_search(doc);
                    updateUIWithThreadName("now i search for links in " + Thread.currentThread().getName());
                    links_search(doc);
                } catch (Exception e) {
                    updateUIWithThreadName(e.getMessage());
                    e.printStackTrace();
                    //Log.d()
                }
            }
        });
        newThread.setName(getResources().getString(R.string.super_thread_title) + String.valueOf(COUNT_THREADS));
        threadQueue.add(newThread);
        threadQueue.peek().start();
        updateUINameThread(threadQueue.peek().getName());
    }

    private void updateUINameThread(final String name) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                status_text.append(name + "\n");
            }
        });
    }

    private void  updateUIWithThreadName(final String name) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                status_text.append(name + "\n");
                imageAdapter.notifyDataSetChanged();
            }
        });
    }

    private void image_search(Document document) {
        Elements img = document.getElementsByTag(getResources().getString(R.string.image_tag));
        for (Element el : img){
            urls.add(getResources().getString(R.string.main_url) + el.attr(getResources().getString(R.string.src_tag)));
        }
        updateUIWithThreadName("parse completed at " + Thread.currentThread().getName());
        updateUIWithThreadName("now threadqueue count is = " + String.valueOf(threadQueue.size()));

        threadQueue.remove(Thread.currentThread());

    }

    private void links_search(Document document) throws InterruptedException {
        Elements href = document.getElementsByTag(getResources().getString(R.string.newlink_tag));
        for (Element el : href) {
            if (!el.attr(getResources().getString(R.string.href_tag)).contains(".jpg")) {
                startParseNewPage(getResources().getString(R.string.main_url) + el.attr(getResources().getString(R.string.href_tag)));
            }
        }
    }
}
