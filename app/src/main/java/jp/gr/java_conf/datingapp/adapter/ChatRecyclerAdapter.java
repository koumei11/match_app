package jp.gr.java_conf.datingapp.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.gr.java_conf.datingapp.R;
import jp.gr.java_conf.datingapp.UserDetailActivity;
import jp.gr.java_conf.datingapp.model.Chat;
import jp.gr.java_conf.datingapp.utility.WeekDayConverter;
import jp.gr.java_conf.datingapp.utility.WindowSizeGetter;

public class ChatRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private List<Chat> mChatList;
    private FirebaseAuth mAuth;
    private SimpleDateFormat sdFormat1 = new SimpleDateFormat("M/d");
    private SimpleDateFormat sdFormat2 = new SimpleDateFormat("yyyy/M/d");
    private SimpleDateFormat sdFormat3 = new SimpleDateFormat("H:mm");

    private Set<Integer> positionSet = new HashSet<>();


    public ChatRecyclerAdapter(Context context, List<Chat> chatList) {
        mContext = context;
        mChatList = chatList;
        mAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        if (viewType == 0) {
            view = LayoutInflater.from(mContext).inflate(R.layout.sender_single_item, parent, false);
            return new SenderViewHolder(view);
        }
        else {
            view = LayoutInflater.from(mContext).inflate(R.layout.receiver_single_item, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Calendar calendarNow = Calendar.getInstance();
        java.sql.Date theDay = new java.sql.Date(mChatList.get(position).getTime_stamp());
        java.sql.Date today = new java.sql.Date(new Date().getTime());
        java.sql.Date yesterday = new java.sql.Date(new Date().getTime() - (1000 * 60 * 60 * 24));
        boolean isToday = theDay.toString().equals(today.toString());
        boolean isYesterday = theDay.toString().equals(yesterday.toString());
//        System.out.println(mChatList.get(position).isFirstMessageOfTheDay());
        if (holder.getItemViewType() == 0) {
            SenderViewHolder senderViewHolder = (SenderViewHolder) holder;
            if (mChatList.get(position).isSeen()) {
                senderViewHolder.seen.setVisibility(View.VISIBLE);
                senderViewHolder.seen.setText(mContext.getString(R.string.seen));
            } else {
                senderViewHolder.seen.setVisibility(View.GONE);
            }
            if (mChatList.get(position).getMessage() !=  null) {
                senderViewHolder.imageView.setVisibility(View.GONE);
                senderViewHolder.mMessage.setVisibility(View.VISIBLE);
                senderViewHolder.mMessage.setText(mChatList.get(position).getMessage());
            } else if (mChatList.get(position).getImg_uri() != null){
                senderViewHolder.mMessage.setVisibility(View.GONE);
                senderViewHolder.imageView.setVisibility(View.VISIBLE);
                Glide.with(mContext).load(mChatList.get(position).getImg_uri()).into(senderViewHolder.imageView);
                senderViewHolder.imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Point windowSize = WindowSizeGetter.getDisplaySize(((Activity)mContext).getWindowManager());
                        final Dialog dialog = new Dialog(mContext);
                        dialog.setContentView(R.layout.dialog_img_preview);
                        ImageView imageView = dialog.findViewById(R.id.dialog_image);
                        imageView.getLayoutParams().width = windowSize.x;
                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.dismiss();
                            }
                        });
                        Glide.with(mContext).load(mChatList.get(position).getImg_uri()).into(imageView);
                        dialog.show();
                    }
                });
            }
            senderViewHolder.messageDate.setText(sdFormat3.format(new Date(mChatList.get(position).getTime_stamp())));
            if (mChatList.get(position).isFirstMessageOfTheDay()) {
                if (isToday) {
                    senderViewHolder.sentDate.setVisibility(View.VISIBLE);
                    senderViewHolder.sentDate.setText(mContext.getString(R.string.today));
                } else if (isYesterday) {
                    senderViewHolder.sentDate.setVisibility(View.VISIBLE);
                    senderViewHolder.sentDate.setText(mContext.getString(R.string.yesterday));
                } else {
                    Calendar calendarSent = Calendar.getInstance();
                    calendarSent.setTime(new Date(mChatList.get(position).getTime_stamp()));
                    calendarNow.setTime(new Date());
                    int sentYear = calendarSent.get(Calendar.YEAR);
                    int thisYear = calendarNow.get(Calendar.YEAR);
                    senderViewHolder.sentDate.setVisibility(View.VISIBLE);
                    String dayOfWeek = WeekDayConverter.convertWeek(new Date(mChatList.get(position).getTime_stamp()));
                    String displayString;
                    if (sentYear == thisYear) {
                        displayString = sdFormat1.format(new Date(mChatList.get(position).getTime_stamp())) + "（" + dayOfWeek + "）";
                        senderViewHolder.sentDate.setText(displayString);
                    } else {
                        displayString = sdFormat2.format(new Date(mChatList.get(position).getTime_stamp())) + "（" + dayOfWeek + "）";
                        senderViewHolder.sentDate.setText(displayString);
                    }
                }
            } else {
                senderViewHolder.sentDate.setVisibility(View.GONE);
            }
        } else {
            ReceiverViewHolder receiverViewHolder = (ReceiverViewHolder) holder;
//            receiverViewHolder.mMessage.setText(mChatList.get(position).getMessage());
            if (mChatList.get(position).getMessage() !=  null) {
                receiverViewHolder.imageView.setVisibility(View.GONE);
                receiverViewHolder.mMessage.setVisibility(View.VISIBLE);
                receiverViewHolder.mMessage.setText(mChatList.get(position).getMessage());
            } else if (mChatList.get(position).getImg_uri() != null){
                receiverViewHolder.mMessage.setVisibility(View.GONE);
                receiverViewHolder.imageView.setVisibility(View.VISIBLE);
                Glide.with(mContext).load(mChatList.get(position).getImg_uri()).into(receiverViewHolder.imageView);
                receiverViewHolder.imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Point windowSize = WindowSizeGetter.getDisplaySize(((Activity)mContext).getWindowManager());
                        final Dialog dialog = new Dialog(mContext);
                        dialog.setContentView(R.layout.dialog_img_preview);
                        ImageView imageView = dialog.findViewById(R.id.dialog_image);
                        imageView.getLayoutParams().width = windowSize.x;
                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.dismiss();
                            }
                        });
                        Glide.with(mContext).load(mChatList.get(position).getImg_uri()).into(imageView);
                        dialog.show();
                    }
                });
            }
            receiverViewHolder.messageDate.setText(sdFormat3.format(new Date(mChatList.get(position).getTime_stamp())));
            if (mChatList.get(position).getProfile().getImg_url() != null) {
                Uri url = Uri.parse(mChatList.get(position).getProfile().getImg_url());
                Glide.with(mContext).load(url).into(receiverViewHolder.mImageView);
            }
            if (mChatList.get(position).isFirstMessageOfTheDay()) {
                if (isToday) {
                    receiverViewHolder.sentDate.setVisibility(View.VISIBLE);
                    receiverViewHolder.sentDate.setText(mContext.getString(R.string.today));
                } else if (isYesterday) {
                    receiverViewHolder.sentDate.setVisibility(View.VISIBLE);
                    receiverViewHolder.sentDate.setText(mContext.getString(R.string.yesterday));
                } else {
                    Calendar calendarSent = Calendar.getInstance();
                    calendarSent.setTime(new Date(mChatList.get(position).getTime_stamp()));
                    calendarNow.setTime(new Date());
                    int sentYear = calendarSent.get(Calendar.YEAR);
                    int thisYear = calendarNow.get(Calendar.YEAR);
                    receiverViewHolder.sentDate.setVisibility(View.VISIBLE);
                    String dayOfWeek = WeekDayConverter.convertWeek(new Date(mChatList.get(position).getTime_stamp()));
                    String displayString;
                    if (sentYear == thisYear) {
                        displayString = sdFormat1.format(new Date(mChatList.get(position).getTime_stamp())) + "（" + dayOfWeek + "）";
                        receiverViewHolder.sentDate.setText(displayString);
                    } else {
                        displayString = sdFormat2.format(new Date(mChatList.get(position).getTime_stamp())) + "（" + dayOfWeek + "）";
                        receiverViewHolder.sentDate.setText(displayString);
                    }
                }
            } else {
                receiverViewHolder.sentDate.setVisibility(View.GONE);
            }

            receiverViewHolder.mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    view.setEnabled(false);
                    Intent intent = new Intent(mContext, UserDetailActivity.class);
                    intent.putExtra("profile", mChatList.get(position).getProfile());
                    intent.putExtra("user_id", mChatList.get(position).getProfile().getUser_id());
                    intent.putExtra("match_flg", true);
                    mContext.startActivity(intent);
                    view.setEnabled(true);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mChatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (mChatList.get(position).getFrom().equals(mAuth.getCurrentUser().getUid())) {
            return 0;
        }
        return 1;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public class SenderViewHolder extends RecyclerView.ViewHolder {
        private TextView mMessage;
        private TextView sentDate;
        private TextView messageDate;
        private TextView seen;
        private ImageView imageView;
        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            mMessage = itemView.findViewById(R.id.sender_id);
            sentDate = itemView.findViewById(R.id.message_date_sender);
            messageDate = itemView.findViewById(R.id.send_datetime);
            seen = itemView.findViewById(R.id.seen);
            imageView = itemView.findViewById(R.id.sender_image);
        }
    }

    public class ReceiverViewHolder extends RecyclerView.ViewHolder {
        private TextView mMessage;
        private CircleImageView mImageView;
        private TextView sentDate;
        private TextView messageDate;
        private ImageView imageView;
        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.chat_image_circle_receiver);
            mMessage = itemView.findViewById(R.id.receiver_id);
            sentDate = itemView.findViewById(R.id.message_date_receiver);
            messageDate = itemView.findViewById(R.id.receive_datetime);
            imageView = itemView.findViewById(R.id.receiver_image);
        }
    }
}
