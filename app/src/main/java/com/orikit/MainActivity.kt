package com.orikit

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.orikit.databinding.ActivityMainBinding
import com.orikit.networks.NetworkInfoManager
import com.orikit.permissions.PermissionManager
import com.orikitx.OriKit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var networkInfoManager: NetworkInfoManager
    private lateinit var permissionManager: PermissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        networkInfoManager = NetworkInfoManager(this)
        permissionManager = PermissionManager(this)

        setupUI()
        setupClickListeners()
        registerMsgObserve()

        if (permissionManager.checkAndRequestPermissions()) {
            displayNetworkInfo()
        }
    }

    private fun registerMsgObserve() {
        OriKit.setMsgObserve {
            Toast.makeText(this, "Receive Msg >  $it", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupUI() {
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimaryDark)
        setSupportActionBar(binding.toolbar)

        // 设置设备信息
        val (model, version) = networkInfoManager.getDeviceInfo()
        binding.tvDeviceModel.text = model
        binding.tvAndroidVersion.text = version
    }

    private fun setupClickListeners() {
        binding.btnRefresh.setOnClickListener {
            if (permissionManager.hasLocationPermission()) {
                displayNetworkInfo()
                Toast.makeText(this, "信息已刷新", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "需要位置权限才能刷新", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnCopyIp.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("IP地址", binding.ipValueTextView.text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "IP地址已复制", Toast.LENGTH_SHORT).show()
        }

        binding.btnShare.setOnClickListener {
            networkInfoManager.shareNetworkInfo(this)
        }

        binding.btnConnect.setOnClickListener {
            testConnection()
        }
    }

    private fun displayNetworkInfo() {
        val networkInfo = networkInfoManager.getNetworkInfo()

        binding.ipValueTextView.text = networkInfo.ipAddress
        binding.wifiNameValueTextView.text = networkInfo.wifiName
        binding.macValueTextView.text = networkInfo.macAddress
        binding.networkTypeValueTextView.text = networkInfo.networkType

        updateNetworkStatusChip(networkInfo.isOnline)
    }

    private fun updateNetworkStatusChip(isOnline: Boolean) {
        binding.chipStatus.apply {
            text = if (isOnline) "在线" else "离线"
            val colorValue = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getColor(if (isOnline) R.color.chipOnline else R.color.chipOffline)
            } else {
                0
            }
            setBackgroundColor(colorValue)
        }
    }

    private fun testConnection() {
        Toast.makeText(this, "正在测试连接...", Toast.LENGTH_SHORT).show()
        // 这里可以添加实际的连接测试逻辑
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PermissionManager.LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    displayNetworkInfo()
                } else {
                    binding.wifiNameValueTextView.text = "需要位置权限获取WiFi信息"
                    binding.macValueTextView.text = "需要位置权限获取MAC地址"
                }
            }
        }
    }
}
