package net.yaiba.eat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import net.yaiba.eat.db.EatDB;
import net.yaiba.eat.utils.UpdateTask;

import static android.R.attr.data;
import static net.yaiba.eat.utils.Custom.*;
//import net.yaiba.eat.data.ListViewData;


public class MainActivity extends Activity implements  AdapterView.OnItemClickListener,AdapterView.OnItemLongClickListener {

    private static final int MENU_ABOUT = 0;
    private static final int MENU_SUPPORT = 1;
    private static final int MENU_WHATUPDATE = 2;
    private static final int MENU_IMPORT_EXPOERT = 3;
    private static final int MENU_CHANGE_LOGIN_PASSWORD = 4;
    private static final int MENU_CHECK_UPDATE = 5;

    private EatDB EatDB;
    private Cursor mCursor;
    private ListView RecordList;
    private EditText SearchInput;
    private Spinner spinner_filter_create_time;
    private Spinner spinner_filter_eat_time;

    private UpdateTask updateTask;

    private LinearLayout filtersOption;
    private Button bn_filters;
    private boolean isButton = true;

    private Button bn_filter_now;

    private int RECORD_ID = 0;
    private  ArrayList<Category> makeDataListData = new ArrayList<Category>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpViews("listInit",null);

        Button bn_go_add = (Button)findViewById(R.id.go_add);
        bn_go_add.setOnClickListener(new View.OnClickListener(){
            public void  onClick(View v)
            {
                Intent mainIntent = new Intent(MainActivity.this,AddActivity.class);
                startActivity(mainIntent);
                setResult(RESULT_OK, mainIntent);
                finish();
            }
        });

        //设置一览数据过滤区域 开和隐藏
        bn_filters = (Button)findViewById(R.id.filters);
        filtersOption = (LinearLayout) findViewById(R.id.filters_option);
        bn_filters.setOnClickListener(new View.OnClickListener(){
            public void  onClick(View v)
            {
                if(isButton){
                    filtersOption.setVisibility(View.VISIBLE);
                    isButton = false;
                    bn_filters.setText("-");
                }else {
                    filtersOption.setVisibility(View.GONE);
                    isButton = true;
                    bn_filters.setText("+");
                    SearchInput = (EditText)findViewById(R.id.searchInput);
                    if(SearchInput.getText().toString().trim().isEmpty()){
                        setUpViews("listInit",null);
                    } else{
                        setUpViews("search",SearchInput.getText().toString().trim());
                    }
                }
            }
        });

        bn_filter_now = (Button)findViewById(R.id.filter_now);
        filtersOption = (LinearLayout) findViewById(R.id.filters_option);
        spinner_filter_create_time = (Spinner)findViewById(R.id.filter_create_time);
        spinner_filter_eat_time = (Spinner)findViewById(R.id.filter_eat_time);
        spinner_filter_create_time.setSelection(0,true);
        spinner_filter_eat_time.setSelection(0,true);
        bn_filter_now.setOnClickListener(new View.OnClickListener(){
            public void  onClick(View v)
            {
                //Toast.makeText(MainActivity.this, "日期范围："+spinner_filter_create_time.getSelectedItem().toString()+",餐别："+spinner_filter_eat_time.getSelectedItem().toString(), Toast.LENGTH_SHORT).show();
                //Toast.makeText(MainActivity.this, "文本框内容："+SearchInput.getText().toString().trim()+ ",查询时间："+spinner_filter_create_time.getSelectedItem().toString()+"，查询类型："+spinner_filter_eat_time.getSelectedItem().toString(), Toast.LENGTH_SHORT).show();
                SearchInput = (EditText)findViewById(R.id.searchInput);
                setUpViews("search",SearchInput.getText().toString().trim());

            }
        });

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        SearchInput = (EditText)findViewById(R.id.searchInput);
        SearchInput.clearFocus();
        SearchInput.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(SearchInput.getText().toString().trim().length()!=0){
                    try {
                        setUpViews("search",SearchInput.getText().toString().trim());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    setUpViews("listInit",null);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //Toast.makeText(LoginActivity.this, "beforeTextChanged", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void afterTextChanged(Editable s) {
                //Toast.makeText(LoginActivity.this, "afterTextChanged", Toast.LENGTH_SHORT).show();
            }

        });


    }


    public void setUpViews(String type, String value){
        makeDataListData.clear();
        EatDB = new EatDB(this);
        if("listInit".equals(type)){
            mCursor = EatDB.getDay30("create_time desc");
        } else if("search".equals(type)) {

            bn_filters = (Button)findViewById(R.id.filters);
            if(bn_filters.getText().equals("+")){
                mCursor = EatDB.getForSearch(value,"","");
            } else{
                spinner_filter_create_time = (Spinner)findViewById(R.id.filter_create_time);
                spinner_filter_eat_time = (Spinner)findViewById(R.id.filter_eat_time);
                mCursor = EatDB.getForSearch(value,spinner_filter_create_time.getSelectedItem().toString(),spinner_filter_eat_time.getSelectedItem().toString());
            }
        } else if("filter".equals(type)){
            spinner_filter_create_time = (Spinner)findViewById(R.id.filter_create_time);
            spinner_filter_eat_time = (Spinner)findViewById(R.id.filter_eat_time);
            if(spinner_filter_eat_time.getSelectedItem().toString().isEmpty()){
                mCursor = EatDB.getForSearch(value,spinner_filter_create_time.getSelectedItem().toString(),"");
            } else {
                mCursor = EatDB.getForSearch(value,spinner_filter_create_time.getSelectedItem().toString(),spinner_filter_eat_time.getSelectedItem().toString());
            }
        }

        RecordList = (ListView)findViewById(R.id.recordslist);

        ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();

        for(mCursor.moveToFirst();!mCursor.isAfterLast();mCursor.moveToNext()) {
            int idColumn = mCursor.getColumnIndex("id");
            int foodColumn = mCursor.getColumnIndex("food_name");
            int eatTimeColumn = mCursor.getColumnIndex("eat_time");
            int eatWhereColumn = mCursor.getColumnIndex("eat_where");
            int remarkColumn = mCursor.getColumnIndex("remark");
            int createTimeColumn = mCursor.getColumnIndex("create_time");
            /*String resNo = "["+mCursor.getString(resNoColumn)+"]"; */
            String id = mCursor.getString(idColumn);
            String foodName = mCursor.getString(foodColumn);
            String eatTime = mCursor.getString(eatTimeColumn);
            String eatWhere = mCursor.getString(eatWhereColumn);
            String remark = mCursor.getString(remarkColumn);
            String createTime = mCursor.getString(createTimeColumn);

            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("id", id);
            map.put("foodName", foodName);
            map.put("eatTime", getEatTimeName(eatTime));
            map.put("eatWhere", eatWhere);
//            map.put("remark", remark);
            String[] data = createTime.split("-");
            if(data.length==3){
                map.put("createTime", data[1]+"/"+data[2]+"("+dayForWeek(createTime)+")");
            }else {
                map.put("createTime", "-");
            }

            listItem.add(map);
        }

        SimpleAdapter listItemAdapter = new SimpleAdapter(this,listItem,R.layout.record_items,
            /*new String[] {"res_no", "site_name", "user_name"},
            new int[] {R.id.res_no, R.id.site_name,R.id.user_name} */
                new String[] { "foodName","createTime","eatWhere","eatTime","remark"},
                new int[] {R.id.food_name,R.id.create_time,R.id.eat_where,R.id.eat_time,R.id.remark}
        );

        RecordList.setAdapter(listItemAdapter);
        RecordList.setOnItemClickListener(this);
//        RecordList.setOnItemLongClickListener(this);
//        RecordList.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
//
//            @Override
//            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
//                //menu.setHeaderTitle("操作");
//                menu.add(0, 0, 0, "编辑");
//                menu.add(0, 1, 0, "删除");
//            }
//        });





        ListView listView = (ListView) findViewById(R.id.listView1);
        TextView ListCount = (TextView)findViewById(R.id.list_counts);
        bn_filters = (Button)findViewById(R.id.filters);
        if(bn_filters.getText().equals("-")){
            //“-”号时：当前搜索结果共xx条记录
            int listCount = listItem.size();
            ListCount.setText("当前搜索结果共"+listCount+"条记录");
        } else {
            //“+”号时：您已经使用了xx天，默认显示最近30天的记录
            Cursor dCursor = EatDB.getStartUsageDay();
            for(dCursor.moveToFirst();!dCursor.isAfterLast();dCursor.moveToNext()) {
                int createTimeColumn = dCursor.getColumnIndex("create_time");
                String createTime = dCursor.getString(createTimeColumn);
                int daysDiff = 0;
                try {
                    daysDiff = getDiffDays(getStringToDate(createTime),new Date());
                } catch (ParseException e) {
                    Log.v("debug","getDiffDays error!!");
                }
                ListCount.setText("最近30天的记录/累计"+daysDiff+"天");
            }
        }


        // 数据
        ArrayList<Category> listData = makeData(listItem);

        mCustomBaseAdapter = new CategoryAdapter(getBaseContext(), listData);

        // 适配器与ListView绑定
        listView.setAdapter(mCustomBaseAdapter);

        //listView.setOnItemClickListener(new ItemClickListener());
        listView.setOnItemClickListener(this);
//        listView.setOnItemLongClickListener(this);
//        listView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
//
//            @Override
//            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
//                //menu.setHeaderTitle("操作");
//                menu.add(0, 0, 0, "编辑");
//                menu.add(0, 1, 0, "删除");
//            }
//        });


    }

    //生成一览数据
    private  ArrayList<Category> makeData(ArrayList<HashMap<String, Object>> listItem){

        Cursor mCursor = EatDB.getAllCreateTime();
//        ArrayList<Category> listData = new ArrayList<Category>();
//        Category categoryOne = new Category("路人甲");
//        categoryOne.addItem("马三立");
//        categoryOne.addItem("赵本山");
//        categoryOne.addItem("郭德纲");
//        categoryOne.addItem("周立波");
//        listData.add(categoryOne);
//
//        Category categoryTwo = new Category("事件乙");
//        categoryTwo.addItem("**贪污");
//        categoryTwo.addItem("**照门");
//        listData.add(categoryTwo);

        //ArrayList<Category> makeDataListData = new ArrayList<Category>();
        for(mCursor.moveToFirst();!mCursor.isAfterLast();mCursor.moveToNext()) {
            int createTimeColumn = mCursor.getColumnIndex("create_time");
            String createTime = mCursor.getString(createTimeColumn);
            Log.v("createTime",createTime);

            String[] data = createTime.split("-");
            if(data.length==3){
                createTime = data[1]+"/"+data[2]+"("+dayForWeek(createTime)+")";
            }else {
                createTime =  "-";
            }
            //生成header数据
            Category categoryOne = new Category(createTime);

            for (int i=0;i<listItem.size();i++){

                String listItem_r_createTime = listItem.get(i).get("createTime").toString();
                if(createTime.equals(listItem_r_createTime)){
                    String listItem_r_id = listItem.get(i).get("id").toString();
                    String listItem_r_foodName = listItem.get(i).get("foodName").toString();
                    String listItem_r_eatTime = listItem.get(i).get("eatTime").toString();
                    String listItem_r_eatWhere = listItem.get(i).get("eatWhere").toString();
                    String strs = listItem_r_id+"_///_"+listItem_r_foodName+"_///_"+listItem_r_eatTime+"_///_"+listItem_r_eatWhere+"_///_"+listItem_r_createTime;
                    //Log.v("debug",createTime+"/"+listItem_r_createTime);

                    //生成items数据，根据日期add同一天下面的所有记录
                    categoryOne.addItem(strs);
                }
            }
            if(categoryOne.getItemCount() > 1){
                makeDataListData.add(categoryOne);
            }

        }


       return makeDataListData;
    }



//    @Override
//    public boolean onContextItemSelected(MenuItem item) {
//        //setTitle("点击了长按菜单里面的第"+item.getItemId()+"个项目");
//        //Toast.makeText(this, "点击了长按菜单里面的第"+item.getItemId()+"个项目", Toast.LENGTH_SHORT).show();
//        super.onContextItemSelected(item);
//        switch (item.getItemId())
//        {
//            case 0:
//                go_update();
//                break;
//            case 1:
//                AlertDialog.Builder builder= new AlertDialog.Builder(this);
//                builder.setIcon(android.R.drawable.ic_dialog_info);
//                builder.setTitle("确认");
//                builder.setMessage("确定要删除这条记录吗？");
//                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int whichButton) {
//                        delete();
//                        setUpViews("listInit",null);
//                    }
//                });
//                builder.setNegativeButton("取消", null);
//                builder.create().show();
//                break;
//        }
//        return true;
//    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //保存当前一览位置
        //saveListViewPositionAndTop();
        //迁移到详细页面

        Intent mainIntent = new Intent(MainActivity.this,DetailActivity.class);
        Log.v("debug","position:"+position);
        Log.v("debug","long id:"+id);


        Adapter adapter=parent.getAdapter();
        //Map<String,String> map=(Map<String, String>) adapter.getItem(position);

        //String testid = makeDataListData.get(position).getItem(0);

        //Log.v("debug","makeDataListData_0:"+makeDataListData.get(position).getItem(0));
        //Log.v("debug","makeDataListData_1:"+makeDataListData.get(position).getItem(1));


        int bigListCount = makeDataListData.size();
        int checkPosition = 0;
        outer: for(int i=0;i<bigListCount;i++){

            int smallListCount = makeDataListData.get(i).getItemCount();
            Log.v("debug","smallListCount"+i+":"+smallListCount);

            for(int j=0;j<smallListCount;j++){

                Log.v("debug","makeDataListData.get("+i+").getItem("+j+")"+i+":"+makeDataListData.get(i).getItem(j));

                String[] infoArr =makeDataListData.get(i).getItem(j).split("_///_");
                //if(infoArr.length>1){

                //}
                if(checkPosition ==position){
                    RECORD_ID = Integer.valueOf(infoArr[0]);
                    break outer;
                }
                checkPosition ++;
            }

        }


//        mCursor.moveToPosition(position);
//        RECORD_ID = mCursor.getInt(0);
        //RECORD_ID = Integer.valueOf(testid);
        Log.v("debug","RECORD_ID:"+RECORD_ID);
        mainIntent.putExtra("INT", RECORD_ID);
        startActivity(mainIntent);
        setResult(RESULT_OK, mainIntent);
        finish();
    }

//    @SuppressWarnings("deprecation")
//    public void delete(){
//        if (RECORD_ID == 0) {
//            return;
//        }
//        EatDB.delete(RECORD_ID);
//        mCursor.requery();
//        Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show();
//    }
//
//    public void go_update(){
//        //保存当前位置
//        //saveListViewPositionAndTop();
//        //画面迁移到edit画面
//        Intent mainIntent = new Intent(MainActivity.this,EditActivity.class);
//        mainIntent.putExtra("INT", RECORD_ID);
//        startActivity(mainIntent);
//        setResult(RESULT_OK, mainIntent);
//        finish();
//    }


    /**
     * 保存当前页签listView的第一个可见的位置和top
     */
//    private void saveListViewPositionAndTop() {
//
//        final ListViewData app = (ListViewData)getApplication();
//
//        app.setFirstVisiblePosition(RecordList.getFirstVisiblePosition());
//        View item = RecordList.getChildAt(0);
//        app.setFirstVisiblePositionTop((item == null) ? 0 : item.getTop());
//    }




    public class RecordListAdapter extends BaseAdapter {
        private Context mContext;
        private Cursor mCursor;
        public RecordListAdapter(Context context,Cursor cursor) {

            mContext = context;
            mCursor = cursor;
        }

        @Override
        public int getCount() {
            return mCursor.getCount();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView mTextView = new TextView(mContext);
            mCursor.moveToPosition(position);
            mTextView.setText(mCursor.getString(1) + "___" + mCursor.getString(2)+ "___" + mCursor.getString(3)+ "___" + mCursor.getString(4));
            return mTextView;
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        mCursor.moveToPosition(position);
        RECORD_ID = mCursor.getInt(0);
        return false;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(Menu.NONE, MENU_IMPORT_EXPOERT, 0, this.getString(R.string.menu_inport_export));//备份与恢复
        //menu.add(Menu.NONE, MENU_CHANGE_LOGIN_PASSWORD, 0, this.getString(R.string.menu_change_login_password));//修改登录密码
        menu.add(Menu.NONE, MENU_WHATUPDATE, 0, this.getString(R.string.menu_whatupdate));//更新信息
        menu.add(Menu.NONE, MENU_CHECK_UPDATE, 0, this.getString(R.string.menu_checkupdate));//检查更新
        menu.add(Menu.NONE, MENU_SUPPORT, 0, this.getString(R.string.menu_support));//技术支持
        menu.add(Menu.NONE, MENU_ABOUT, 0, this.getString(R.string.menu_about));//关于Keep
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item)
    {
        String title = "";
        String msg = "";
        //Context mContext = null;

        super.onOptionsItemSelected(item);
        switch (item.getItemId())
        {
            case MENU_ABOUT://关于Eat
                title = this.getString(R.string.menu_about);
                msg = this.getString(R.string.about_eat);
                msg = msg + "\n\n";
                msg = msg + "@"+getAppVersion(MainActivity.this);
                showAboutDialog(title,msg);
                break;
            case MENU_SUPPORT://技术支持
                title = this.getString(R.string.menu_support);
                msg = this.getString(R.string.partners);
                showAboutDialog(title,msg);
                break;
            case MENU_WHATUPDATE://更新信息
                title = this.getString(R.string.menu_whatupdate);
                msg = msg + this.getString(R.string.what_updated);
                msg = msg + "\n\n\n";
                showAboutDialog(title,msg);
                break;
            case MENU_CHECK_UPDATE://检查更新
//                title = this.getString(R.string.menu_checkupdate);
//                msg = "本功能正在升级";//1.增加双服务器检测更新机制\n2.检查更新\n\n以上功能
                updateTask = new UpdateTask(MainActivity.this,true);
                updateTask.update();

//                showAboutDialog(title,msg);
                break;
            case MENU_IMPORT_EXPOERT://备份与恢复
                Intent mainIntent = new Intent(MainActivity.this, DataManagementActivity.class);
                startActivity(mainIntent);
                setResult(RESULT_OK, mainIntent);
                finish();
                break;
//            case MENU_CHANGE_LOGIN_PASSWORD:
//                Intent mainIntent2 = new Intent(MainActivity.this, LoginPEditActivity.class);
//                startActivity(mainIntent2);
//                setResult(RESULT_OK, mainIntent2);
//                finish();
//                break;
        }
        return true;
    }

    public void showAboutDialog(String title,String msg){
        AlertDialog.Builder builder= new AlertDialog.Builder(this);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setPositiveButton("确定", null);
        builder.create().show();
    }

    private CategoryAdapter mCustomBaseAdapter;

    private class ItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            Toast.makeText(getBaseContext(),  (String)mCustomBaseAdapter.getItem(position),
                    Toast.LENGTH_SHORT).show();
        }

    }




}
