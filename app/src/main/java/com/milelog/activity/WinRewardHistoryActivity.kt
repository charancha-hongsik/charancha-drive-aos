package com.milelog.activity

import WinRewardHistoryResponse
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.milelog.DividerItemDecoration
import com.milelog.R
import com.milelog.viewmodel.BaseViewModel
import com.milelog.viewmodel.WinRewardHistoryViewModel
import com.milelog.viewmodel.state.GetWinRewardHistoryMoreState
import com.milelog.viewmodel.state.GetWinRewardHistoryState

class WinRewardHistoryActivity:BaseRefreshActivity() {
    lateinit var btn_back: View
    lateinit var layout_no_data:ConstraintLayout
    lateinit var lv_win_reward:RecyclerView
    lateinit var layout_filter:ConstraintLayout
    private val winRewardHistoryViewModel: WinRewardHistoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_win_reward)

        init()
        setListener()
        setObserver()

    }

    private fun init(){
        btn_back = findViewById(R.id.btn_back)
        layout_no_data = findViewById(R.id.layout_no_data)
        lv_win_reward = findViewById(R.id.lv_win_reward)
        layout_filter = findViewById(R.id.layout_filter)

        winRewardHistoryViewModel.init(applicationContext)
        winRewardHistoryViewModel.getHistories()
    }

    private fun setListener(){
        btn_back.setOnClickListener(object:OnSingleClickListener(){
            override fun onSingleClick(v: View?) {
                finish()
            }
        })
    }

    private fun setObserver(){
        winRewardHistoryViewModel.winRewardHistoryResult.observe(this, BaseViewModel.EventObserver { state ->
            when (state) {
                is GetWinRewardHistoryState.Loading -> {

                }
                is GetWinRewardHistoryState.Success -> {
                    if(state.data.items.size == 0){
                        setBlank()
                    }else{
                        Log.d("testsetestest","testestesestset winRewardHistoryResponse :: " + state.data.items.size)

                        setRecyclerviewData(state.data)
                    }
                }
                is GetWinRewardHistoryState.Error -> {

                }
                is GetWinRewardHistoryState.Empty -> {

                }
            }
        })

        winRewardHistoryViewModel.winRewardHistoryMoreResult.observe(this, BaseViewModel.EventObserver { state ->
            when (state) {
                is GetWinRewardHistoryMoreState.Loading -> {

                }
                is GetWinRewardHistoryMoreState.Success -> {

                }
                is GetWinRewardHistoryMoreState.Error -> {

                }
                is GetWinRewardHistoryMoreState.Empty -> {

                }
            }
        })

    }

    private fun setBlank(){
        layout_no_data.visibility = VISIBLE
        lv_win_reward.visibility = GONE
    }

    private fun setRecyclerviewData(winRewardHistoryResponse: WinRewardHistoryResponse){
        lv_win_reward.layoutManager = LinearLayoutManager(this)
        winRewardHistoryResponse.items.add(null)
        val driveItemAdapter = WinRewardHistoryAdapter(this, winRewardHistoryResponse)
        lv_win_reward.adapter = driveItemAdapter
        val dividerItemDecoration = DividerItemDecoration(
            this,
            R.color.gray_50,
            dpToPx(this, 32)
        ) // 색상 리소스와 구분선 높이 설정
        lv_win_reward.addItemDecoration(dividerItemDecoration)



        layout_no_data.visibility = GONE
        lv_win_reward.visibility = VISIBLE
    }

    class WinRewardHistoryAdapter(
        private val context: Context,
        private val rewardResponse: WinRewardHistoryResponse,
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        class WinRewardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val item_expired: LinearLayout = view.findViewById(R.id.item_expired)
            val item_win_reward: LinearLayout = view.findViewById(R.id.item_win_reward)
            val item_input_completed: LinearLayout = view.findViewById(R.id.item_input_completed)
            val item_send_completed: LinearLayout = view.findViewById(R.id.item_send_completed)
        }

        class LastItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvMore: TextView = view.findViewById(R.id.tv_more)
            val tvLast: TextView = view.findViewById(R.id.tv_last)
        }

        companion object {
            private const val VIEW_TYPE_ITEM = 0
            private const val VIEW_TYPE_LAST_ITEM = 1
        }

        override fun getItemViewType(position: Int): Int {
            return if (position == rewardResponse.items.size - 1) VIEW_TYPE_LAST_ITEM else VIEW_TYPE_ITEM
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflater = LayoutInflater.from(context)
            return if (viewType == VIEW_TYPE_ITEM) {
                val view = inflater.inflate(R.layout.item_win_reward, parent, false)
                WinRewardViewHolder(view)
            } else {
                val view = inflater.inflate(R.layout.item_drive_history_last, parent, false)
                LastItemViewHolder(view)
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is WinRewardViewHolder) {
                holder.item_win_reward.visibility = VISIBLE
                holder.item_expired.visibility = GONE
                holder.item_send_completed.visibility = GONE
                holder.item_input_completed.visibility = GONE
            } else if (holder is LastItemViewHolder) {
//                if (rewardResponse.meta.afterCursor.isNullOrBlank()) {
//                    holder.tvMore.visibility = GONE
//                    holder.tvLast.visibility = View.VISIBLE
//                } else {
//                    holder.tvMore.visibility = View.VISIBLE
//                    holder.tvLast.visibility = GONE
//                }
//                holder.tvMore.setOnClickListener {
//
//                }
            }
        }

        override fun getItemCount(): Int = rewardResponse.items.size

        // dpToPx 메서드 최적화
        private fun dpToPx(context: Context, dp: Float): Int {
            return (dp * context.resources.displayMetrics.density).toInt()
        }
    }
}