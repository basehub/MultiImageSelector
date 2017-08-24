package me.nereo.multi_image_selector;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import me.nereo.multi_image_selector.bean.Image;
import me.nereo.multi_image_selector.fragment.ImagePreviewFragment;

public class ImagePreviewActivity extends AppCompatActivity {
    public static final int REQUEST_PREVIEW = 68;
    public static final String EXTRA_PREVIEW_LIST = "previewList";
    public static final String EXTRA_PREVIEW_SELECT_LIST = "previewSelectList";
    public static final String EXTRA_MAX_SELECT_NUM = "maxSelectNum";
    public static final String EXTRA_POSITION = "position";

    public static final String OUTPUT_LIST = "select_result";
    public static final String OUTPUT_ISDONE = "isDone";

    private LinearLayout barLayout;
    private RelativeLayout selectBarLayout;
    private Toolbar toolbar;
    private TextView doneText;
    private CheckBox checkboxSelect;
    private ViewPager viewPager;


    private int position;
    private int maxSelectNum;
    private List<Image> images = new ArrayList<>();
    //    private List<Image> selectImages = new ArrayList<>();
    private List<String> selectImages = new ArrayList<>();


    private boolean isShowBar = true;


    public static void startPreview(Activity context, List<Image> images, ArrayList<String> selectImages, int maxSelectNum, int position) {
        Intent intent = new Intent(context, ImagePreviewActivity.class);
        intent.putExtra(EXTRA_PREVIEW_LIST, (ArrayList) images);
        intent.putStringArrayListExtra(EXTRA_PREVIEW_SELECT_LIST, selectImages);
        intent.putExtra(EXTRA_POSITION, position);
        intent.putExtra(EXTRA_MAX_SELECT_NUM, maxSelectNum);
        context.startActivityForResult(intent, REQUEST_PREVIEW);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.mis_activity_image_preview);
        initView();
        registerListener();
    }

    public void initView() {
        images = getIntent().getParcelableArrayListExtra(EXTRA_PREVIEW_LIST);
        selectImages = getIntent().getStringArrayListExtra(EXTRA_PREVIEW_SELECT_LIST);
        maxSelectNum = getIntent().getIntExtra(EXTRA_MAX_SELECT_NUM, 9);
        position = getIntent().getIntExtra(EXTRA_POSITION, 1);

        barLayout = (LinearLayout) findViewById(R.id.bar_layout);
        selectBarLayout = (RelativeLayout) findViewById(R.id.select_bar_layout);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle((position + 1) + "/" + images.size());
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        doneText = (TextView) findViewById(R.id.done_text);
        onSelectNumChange();

        checkboxSelect = (CheckBox) findViewById(R.id.checkbox_select);
        onImageSwitch(position);


        viewPager = (ViewPager) findViewById(R.id.preview_pager);
        viewPager.setAdapter(new SimpleFragmentAdapter(getSupportFragmentManager()));
        viewPager.setCurrentItem(position);
    }

    public void registerListener() {
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                toolbar.setTitle(position + 1 + "/" + images.size());
                onImageSwitch(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDoneClick(false);
            }
        });
        checkboxSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = checkboxSelect.isChecked();
                if (selectImages.size() >= maxSelectNum && isChecked) {
                    Toast.makeText(ImagePreviewActivity.this, getString(R.string.mis_msg_amount_limit, maxSelectNum), Toast.LENGTH_LONG).show();
                    checkboxSelect.setChecked(false);
                    return;
                }
                Image image = images.get(viewPager.getCurrentItem());
                if (isChecked) {
                    selectImages.add(image.path);
                } else {
                    for (String media : selectImages) {
                        if (media.equals(image.path)) {
                            selectImages.remove(media);
                            break;
                        }
                    }
                }
                onSelectNumChange();
            }
        });
        doneText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDoneClick(true);
            }
        });
    }

    public class SimpleFragmentAdapter extends FragmentPagerAdapter {
        public SimpleFragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return ImagePreviewFragment.getInstance(images.get(position).path);
        }

        @Override
        public int getCount() {
            return images.size();
        }
    }

    public void onSelectNumChange() {
        boolean enable = selectImages.size() != 0;
        doneText.setEnabled(enable);
        if (enable) {
            doneText.setText(getString(R.string.mis_action_button_string,
                    getString(R.string.mis_action_done), selectImages.size(), maxSelectNum));
        } else {
            doneText.setText(R.string.mis_action_done);
        }
    }

    public void onImageSwitch(int position) {
        checkboxSelect.setChecked(isSelected(images.get(position)));
    }

    public boolean isSelected(Image image) {
        for (String media : selectImages) {
            if (media.equals(image.path)) {
                return true;
            }
        }
        return false;
    }

    private void hideStatusBar() {
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setAttributes(attrs);
    }

    private void showStatusBar() {
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setAttributes(attrs);
    }

    public void switchBarVisibility() {
        barLayout.setVisibility(isShowBar ? View.GONE : View.VISIBLE);
        toolbar.setVisibility(isShowBar ? View.GONE : View.VISIBLE);
        selectBarLayout.setVisibility(isShowBar ? View.GONE : View.VISIBLE);
        if (isShowBar) {
            hideStatusBar();
        } else {
            showStatusBar();
        }
        isShowBar = !isShowBar;
    }

    public void onDoneClick(boolean isDone) {
        Intent intent = new Intent();
        intent.putExtra(OUTPUT_LIST, (ArrayList) selectImages);
        intent.putExtra(OUTPUT_ISDONE, isDone);
        setResult(RESULT_OK, intent);
        finish();
    }
}
