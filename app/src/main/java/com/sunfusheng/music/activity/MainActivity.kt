package com.sunfusheng.music.activity

import android.os.Bundle
import android.text.TextUtils
import com.sunfusheng.music.Constants
import com.sunfusheng.music.R
import com.sunfusheng.music.http.Api
import com.sunfusheng.music.model.BillboardModel
import com.sunfusheng.music.model.MusicModel
import com.sunfusheng.music.util.Util
import com.sunfusheng.music.viewbinder.MusicItemViewBinder
import com.sunfusheng.music.viewbinder.MusicTitleViewBinder
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import me.drakeet.multitype.Items

class MainActivity : BaseActivity() {

    var items: Items = Items()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerViewWrapper.register(BillboardModel::class.java, MusicTitleViewBinder())
        recyclerViewWrapper.register(MusicModel::class.java, MusicItemViewBinder())

        Observable.defer { Observable.fromIterable(Constants.musicTypeList) }
                .subscribeOn(Schedulers.io())
                .flatMap { it ->
                    Api.instance.apiService.getMusicList(it.type, Constants.REQUEST_SIZE, 0)
                            .subscribeOn(Schedulers.io())
                            .filter { !Util.isEmpty(it.song_list) }
                            .filter { !TextUtils.isEmpty(it.billboard.name) }
                            .filter { it.billboard.billboard_songnum > Constants.REQUEST_SIZE }
                            .doOnNext {
                                items.add(it.billboard)
                                items.addAll(it.song_list)
                            }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    recyclerViewWrapper.setData(items)
                }, Throwable::printStackTrace)
    }
}
