package jiang.okhttp;

/**
 * Create by JFZ
 * date: 2020-06-12 11:19
 **/
public class ResponseData<T> {

    public int code;
    public String msg;
    public T data;

    @Override
    public String toString() {
        return "ResponseData{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + ((data == null) ? "null" : data.toString()) +
                '}';
    }
}
