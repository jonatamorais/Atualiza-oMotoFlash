package br.com.motoflash.courier.di.component

import br.com.motoflash.courier.di.annotations.PerActivity
import br.com.motoflash.courier.di.module.FragmentModule
import br.com.motoflash.courier.ui.history.HistoryFragment
import br.com.motoflash.courier.ui.home.HomeFragment
import br.com.motoflash.courier.ui.profile.ProfileFragment
import dagger.Component


@PerActivity
@Component(
    dependencies = [ApplicationComponent::class],
    modules = [FragmentModule::class]
)
interface FragmentComponent {
    fun inject(profileFragment: ProfileFragment)
    fun inject(historyFragment: HistoryFragment)
    fun inject(homeFragment: HomeFragment)
}
