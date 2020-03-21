package br.com.motoflash.client.di.component

import br.com.motoflash.client.ui.maps.MapsActivity
import br.com.motoflash.client.di.annotations.PerActivity
import br.com.motoflash.client.di.module.ActivityModule
import br.com.motoflash.client.ui.detail.WorkOrderDetailActivity
import br.com.motoflash.client.ui.login.LoginActivity
import br.com.motoflash.client.ui.main.MainActivity
import br.com.motoflash.client.ui.splash.SplashActivity
import dagger.Component


@PerActivity
@Component(
    dependencies = [ApplicationComponent::class],
    modules = [ActivityModule::class]
)
interface ActivityComponent {
    fun inject(splashActivity: SplashActivity)
    fun inject(loginActivity: LoginActivity)
    fun inject(mainActivity: MainActivity)
    fun inject(workOrderDetailActivity: WorkOrderDetailActivity)
}
