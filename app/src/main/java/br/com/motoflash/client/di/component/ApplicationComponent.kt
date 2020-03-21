package br.com.motoflash.client.di.component

import android.app.Application
import android.content.Context
import br.com.motoflash.client.BaseApp
import br.com.motoflash.client.di.annotations.ApplicationContext
import br.com.motoflash.client.di.module.ApplicationModule
import br.com.motoflash.client.di.module.NetworkModule
import dagger.Component

@Component(modules =  [ApplicationModule::class, NetworkModule::class])
interface ApplicationComponent {

    fun inject(app: BaseApp)
    fun application(): Application
    @ApplicationContext
    fun context(): Context
}