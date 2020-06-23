package nativedownload.download2;


import fz.okhttplib.tool.OkhttpUtil;

public abstract class DownLoadStatusCallbackAdapter<T> implements
        DownLoadStatusCallback<T> {

	public DownLoadStatusCallbackAdapter() {
		super();
	}

	@Override
	public void onStart(T bean) {
		if (bean != null) {
			OkhttpUtil.log("","#开始下载#" + bean);
		}
	}

	@Override
	public void onError(T bean) {
		if (bean != null) {
			OkhttpUtil.log("","#下载失败#" + bean);
		}
	}

	@Override
	public void onSuccess(T bean) {
		if (bean != null) {
			OkhttpUtil.log("","#下载成功#" + bean);
		}
	}

	@Override
	public void onFinished(T bean) {
		if (bean != null) {
			OkhttpUtil.log("","#下载完成#" + bean);
		}
	}

	@Override
	public void onStop(T bean) {
		if (bean != null) {
			OkhttpUtil.log("","#下载停止#" + bean);
		}
	}

	@Override
	public void onPause(T bean) {
		if (bean != null) {
			OkhttpUtil.log("","#下载暂停#" + bean);
		}
	}

	@Override
	public void onCancel(T bean) {
		if (bean != null) {
			OkhttpUtil.log("","#取消下载#" + bean);
		}
	}

	@Override
	public void onProgress(T bean, long currentSize) {
		if (bean != null) {
			OkhttpUtil.log("","#下载进度#" + currentSize);
		}
	}

	@Override
	public void onPrepare(T bean) {
		if (bean != null) {
			OkhttpUtil.log("","#下载准备#" + bean);
		}
	}

}
