package custom.widgets.jvpager;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import custom.widgets.texttoggle.TextToggle;

public class TextPager extends LinearLayout implements
        View.OnClickListener,
        AdapterView.OnItemSelectedListener {

    public interface OnPageClickListener {
        public void onPageClick(TextPager pager);
    }

    private OnPageClickListener mOnPageClickListener;

    private int mIDs = 1000;
    private int mPageNumber = 1;
    private List<CharSequence> mTextPages;
    private CharSequence mTextPage = "";
    private boolean resetPageNumber = false;
    private LinearLayout mPagePrevious, mPageNext, mView;
    private TextView mLastSelectedPage, mNextPageNum, mPreviousPageNum;
    private TextToggle mTextToggle;
    private Spinner mPageOldChoice;
    private Context context;

    public void setOnPageClickListener(OnPageClickListener instance) {
        mOnPageClickListener = instance;
    }

    public TextPager(Context context) {
        super(context);
        initialize(context);
    }

    public TextPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    private void initialize(Context context) {
        this.context = context;

        LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.textpager, this, true);
        Resources res = context.getResources();

        mTextPages = Arrays.asList(res.getTextArray(R.array.textpager_letters));

        // next button - layout settings
        mPageNext = (LinearLayout) findViewById(R.id.go_next);
        mPageNext.setOnClickListener(this);

        // previous button - layout settings
        mPagePrevious = (LinearLayout) findViewById(R.id.go_previous);
        mPagePrevious.setOnClickListener(this);

        LinearLayout centerView = (LinearLayout) findViewById(R.id.textpager_center);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB_MR2) { // api 14+
            newStylePagination(centerView, inflater);
        } else {
            oldStylePagination(centerView, inflater);
        }
        mNextPageNum = (TextView) findViewById(R.id.textpager_next_page_num);
        mPreviousPageNum = (TextView) findViewById(R.id.textpager_previous_page_num);

        mTextToggle = (TextToggle) findViewById(R.id.texttoggle);
    }

    private void newStylePagination(ViewGroup main, LayoutInflater inflater) {
        // scroll of letters
        HorizontalScrollView scroll = new HorizontalScrollView(context);
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.CENTER;
        scroll.setLayoutParams(params);

        // scrolled view of children
        mView = new LinearLayout(context);
        mView.setOrientation(LinearLayout.HORIZONTAL);
        params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.CENTER;
        mView.setLayoutParams(params);

        for (int index = 0; index < mTextPages.size(); index++) {
            LinearLayout view = (LinearLayout)
                    inflater.inflate(R.layout.option_page, mView, false);
            view.setId(mIDs + index);

            TextView text = (TextView) view.findViewById(R.id.textpager_text);
            text.setText(mTextPages.get(index));

            params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER;
            view.setLayoutParams(params);
            view.setOnClickListener(this);
            mView.addView(view);
        }
        scroll.addView(mView);
        main.addView(scroll);
    }

    private void oldStylePagination(ViewGroup main, LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.textpager_choice, main, false);
        LayoutParams params = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        view.setLayoutParams(params);
        mPageOldChoice = (Spinner) view.findViewById(R.id.textpager_pages_old_style);
        List<String> strings = new ArrayList<String>();
        for (CharSequence page : mTextPages) {
            strings.add(String.format(" %s ", page));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                context, android.R.layout.simple_spinner_item, strings
        );
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        mPageOldChoice.setAdapter(adapter);
        mPageOldChoice.setOnItemSelectedListener(this);
        main.addView(view);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        onPageChangeOldStyle(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        onPageChangeOldStyle(1);
    }

    private void onPageChangeOldStyle(int position) {
        CharSequence textPage = mTextPages.get(position);
        checkResetPageNumber(textPage);
        mTextPage = textPage;
        if (mOnPageClickListener != null)
            mOnPageClickListener.onPageClick(this);
        updatePageNumberDisplay();
    }

    private void checkResetPageNumber(CharSequence textPage) {
        if (!textPage.equals(mTextPage)) {
            if (resetPageNumber) mPageNumber = 1;
            else resetPageNumber = true;
        }
    }

    public int getTextPosition(String text) {
        return mTextPages.indexOf(text);
    }

    public TextToggle getTextToggle() {
        return mTextToggle;
    }

    /**
     * Click simulation
     */
    public void setSelectedPage(int pageIndex, int pageNumber) {
        if (pageIndex > -1) {
            setPageNumber(pageNumber);
            if (mPageOldChoice == null) {
                onClick(mView.findViewById(mIDs + pageIndex));
            } else {
                mPageOldChoice.setSelection(pageIndex);
            }
        }
    }

    public String getSelectedPageText() {
        return String.valueOf(mTextPage);
    }

    public int getSelectedPagePosition() {
        return getTextPosition(getSelectedPageText());
    }

    public int getPageNumber() {
        return mPageNumber;
    }

    public void setPageNumber(int number) {
        mPageNumber = number;
    }

    private void updatePageNumberDisplay() {
        mPreviousPageNum.setText(String.valueOf(mPageNumber == 1 ? 1 : mPageNumber - 1));
        mNextPageNum.setText(String.valueOf(mPageNumber + 1));
    }

    private void setupPageIndex(View view) {
        Resources res = getResources();
        if (view == mPagePrevious) {
            if (mPageNumber > 1) mPageNumber--;
        } else if (view == mPageNext) {
            mPageNumber++;
        }
        String message = res.getString(R.string.page_information) + " " + String.valueOf(mPageNumber);
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        mOnPageClickListener.onPageClick(this);
    }

    private void setupPageLetter(View view) {
        Resources res = getResources();

        TextView selectedPage = (TextView) view.findViewById(R.id.textpager_text);
        selectedPage.setTextColor(res.getColor(R.color.page_after_select));
        selectedPage.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                res.getDimension(R.dimen.after_selection_font));

        if (mLastSelectedPage != null && selectedPage != mLastSelectedPage) {
            mLastSelectedPage.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    res.getDimension(R.dimen.before_selection_font));
            mLastSelectedPage.setTextColor(res.getColor(R.color.page_before_select));
        }

        checkResetPageNumber(selectedPage.getText());

        mLastSelectedPage = selectedPage;
        mTextPage = selectedPage.getText();

        if (mOnPageClickListener != null)
            mOnPageClickListener.onPageClick(this);

        updatePageNumberDisplay();
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.go_previous || viewId == R.id.go_next) {
            setupPageIndex(view);
        } else {
            setupPageLetter(view);
        }
        updatePageNumberDisplay();
        invalidate(); //update all views
    }
}




