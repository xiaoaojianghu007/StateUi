package com.yzy.stateui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by 杨振宇
 * github地址：https://github.com/1015156849
 * on 2016/12/21.
 */

public class BaseUIFragment extends Fragment {

    public String TAG;
    public String title;
    private HashMap<Integer, StateUiFragment> uiFragmentHashMap = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        TAG = this.getClass().getName();
        return super.onCreateView(inflater, container, savedInstanceState);

    }
    public String getTitle() {
        return TextUtils.isEmpty(title) ? "" : title;
    }

    public BaseUIFragment setTitle(String title) {
        this.title = title;
        return this;
    }

    /**
     * @param containerViewId 容器id
     * @param state  状态布局，定义在StateUi类中
     */
    public void ShowStateUi(int containerViewId, int state) {

        HideStateUi();
        if (state != StateUi.ShowDatasView) {
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();

            if (uiFragmentHashMap.get(state) == null) {
                Log.e(TAG,"初始化StateUi");
                StateUiFragment stateUiFragment = new StateUiFragment(state);
                uiFragmentHashMap.put(state, stateUiFragment);
                transaction.add(containerViewId, stateUiFragment, String.valueOf(state));
                transaction.commit();
            } else {//如果已经存在
                Log.e(TAG,"显示StateUi");
                transaction.show(uiFragmentHashMap.get(state));//那就显示
                transaction.commit();
            }
        }
    }

    public void setOnClick(final int state, final View.OnClickListener onClickListener, final List<Integer> ViewIds) {

        Observable.create(new Observable.OnSubscribe<View>() {

            @Override
            public void call(Subscriber<? super View> subscriber) {
                subscriber.onNext(getStateLayout(state));
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.computation()).subscribe(new Observer<View>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "调用onNext失败");
            }

            @Override
            public void onNext(final View view) {
                if (view != null) {
                    Observable.from(ViewIds).subscribeOn(Schedulers.computation()).subscribe(new Action1<Integer>() {
                        @Override
                        public void call(Integer integer) {
                            if (view != null) {
                                view.findViewById(integer).setOnClickListener(onClickListener);
                            }
                        }
                    });
                }
            }
        });

    }


    public View getStateLayout(int state) {
        if (uiFragmentHashMap.size() > 0 & uiFragmentHashMap.get(state) != null) {
            boolean ifFor = true;
            while (ifFor) {
                if (uiFragmentHashMap.get(state).getStateLayout() != null) {
                    ifFor = false;
                }
            }
            return uiFragmentHashMap.get(state).getStateLayout();
        } else {
            Log.e(TAG, "请先初始化（show）对应的StateUi后再配置点击事件");
            return null;
        }
    }

    public void HideStateUi() {
        if (uiFragmentHashMap.size() > 0) {
            /**如果是Fragment，使用getChildFragmentManager()*/
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            Iterator iter = uiFragmentHashMap.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                Object val = entry.getValue();
                transaction.hide((StateUiFragment) val);
            }
            transaction.commit();
        } else {
            Log.e(TAG, "没有需要隐藏的StateUI");
        }

    }

}
