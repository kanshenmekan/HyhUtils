@file:Suppress("MissingPermission")

package com.huyuhui.hyhutilskotlin.net

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

/**
 * 网络状态检查扩展函数
 */
val Context.connectivityManager get() = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

// 检查网络是否连接
fun Context.isNetworkConnected(): Boolean {
    val activeNetwork = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
    return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
}

// 获取当前网络类型
fun Context.getNetworkType(): NetworkType {
    if (!isNetworkConnected()) return NetworkType.NONE

    val activeNetwork = connectivityManager.activeNetwork ?: return NetworkType.UNKNOWN
    val capabilities =
        connectivityManager.getNetworkCapabilities(activeNetwork) ?: return NetworkType.UNKNOWN

    return when {
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.CELLULAR
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkType.ETHERNET
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> NetworkType.VPN
        else -> NetworkType.UNKNOWN
    }
}

// 检查是否计量网络
fun Context.isMeteredNetwork(): Boolean {
    val activeNetwork = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
    return !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
}

// 检查是否使用VPN
fun Context.isUsingVPN(): Boolean {
    val activeNetwork = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
    return capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) ?: false
}

// 获取网络性能评估
fun Context.getNetworkPerformance(): NetworkPerformance {
    if (!isNetworkConnected()) return NetworkPerformance.UNKNOWN

    val activeNetwork = connectivityManager.activeNetwork ?: return NetworkPerformance.UNKNOWN
    val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        ?: return NetworkPerformance.UNKNOWN

    return when {
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
            when (capabilities.linkDownstreamBandwidthKbps) {
                in 50000..Int.MAX_VALUE -> NetworkPerformance.EXCELLENT
                in 10000..49999 -> NetworkPerformance.GOOD
                else -> NetworkPerformance.MODERATE
            }
        }

        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
            if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_TEMPORARILY_NOT_METERED)) {
                NetworkPerformance.GOOD
            } else {
                when (capabilities.linkDownstreamBandwidthKbps) {
                    in 10000..Int.MAX_VALUE -> NetworkPerformance.GOOD
                    in 2000..9999 -> NetworkPerformance.MODERATE
                    else -> NetworkPerformance.POOR
                }
            }
        }

        else -> NetworkPerformance.UNKNOWN
    }
}

enum class NetworkType {
    WIFI, CELLULAR, ETHERNET, VPN, UNKNOWN, NONE
}

enum class NetworkPerformance {
    UNKNOWN, POOR, MODERATE, GOOD, EXCELLENT
}


class NetworkStateMonitor(val context: Context) {
    private val connectivityManager = context.connectivityManager
    private var networkCallback: MonitorNetworkCallback? = null
    private var listener: NetworkStateListener? = null
    private var lifecycleObserver: LifecycleEventObserver? = null
    private var lastPerformance: NetworkPerformance? = null

    // 带生命周期感知的注册
    fun registerListener(owner: LifecycleOwner, listener: NetworkStateListener) {
        lifecycleObserver?.let { owner.lifecycle.removeObserver(it) }
        registerListener(listener)
        lifecycleObserver = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                owner.lifecycle.removeObserver(lifecycleObserver!!)
                unregisterListener()
            }
        }.also {
            owner.lifecycle.addObserver(it)
        }
    }

    /**
     * 注册网络状态监听（手动取消）
     */
    fun registerListener(listener: NetworkStateListener) {
        this.listener = listener
        registerNetworkCallback()
    }

    /**
     * 取消注册网络状态监听
     */
    fun unregisterListener() {
        unregisterNetworkCallback()
        listener = null
    }

    private fun registerNetworkCallback() {
        if (networkCallback != null) return
        networkCallback = MonitorNetworkCallback().also {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_VPN)
                .build()
            connectivityManager.registerNetworkCallback(request, it)
        }
    }

    private fun updatePerformance() {
        val currentPerformance = context.getNetworkPerformance()
        if (lastPerformance != currentPerformance) {
            listener?.onNetworkPerformanceChanged(currentPerformance)
            lastPerformance = currentPerformance
        }
    }

    private fun unregisterNetworkCallback() {
        networkCallback?.let {
            connectivityManager.unregisterNetworkCallback(it)
            networkCallback = null
        }
    }

    private inner class MonitorNetworkCallback : ConnectivityManager.NetworkCallback() {
        // 网络变为可用状态
        override fun onAvailable(network: Network) {
            val networkType = context.getNetworkType()
            listener?.onNetworkAvailable(networkType)
            updatePerformance()
        }

        // 网络丢失
        override fun onLost(network: Network) {
            if (!context.isNetworkConnected()) {
                listener?.onNetworkLost()
            }
            updatePerformance()
        }

        // 网络能力变化（如带宽变化、网络类型切换等）
        override fun onCapabilitiesChanged(
            network: Network,
            capabilities: NetworkCapabilities
        ) {
            val networkType = context.getNetworkType()
            listener?.onNetworkCapabilitiesChanged(networkType, capabilities)
            updatePerformance()
        }

        // 网络阻塞状态变化
        override fun onBlockedStatusChanged(network: Network, blocked: Boolean) {
            super.onBlockedStatusChanged(network, blocked)
            // blocked为true表示网络被系统或用户阻止
            listener?.onNetworkBlockedStatusChanged(context.getNetworkType(), blocked)
        }

        // 网络即将丢失（提前警告）
        override fun onLosing(network: Network, maxMsToLive: Int) {
            super.onLosing(network, maxMsToLive)
            // 在网络完全断开前收到通知
            // maxMsToLive: 预计多少毫秒后网络将断开
            listener?.onNetworkLosing(context.getNetworkType(), maxMsToLive)
        }

        // 网络不可用（请求的网络不可用）
        override fun onUnavailable() {
            super.onUnavailable()
            listener?.onNetworkUnavailable()
        }

        // 网络链接属性变化（如IP地址、DNS等变化）
        override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
            super.onLinkPropertiesChanged(network, linkProperties)
            listener?.onNetworkLinkPropertiesChanged(context.getNetworkType(), linkProperties)
        }
    }

    interface NetworkStateListener {
        // 基本回调
        fun onNetworkAvailable(networkType: NetworkType)
        fun onNetworkLost()
        fun onNetworkCapabilitiesChanged(
            networkType: NetworkType,
            capabilities: NetworkCapabilities
        )

        fun onNetworkPerformanceChanged(performance: NetworkPerformance)

        // 高级回调
        fun onNetworkLinkPropertiesChanged(
            networkType: NetworkType,
            linkProperties: LinkProperties
        ) {
        }

        fun onNetworkLosing(networkType: NetworkType, maxMsToLive: Int) {}
        fun onNetworkUnavailable() {}
        fun onNetworkBlockedStatusChanged(networkType: NetworkType, blocked: Boolean) {}
    }
}




