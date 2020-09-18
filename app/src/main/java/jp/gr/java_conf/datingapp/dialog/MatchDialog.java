package jp.gr.java_conf.datingapp.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.gr.java_conf.datingapp.ChatActivity;
import jp.gr.java_conf.datingapp.HomeActivity;
import jp.gr.java_conf.datingapp.R;
import jp.gr.java_conf.datingapp.model.Profile;

public class MatchDialog {

    public static int REQUEST_CODE = 202;

    public static void showDialog(Context context, Profile profile, String imageUrl, String sex) {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.match_done);
        dialog.findViewById(R.id.later_message).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.findViewById(R.id.now_message).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (profile != null) {
                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.putExtra("profile", profile);
                    intent.putExtra("doc_id", profile.getUser_id());
                    intent.putExtra("user_img", profile.getImg_url());
                    intent.putExtra("user_name", profile.getName());
                    intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                    ((Activity)context).startActivityForResult(intent, REQUEST_CODE);
                    dialog.dismiss();
                }
            }
        });
        if (sex != null && sex.equals("man")) {
            if (imageUrl != null) {
                Glide.with(context).load(imageUrl).into((CircleImageView)dialog.findViewById(R.id.man_image));
            } else {
                ((CircleImageView)dialog.findViewById(R.id.man_image)).setImageDrawable(context.getResources().getDrawable(R.drawable.avatornew));
            }
            if (profile.getImg_url() != null) {
                Glide.with(context).load(profile.getImg_url()).into((CircleImageView)dialog.findViewById(R.id.woman_image));
            } else {
                ((CircleImageView)dialog.findViewById(R.id.woman_image)).setImageDrawable(context.getResources().getDrawable(R.drawable.avatornew));
            }
        } else if (sex != null && sex.equals("woman")){
            if (imageUrl != null) {
                Glide.with(context).load(imageUrl).into((CircleImageView)dialog.findViewById(R.id.woman_image));
            } else {
                ((CircleImageView)dialog.findViewById(R.id.woman_image)).setImageDrawable(context.getResources().getDrawable(R.drawable.avatornew));
            }

            if (profile.getImg_url() != null) {
                Glide.with(context).load(profile.getImg_url()).into((CircleImageView)dialog.findViewById(R.id.man_image));
            } else {
                ((CircleImageView)dialog.findViewById(R.id.man_image)).setImageDrawable(context.getResources().getDrawable(R.drawable.avatornew));
            }
        }

        if (profile.getName() != null) {
            TextView name = dialog.findViewById(R.id.match_user_name);
            name.setText(profile.getName());
        }
        dialog.show();
    }
}
