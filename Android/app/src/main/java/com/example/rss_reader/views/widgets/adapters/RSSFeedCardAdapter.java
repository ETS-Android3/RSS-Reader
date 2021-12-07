package com.example.rss_reader.views.widgets.adapters;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rss_reader.R;
import com.example.rss_reader.models.RSSFeed;
import com.example.rss_reader.utils.Converter;
import com.example.rss_reader.utils.RSSDialogFactory;
import com.example.rss_reader.utils.RSSToastFactory;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;


public class RSSFeedCardAdapter extends RecyclerView.Adapter<RSSFeedCardAdapter.ViewHolder> {

    private final ArrayList<RSSFeed> set;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final CardView rssCard;
        private final TextView url;
        private final MaterialButton delete;
        private boolean tapped = false;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            rssCard = view.findViewById(R.id.rss_feed_cardView);
            url = view.findViewById(R.id.url_rss_feed_card);
            delete = view.findViewById(R.id.delete_rss_feed_card);
        }

        public CardView getCardView() {
            return rssCard;
        }

        public TextView getUrl() {
            return url;
        }

        public MaterialButton getDelete() {
            return delete;
        }

        public boolean isTapped() {
            return tapped;
        }

        public void setTapped(boolean tapped) {
            this.tapped = tapped;
        }
    }

    public RSSFeedCardAdapter(ArrayList<RSSFeed> set) {
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
                .inflate(R.layout.rss_feed_card, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, @SuppressLint("RecyclerView") int position) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.getUrl().setText(set.get(position).getUrl());

        viewHolder.getDelete().setOnClickListener(v -> new AlertDialog.Builder(v.getContext(), R.style.DialogFactory).setMessage("Are you sure to remove this rss feed").setPositiveButton("Yes", (dialog, which) -> {
            dialog.dismiss();
            final AlertDialog loading = RSSDialogFactory.createDialog(RSSDialogFactory.RSSDialog.Loading, v.getContext(), null);
            Objects.requireNonNull(loading).show();

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("RSS");
            reference.child("users").child(set.get(position).getUser()).child(Converter.urlToPath(set.get(position).getUrl())).setValue(null, (error, ref) -> {
                if (error == null) {
                    remove(position);

                    loading.dismiss();
                } else {
                    loading.dismiss();

                    Objects.requireNonNull(RSSToastFactory.createToast(RSSToastFactory.RSSToast.Information, v.getContext(), "Remove file failed!")).show();
                }
            });
        }).setNegativeButton("No", (dialog, which) -> dialog.dismiss()).create().show());

        viewHolder.getCardView().setOnClickListener(v -> {
            if (viewHolder.isTapped()) {
                viewHolder.getUrl().setLines(3);
                viewHolder.setTapped(false);
            } else {
                viewHolder.getUrl().setMaxLines(10);
                viewHolder.getUrl().setMinLines(3);
                viewHolder.setTapped(true);
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
}
