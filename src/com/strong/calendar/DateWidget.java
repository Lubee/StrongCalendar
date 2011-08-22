package com.strong.calendar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class DateWidget extends Activity {
  private ArrayList<DateWidgetDayCell> days = new ArrayList<DateWidgetDayCell>();
  private ArrayList<DateWidgetDayCell> daysNext = new ArrayList<DateWidgetDayCell>();
  private Calendar calStartDate = Calendar.getInstance();
  private Calendar calToday = Calendar.getInstance();
  private Calendar calCalendar = Calendar.getInstance();
  private Calendar calSelected = Calendar.getInstance();

  public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

  LinearLayout layContent = null;
  LinearLayout layContentNext = null;
  boolean isOneFrame = true;
  Button btnToday = null;

  private int iFirstDayOfWeek = Calendar.SUNDAY;
  private int iMonthViewCurrentMonth = 0;
  private int iMonthViewCurrentYear = 0;
  public static final int SELECT_DATE_REQUEST = 111;
  private static int iDayCellSize = 38;
  private static final int iDayHeaderHeight = 19;
  private static final int iTotalWidth = (iDayCellSize * 7);
  private TextView tv, yearTextView;
  private int mYear;
  private int mMonth;
  private int mDay;

  private Animation slideLeftIn;
  private Animation slideLeftOut;
  private Animation slideRightIn;
  private Animation slideRightOut;
  private Animation slideTopIn;
  private Animation slideTopOut;
  private Animation slideBottomIn;
  private Animation slideBottomOut;
  private ViewFlipper viewFlipper;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

    setDayCellSize();
    requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); // 声明使用自定义标题
    iFirstDayOfWeek = Calendar.SUNDAY;
    mYear = calSelected.get(Calendar.YEAR);
    mMonth = calSelected.get(Calendar.MONTH);
    mDay = calSelected.get(Calendar.DAY_OF_MONTH);

    setContentView(generateContentView());
    getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);// 自定义布局赋值

    initSlide();
    calStartDate = getCalendarStartDate();
    DateWidgetDayCell daySelected = slideCalendar();
    updateControlsState();
    if (daySelected != null)
      daySelected.requestFocus();
  }

  @Override
  public void onStart() {
    super.onStart();
  }

  private ViewFlipper createViewFlipper() {
    ViewFlipper vf = new ViewFlipper(this);
    vf.setLayoutParams(new LayoutParams(android.view.ViewGroup.LayoutParams.FILL_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
    return vf;
  }

  private LinearLayout createLayout(int iOrientation) {
    LinearLayout lay = new LinearLayout(this);
    lay.setLayoutParams(new LayoutParams(android.view.ViewGroup.LayoutParams.FILL_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
    lay.setOrientation(iOrientation);
    return lay;
  }

  private void initSlide() {
    slideLeftIn = AnimationUtils.loadAnimation(this, R.anim.slide_left_in);
    slideLeftOut = AnimationUtils.loadAnimation(this, R.anim.slide_left_out);
    slideRightIn = AnimationUtils.loadAnimation(this, R.anim.slide_right_in);
    slideRightOut = AnimationUtils.loadAnimation(this, R.anim.slide_right_out);
    slideTopIn = AnimationUtils.loadAnimation(this, R.anim.slide_top_in);
    slideTopOut = AnimationUtils.loadAnimation(this, R.anim.slide_top_out);
    slideBottomIn = AnimationUtils.loadAnimation(this, R.anim.slide_bottom_in);
    slideBottomOut = AnimationUtils.loadAnimation(this, R.anim.slide_bottom_out);
  }

  private void slideRight() {
    setPrevMonthViewItem();
    viewFlipper.setInAnimation(slideRightIn);
    viewFlipper.setOutAnimation(slideRightOut);
    viewFlipper.showPrevious();
  }

  private void slideLeft() {
    setNextMonthViewItem();
    viewFlipper.setInAnimation(slideLeftIn);
    viewFlipper.setOutAnimation(slideLeftOut);
    viewFlipper.showNext();
  }

  private void slideTop() {
    setNextYearViewItem();
    viewFlipper.setInAnimation(slideTopIn);
    viewFlipper.setOutAnimation(slideTopOut);
    viewFlipper.showNext();
  }

  private void slideBottom() {
    setPrevYearViewItem();
    viewFlipper.setInAnimation(slideBottomIn);
    viewFlipper.setOutAnimation(slideBottomOut);
    viewFlipper.showPrevious();
  }

  private void generateTopButtons(LinearLayout layTopControls) {

    final int iSmallButtonWidth = 40;
    final int iSamllHeight = 35;

    yearTextView = new TextView(this);
    yearTextView.setPadding(15, 8, 8, 8);
    yearTextView.setText(mYear + "年" + format(mMonth + 1) + "月");
    yearTextView.setTextColor(Color.BLACK);
    yearTextView.setWidth(105);
    yearTextView.setHeight(iSamllHeight);

    Button btnPrevMonth = new Button(this);
    btnPrevMonth.setLayoutParams(new LayoutParams(iSmallButtonWidth / 2, iSamllHeight));
    btnPrevMonth.setBackgroundResource(R.drawable.prev_month);

    Button btnNextMonth = new Button(this);
    btnNextMonth.setLayoutParams(new LayoutParams(iSmallButtonWidth / 2, iSamllHeight));
    btnNextMonth.setBackgroundResource(R.drawable.next_month);

    // set events
    btnPrevMonth.setOnClickListener(new Button.OnClickListener() {
      public void onClick(View arg0) {
        slideRight();
      }
    });

    btnNextMonth.setOnClickListener(new Button.OnClickListener() {
      public void onClick(View arg0) {
        slideLeft();
      }
    });

    layTopControls.setGravity(Gravity.CENTER_HORIZONTAL);
    layTopControls.addView(btnPrevMonth);
    layTopControls.addView(yearTextView);
    layTopControls.addView(btnNextMonth);

  }

  private View generateContentView() {
    LinearLayout mainLayout = createLayout(LinearLayout.VERTICAL);
    mainLayout.setBackgroundColor(this.getResources().getColor(R.color.solid_backgroudcolor));
    LinearLayout firstLayout = createLayout(LinearLayout.VERTICAL);
    firstLayout.setPadding(0, 0, 0, 0);

    layContent = createLayout(LinearLayout.VERTICAL);
    layContent.setPadding(0, 0, 0, 0);
    generateCalendar(layContent, days);
    firstLayout.addView(layContent);

    viewFlipper = createViewFlipper();
    viewFlipper.setBackgroundColor(this.getResources().getColor(R.color.solid_backgroudcolor));

    LinearLayout secondLayout= createLayout(LinearLayout.VERTICAL);
    secondLayout.setPadding(0, 0, 0, 0);

    LinearLayout layTopControls = createLayout(LinearLayout.HORIZONTAL);
    layTopControls.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.topbar_bg));
    generateTopButtons(layTopControls);

    layContentNext = createLayout(LinearLayout.VERTICAL);
    layContentNext.setPadding(0, 0, 0, 0);
    generateCalendar(layContentNext, daysNext);
    secondLayout.addView(layContentNext);

    mainLayout.addView(layTopControls);
    viewFlipper.addView(firstLayout);
    viewFlipper.addView(secondLayout);
    mainLayout.addView(viewFlipper);

    tv = new TextView(this);
    tv.setPadding(0, 0, 0, 0);
    mainLayout.addView(tv);

    return mainLayout;
  }

  private View generateCalendarRow( ArrayList<DateWidgetDayCell> dayList) {
    LinearLayout layRow = createLayout(LinearLayout.HORIZONTAL);
    for (int iDay = 0; iDay < 7; iDay++) {
      DateWidgetDayCell dayCell = new DateWidgetDayCell(this, iDayCellSize, (int) Math.floor(iDayCellSize * 1.2));
      dayCell.setItemClick(mOnDayCellClick);
      dayList.add(dayCell);
      layRow.addView(dayCell);
    }
    return layRow;
  }

  private View generateCalendarHeader() {
    LinearLayout layRow = createLayout(LinearLayout.HORIZONTAL);
    for (int iDay = 0; iDay < 7; iDay++) {
      DateWidgetDayHeader day = new DateWidgetDayHeader(this, iDayCellSize, iDayHeaderHeight);
      final int iWeekDay = DayStyle.getWeekDay(iDay, iFirstDayOfWeek);
      day.setData(iWeekDay);
      layRow.addView(day);
    }
    return layRow;
  }

  private void generateCalendar(LinearLayout layContent,  ArrayList<DateWidgetDayCell> dayList) {
    layContent.addView(generateCalendarHeader());
    dayList.clear();
    for (int iRow = 0; iRow < 6; iRow++) {
      layContent.addView(generateCalendarRow(dayList));
    }
  }

  private Calendar getCalendarStartDate() {
    calToday.setTimeInMillis(System.currentTimeMillis());
    calToday.setFirstDayOfWeek(iFirstDayOfWeek);

    if (calSelected.getTimeInMillis() == 0) {
      calStartDate.setTimeInMillis(System.currentTimeMillis());
      calStartDate.setFirstDayOfWeek(iFirstDayOfWeek);
    } else {
      calStartDate.setTimeInMillis(calSelected.getTimeInMillis());
      calStartDate.setFirstDayOfWeek(iFirstDayOfWeek);
    }
    updateStartDateForMonth();

    return calStartDate;
  }

  private DateWidgetDayCell slideCalendar() {
    ArrayList<DateWidgetDayCell> daylist = null;
    if (isOneFrame) {
      daylist = days;
      isOneFrame = false;
    } else {
      daylist = daysNext;
      isOneFrame = true;
    }
    return updateCalendar(daylist);
  }

  private DateWidgetDayCell updateCalendar(ArrayList<DateWidgetDayCell> day) {
    DateWidgetDayCell daySelected = null;
    boolean bSelected = false;
    final boolean bIsSelection = (calSelected.getTimeInMillis() != 0);
    final int iSelectedYear = calSelected.get(Calendar.YEAR);
    final int iSelectedMonth = calSelected.get(Calendar.MONTH);
    final int iSelectedDay = calSelected.get(Calendar.DAY_OF_MONTH);
    calCalendar.setTimeInMillis(calStartDate.getTimeInMillis());
    for (int i = 0; i < day.size(); i++) {
      final int iYear = calCalendar.get(Calendar.YEAR);
      final int iMonth = calCalendar.get(Calendar.MONTH);
      final int iDay = calCalendar.get(Calendar.DAY_OF_MONTH);
      final int iDayOfWeek = calCalendar.get(Calendar.DAY_OF_WEEK);

      Lunar lunar = new Lunar(calCalendar);
      final String iLunarDay = lunar.getChinaDays();
      DateWidgetDayCell dayCell = day.get(i);
      // check today
      boolean bToday = false;
      if (calToday.get(Calendar.YEAR) == iYear)
        if (calToday.get(Calendar.MONTH) == iMonth)
          if (calToday.get(Calendar.DAY_OF_MONTH) == iDay)
            bToday = true;
      // check holiday
      boolean bHoliday = false;
      if ((iDayOfWeek == Calendar.SATURDAY) || (iDayOfWeek == Calendar.SUNDAY))
        bHoliday = true;
      if ((iMonth == Calendar.JANUARY) && (iDay == 1))
        bHoliday = true;

      dayCell.setData(iYear, iMonth, iDay, iLunarDay, bToday, bHoliday, iMonthViewCurrentMonth, iDayOfWeek);
      bSelected = false;
      if (bIsSelection)
        if ((iSelectedDay == iDay) && (iSelectedMonth == iMonth) && (iSelectedYear == iYear)) {
          bSelected = true;
        }
      dayCell.setSelected(bSelected);
      if (bSelected)
        daySelected = dayCell;
      calCalendar.add(Calendar.DAY_OF_MONTH, 1);
    }
    // layContent.invalidate();
    // layContentNext.invalidate();
    return daySelected;
  }

  private void updateStartDateForMonth() {
    iMonthViewCurrentMonth = calStartDate.get(Calendar.MONTH);
    iMonthViewCurrentYear = calStartDate.get(Calendar.YEAR);
    calStartDate.set(Calendar.DAY_OF_MONTH, 1);
    UpdateCurrentMonthDisplay();
    // update days for week
    int iDay = 0;
    int iStartDay = iFirstDayOfWeek;
    if (iStartDay == Calendar.MONDAY) {
      iDay = calStartDate.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY;
      if (iDay < 0)
        iDay = 6;
    }
    if (iStartDay == Calendar.SUNDAY) {
      iDay = calStartDate.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY;
      if (iDay < 0)
        iDay = 6;
    }
    calStartDate.add(Calendar.DAY_OF_WEEK, -iDay);
  }

  private void UpdateCurrentMonthDisplay() {

    mYear = calCalendar.get(Calendar.YEAR);
  }

  private void setPrevMonthViewItem() {
    iMonthViewCurrentMonth--;
    if (iMonthViewCurrentMonth == -1) {
      iMonthViewCurrentMonth = 11;
      iMonthViewCurrentYear--;
    }
    calStartDate.set(Calendar.DAY_OF_MONTH, 1);
    calStartDate.set(Calendar.MONTH, iMonthViewCurrentMonth);
    calStartDate.set(Calendar.YEAR, iMonthViewCurrentYear);
    updateDate();
    Log.i("fdfd", iMonthViewCurrentMonth + "");
    updateCenterTextView(iMonthViewCurrentMonth, iMonthViewCurrentYear);
  }

  private void setNextMonthViewItem() {
    iMonthViewCurrentMonth++;
    if (iMonthViewCurrentMonth == 12) {
      iMonthViewCurrentMonth = 0;
      iMonthViewCurrentYear++;
    }
    calStartDate.set(Calendar.DAY_OF_MONTH, 1);
    calStartDate.set(Calendar.MONTH, iMonthViewCurrentMonth);
    calStartDate.set(Calendar.YEAR, iMonthViewCurrentYear);
    updateDate();
    updateCenterTextView(iMonthViewCurrentMonth, iMonthViewCurrentYear);
  }

  private void setPrevYearViewItem() {
    iMonthViewCurrentYear--;
    calStartDate.set(Calendar.DAY_OF_MONTH, 1);
    calStartDate.set(Calendar.MONTH, iMonthViewCurrentMonth);
    calStartDate.set(Calendar.YEAR, iMonthViewCurrentYear);
    updateDate();
    updateCenterTextView(iMonthViewCurrentMonth, iMonthViewCurrentYear);
  }

  private void setNextYearViewItem() {
    iMonthViewCurrentYear++;
    calStartDate.set(Calendar.DAY_OF_MONTH, 1);
    calStartDate.set(Calendar.MONTH, iMonthViewCurrentMonth);
    calStartDate.set(Calendar.YEAR, iMonthViewCurrentYear);
    updateDate();
    updateCenterTextView(iMonthViewCurrentMonth, iMonthViewCurrentYear);
  }

  private DateWidgetDayCell.OnItemClick mOnDayCellClick = new DateWidgetDayCell.OnItemClick() {
    public void OnClick(DateWidgetDayCell item) {
      calSelected.setTimeInMillis(item.getDate().getTimeInMillis());
      item.setSelected(true);
      isOneFrame = isOneFrame ? false : true;
      slideCalendar();
      updateControlsState();
      // Intent i = new Intent(DateWidget.this,Main.class);
      // startActivity(i);
    }

    @Override
    public void OnTouchMove(DateWidgetDayCell item, int type) {
      switch (type) {
      case 1:
        slideLeft();
        break;
      case 2:
        slideRight();
        break;
      case 3:
        slideTop();
        break;
      case 4:
        slideBottom();
        break;
      default:
        break;
      }
    }
  };

  private void updateCenterTextView(int iMonthViewCurrentMonth, int iMonthViewCurrentYear) {
    yearTextView.setText(iMonthViewCurrentYear + "年" + format(iMonthViewCurrentMonth + 1) + "月");
    // monthTextView.setText(format(iMonthViewCurrentMonth+1)+"");
  }

  private void updateDate() {
    updateStartDateForMonth();
    slideCalendar();
  }

  private void updateControlsState() {
    mYear = calSelected.get(Calendar.YEAR);
    mMonth = calSelected.get(Calendar.MONTH);
    mDay = calSelected.get(Calendar.DAY_OF_MONTH);
    Lunar lunar = new Lunar(calToday);
    Lunar lselected = new Lunar(calSelected);
    tv.setTextSize(12);
    tv.setTextColor(Color.BLACK);
    tv.setText(new StringBuilder().append("选择的日期是:").append(dateFormat.format(calSelected.getTime())).append(" 农历:").append(lselected.getChinaMonthAndDay()).append("\n农历今天:")
        .append(lunar.getChinaLunar()));
    tv.setHorizontallyScrolling(true);
  }

  private String format(int x) {
    String s = "" + x;
    if (s.length() == 1)
      s = "0" + s;
    return s;
  }

  private void setDayCellSize() {
    DisplayMetrics dm = new DisplayMetrics();
    this.getWindowManager().getDefaultDisplay().getMetrics(dm);
    iDayCellSize = dm.widthPixels / 7 + 1;
  }
}
