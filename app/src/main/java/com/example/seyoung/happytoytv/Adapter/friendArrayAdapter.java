package com.example.seyoung.happytoytv.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.seyoung.happytoytv.R;
import com.example.seyoung.happytoytv.model.addFriendItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 사용자가 그룹통화 싶은 친구를 추가, 삭제할 수 있도록
 *  유저들을 나열시켜주는 어댑터입니다.
 */

public class friendArrayAdapter extends RecyclerView.Adapter<friendArrayAdapter.MyViewHolder> {
    private List<addFriendItem> friendItemList = new ArrayList<addFriendItem>();
    Context context;                                        //리싸이클뷰가 가질 뷰값.
    private String friendst;
    public friendArrayAdapter(List<addFriendItem> friendItemList, Context context) {
        this.friendItemList = friendItemList;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_call_friends, parent, false);          //미리 지정한 커스텀 뷰를 팽창시킨다
        return new MyViewHolder(itemView);
    }
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final addFriendItem friendItem = friendItemList.get(position);
        //검색한 사용자의 닉네임, 사진을 지정해줍니다.
        holder.userNameTv.setText(friendItem.getUserid());
        Log.e("친구 이름",friendItem.getUserid());

        Glide.with(context)                         //글라이드로 빠르게 사용자의 사진을 넣는다.
                .load(friendItem.getPic())
                .apply(new RequestOptions()
                        .override(80, 80)
                        .error(R.drawable.fbnull)
                        .centerCrop()
                        .circleCrop())
                .into(holder.profileImgView);


        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemClick(friendItem);
                friendst=friendItem.getZero();
                if(Objects.equals(friendItem.getZero(), "0")){
                    friendst ="1";
                    friendItem.setZero("1");
                    holder.addfriend.setText("통화목록 해제");
                }else if(Objects.equals(friendItem.getZero(), "1")){
                    friendst="0";
                    friendItem.setZero("0");
                    holder.addfriend.setText("통화목록 추가");
                }
            }
        };

        holder.addfriend.setOnClickListener(listener);
    }

    @Override
    public int getItemCount() {
        return friendItemList.size();
    }
    @Override
    public long getItemId(int position) {
        return position;
    }

    public addFriendItem getItem(int position){                             //이게 중요하다. 필터의 결과인 favorlist의 포지션을 줘야 하는 것이다.
        return friendItemList.get(position);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView userNameTv;                   //맛집 이름
         ImageView profileImgView;              //사용자 사진
         Button addfriend;                      //친구추가시키거나 삭제시키는 버튼
         MyViewHolder(View view) {
            super(view);
             userNameTv = (TextView) view.findViewById(R.id.userNameTv);
             profileImgView = (ImageView) view.findViewById(R.id.profileImgView);
             addfriend = view.findViewById(R.id.addfriend);
        }
    }

    public void clear(){
        friendItemList.clear();
    }

    private OnItemClickListener onItemClickListener;                        //아이템을 클릭하면 설정되는 리스너

    public OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(addFriendItem item);
    }

}
