package com.example.pantrymanager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pantrymanager.R;
import com.example.pantrymanager.logic.UnitConverter;
import com.example.pantrymanager.model.PantryItem;
import com.example.pantrymanager.util.ExpiryUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom RecyclerView adapter that binds pantry rows from the SQLite database to item_pantry.xml.
 */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.ViewHolder> {

    public interface Listener {
        void onItemClicked(PantryItem item);
        void onDeleteClicked(PantryItem item);
    }

    private final List<PantryItem> items = new ArrayList<>();
    private final Listener listener;
    private boolean imperial;
    private boolean highlightExpiring;
    private int expiringDays;

    public PantryAdapter(Listener listener) {
        this.listener = listener;
    }

    /** Replaces the list contents and display settings then redraws*/
    public void submit(List<PantryItem> newItems, boolean imperial, boolean highlightExpiring, int expiringDays) {
        items.clear();
        items.addAll(newItems);
        this.imperial = imperial;
        this.highlightExpiring = highlightExpiring;
        this.expiringDays = expiringDays;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pantry, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        PantryItem item = items.get(position);
        h.name.setText(item.getName());
        h.quantity.setText(UnitConverter.format(item.getQuantity(), item.getUnit(), imperial));

        if (item.hasExpiryDate()) {
            h.expiry.setVisibility(View.VISIBLE);
            h.expiry.setText(ExpiryUtils.describe(item.getExpiryDate()));
            int colour;
            if (ExpiryUtils.isExpired(item.getExpiryDate())) {
                colour = R.color.status_bad;
            } else if (highlightExpiring && ExpiryUtils.isExpiringSoon(item.getExpiryDate(), expiringDays)) {
                colour = R.color.status_warn;
            } else {
                colour = R.color.text_secondary;
            }
            h.expiry.setTextColor(ContextCompat.getColor(h.itemView.getContext(), colour));
        } else {
            h.expiry.setVisibility(View.GONE);
        }

        h.itemView.setOnClickListener(v -> listener.onItemClicked(item));
        h.itemView.setOnLongClickListener(v -> {
            listener.onDeleteClicked(item);
            return true;
        });
        h.delete.setOnClickListener(v -> listener.onDeleteClicked(item));
        h.delete.setContentDescription(h.itemView.getContext().getString(R.string.cd_delete_item, item.getName()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView quantity;
        final TextView expiry;
        final ImageButton delete;

        ViewHolder(@NonNull View v) {
            super(v);
            name = v.findViewById(R.id.text_name);
            quantity = v.findViewById(R.id.text_quantity);
            expiry = v.findViewById(R.id.text_expiry);
            delete = v.findViewById(R.id.button_delete);
        }
    }
}
