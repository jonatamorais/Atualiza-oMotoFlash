package br.com.motoflash.courier.di.module

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import br.com.motoflash.courier.di.annotations.ActivityContext
import br.com.motoflash.courier.ui.login.LoginMvpPresenter
import br.com.motoflash.courier.ui.login.LoginMvpView
import br.com.motoflash.courier.ui.login.LoginPresenter
import br.com.motoflash.courier.ui.main.MainMvpPresenter
import br.com.motoflash.courier.ui.main.MainMvpView
import br.com.motoflash.courier.ui.main.MainPresenter
import br.com.motoflash.courier.ui.splash.SplashMvpPresenter
import br.com.motoflash.courier.ui.splash.SplashMvpView
import br.com.motoflash.courier.ui.splash.SplashPresenter
import br.com.motoflash.courier.ui.workorder.alert.AlertMvpPresenter
import br.com.motoflash.courier.ui.workorder.alert.AlertMvpView
import br.com.motoflash.courier.ui.workorder.alert.AlertPresenter
import dagger.Module
import dagger.Provides


@Module
class ActivityModule(private val mActivity: AppCompatActivity) {

    @Provides
    @ActivityContext
    internal fun provideContext(): Context {
        return mActivity
    }

    @Provides
    internal fun provideActivity(): AppCompatActivity {
        return mActivity
    }

    @Provides
    internal fun providSplashMvpPresenter(presenter: SplashPresenter<SplashMvpView>): SplashMvpPresenter<SplashMvpView> =
        presenter

    @Provides
        internal fun providMainMvpPresenter(presenter: MainPresenter<MainMvpView>): MainMvpPresenter<MainMvpView> =
        presenter

    @Provides
    internal fun providLoginMvpPresenter(presenter: LoginPresenter<LoginMvpView>): LoginMvpPresenter<LoginMvpView> =
        presenter

    @Provides
    internal fun providAlertMvpPresenter(presenter: AlertPresenter<AlertMvpView>): AlertMvpPresenter<AlertMvpView> =
        presenter
}