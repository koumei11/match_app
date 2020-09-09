package jp.gr.java_conf.datingapp.utility;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import jp.gr.java_conf.datingapp.R;
import jp.gr.java_conf.datingapp.enums.ImageEnum;
import jp.gr.java_conf.datingapp.interfaces.IImagePickerLister;

import static jp.gr.java_conf.datingapp.ImageActivity.CAMERA_STORAGE_REQUEST_CODE;
import static jp.gr.java_conf.datingapp.ImageActivity.ONLY_CAMERA_REQUEST_CODE;
import static jp.gr.java_conf.datingapp.ImageActivity.ONLY_STORAGE_REQUEST_CODE;

public class UiHelper {

    public void showImagePickerDialog(@NonNull Context context, IImagePickerLister imagePickerLister) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                ((Activity) context).finish();
            }
        }).setItems(context.getResources().getStringArray(R.array.image_picks), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int position) {
                System.out.println(position);
                if (position == 0) {
                    imagePickerLister.onOptionSelected(ImageEnum.FROM_CAMERA);
                } else if (position == 1) {
                    imagePickerLister.onOptionSelected(ImageEnum.FROM_GALLERY);
                }
                dialogInterface.dismiss();
            }
        });

        builder.create().show();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean checkSelfPermissions(@NonNull Activity activity) {
        if (activity.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAMERA_STORAGE_REQUEST_CODE);
            return false;
        } else if (activity.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, ONLY_CAMERA_REQUEST_CODE);
            return false;
        } else if (activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, ONLY_STORAGE_REQUEST_CODE);
            return false;
        }
        return true;
    }
}
