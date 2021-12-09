package com.example.rss_reader.views.widgets.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.text.HtmlCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rss_reader.R;
import com.example.rss_reader.databases.InternalStorageHandler;
import com.example.rss_reader.databases.RSSSQLiteHandler;
import com.example.rss_reader.models.RSSPost;
import com.example.rss_reader.utils.Converter;
import com.example.rss_reader.utils.RSSDialogFactory;
import com.example.rss_reader.utils.RSSToastFactory;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Objects;


public class RSSArchiveCardAdapter extends RecyclerView.Adapter<RSSArchiveCardAdapter.ViewHolder> {

    private final ArrayList<RSSPost> set;

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
        private final MaterialButton delete;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            rssCard = view.findViewById(R.id.rss_archive_card);
            rssImage = view.findViewById(R.id.rss_archive_image);
            title = view.findViewById(R.id.title_rss_archive_card);
            time = view.findViewById(R.id.time_rss_archive_card);
            description = view.findViewById(R.id.description_rss_archive_card);
            delete = view.findViewById(R.id.delete_rss_archive_card);
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

        public MaterialButton getDelete() {
            return delete;
        }

        public ShapeableImageView getRssImage() {
            return rssImage;
        }
    }

    public RSSArchiveCardAdapter(ArrayList<RSSPost> set) {
        this.set = set;
    }

    public void remove(int position) {
        set.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, set.size());
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.rss_archive_card, viewGroup, false);

        view.setClickable(true);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, @SuppressLint("RecyclerView") int position) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.getTitle().setText(HtmlCompat.fromHtml(set.get(position).getRSSArticle().getTitle(), HtmlCompat.FROM_HTML_OPTION_USE_CSS_COLORS));
        try {
            viewHolder.getTime().setText(Converter.timeDifToPresent(set.get(position).getRSSArticle().getPubDate()));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Spanned spanned = HtmlCompat.fromHtml(set.get(position).getRSSArticle().getDescription(), HtmlCompat.FROM_HTML_OPTION_USE_CSS_COLORS, (source) -> {
            new RSSPostLoadImage().execute(viewHolder.getRssImage(), source);
            return null;
        }, null);

        viewHolder.getDescription().setText(spanned.toString().replace("￼", "").replace("\n", ""));

        viewHolder.getDelete().setOnClickListener(v -> new AlertDialog.Builder(v.getContext(), R.style.DialogFactory).setMessage("Are you sure to remove this article").setPositiveButton("Yes", (dialog, which) -> {
            dialog.dismiss();

            final AlertDialog loading = RSSDialogFactory.createDialog(RSSDialogFactory.RSSDialog.Loading, v.getContext(), null);
            Objects.requireNonNull(loading).show();

            if (RSSSQLiteHandler.getInstance(v.getContext()).removeRSS(v.getContext(), set.get(position))) {
                remove(position);

                loading.dismiss();
            } else {
                loading.dismiss();

                Toast.makeText(v.getContext(), "Remove file failed!", Toast.LENGTH_SHORT).show();
            }
        }).setNegativeButton("No", (dialog, which) -> dialog.dismiss()).create().show());

        viewHolder.getCardView().setOnClickListener(v -> {

            final AlertDialog loading = RSSDialogFactory.createDialog(RSSDialogFactory.RSSDialog.Loading, v.getContext(), null);
            Objects.requireNonNull(loading).show();

            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext(), R.style.FullDialog);
            View rssPostDialogView = ((LayoutInflater) v.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.rss_post, null);
            builder.setView(rssPostDialogView).setCancelable(true);

            WebView content = rssPostDialogView.findViewById(R.id.rss_post_webView);
            MaterialButton close = rssPostDialogView.findViewById(R.id.close_rssPost);
            FloatingActionButton top = rssPostDialogView.findViewById(R.id.goTop_rssPost);
            NestedScrollView nestedScrollView = rssPostDialogView.findViewById(R.id.rssPost_nestedScrollView);

            String data = InternalStorageHandler.readHTML(v.getContext(), set.get(position).getHtml());
            if (data != null) {
                WebSettings settings = content.getSettings();

                settings.setBuiltInZoomControls(true);
                settings.setDomStorageEnabled(true);
                settings.setJavaScriptEnabled(true);
                settings.setAppCacheEnabled(true);
                settings.setAppCachePath(v.getContext().getApplicationContext().getCacheDir().getPath());
                settings.setCacheMode(WebSettings.LOAD_DEFAULT);
                content.setWebViewClient(new WebViewClient());
                if (isNetworkAvailable(v.getContext())) {
                    URL url;
                    try {
                        url = new URL(set.get(position).getRSSArticle().getLink());
                    } catch (MalformedURLException e) {
                        e.printStackTrace();

                        try {
                            url = new URL(set.get(position).getRSSArticle().getGuid());
                        } catch (MalformedURLException malformedURLException) {
                            malformedURLException.printStackTrace();
                            loading.dismiss();

                            Objects.requireNonNull(RSSToastFactory.createToast(RSSToastFactory.RSSToast.Information, v.getContext(), "Load file failed!")).show();
                            return;
                        }
                    }

                    content.loadUrl(url.toString());
                } else
                    content.loadDataWithBaseURL(null, data, "text/html", "UTF-8", null);


//                content.loadDataWithBaseURL(url.getProtocol() + "://" + url.getHost() + "/", data, "text/html", "UTF-8", null);
//                content.loadUrl(set.get(position).getRSSArticle().getLink());

                AlertDialog dialog = builder.create();

                top.setOnClickListener(v12 -> nestedScrollView.smoothScrollTo(0, 0, 700));

                close.setOnClickListener(v1 -> dialog.dismiss());

                loading.dismiss();

                dialog.show();
            } else {
                loading.dismiss();

                Objects.requireNonNull(RSSToastFactory.createToast(RSSToastFactory.RSSToast.Information, v.getContext(), "Load file failed!")).show();
            }
        });

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return set.size();
    }

    private static class RSSPostLoadImage extends AsyncTask<Object, Void, ArrayList<Object>> {
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

    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
