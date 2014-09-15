/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.tv.settings;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v17.leanback.widget.Presenter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.tv.settings.widget.BitmapDownloader;
import com.android.tv.settings.widget.BitmapDownloader.BitmapCallback;
import com.android.tv.settings.widget.BitmapWorkerOptions;

public class MenuItemPresenter extends Presenter {

    private static class MenuItemViewHolder extends ViewHolder {
        public final ImageView mIconView;
        public final TextView mTitleView;
        public final TextView mDescriptionView;
        public BitmapCallback mBitmapCallBack;

        MenuItemViewHolder(View v) {
            super(v);
            mIconView = (ImageView) v.findViewById(R.id.icon);
            mTitleView = (TextView) v.findViewById(R.id.title);
            mDescriptionView = (TextView) v.findViewById(R.id.description);
            setTitleTopMargin();
            setDescriptionBottomMargin();
        }

        private void setTitleTopMargin() {
            FontMetricsInt fm = getFontMetricsInt(mTitleView, R.dimen.browse_item_title_font_size);
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) mTitleView
                    .getLayoutParams();
            lp.topMargin = lp.topMargin + fm.ascent;
            mTitleView.setLayoutParams(lp);
        }

        private void setDescriptionBottomMargin() {
            FontMetricsInt fm = getFontMetricsInt(mDescriptionView,
                    R.dimen.browse_item_description_font_size);
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) mDescriptionView
                    .getLayoutParams();
            lp.bottomMargin = lp.bottomMargin - fm.descent;
            mDescriptionView.setLayoutParams(lp);
        }

        private FontMetricsInt getFontMetricsInt(View v, int dimenResource) {
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setTextSize(v.getContext().getResources().getDimensionPixelSize(dimenResource));
            paint.setTypeface(Typeface.SANS_SERIF);
            return paint.getFontMetricsInt();
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.browse_item_blended_light, parent, false);
        return new MenuItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        if (item instanceof MenuItem && viewHolder instanceof MenuItemViewHolder) {
            final MenuItem menuItem = (MenuItem) item;
            MenuItemViewHolder menuItemViewHolder = (MenuItemViewHolder) viewHolder;

            prepareTextView(menuItemViewHolder.mTitleView, menuItem.getTitle());
            boolean hasDescription = prepareTextView(menuItemViewHolder.mDescriptionView,
                    menuItem.getDescriptionGetter() == null ? null
                    : menuItem.getDescriptionGetter().getText());

            if (!hasDescription) {
                menuItemViewHolder.mTitleView.setMaxLines(2);
            } else {
                menuItemViewHolder.mTitleView.setMaxLines(1);
            }

            viewHolder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (v != null && menuItem.getIntent() != null) {
                        ((Activity) v.getContext()).startActivity(menuItem.getIntent());
                    }
                }
            });

            prepareImageView(menuItemViewHolder, menuItem.getImageUriGetter().getUri());
        }
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {
        if (viewHolder instanceof MenuItemViewHolder) {
            MenuItemViewHolder menuItemViewHolder = (MenuItemViewHolder) viewHolder;
            menuItemViewHolder.mIconView.setImageBitmap(null);
            BitmapDownloader.getInstance(viewHolder.view.getContext()).cancelDownload(
                    menuItemViewHolder.mBitmapCallBack);
        }
    }

    private void prepareImageView(final MenuItemViewHolder menuItemViewHolder, String imageUri) {
        menuItemViewHolder.mIconView.setVisibility(View.INVISIBLE);
        LayoutParams lp = menuItemViewHolder.mIconView.getLayoutParams();
        if (imageUri != null) {
            menuItemViewHolder.mBitmapCallBack = new BitmapCallback() {
                @Override
                public void onBitmapRetrieved(Bitmap bitmap) {
                    if (bitmap != null) {
                        menuItemViewHolder.mIconView.setImageBitmap(bitmap);
                        menuItemViewHolder.mIconView.setVisibility(View.VISIBLE);
                        menuItemViewHolder.mIconView.setAlpha(0f);
                        fadeIn(menuItemViewHolder.mIconView);
                    }
                }
            };

            Context context = menuItemViewHolder.view.getContext();

            BitmapWorkerOptions bitmapWorkerOptions = new BitmapWorkerOptions.Builder(context)
                    .resource(Uri.parse(imageUri)).height(lp.height).width(lp.width).build();

            BitmapDownloader.getInstance(context).getBitmap(bitmapWorkerOptions,
                    menuItemViewHolder.mBitmapCallBack);
        }
    }

    private void fadeIn(View v) {
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(v, "alpha", 1f);
        alphaAnimator.setDuration(v.getContext().getResources().getInteger(
                android.R.integer.config_shortAnimTime));
        alphaAnimator.start();
    }

    private boolean prepareTextView(TextView textView, String title) {
        boolean hasTextView = !TextUtils.isEmpty(title);
        if (hasTextView) {
            textView.setText(title);
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.GONE);
        }
        return hasTextView;
    }
}
