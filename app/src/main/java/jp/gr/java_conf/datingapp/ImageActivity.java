package jp.gr.java_conf.datingapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;

import jp.gr.java_conf.datingapp.enums.ImageEnum;
import jp.gr.java_conf.datingapp.interfaces.IImagePickerLister;
import jp.gr.java_conf.datingapp.utility.UiHelper;

public class ImageActivity extends AppCompatActivity implements IImagePickerLister {
    private static final int CAMERA_ACTION_PICK_REQUEST_CODE = 610;
    private static final int PICK_IMAGE_GALLERY_REQUEST_CODE = 609;
    public static final int CAMERA_STORAGE_REQUEST_CODE = 611;
    public static final int ONLY_CAMERA_REQUEST_CODE = 612;
    public static final int ONLY_STORAGE_REQUEST_CODE = 613;

    private String currentPhotoPath = "";
    private UiHelper uiHelper = new UiHelper();
    private boolean isValidFlow = true;
    private boolean isFromChat;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        isValidFlow = true;

        Intent intent = getIntent();
        isFromChat = intent.getBooleanExtra("fromChatActivity", false);

        if (!isFromChat) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                if (uiHelper.checkSelfPermissions(this))
                    uiHelper.showImagePickerDialog(this, this);
        } else {
            if (intent.getIntExtra("requestCode", 0) == 100) {
                openCamera();
            } else if (intent.getIntExtra("requestCode", 0) == 200) {
                openImagesDocument();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_STORAGE_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)
                uiHelper.showImagePickerDialog(this, this);
            else if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, getResources().getString(R.string.storage_denied), Toast.LENGTH_LONG).show();
                finish();
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, getResources().getString(R.string.camera_denied), Toast.LENGTH_LONG).show();
                finish();
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED && grantResults[1] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, getResources().getString(R.string.both_denied), Toast.LENGTH_LONG).show();
                finish();
            }
        } else if (requestCode == ONLY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                uiHelper.showImagePickerDialog(this, this);
            else {
                Toast.makeText(this, getResources().getString(R.string.camera_denied), Toast.LENGTH_LONG).show();
                finish();
            }
        } else if (requestCode == ONLY_STORAGE_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                uiHelper.showImagePickerDialog(this, this);
            else {
                Toast.makeText(this, getResources().getString(R.string.storage_denied), Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_ACTION_PICK_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri uri = Uri.parse(currentPhotoPath);
            isValidFlow = true;
            if (isFromChat) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("image_uri", uri.toString());
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            } else {
                openCropActivity(uri, uri);
            }
        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            if (data != null) {
                Uri uri = UCrop.getOutput(data);
                Intent resultIntent = new Intent();
                resultIntent.putExtra("image_uri", uri.toString());
                setResult(Activity.RESULT_OK, resultIntent);
                isValidFlow = true;
                finish();
            }
        } else if (requestCode == PICK_IMAGE_GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            try {
                isValidFlow = true;
                Uri sourceUri = data.getData();
                if (isFromChat) {
                    if (sourceUri != null) {
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("image_uri", sourceUri.toString());
                        setResult(Activity.RESULT_OK, resultIntent);
                        finish();
                    }
                } else {
                    File file = getImageFile();
                    Uri destinationUri = Uri.fromFile(file);
                    openCropActivity(sourceUri, destinationUri);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                Toast.makeText(this, getResources().getString(R.string.another_image), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void openImagesDocument() {
        Intent pictureIntent = new Intent(Intent.ACTION_GET_CONTENT);
        pictureIntent.setType("image/*");
        pictureIntent.addCategory(Intent.CATEGORY_OPENABLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            String[] mimeTypes = new String[]{"image/jpeg", "image/png"};
            pictureIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        }
        startActivityForResult(Intent.createChooser(pictureIntent, getResources().getString(R.string.select_image)), PICK_IMAGE_GALLERY_REQUEST_CODE);
    }

    private File getImageFile() throws IOException {
        String imageFileName = "JPEG_" + System.currentTimeMillis() + "_";
        File storageDir = new File(
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DCIM
                ), "Camera"
        );
        if (storageDir.exists())
            System.out.println("ファイルあり");
        else
            System.out.println("ファイルなし");
            storageDir.mkdirs();
        File file = File.createTempFile(
                imageFileName, ".jpg", storageDir
        );
        System.out.println(!file.exists());
        if (!file.exists()) {
            System.out.println("存在しません！");
        }
        currentPhotoPath = "file:" + file.getAbsolutePath();
        return file;
    }


    private void openCropActivity(Uri sourceUri, Uri destinationUri) {
        UCrop.Options options = new UCrop.Options();
        options.setToolbarTitle(getResources().getString(R.string.crop_image));
        options.setHideBottomControls(true);
        UCrop.of(sourceUri, destinationUri)
                .withOptions(options)
                .withAspectRatio(80, 100)
                .start(this);
    }

    @Override
    public void onOptionSelected(ImageEnum imageEnum) {
        if (imageEnum == ImageEnum.FROM_CAMERA)
            openCamera();
        else if (imageEnum == ImageEnum.FROM_GALLERY)
            openImagesDocument();
    }

    private void openCamera() {
        Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file;
        try {
            file = getImageFile();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, getResources().getString(R.string.another_image), Toast.LENGTH_LONG).show();
            return;
        }
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID.concat(".provider"), file);
        } else
            uri = Uri.fromFile(file);
        isValidFlow = true;
        pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(pictureIntent, CAMERA_ACTION_PICK_REQUEST_CODE);
    }


    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("onResume");

        if (!isValidFlow) {
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        System.out.println("onPause");
        isValidFlow = false;
    }
}
