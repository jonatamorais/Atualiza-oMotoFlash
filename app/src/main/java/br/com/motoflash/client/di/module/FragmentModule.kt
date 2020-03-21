package br.com.motoflash.client.di.module

import android.content.Context
import androidx.fragment.app.Fragment
import br.com.motoflash.client.di.annotations.ActivityContext
import br.com.motoflash.client.ui.history.HistoryMvpPresenter
import br.com.motoflash.client.ui.history.HistoryMvpView
import br.com.motoflash.client.ui.history.HistoryPresenter
import br.com.motoflash.client.ui.home.HomeMvpPresenter
import br.com.motoflash.client.ui.home.HomeMvpView
import br.com.motoflash.client.ui.home.HomePresenter
import br.com.motoflash.client.ui.profile.ProfileMvpPresenter
import br.com.motoflash.client.ui.profile.ProfileMvpView
import br.com.motoflash.client.ui.profile.ProfilePresenter
import dagger.Module
import dagger.Provides


@Module
class FragmentModule(private val mFragment: Fragment) {

    @Provides
    @ActivityContext
    internal fun provideContext(): Context {
        return mFragment.context!!
    }

    @Provides
    internal fun provideFragment(): Fragment {
        return mFragment
    }

    @Provides
    internal fun providProfileMvpPresenter(presenter: ProfilePresenter<ProfileMvpView>): ProfileMvpPresenter<ProfileMvpView> =
        presenter

    @Provides
    internal fun providHomeMvpPresenter(presenter: HomePresenter<HomeMvpView>): HomeMvpPresenter<HomeMvpView> =
        presenter

    @Provides
    internal fun providHistoryMvpPresenter(presenter: HistoryPresenter<HistoryMvpView>): HistoryMvpPresenter<HistoryMvpView> =
        presenter
}