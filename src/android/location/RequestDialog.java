package com.microfountain.location;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class RequestDialog extends AlertDialog implements View.OnClickListener {

    private final Context Context;
    private final String Description;
    private final String ConfirmText;
    private final String CancelText;

    private TextView tvConfirm;
    private TextView tvCancel;

    private final OnDialogClickListener Listener;

    private final int id_text;
    private final int id_cancel;
    private final int id_confirm;
    private final int id_layout;

    private RequestDialog(Builder builder) {
        super(builder.Context);
        Context appContext = getContext();
        Resources resource = appContext.getResources();
        String pkgName = appContext.getPackageName();
        id_layout = resource.getIdentifier("location_request_dialog_layout", "layout", pkgName);
        id_text = resource.getIdentifier("description", "id", pkgName);
        id_cancel = resource.getIdentifier("cancel", "id", pkgName);
        id_confirm = resource.getIdentifier("confirm", "id", pkgName);
        this.Context = builder.Context;
        this.Description = builder.Description;
        this.CancelText = builder.CancelText;
        this.ConfirmText = builder.ConfirmText;
        this.Listener = builder.Listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(id_layout);
        Window window = this.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(0));
        setCanceledOnTouchOutside(false);
        initView();
        initEvent();
        WindowManager.LayoutParams params = this.getWindow().getAttributes();
        params.width = (int) (window.getWindowManager().getDefaultDisplay().getWidth() * 0.7);
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.CENTER;
        this.getWindow().setAttributes(params);
    }

    private void initEvent() {
        this.tvConfirm.setOnClickListener(this);
        this.tvCancel.setOnClickListener(this);
    }

    private void initView() {
        tvCancel = findViewById(id_cancel);
        tvConfirm = findViewById(id_confirm);
        TextView tvDesc = findViewById(id_text);
        this.tvCancel.setText(CancelText);
        this.tvConfirm.setText(ConfirmText);
        tvDesc.setText(Description);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == id_confirm) {
            if (this.Listener != null) {
                this.Listener.onConfirm();
            }

            this.dismiss();
        } else if (id == id_cancel) {
            this.Listener.onCancel();
            this.dismiss();
        }

    }

    public static class Builder {
        private Context Context;
        private String Description;
        private String CancelText;
        private String ConfirmText;
        private OnDialogClickListener Listener;

        public Builder Context(android.content.Context Context) {
            this.Context = Context;
            return this;
        }

        public Builder Description(String Description) {
            this.Description = Description;
            return this;
        }

        public Builder CancelText(String CancelText) {
            this.CancelText = CancelText;
            return this;
        }

        public Builder ConfirmText(String ConfirmText) {
            this.ConfirmText = ConfirmText;
            return this;
        }

        public Builder Listener(OnDialogClickListener Listener) {
            this.Listener = Listener;
            return this;
        }

        public Builder fromPrototype(RequestDialog prototype) {
            Context = prototype.Context;
            Description = prototype.Description;
            CancelText = prototype.CancelText;
            ConfirmText = prototype.ConfirmText;
            Listener = prototype.Listener;
            return this;
        }

        public RequestDialog build() {
            return new RequestDialog(this);
        }
    }

    public interface OnDialogClickListener {
        void onCancel();

        void onConfirm();
    }
}
