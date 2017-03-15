package com.snowdream.folderchooser;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.snowdream.folderchooser.viewholder.ViewHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.provider.ContactsContract.Directory.PACKAGE_NAME;


public class FolderChooserDialog extends DialogFragment {

    private ListView folderListView;
    private List<String> paths = new ArrayList<String>();
    private FolderAdapter folderAdapter;
    private String root_path = "/";
    private String cur_path = "/";
    private String init_path = Environment.getExternalStorageDirectory().getPath()+"/Android/data/"+PACKAGE_NAME+"/Download/";

    private Button newFolderButton, cancelButton, chooseButton;
    private TextView titleTextView;
    private FolderChooseListener folderChooseListener;

    public interface FolderChooseListener {
        public void Choose(String filePath);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        lp.width = LinearLayout.LayoutParams.MATCH_PARENT;
        dialogWindow.setAttributes(lp);

        return dialog;
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        show(manager, tag, null);

    }

    public void show(FragmentManager manager, String tag, FolderChooseListener folderChooseListener) {
        super.show(manager, tag);
        this.init_path = tag;
        this.folderChooseListener = folderChooseListener;

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.cell_setting_folder_chooser_dialog, null);

        folderListView = (ListView) view.findViewById(R.id.lv_folders);
        newFolderButton = (Button) view.findViewById(R.id.bt_new_folder);
        cancelButton =(Button) view.findViewById(R.id.bt_cancel);
        chooseButton = (Button) view.findViewById(R.id.bt_choose);
        newFolderButton.setOnClickListener(clickListener);
        cancelButton.setOnClickListener(clickListener);
        chooseButton.setOnClickListener(clickListener);

        titleTextView = (TextView) view.findViewById(R.id.tv_title);

        folderAdapter = new FolderAdapter(paths);
        folderListView.setAdapter(folderAdapter);

        getFileDir(init_path);

        folderListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position >= paths.size())  return;
                if (position == 0) {
                    getFileDir(new File(cur_path).getParent());
                }else{
                    getFileDir(cur_path + "/" + paths.get(position));
                }
            }
        });

        return view;
    }

    private void getFileDir(String filePath) {
        paths.clear();

        if (!filePath.equals(root_path)) {
            paths.add("...");
        }

        if (!(new File(filePath)).exists()) {
            (new File(filePath)).mkdirs();
        }

        File file = new File(filePath);
        File[] files = file.listFiles();
//        if(files.length <= 0) {
//            return;
//        }
        for (int i = 0; i < files.length; i++) {
            //过滤一遍
            //1.是否为文件夹
            //2.是否可访问
            if (files[i].isDirectory() && files[i].listFiles() != null) {
                paths.add(files[i].getName());
            }
        }
        cur_path = filePath;
        titleTextView.setText(filePath);

        folderAdapter.notifyDataSetChanged();

    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.bt_new_folder:
                    new MaterialDialog.Builder(getContext())
                            .title("New Folder")
                            .input(0, 0, false, new MaterialDialog.InputCallback() {
                                @Override
                                public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                    File file = new File(new File(cur_path), input.toString());
                                    String path = file.getPath();
                                    if (!new File(path).exists()) {
                                        new File(path).mkdirs();
                                    }
                                    getFileDir(path);

                                }
                            })
                            .autoDismiss(true)
                            .show();
                    break;
                case R.id.bt_cancel:
                    dismiss();
                    break;
                case R.id.bt_choose:
                    folderChooseListener.Choose(cur_path);
                    dismiss();
                    break;
            }
        }
    };

    private class FolderAdapter extends BaseAdapter {

        private List<String> paths;

        public FolderAdapter(List<String> paths) {
            this.paths = paths;
        }

        @Override
        public int getCount() {
            return paths.size(); }

        @Override
        public long getItemId(int position) { return position; }

        @Override
        public Object getItem(int position) { return paths.get(position); }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String path = paths.get(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.cell_setting_lv_folder, null);
            }

            TextView folderTextView = (TextView) ViewHolder.get(convertView, R.id.tv_folder);
            folderTextView.setText(path);

            return convertView;
        }

    }

}
