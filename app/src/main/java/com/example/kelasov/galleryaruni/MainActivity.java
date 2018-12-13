package com.example.kelasov.galleryaruni;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.NoSuchElementException;

public class MainActivity extends AppCompatActivity implements ImagesAdapter.EventItemClickListener{

    private static final String TAG = MainActivity.class.getSimpleName();

    private RecyclerView mRecyclerView;

    private CustomArFragment arFragment;
    private TransformableNode recyclerTransformableNode;
    private TransformableNode selectedImageTransformableNode;
    private ImageView mSelectedImageView;

    private boolean isImageSelected=false;

    private Cursor mFilesCursor;

    private String [] projection = {
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.DATA
    };
    private String  mSelection =
            MediaStore.Files.FileColumns.MEDIA_TYPE + "=" +
                    MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

    private String mSortOrder = MediaStore.Files.FileColumns.DATE_ADDED + " DESC";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate: " +MediaStore.Files.FileColumns.DATA);

        initArFragment();
        findViewsById();

        mFilesCursor = queryImages();



        if(mFilesCursor!=null)
            Log.d(TAG, "onCreate: Cursor size :: " + mFilesCursor.getCount());

        initRecyclerView();

        placeRecycleViewInWorld();


    }

    private Cursor queryImages() {
        return getContentResolver().query(
                MediaStore.Files.getContentUri("external"),
                projection,
                mSelection,
                null,
                mSortOrder
        );
    }

    private void initArFragment() {
        arFragment = (CustomArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        arFragment.getPlaneDiscoveryController().hide();
        arFragment.getPlaneDiscoveryController().setInstructionView(null);
        arFragment.setOnTapArPlaneListener(new BaseArFragment.OnTapArPlaneListener() {
            @Override
            public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {

                Anchor anchor = hitResult.createAnchor();
                AnchorNode anchorNode = new AnchorNode(anchor);

                placeRecycleViewInWorld();
            }
        });
        }

    private void placeRecycleViewInWorld() {
        if(mRecyclerView.getParent()!=null){
            ((ViewGroup)(mRecyclerView.getParent())).removeView(mRecyclerView);
        }


        Node cameraNode = new Node();
        cameraNode.setWorldPosition(arFragment.getArSceneView().getScene().getCamera().getWorldPosition());
        AnchorNode cameraAnchorNode = new AnchorNode();
        cameraAnchorNode.setWorldPosition(cameraNode.getWorldPosition());

        Anchor anchor= cameraAnchorNode.getAnchor();

        mRecyclerView.setVisibility(View.VISIBLE);


        ViewRenderable.builder()
                .setView(this,mRecyclerView)
                .build()
                .thenAccept(viewRenderable -> {
                    addRecyclerViewToScene(viewRenderable,anchor);
                });
    }

    private void addRecyclerViewToScene(ViewRenderable viewRenderable,Anchor anchor) {
            AnchorNode anchorNode = new AnchorNode(anchor);

            recyclerTransformableNode = new TransformableNode(arFragment.getTransformationSystem());
            recyclerTransformableNode.setRenderable(viewRenderable);
            recyclerTransformableNode.setParent(anchorNode);
            recyclerTransformableNode.setLocalPosition(new Vector3(-0.5f,-1.2f,-4));

            arFragment.getArSceneView().getScene().addChild(recyclerTransformableNode);
            recyclerTransformableNode.select();
            showToast("added");
    }

    private void findViewsById(){
        //arFragment.getPlaneDiscoveryController().hide();
        //arFragment.getPlaneDiscoveryController().setInstructionView(null);
        mRecyclerView = findViewById(R.id.images_rv);
        mSelectedImageView = findViewById(R.id.selected_image);
    }

    private void initRecyclerView() {
        GridLayoutManager linearLayoutManager = new GridLayoutManager(this,3);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        ImagesAdapter imagesAdapter = new ImagesAdapter(mFilesCursor,MainActivity.this,this);
        mRecyclerView.setAdapter(imagesAdapter);
        mRecyclerView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onEventItemClickListener(View clickedView, int position) {
        if(mFilesCursor==null){
            return;
        }

        if(isImageSelected)
        {
             showSelectedImage(position);
            return;
        }
        isImageSelected = true;
        mFilesCursor.moveToPosition(position);
        createRenderable(Uri.parse(mFilesCursor.getString(mFilesCursor.getColumnIndex(MediaStore.Files.FileColumns.DATA))));
    }

    private void showSelectedImage(int position) {
        mFilesCursor.moveToPosition(position);
        String imageFilePath = mFilesCursor.getString(mFilesCursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));

        // imageView resize code.
        {
            //BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
            //bitmapOptions.inJustDecodeBounds = true;
            //BitmapFactory.decodeFile(imageFilePath,bitmapOptions);
            //int imageWidth = bitmapOptions.outWidth;
            //int imageHeight = bitmapOptions.outHeight;

            //LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mSelectedImageView.getLayoutParams();
            //params.width = imageWidth;
            //params.height = imageHeight;
            //showToast("width : " + imageWidth + " height : " + imageHeight);
            //mSelectedImageView.setLayoutParams(params);
            //bitmapOptions.inJustDecodeBounds = false;
        }


        Glide.with(MainActivity.this)
                .load(imageFilePath)
                .into(mSelectedImageView);
    }

    private void createRenderable(Uri imageUri) {
        Bitmap bm = getBitmapFromUri(imageUri);

        mSelectedImageView.setImageBitmap(bm);

        if(mSelectedImageView.getParent()!=null){
            ((ViewGroup)(mSelectedImageView.getParent())).removeView(mSelectedImageView);
        }
        ViewRenderable.builder()
                .setView(this,mSelectedImageView)
                .build()
                .thenAccept(viewRenderable -> {
                    placeSelectedImageToScene(viewRenderable);
                })
                .exceptionally(throwable -> {
                    Log.d(TAG, "createRenderable: ERROR :: ",throwable);
                    return null;
                });


    }

    private Bitmap getBitmapFromUri(Uri imageUri) {
        try{
            Bitmap bm;
            String path = imageUri.getPath();
            bm = BitmapFactory.decodeFile(path);

            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);

            final int height = options.outHeight;
            final int width = options.outWidth;

            bm = Bitmap.createScaledBitmap(bm,width,height,false);
            return bm;
        }
        catch (NullPointerException e){
            Log.d(TAG, "getBitmapFromUri: ERROR",e);
            showToast("NullPointerException" + e.toString());
        }
        return null;
    }

    private void placeSelectedImageToScene(ViewRenderable viewRenderable) {

        AnchorNode anchorNode = new AnchorNode();
        anchorNode.setParent(recyclerTransformableNode);

        selectedImageTransformableNode = new TransformableNode(arFragment.getTransformationSystem());
        selectedImageTransformableNode.setRenderable(viewRenderable);
        selectedImageTransformableNode.setParent(anchorNode);
        selectedImageTransformableNode.setLocalRotation(recyclerTransformableNode.getWorldRotation());
        selectedImageTransformableNode.setLocalPosition(new Vector3(
                recyclerTransformableNode.getRight().x+0.2f,
                ((recyclerTransformableNode.getUp().y+recyclerTransformableNode.getDown().y)/2)-1.2f,
                recyclerTransformableNode.getForward().z-4));

        arFragment.getArSceneView().getScene().addChild(selectedImageTransformableNode);
        selectedImageTransformableNode.select();

    }

    private void showToast(String text) {
        Toast.makeText(this,text,Toast.LENGTH_LONG).show();
    }

}
