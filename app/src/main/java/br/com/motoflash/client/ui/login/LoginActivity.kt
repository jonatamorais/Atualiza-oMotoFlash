package br.com.motoflash.client.ui.login

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.core.view.ViewCompat
import androidx.core.view.ViewPropertyAnimatorCompat
import br.com.motoflash.client.R
import br.com.motoflash.client.ui.base.BaseActivity
import br.com.motoflash.core.data.network.model.ErrorCode
import br.com.motoflash.core.ui.util.hideItems
import br.com.motoflash.core.ui.util.showSnack
import kotlinx.android.synthetic.main.activity_login.*
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.schedule
import android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.view.inputmethod.InputMethodManager
import br.com.motoflash.client.ui.main.MainActivity
import br.com.motoflash.core.ui.util.Mask
import br.com.motoflash.core.ui.util.unMaskOnlyNumbers


class LoginActivity : BaseActivity(), LoginMvpView {

    @Inject
    lateinit var presenter: LoginMvpPresenter<LoginMvpView>

    private var checkedEmail = false
    private var newUser = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        activityComponent.inject(this)
        presenter.onAttach(this)

        setUp()
    }

    override fun setUp() {
        edtEmail.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(p0!=null){
                    val text = p0.toString()
                    if(text.contains('@') && text.contains('.')){
                        btnNext.visibility = View.VISIBLE
                    }else{
                        btnNext.visibility = View.GONE
                    }
                }else{
                    btnNext.visibility = View.GONE
                }
            }
        })

        val mobilePhoneMask = Mask.insert(Mask.MOBILE_PHONE_MASK, edtMobilePhone)
        edtMobilePhone.addTextChangedListener(mobilePhoneMask)

        btnNext.setOnClickListener {
            val inputManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            inputManager.hideSoftInputFromWindow(
                currentFocus!!.windowToken,
                HIDE_NOT_ALWAYS
            )
            showLoading()
            if(checkedEmail){
                if(newUser){
                    val email = edtEmail.text.toString()
                    val password = edtPassword.text.toString()
                    val confirmPassword = edtPasswordConfirm.text.toString()
                    val name = edtName.text.toString()
                    val mobilePhone = edtMobilePhone.text.toString()

                    if(password.length > 5){
                        if(confirmPassword == password){
                            if(name.isNotBlank()){
                                if(mobilePhone.unMaskOnlyNumbers().length == 11){
                                    presenter.doCreatedUser(
                                        email = email,
                                        password = password,
                                        mobilePhone = String.format("+55%s",mobilePhone.unMaskOnlyNumbers()),
                                        name = name
                                    )
                                }else{
                                    hideLoading()
                                    "Número de celular inválido".showSnack(container, backgroundColor = R.color.colorRed)
                                }
                            }else{
                                hideLoading()
                                "Informe um nome".showSnack(container, backgroundColor = R.color.colorRed)
                            }
                        }else{
                            hideLoading()
                            "As senhas devem ser iguais".showSnack(container, backgroundColor = R.color.colorRed)
                        }
                    }else{
                        hideLoading()
                        "A senha deve ter 6 caracteres no mínimo".showSnack(container, backgroundColor = R.color.colorRed)
                    }
                }else{
                    if(edtPassword.text.toString().isNotBlank()){
                        presenter.doLoginUser(edtEmail.text.toString(), edtPassword.text.toString())
                    }else{
                        hideLoading()
                        "Informe uma senha!".showSnack(container, backgroundColor = R.color.colorRed)
                    }
                }
            }else{
                presenter.doVerifyUser(edtEmail.text.toString())
            }
        }
    }

    override fun onVerfyUser(exists: Boolean) {
        hideLoading()
        checkedEmail = true
        newUser = !exists
        if(newUser){
            "Vamos criar uma nova conta!".showSnack(container, backgroundColor = R.color.colorPurple)
            for (i in 0 until containerLoginFields.childCount) {
                val v = containerLoginFields.getChildAt(i)

                if(v.scaleX == 0f && v !is ProgressBar){
                    v.visibility = View.VISIBLE
                    val viewAnimator: ViewPropertyAnimatorCompat = ViewCompat.animate(v)
                        .scaleY(1f).scaleX(1f)
                        .setStartDelay(100L * i)
                        .setDuration(500)

                    viewAnimator.setInterpolator(DecelerateInterpolator()).start()
                }
            }
        }else{
            "Bem-vindo de volta, informe suas credenciais!".showSnack(container, backgroundColor = R.color.colorPurple)
            inputPassword.visibility = View.VISIBLE
            val viewAnimator: ViewPropertyAnimatorCompat = ViewCompat.animate(inputPassword)
                .scaleY(1f).scaleX(1f)
                .setDuration(500)
            viewAnimator.setInterpolator(DecelerateInterpolator()).start()
        }
    }

    override fun onLoginUser() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onLoginInvalidCredentials() {
        "Credenciais inválidos, tente novamente".showSnack(container, backgroundColor = R.color.colorRed)
        hideLoading()
    }

    override fun onLoginFailGeneric() {
        hideLoading()
        "Falha ao realizar login, tente novamente".showSnack(container, backgroundColor = R.color.colorRed)
    }

    override fun onCreatedUser() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onCreatedUserFail() {
        "Falha ao criar sua conta, tente novamente".showSnack(container, backgroundColor = R.color.colorRed)
        hideLoading()
    }

    override fun onCreatedUserInvalidCredentials(errorCode: ErrorCode) {

        if(errorCode.field == "mobilePhone"){
            "Número de celular já cadastrado!".showSnack(container, backgroundColor = R.color.colorRed)
        }

        if(errorCode.field == "email"){
            "E-mail já cadastrado!".showSnack(container, backgroundColor = R.color.colorRed)
        }
        hideLoading()
    }

    override fun hideLoading() {
        progress.visibility = View.GONE
    }

    override fun showLoading() {
        progress.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDetach()
    }
}
