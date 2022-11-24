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
import com.example.seyoung.happytoytv.model.anotheritem;

import java.util.List;

/**
 * 세번째 프래그먼트에서 여러가지 기능들을 목록으로 보는 어댑터입니다.
 * 크롤링, AR, 영상통화 등...
 */

public class anotherAdapter extends RecyclerView.Adapter<anotherAdapter.MyViewHolder> {
    private List<anotheritem> atList; // 서버에서 해당 정보들을 긁어와 보여줍니다.
    Context context;                  // 다른 액티비티에서 해당 어댑터를 팽창시킬 수 있도록 context를 지정해줍니다.

    public anotherAdapter(List<anotheritem> atList, Context context) {
        this.atList = atList;
        this.context = context;
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.another_list_row, parent, false);          // 리싸이클러뷰를 호출한 프레그먼트(ThrFragment)에서, 원하는 레이아웃(curl_list_row)을 팽창시켜 넣어줍니다.
        return new MyViewHolder(itemView);
    }
    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final anotheritem at = atList.get(position);
        holder.tvTitle.setText(at.getTitle());                      //데이터의 이름을 지정
        holder.tvtext.setText(at.getText());                //데이터의 가격 지정

        Glide.with(context)
                .load(at.getPicpath())
                .apply(new RequestOptions()
                .error(R.drawable.curlimage)        // 오류가 날 경우, 안드로이드에 미리 넣어놓은 사진으로 대체합니다.
                .centerCrop())                      // 사진의 크기가 다 다를수있으니 가운데에 오게 합니다.
                .into(holder.imageView);            // 받아온 장난감의 이미지를 글라이드로 넣습니다.

        View.OnClickListener listener = v -> onItemClickListener.onItemClick(at);     //해당 아이템들의 뷰를 누를 경우, 온클릭 메소드가 실행되도록
        holder.imageView.setOnClickListener(listener);                                  // 해당 아이템들에 리스너를 추가시킵니다.
        holder.tvTitle.setOnClickListener(listener);
        holder.tvtext.setOnClickListener(listener);

    }
    @Override
    public int getItemCount() {                                                         // 현재 메세지 데이터가 얼마나 쌓엿는지 갯수를 주는 메서드
        return atList.size();
    }

    public long getItemId(int position) {
        return position;
    }
    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;       // 장난감 이름 텍스트뷰
        TextView tvtext;       // 장난감 가격 텍스트뷰
        ImageView imageView;    // 장난감 이미지뷰
        public MyViewHolder(View view) {
            super(view);
            tvTitle = (TextView) view.findViewById(R.id.tvTitle);
            tvtext = (TextView) view.findViewById(R.id.tvprice);
            imageView = (ImageView) view.findViewById(R.id.imageView);

        }
    }
    public void clear(){                    // 어뎁터에 추가된 리스트들을 비워주는 메소드.
        atList.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<anotheritem> list) {    // 원하는 데이터들을 curllist에 저장할 수 있습니다.
        atList.addAll(list);
        notifyDataSetChanged();
    }

    private OnItemClickListener onItemClickListener;

    public OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
    public interface OnItemClickListener {                          // 다른 액티비티에서 누른 아이템의 위치를 받아와, 해당 위치의 데이터를 보내줍니다.
        void onItemClick(anotheritem item);
    }


}
