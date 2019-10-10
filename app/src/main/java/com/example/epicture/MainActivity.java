package com.example.epicture;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity
{
    private static final String TAG = MainActivity.class.getSimpleName();
    private OkHttpClient httpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fetchData();
    }

    private void fetchData() {
        httpClient = new OkHttpClient.Builder().build();

        Request request = new Request.Builder()
                .url("https://api.imgur.com/3/gallery/user/rising/0.json")
                .header("Authorization","Client-ID 4ec810250ea92fb")
                .header("User-Agent","Epicture")
                .build();

        httpClient.newCall(request).enqueue(new Callback()
        {
            @Override
            public void onFailure(Call call, IOException e)
            {
                Log.e(TAG, "An error has occurred " + e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException
            {
                try
                {
                    JSONObject data = new JSONObject(response.body().string());
                    JSONArray items = data.getJSONArray("data");
                    final List<Photo> photos = new ArrayList<>();

                    for (int i = 0; i < items.length(); i++)
                    {
                        JSONObject item = items.getJSONObject(i);
                        Photo photo = new Photo();
                        if(item.getBoolean("is_album")) {
                            photo.id = item.getString("cover");
                        } else {
                            photo.id = item.getString("id");
                        }
                        photo.title = item.getString("title");

                        photos.add(photo);

                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                render(photos);
                            }
                        });
                    }

                } catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    private static class PhotoVH extends RecyclerView.ViewHolder {
        AppCompatImageView photo;
        AppCompatTextView title;

        public PhotoVH(View itemView) {
            super(itemView);
        }
    }

    private void render(final List<Photo> photos)
    {
        RecyclerView recyclerView = findViewById(R.id.rv_of_photos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        RecyclerView.Adapter<PhotoVH> adapter = new RecyclerView.Adapter<PhotoVH>()
        {
            @NonNull
            @Override
            public PhotoVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                PhotoVH vh = new PhotoVH(getLayoutInflater().inflate(R.layout.item, null));
                vh.photo = vh.itemView.findViewById(R.id.photo);
                vh.title = vh.itemView.findViewById(R.id.title);
                return vh;
            }

            @Override
            public void onBindViewHolder(@NonNull PhotoVH holder, int position)
            {
                Picasso.with(MainActivity.this)
                        .load("https://i.imgur.com/" + photos.get(position).id + ".jpg")
                        .into(holder.photo);
                holder.title.setText(photos.get(position).title);
            }

            @Override
            public int getItemCount()
            {
                return photos.size();
            }
        };

        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration()
        {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state)
            {
                outRect.bottom = 16;
            }
        });
    }

    private static class Photo {
        String id;
        String title;
    }
}
