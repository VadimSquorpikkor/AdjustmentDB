package com.squorpikkor.app.adjustmentdb.ui.main.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squorpikkor.app.adjustmentdb.R;

import java.util.ArrayList;

/**Немного необычный адаптер: суть в том, что адаптер в конструкторе получает два листа: с id и с именами.
 * При этом для заполнения списка используется лист имен, а при срабатывании clickListener выдается
 * значение из листа идентификаторов. Сам клик лисенер отдает не позицию кликнутого элемента, а
 * значение элемента (точнее идентификатор выбранного имени). Короче — я ♥ RecyclerView*/
public class ShortStateAdapter extends RecyclerView.Adapter<ShortStateAdapter.ShortStateViewHolder> {

    ArrayList<String> ids;
    ArrayList<String> names;
    OnItemClickListener onItemClickListener;

    public ShortStateAdapter(ArrayList<String> ids, ArrayList<String> names) {
        this.ids = ids;
        this.names = names;
    }

    public interface OnItemClickListener {
        void getNameOnItemClick(String name);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ShortStateAdapter.ShortStateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_short_state_adapter, parent, false);
        return new ShortStateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShortStateAdapter.ShortStateViewHolder holder, int position) {
        String state = names.get(position);
        holder.text.setText(state);
    }

    @Override
    public int getItemCount() {
        return ids.size();
    }

    public class ShortStateViewHolder extends RecyclerView.ViewHolder {
        TextView text;
        public ShortStateViewHolder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.state_text);

            itemView.setOnClickListener(v -> {
                if (onItemClickListener!=null) onItemClickListener.getNameOnItemClick(ids.get(getAdapterPosition()));
            });
        }
    }
}
