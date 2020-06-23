package jiang.okhttp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import fz.okhttplib.HttpImpl;
import fz.okhttplib.base.OkHttpBaseRequest;
import fz.okhttplib.callback.OkRequestCallback;
import fz.okhttplib.file.download.DownLoadListenerAdapter;
import fz.okhttplib.tool.DefLoad;
import fz.okhttplib.tool.OkhttpUtil;
import nativedownload.NativeDownload;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
            }, 100);
        }
    }

    public void download(View view) {
        File video = getExternalFilesDir("video");
        OkhttpUtil.log("下载", "路径:" + video.getPath());
        String name = System.currentTimeMillis() + ".mp4";
        NativeDownload.download("http://cdnvideo.dev.koibone.com/fd22ac03vodcq1400255844/14c9d38b5285890804190093348/DkfiREPG21YA.mp4",
                video.getPath() + File.separator,
                name,
                new DownLoadListenerAdapter() {
                    @Override
                    public void update(long progress, float percent, long contentLength, boolean done) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Button button = (Button) view;
                                button.setText("download(" + percent + "%)");
                            }
                        });
                    }

                    @Override
                    public void complete(File file) {
                        OkhttpUtil.log("download", "成功:" + file.getPath());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Button button = (Button) view;
                                button.setText("download(完成)");
                                Toast.makeText(MainActivity.this, "下载成功", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void error(Exception e) {
                        OkhttpUtil.log("download", "失败:" + e.getMessage());
                    }
                });
    }

    public void get(View view) {
        HashMap<String, Object> params = new HashMap<>();

        HttpImpl.get("http://www.baidu,com")
                .bind(this)//绑定生命周期
                .load(DefLoad.use(this))//使用loading
                .request(new OkHttpBaseRequest(params))//设置请求参数
                .enqueue(new OkRequestCallback<String>() {
                    @Override
                    public void onResponse(String data) {
                        System.out.println("请求成功:" + data);
                    }

                    @Override
                    public void onError(int code, Exception e) {
                        System.out.println(code + "请求失败:" + e.getMessage());
                    }
                });

    }

    public void postJson(View view) {
        Login login = new Login("张三", "123456");
        HttpImpl.postJson("https://xxxx.xxxx.xxx.xxxx")
                .bind(this)
                .request(new OkHttpBaseRequest(login))
                .enqueue(new OkRequestCallback<ResponseData<LoginResp>>() {
                    @Override
                    public void onResponse(ResponseData<LoginResp> data) {
                        System.out.println("请求成功:" + data.toString());
                    }

                    @Override
                    public void onError(int code, Exception e) {
                        System.out.println(code + "请求失败:" + e.getMessage());
                    }
                });

    }


    class Login {

        private String name, password;

        public Login(String name, String password) {
            this.name = name;
            this.password = password;
        }
    }
}
