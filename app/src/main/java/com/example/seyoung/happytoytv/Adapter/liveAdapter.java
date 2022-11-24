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
import com.bumptech.glide.signature.ObjectKey;
import com.example.seyoung.happytoytv.R;
import com.example.seyoung.happytoytv.model.liveitem;

import java.util.List;


/**
 * 실시간 방송 데이터를 저장하는 어댑터입니다.
 * 서버에서 불러온 실시간 방송중인 유저들의
   1.썸네일과 2.방송 제목, 3.유저 아이디를 리스트뷰에 저장합니다.
 */

public class liveAdapter extends RecyclerView.Adapter<liveAdapter.MyViewHolder> {
    private List<liveitem> liveList;                                                    // 서버에서 실시간 방송 데이터를 넣을 list입니다.
    Context context;

    public liveAdapter(List<liveitem> liveList, Context context) {
        this.liveList = liveList;
        this.context = context;
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_list_row, parent, false);          // 리싸이클러뷰를 호출한 실시간 방송(OneFragment)프래그먼트에서, 원하는 레이아웃(custom_list_row)을 팽창시켜 넣어줍니다.
        return new MyViewHolder(itemView);
    }
    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final liveitem live = liveList.get(position);
        holder.tvTitle.setText(live.getText());                                         // 방송의 제목을 넣습니다.
        holder.tvUser.setText(live.getStreamer());                                      // 현재 방송중인 유저의 아이디를 넣습니다.

        Glide.with(context)                         //글라이드로 빠르게 사진을 넣는다.
                .load(live.getThumpath())                                               // 글라이드를 사용해, 서버에서 썸네일을 받아옵니다.
                .apply(new RequestOptions()
                        .signature(new ObjectKey(String.valueOf(System.currentTimeMillis())))
                        .error(R.drawable.live_logo)
                        .centerCrop())
                .into(holder.imageView);

       holder.liveView.setImageResource(R.drawable.live);                               // 라이브 방송 중인 이미지를 작게 표시해줍니다.


        View.OnClickListener listener = v -> onItemClickListener.onItemClick(live);     // 해당 이미지들을 누르면 방송을 볼 수 있게 BroadCasterActivity로 넘어갑니다.
        holder.imageView.setOnClickListener(listener);
        holder.tvTitle.setOnClickListener(listener);
        holder.tvUser.setOnClickListener(listener);
        holder.tvNumber.setOnClickListener(listener);
        holder.imageView.setOnClickListener(listener);
        holder.liveView.setOnClickListener(listener);

    }
    @Override
    public int getItemCount() {
        return liveList.size();
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;           // 방송 제목
        TextView tvUser;            // 방송하는 유저 아이디
        TextView tvNumber;          // 현재 방송을 보고있는 유저수
        ImageView imageView;        // 썸네일
        ImageView liveView;         // 라이브 표시 이미지

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
        liveList.clear();
        notifyDataSetChanged();
    }

    private OnItemClickListener onItemClickListener;                                                //해당 아이템들을 누르면 호출 될 클릭 리스너들입니다.

    public OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
    public interface OnItemClickListener {
        void onItemClick(liveitem item);
    }


}
