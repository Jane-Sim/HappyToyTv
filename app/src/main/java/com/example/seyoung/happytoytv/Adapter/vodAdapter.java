package com.example.seyoung.happytoytv.Adapter;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.seyoung.happytoytv.R;
import com.example.seyoung.happytoytv.model.voditem;

import java.util.List;

/**
 * vod 영상 다시보기 어댑터입니다.
 * 서버에서 불러온 VOD 영상의 1.썸네일과 2.사용자 아이디, 3.제목을 저장합니다.
 */

public class vodAdapter extends RecyclerView.Adapter<vodAdapter.MyViewHolder> {
    private List<voditem> vodList;                                                      //서버에서 받아온 vod 데이터를 담아줄 list입니다.
    Context context;

    public vodAdapter(List<voditem> vodList, Context context) {
        this.vodList = vodList;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_list_row, parent, false);           // 리싸이클러뷰를 호출한 VOD (TwoFragment)프래그먼트에서, 원하는 레이아웃(custom_list_row)을 팽창시켜 넣어줍니다.
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final voditem vod = vodList.get(position);                                          // 추가된 데이터를 가져와, 해당 레이아웃에 지정해줍다.

        holder.tvTitle.setText(vod.getText());                                              // VOD의 제목을 지정합니다.
        holder.tvUser.setText(vod.getStreamer());                                           // 방송했던 유저의 아이디를 넣습니다.
        holder.liveView.setVisibility(View.GONE);                                           // 라이브 방송이 아니니, 라이브 이미지를 가려줍니다.

        Glide.with(context)                                                                 // 글라이드를 사용해, 서버에서 해당 VOD의 썸네일을 받아옵니다.
                .load(vod.getThumpath())
                .apply(new RequestOptions()
                .error(R.drawable.live_logo)
                .centerCrop())
                .into(holder.imageView);

        View.OnClickListener listener = v -> onItemClickListener.onItemClick(vod);          // 해당 이미지들을 누르면 VOD방송을 볼 수 있게 VideoPlayerActivity로 넘어갑니다.
        holder.imageView.setOnClickListener(listener);
        holder.tvTitle.setOnClickListener(listener);
        holder.tvUser.setOnClickListener(listener);
        holder.tvNumber.setOnClickListener(listener);
        holder.imageView.setOnClickListener(listener);

    }
    @Override
    public int getItemCount() {
        return vodList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;                                                           //VOD의 제목, 유저아이디, 인덱스, 썸네일입니다.
        TextView tvUser;
        TextView tvNumber;
        ImageView imageView;
        ImageView liveView;

        public MyViewHolder(View view) {
            super(view);
            tvTitle = (TextView) view.findViewById(R.id.tvTitle);
            tvUser = (TextView) view.findViewById(R.id.tvUser);
            tvNumber = (TextView) view.findViewById(R.id.tvNumber);
            imageView = (ImageView) view.findViewById(R.id.imageView);
            liveView = (ImageView) view.findViewById(R.id.liveView);

        }

    }

    public void clear(){
        vodList.clear();
        notifyDataSetChanged();
    }

    private OnItemClickListener onItemClickListener;
    public OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
    public interface OnItemClickListener {
        void onItemClick(voditem item);
    }


}
