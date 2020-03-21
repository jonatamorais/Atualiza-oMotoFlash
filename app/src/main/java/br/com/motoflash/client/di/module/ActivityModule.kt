package br.com.motoflash.client.di.module

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import br.com.motoflash.client.di.annotations.ActivityContext
import br.com.motoflash.client.ui.detail.WorkOrderDetailMvpPresenter
import br.com.motoflash.client.ui.detail.WorkOrderDetailMvpView
import br.com.motoflash.client.ui.detail.WorkOrderDetailPresenter
import br.com.motoflash.client.ui.login.LoginMvpPresenter
import br.com.motoflash.client.ui.login.LoginMvpView
import br.com.motoflash.client.ui.login.LoginPresenter
import br.com.motoflash.client.ui.main.MainMvpPresenter
import br.com.motoflash.client.ui.main.MainMvpView
import br.com.motoflash.client.ui.main.MainPresenter
import br.com.motoflash.client.ui.splash.SplashMvpPresenter
import br.com.motoflash.client.ui.splash.SplashMvpView
import br.com.motoflash.client.ui.splash.SplashPresenter
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
    internal fun providWorkOrderDetailMvpPresenter(presenter: WorkOrderDetailPresenter<WorkOrderDetailMvpView>): WorkOrderDetailMvpPresenter<WorkOrderDetailMvpView> =
        presenter
}