package com.example.kelasov.galleryaruni;

import android.app.Activity;
import android.content.Context;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;

import java.util.NoSuchElementException;

public class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.ImagesViewHolder> {

    private final String TAG = ImagesAdapter.class.getSimpleName();
    private EventItemClickListener mOnClickListener;
    private Cursor mMediaStoreCursor;
    private final Activity mActivity;


        ImagesAdapter(Cursor cursor,Activity activity, EventItemClickListener listener){
            Log.d(TAG, "ImagesAdapter: Constructor :: ENTERED");
            mOnClickListener = listener;
            mMediaStoreCursor = cursor;
            mActivity = activity;
        }

        @NonNull
        @Override
        public ImagesAdapter.ImagesViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            Context context = viewGroup.getContext();
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            View view = layoutInflater.inflate(R.layout.image_view_item_list,viewGroup,false);

            return new ImagesViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ImagesAdapter.ImagesViewHolder imagesViewHolder, int position) {
            imagesViewHolder.bind(position);
        }

        @Override
        public int getItemCount() {
            return (mMediaStoreCursor == null) ? 0 : mMediaStoreCursor.getCount();
        }


        class ImagesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            ImageView imageView;

            ImagesViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView=itemView.findViewById(R.id.imageView);
                itemView.setOnClickListener(this);
            }

            void bind(int position) {
                mMediaStoreCursor.moveToPosition(position);
                String imageFilePath = mMediaStoreCursor.getString(mMediaStoreCursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
                Log.d(TAG, "bind: position :: " + position + " path :: " + imageFilePath);
                Glide.with(mActivity)
                        .load(imageFilePath)
                        .into(imageView);
            }

            @Override
            public void onClick(View v) {
                int adapterPosition =getAdapterPosition();
                mOnClickListener.onEventItemClickListener(v,adapterPosition);
            }


        }



    public interface EventItemClickListener{
            void onEventItemClickListener(View clickedEvent,int position);
        }





}
