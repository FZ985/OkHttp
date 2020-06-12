package jiang.okhttp;

import androidx.appcompat.app.AppCompatActivity;
import fz.okhttplib.HttpImpl;
import fz.okhttplib.base.OkHttpBaseRequest;
import fz.okhttplib.callback.OkRequestCallback;
import fz.okhttplib.tool.DefLoad;

import android.os.Bundle;
import android.view.View;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
