package com.example.rvip2;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class ImageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    List<View> headers = new ArrayList<>();

    public static final int TYPE_HEADER = 111;
    public static final int TYPE_ITEM = 333;


    Activity activity;

    ArrayList<String> urls;

    String _url;

    public ImageAdapter(Activity activity, ArrayList<String> urls) {
        this.activity = activity;

        this.urls = urls;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
            ImageViewHolder imageViewHolder = new ImageViewHolder(v);
            return imageViewHolder;
        } else {
            FrameLayout frameLayout = new FrameLayout(parent.getContext());
            frameLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            return new HeaderFooterViewHolder(frameLayout);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof ImageViewHolder) {
            ((ImageViewHolder) holder).url.setText(urls.get(position - 1));
            _url = urls.get(position - 1);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Bitmap bitmap = getBitmapFromURL(urls.get(position - 1 ));
                    onUi(bitmap, ((ImageViewHolder) holder));
                }
            }).start();
        } else {

            View v = headers.get(position);
            prepareHeaderFooter((HeaderFooterViewHolder) holder, v);
        }
    }

    private void onUi(final Bitmap bitmap, final ImageViewHolder holder) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                holder.imageView.setImageBitmap(bitmap);
            }
        });
    }

    private class ImageViewHolder extends RecyclerView.ViewHolder {

        TextView url;
        ImageView imageView;
        public ImageViewHolder(View itemView) {
            super(itemView);

            url = (TextView) itemView.findViewById(R.id.url_image);
            imageView = (ImageView) itemView.findViewById(R.id.image);

        }
    }


    private class HeaderViewHolder extends RecyclerView.ViewHolder {

        public HeaderViewHolder(View itemView) {
            super(itemView);
        }
    }

    public  Bitmap getBitmapFromURL(String srcurl){
        try {
            URL url = new URL(srcurl);
            HttpURLConnection connection=(HttpURLConnection)url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input=connection.getInputStream();
            Bitmap myBitmap=BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    @Override
    public int getItemCount() {
        return headers.size() + urls.size();
    }

    private void prepareHeaderFooter(HeaderFooterViewHolder vh, View view){
        vh.base.removeAllViews();
        vh.base.addView(view);

    }

    @Override
    public int getItemViewType(int position) {

        if(position < headers.size()){
            return TYPE_HEADER;
        }
        return TYPE_ITEM;
    }

    public void addHeader(View header){
        if(!headers.contains(header)){
            headers.add(header);
            notifyItemInserted(headers.size()-1);
        }
    }

    public void removeHeader(View header){
        if(headers.contains(header)){
            notifyItemRemoved(headers.indexOf(header));
            headers.remove(header);
        }
    }

    public static class HeaderFooterViewHolder extends RecyclerView.ViewHolder{
        FrameLayout base;
        public HeaderFooterViewHolder(View itemView) {
            super(itemView);
            this.base = (FrameLayout) itemView;
        }
    }

}
