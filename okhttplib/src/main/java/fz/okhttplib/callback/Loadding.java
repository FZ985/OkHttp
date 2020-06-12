package fz.okhttplib.callback;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;

/**
 * Created by JFZ .
 * on 2018/1/15.
 */

public abstract class Loadding {

    public abstract void show();

    public abstract void dismiss();

    public abstract boolean isShowing();

    protected Activity scanForActivity(Context cont) {
        if (cont == null)
            return null;
        else if (cont instanceof Activity)
            return (Activity) cont;
        else if (cont instanceof ContextWrapper)
            return scanForActivity(((ContextWrapper) cont).getBaseContext());
        return null;
    }
}
