package com.dawn.kotlinbasedemo.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dawn.kotlinbasedemo.http.Api
import com.dawn.kotlinbasedemo.http.ApiException
import com.dawn.kotlinbasedemo.http.ApiInterface
import com.dawn.kotlinbasedemo.http.BaseResponse
import com.dawn.kotlinbasedemo.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

open class BaseViewModel : ViewModel() {

     val loadingEvent = MutableLiveData<Boolean>()


    /**
     * 简单的请求，没有加try-catch捕获异常，使用的时候切记！！！！！需要手动try-catch或使用runCatching方法捕获
     * @receiver BaseViewModel
     * @param request [@kotlin.ExtensionFunctionType] SuspendFunction1<ApiInterface, BaseResponse<T>?>
     * @return BaseResponse<T>
     */
    suspend fun <T> requestSimple(request: suspend ApiInterface.() -> BaseResponse<T>?): BaseResponse<T> {
        /*withContext表示挂起块  配合Retrofit声明的suspend函数执行 该块会挂起直到里面的网络请求完成 最一行就是返回值*/
        val response = withContext(Dispatchers.IO) {
            /*扩展函数可以很方便的解析出我们想要的数据  接口很多的情况下下可以节省不少无用代码*/
            request(Api)
        } ?: throw IllegalArgumentException("数据非法，获取响应数据为空")
        if (response.errorCode != 0) {
            throw  ApiException(response.errorCode, response.errorMsg ?: "")
        }
        return response;
    }

    suspend fun <T> request(
        showLoading: Boolean = true,
        request: suspend ApiInterface.() -> BaseResponse<T>?
    ): BaseResponse<T> {
        return try {
            if (showLoading) {
                showLoading()
            }
            requestSimple(request);

        } catch (e: Exception) {
            toast(e.message?:"null")
            BaseResponse<T>().apply {
                exception =e;
            };
        } finally {
            closeLoading()
        }
    }




     private fun showLoading() {
        loadingEvent.value = true
    }

     private fun closeLoading() {
        loadingEvent.value = false
    }
}