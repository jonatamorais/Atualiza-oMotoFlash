package br.com.motoflash.core.ui.util

import android.content.DialogInterface

interface AlertInterface {

    fun onPositiveClick(dialog: DialogInterface, id: Int)

    fun onNegativeClick(dialog: DialogInterface, id: Int)
}