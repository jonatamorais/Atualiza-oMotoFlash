package br.com.motoflash.courier.di.component

import br.com.motoflash.courier.di.annotations.PerActivity
import br.com.motoflash.courier.di.module.ActivityModule
import br.com.motoflash.courier.ui.login.LoginActivity
import br.com.motoflash.courier.ui.main.MainActivity
import br.com.motoflash.courier.ui.splash.SplashActivity
import br.com.motoflash.courier.ui.workorder.alert.AlertActivity
import dagger.Component


@PerActivity
@Component(
    dependencies = [ApplicationComponent::class],
    modules = [ActivityModule::class]
)
interface ActivityComponent {
    fun inject(splashActivity: SplashActivity)
    fun inject(mainActivity: MainActivity)
    fun inject(loginActivity: LoginActivity)
    fun inject(alertActivity: AlertActivity)
}
