package forestry.counter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import forestry.counter.dto.Timber;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private LayoutInflater mInflater;
    private SparseArray<Timber> mData;
    private Context mContext;
    private OnRecyclerListener mListener;

    public RecyclerAdapter(Context context, SparseArray<Timber> data, OnRecyclerListener listener) {
        mInflater = LayoutInflater.from(context);
        mContext = context;
        mData = data;
        mListener = listener;
    }

    @Override
    public RecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        // 表示するレイアウトを設定
        return new ViewHolder(mInflater.inflate(R.layout.list_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int i) {
        // データ表示
        if (mData != null && mData.size() > i && mData.get(i) != null) {
            viewHolder.textDate.setText(mData.get(i).getRegDateString());
            viewHolder.textTimberType.setText(mData.get(i).getKind());
            viewHolder.textDia.setText(String.valueOf(mData.get(i).getDia()));
            if (mData.get(i).getSend() == 1) {
                viewHolder.imageSend.setImageResource(android.R.drawable.presence_online);
            }
            else {
                viewHolder.imageSend.setImageResource(android.R.drawable.presence_invisible);
            }
        }

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onRecyclerClicked(v, i);
            }
        });

    }

    @Override
    public int getItemCount() {
        if (mData != null) {
            return mData.size();
        } else {
            return 0;
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView textDate;
        TextView textTimberType;
        TextView textDia;
        ImageView imageSend;

        public ViewHolder(View itemView) {
            super(itemView);
            textDate = (TextView) itemView.findViewById(R.id.text_date);
            textTimberType = (TextView) itemView.findViewById(R.id.text_timber_type);
            textDia = (TextView) itemView.findViewById(R.id.text_dia);
            imageSend = (ImageView) itemView.findViewById(R.id.image_send);
        }
    }

}
