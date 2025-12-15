package com.bodycamera.ba.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bodycamera.tests.R

class VeinResultActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_VEIN_RESULT = "vein_result"
        const val EXTRA_VEIN_ID = "vein_id"
        const val EXTRA_AUTH_MODE = "extra_auth_mode"
    }

    // UI
    private lateinit var ivResult: ImageView
    private lateinit var tvResultTitle: TextView
    private lateinit var tvNameLabel: TextView   // 氏名ラベル
    private lateinit var tvName: TextView        // 氏名表示
    private lateinit var tvIdLine: TextView      // ID: 12345 を1行で表示
    private lateinit var btnFinish: Button
    private lateinit var llRetryActions: LinearLayout
    private lateinit var btnRetry: Button
    private lateinit var btnBack: Button

    // Flow1 / Flow2 / Flow3
    private var currentAuthMode: String = ""
    private var faceName: String? = null          // Flow1 用
    private var faceId: String? = null            // Flow1 用

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vein_result)

        initViews()
        handleIntent()
        setupClickListeners()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == TopActivity.REQUEST_PALMSECURE && resultCode == Activity.RESULT_OK) {
            val veinResult = data?.getStringExtra(EXTRA_VEIN_RESULT)
            val veinId = data?.getStringExtra(EXTRA_VEIN_ID)
            updateUI(veinResult, veinId)
        }
    }

    private fun initViews() {
        ivResult = findViewById(R.id.ivResult)
        tvResultTitle = findViewById(R.id.tvResultTitle)
        tvNameLabel = findViewById(R.id.tvNameLabel)
        tvName = findViewById(R.id.tvName)
        tvIdLine = findViewById(R.id.tvIdLine)
        btnFinish = findViewById(R.id.btnFinish)
        llRetryActions = findViewById(R.id.llRetryActions)
        btnRetry = findViewById(R.id.btnRetry)
        btnBack = findViewById(R.id.btnBack)
    }

    private fun handleIntent() {

        val veinResult = intent.getStringExtra(EXTRA_VEIN_RESULT)
        val veinId = intent.getStringExtra(EXTRA_VEIN_ID)

        // Flow1: nhận thêm RESULTNAME + RESULTID từ camera app
        faceName = intent.getStringExtra("ResultName")
        faceId = intent.getStringExtra("ResultID")

        currentAuthMode =
            intent.getStringExtra(EXTRA_AUTH_MODE)
                ?: getSharedPreferences("AuthMode", MODE_PRIVATE)
                    .getString("AuthName", "") ?: ""

        val isSuccess = veinResult == "OK"

        ivResult.setImageResource(
            if (isSuccess) R.drawable.ic_check_circle else R.drawable.ic_error
        )
        ivResult.setColorFilter(
            resources.getColor(
                if (isSuccess) R.color.success_green else R.color.error_red,
                theme
            )
        )

        tvResultTitle.text =
            getString(if (isSuccess) R.string.auth_success else R.string.auth_failed)

        tvResultTitle.setTextColor(
            resources.getColor(
                if (isSuccess) R.color.success_green else R.color.error_red,
                theme
            )
        )

        // Flow1: Hiển thị 氏名 + ID (một dòng "ID: 12345")
        if (currentAuthMode == "Face" && !faceName.isNullOrEmpty()) {
            tvNameLabel.visibility = View.VISIBLE
            tvName.visibility = View.VISIBLE
            tvName.text = faceName

            if (!faceId.isNullOrEmpty()) {
                tvIdLine.text = "ID: $faceId"
                tvIdLine.visibility = View.VISIBLE
            } else {
                tvIdLine.visibility = View.GONE
            }

            showButtons(isSuccess = true)     // Flow1 luôn nút 終了
            return
        }

        // Flow2 & Flow3: chỉ hiển thị ID nếu có, không dùng name
        if (isSuccess && !veinId.isNullOrEmpty()) {
            tvIdLine.text = "ID: $veinId"
            tvIdLine.visibility = View.VISIBLE
        } else {
            tvIdLine.visibility = View.GONE
        }

        tvNameLabel.visibility = View.GONE
        tvName.visibility = View.GONE

        showButtons(isSuccess)
    }

    private fun showButtons(isSuccess: Boolean) {
        if (isSuccess) {
            btnFinish.visibility = View.VISIBLE
            llRetryActions.visibility = View.GONE
        } else {
            btnFinish.visibility = View.GONE
            llRetryActions.visibility = View.VISIBLE
        }
    }

    private fun setupClickListeners() {

        // 成功時：「終了」→ TopActivityへ
        btnFinish.setOnClickListener {
            startActivity(
                Intent(this, TopActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            )
            finish()
        }

        // 失敗時：「再実行」
        btnRetry.setOnClickListener {

            when (currentAuthMode) {

                // Flow1: retry → chạy lại Face (quy về FaceAndVein để Top xử lý được)
                "Face" -> {
                    val intent = Intent(this, TopActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        putExtra(TopActivity.EXTRA_RETRY_FLOW, "FaceAndVein")
                    }
                    startActivity(intent)
                    finish()
                }

                // Flow2 → Vein retry
                "Vein" -> {
                    val intent = Intent(this, TopActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        putExtra(TopActivity.EXTRA_RETRY_FLOW, "Vein")
                    }
                    startActivity(intent)
                    finish()
                }

                // Flow3 → Face+Vein retry
                "FaceAndVein" -> {
                    val intent = Intent(this, TopActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        putExtra(TopActivity.EXTRA_RETRY_FLOW, "FaceAndVein")
                    }
                    startActivity(intent)
                    finish()
                }
            }
        }


        // 「戻る」→ TopActivity
        btnBack.setOnClickListener {
            startActivity(
                Intent(this, TopActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            )
            finish()
        }
    }

    // PalmSecure 再実行から返ってきた結果更新
    private fun updateUI(veinResult: String?, veinId: String?) {

        val isSuccess = veinResult == "OK"

        ivResult.setImageResource(
            if (isSuccess) R.drawable.ic_check_circle else R.drawable.ic_error
        )
        ivResult.setColorFilter(
            resources.getColor(
                if (isSuccess) R.color.success_green else R.color.error_red,
                theme
            )
        )

        tvResultTitle.text =
            getString(if (isSuccess) R.string.auth_success else R.string.auth_failed)

        if (isSuccess && !veinId.isNullOrEmpty()) {
            tvIdLine.text = "ID: $veinId"
            tvIdLine.visibility = View.VISIBLE
        } else {
            tvIdLine.visibility = View.GONE
        }

        tvNameLabel.visibility = View.GONE
        tvName.visibility = View.GONE

        showButtons(isSuccess)
    }
}
