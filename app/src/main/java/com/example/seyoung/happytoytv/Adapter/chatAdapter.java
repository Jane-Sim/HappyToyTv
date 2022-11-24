package com.example.seyoung.happytoytv.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.seyoung.happytoytv.R;
import com.example.seyoung.happytoytv.model.Chat;

import java.util.ArrayList;

/**
 * 사용자들이 실시간 방송에서 사용하는 채팅 어탭터입니다.
 * 채팅 목록을 액티비티와 카드뷰 xml 아이템과(textm image 등) 연결시켜줍니다.
   서버에서 불러온 채팅 데이터를 사용자 1.아이디 2.사진주소 3.메세지 4.시간을 해당 어댑터에 지정해줍니다.
 */

public class chatAdapter extends RecyclerView.Adapter<chatAdapter.MyViewHolder> {
    private ArrayList<Chat> chats;                                                     //채팅 데이터를 저장할 Arraylist 입니다.
    private Context context;                                                           //원하는 액티비티에서 불러올 context. 액티비티와 어댑터를 연결시킵니다.

    public chatAdapter(ArrayList<Chat> chats, Context context) {
        this.chats = chats;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.vod_item_chat, parent, false);          // 리싸이클러뷰를 호출한 실시간 방송(BroadCasterActivity)액티비티에서, 원하는 레이아웃(vod_item_chat)을 팽창시켜 넣어줍니다.
        return new MyViewHolder(itemView);
    }
    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        String who = chats.get(position).getWho();

        String msg = chats.get(position).getChat();                                    //서버에서 받은 해당 방의 메세지 내용입니다.
        switch (who) {
                // 1은 현재 사용자가 보낸 메세지입니다.
            case "1":
                holder.MsgTxtView.setText(msg);
                holder.name.setText(chats.get(position).getFriend());         // 현재 사용자의 메세지일 경우, 앞에 [방장] 이라고 적어줍니다.

                Glide.with(context)                                                    // 글라이드를 사용해, 서버에서 이미지를 받아옵니다.
                        .load(chats.get(position).getProfile())
                        .apply(new RequestOptions()
                                .error(R.drawable.fbnull)
                                .fitCenter()
                                .override(200, 200)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .centerCrop()
                                .circleCrop())
                        .into(holder.ImgView);

                holder.ImgView.setBackground(new ShapeDrawable(new OvalShape()));
                if (Build.VERSION.SDK_INT >= 21) {
                    holder.ImgView.setClipToOutline(true);
                }
                break;

            case "0":
                //상대방의 메세지일 경우 처리입니다.

                holder.MsgTxtView.setText(msg);
                holder.name.setText(chats.get(position).getFriend());

                Glide.with(context)                         //글라이드로 빠르게 사진을 넣는다.
                        .load(chats.get(position).getProfile())
                        .apply(new RequestOptions()
                                .error(R.drawable.fbnull)
                                .fitCenter()
                                .override(200, 200)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .centerCrop()
                                .circleCrop())
                        .into(holder.ImgView);

                holder.ImgView.setBackground(new ShapeDrawable(new OvalShape()));
                if (Build.VERSION.SDK_INT >= 21) {
                    holder.ImgView.setClipToOutline(true);
                }

                break;
        }
    }

    @Override
    public int getItemCount() {                                         // 현재 메세지 데이터가 얼마나 쌓엿는지 갯수를 주는 메서드
        return chats.size();
    }
    @Override
    public long getItemId(int position) {                                // 현재 메세지가 몇번째에 있는지 포지션값을 반납.
        return position;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView MsgTxtView,name;                                 // 메세지 내용과, 보낸 사용자의 아이디
        public ImageView ImgView;                                        // 프로필 사진
        public MyViewHolder(View view) {
            super(view);
            MsgTxtView = (TextView) view.findViewById(R.id.friendMsgTxtView);
            name = (TextView) view.findViewById(R.id.friendname);
            ImgView = (ImageView) view.findViewById(R.id.friendImgView);
        }
    }
    public void clear(){
        chats.clear();
    }                              // 현재 메세지를 담은 리스트를 비웁니다.

}
