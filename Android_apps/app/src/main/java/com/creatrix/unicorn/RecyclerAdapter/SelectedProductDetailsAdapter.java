package com.creatrix.unicorn.RecyclerAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.creatrix.unicorn.Model.Product;
import com.creatrix.unicorn.R;

import java.text.DecimalFormat;
import java.util.List;

public class SelectedProductDetailsAdapter
        extends RecyclerView.Adapter<SelectedProductDetailsAdapter.ProductViewHolder> {

    private final List<Product> products;
    private final DecimalFormat amountFormat = new DecimalFormat("0.##");

    public SelectedProductDetailsAdapter(List<Product> products) {
        this.products = products;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_selected_product_detail, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);
        double unitPrice = product.getUnitPrice() == null ? 0 : product.getUnitPrice();
        double vatPerUnit = product.getVatPercentage() == null ? 0 : product.getVatPercentage();
        double subtotal = unitPrice * product.getQuantity();
        double vat = vatPerUnit * product.getQuantity();

        holder.name.setText(product.getProductName());
        holder.code.setText(product.getProductCode());
        holder.unitPrice.setText(amountFormat.format(unitPrice));
        holder.quantity.setText(String.valueOf(product.getQuantity()));
        holder.subtotal.setText(amountFormat.format(subtotal));
        holder.vat.setText(amountFormat.format(vat));
        holder.total.setText(amountFormat.format(subtotal + vat));
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView code;
        final TextView unitPrice;
        final TextView quantity;
        final TextView subtotal;
        final TextView vat;
        final TextView total;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.selectedProductName);
            code = itemView.findViewById(R.id.selectedProductCode);
            unitPrice = itemView.findViewById(R.id.selectedProductUnitPrice);
            quantity = itemView.findViewById(R.id.selectedProductQuantity);
            subtotal = itemView.findViewById(R.id.selectedProductSubtotal);
            vat = itemView.findViewById(R.id.selectedProductVat);
            total = itemView.findViewById(R.id.selectedProductTotal);
        }
    }
}
