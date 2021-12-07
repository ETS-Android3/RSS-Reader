package com.example.rss_reader.views.widgets.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.text.HtmlCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rss_reader.R;
import com.example.rss_reader.databases.InternalStorageHandler;
import com.example.rss_reader.databases.RSSSQLiteHandler;
import com.example.rss_reader.models.RSSArticle;
import com.example.rss_reader.models.RSSPost;
import com.example.rss_reader.networking.repositories.PostRepository;
import com.example.rss_reader.utils.Converter;
import com.example.rss_reader.utils.RSSDialogFactory;
import com.example.rss_reader.utils.RSSToastFactory;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class RSSCardAdapter extends RecyclerView.Adapter<RSSCardAdapter.ViewHolder> {

    private final ArrayList<RSSArticle> set;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final CardView rssCard;
        private final ShapeableImageView rssImage;
        private final TextView title;
        private final TextView time;
        private final TextView description;
        private final MaterialButton save;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            rssCard = view.findViewById(R.id.rss_card);
            rssImage = view.findViewById(R.id.rss_image);
            title = view.findViewById(R.id.title_rss_card);
            time = view.findViewById(R.id.time_rss_card);
            description = view.findViewById(R.id.description_rss_card);
            save = view.findViewById(R.id.bookmark_rss_card);
        }

        public TextView getTitle() {
            return title;
        }


        public TextView getTime() {
            return time;
        }

        public CardView getCardView() {
            return rssCard;
        }

        public TextView getDescription() {
            return description;
        }

        public MaterialButton getSave() {
            return save;
        }

        public ShapeableImageView getRssImage() {
            return rssImage;
        }
    }

    public RSSCardAdapter(ArrayList<RSSArticle> set) {
        this.set = set;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.rss_card, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, @SuppressLint("RecyclerView") int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.getTitle().setText(HtmlCompat.fromHtml(set.get(position).getTitle(), HtmlCompat.FROM_HTML_OPTION_USE_CSS_COLORS));
        try {
            viewHolder.getTime().setText(Converter.timeDifToPresent(set.get(position).getPubDate()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        viewHolder.getDescription().setText(HtmlCompat.fromHtml(set.get(position).getDescription(), HtmlCompat.FROM_HTML_OPTION_USE_CSS_COLORS, (source) -> {
            new RSSCardImageLoad().execute(viewHolder.getRssImage(), source);
            return null;
        }, null).toString().replace("￼", "").replace("\n", ""));
        viewHolder.getSave().setOnClickListener(v -> new AlertDialog.Builder(v.getContext(), R.style.DialogFactory).setMessage("Are you sure to save this article").setPositiveButton("Yes", (dialog, which) -> {
            dialog.dismiss();

            final AlertDialog loading = RSSDialogFactory.createDialog(RSSDialogFactory.RSSDialog.Loading, v.getContext(), null);
            Objects.requireNonNull(loading).show();

            try {
                String url;
                if (set.get(position).getLink() != null)
                    url = set.get(position).getLink();
                else
                    url = set.get(position).getGuid();

                Log.i("Download", url);
                PostRepository.getInstance(url).initializeService().getArticle().enqueue(new Callback<String>() {
                    @SuppressLint("UseCompatLoadingForDrawables")
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.isSuccessful()) {
                            RSSPost post = new RSSPost(set.get(position), response.body());
                            if (RSSSQLiteHandler.getInstance(v.getContext()).saveRSS(v.getContext(), post)) {
                                viewHolder.getSave().setIcon(v.getResources().getDrawable(R.drawable.ic_baseline_cloud_done_24, v.getContext().getTheme()));
                                viewHolder.getSave().setIconTint(ColorStateList.valueOf(v.getResources().getColor(R.color.primary, v.getContext().getTheme())));
                                loading.dismiss();
                                Objects.requireNonNull(RSSToastFactory.createToast(RSSToastFactory.RSSToast.Information, v.getContext(), "Downloaded!")).show();
                            } else {
                                loading.dismiss();
                                Objects.requireNonNull(RSSToastFactory.createToast(RSSToastFactory.RSSToast.Information, v.getContext(), "Download failed!")).show();

                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        Log.e("Download failed", t.getMessage());

                        loading.dismiss();
                        Objects.requireNonNull(RSSToastFactory.createToast(RSSToastFactory.RSSToast.Information, v.getContext(), "Download article failed!")).show();
                    }
                });
            } catch (Exception e) {
                loading.dismiss();
                e.printStackTrace();

                Objects.requireNonNull(RSSToastFactory.createToast(RSSToastFactory.RSSToast.Information, v.getContext(), "Download article failed!")).show();
            }
        }).setNegativeButton("No", (dialog, which) -> dialog.dismiss()).create().show());
        viewHolder.getCardView().setOnClickListener(v -> {
            String url;
            if (set.get(position).getLink() != null)
                url = set.get(position).getLink();
            else
                url = set.get(position).getGuid();

//            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//            v.getContext().startActivity(browserIntent);
            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext(), R.style.FullDialog);
            View rssPostDialogView = ((LayoutInflater) v.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.rss_post, null);
            builder.setView(rssPostDialogView).setCancelable(true);

            WebView content = rssPostDialogView.findViewById(R.id.rss_post_webView);
            MaterialButton close = rssPostDialogView.findViewById(R.id.close_rssPost);
            FloatingActionButton top = rssPostDialogView.findViewById(R.id.goTop_rssPost);
            NestedScrollView nestedScrollView = rssPostDialogView.findViewById(R.id.rssPost_nestedScrollView);

            WebSettings settings = content.getSettings();

            settings.setBuiltInZoomControls(true);
            settings.setDomStorageEnabled(true);
            settings.setJavaScriptEnabled(true);
            settings.setAppCacheEnabled(true);
            settings.setAppCachePath(v.getContext().getApplicationContext().getCacheDir().getPath());
            settings.setCacheMode(WebSettings.LOAD_DEFAULT);
            content.setWebViewClient(new WebViewClient());
            content.loadUrl(url);

//                content.loadUrl(set.get(position).getRSSArticle().getLink());

            AlertDialog dialog = builder.create();

            top.setOnClickListener(v12 -> nestedScrollView.smoothScrollTo(0, 0, 700));

            close.setOnClickListener(v1 -> dialog.dismiss());

            dialog.show();
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return set.size();
    }

    private static class RSSCardImageLoad extends AsyncTask<Object, Void, ArrayList<Object>> {
        @Override
        protected ArrayList<Object> doInBackground(Object... objects) {
            String source = (String) objects[1];
            ImageView image = (ImageView) objects[0];
            try {
                Bitmap bmp = BitmapFactory.decodeStream(new URL(source).openConnection().getInputStream());

                ArrayList<Object> results = new ArrayList<>();
                results.add(image);
                results.add(new BitmapDrawable(image.getResources(), bmp));
                return results;
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Object> objects) {
            try {
                ((ImageView) objects.get(0)).setImageDrawable((Drawable) objects.get(1));
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }
}
