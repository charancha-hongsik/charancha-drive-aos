package com.milelog.activity

import WinRewardHistoryResponse
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.milelog.DividerItemDecoration
import com.milelog.R
import com.milelog.viewmodel.BaseViewModel
import com.milelog.viewmodel.WinRewardHistoryViewModel
import com.milelog.viewmodel.state.GetWinRewardHistoryMoreState
import com.milelog.viewmodel.state.GetWinRewardHistoryState
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import kotlin.math.exp

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

    /**
     *    - 당첨: 수령 정보 입력 전
     *     userDelivery == null && Date.now =< expiredAt
     *   - 입력완료: 수령 정보 입력 완료
     *     userDelivery != null
     *   - 발송완료: admin에서 발송 완료 값 내려올 시
     *     userDelivery == COMPLETED
     *   - 기간만료: 만료시
     *     userDelivery == null && Date.now > expiredAt
     */
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
            val item_win_reward: LinearLayout = view.findViewById(R.id.item_win_reward)
            val tv_dday:TextView = view.findViewById(R.id.tv_dday)
            val tv_dday_date:TextView = view.findViewById(R.id.tv_dday_date)
            val iv_reward:ImageView = view.findViewById(R.id.iv_reward)
            val tv_reward_title:TextView = view.findViewById(R.id.tv_reward_title)
            val tv_reward_detail:TextView = view.findViewById(R.id.tv_reward_detail)
            val tv_input_info:TextView = view.findViewById(R.id.tv_input_info)

            val item_input_completed: LinearLayout = view.findViewById(R.id.item_input_completed)
            val tv_dday_date_for_input_complete:TextView = view.findViewById(R.id.tv_dday_date_for_input_complete)
            val iv_reward_for_input_complete:ImageView = view.findViewById(R.id.iv_reward_for_input_complete)
            val tv_reward_title_for_input_complete:TextView = view.findViewById(R.id.tv_reward_title_for_input_complete)
            val tv_reward_detail_for_input_complete:TextView = view.findViewById(R.id.tv_reward_detail_for_input_complete)

            val item_send_completed: LinearLayout = view.findViewById(R.id.item_send_completed)
            val iv_reward_for_send_complete:ImageView = view.findViewById(R.id.iv_reward_for_send_complete)
            val tv_reward_title_for_send_complete:TextView = view.findViewById(R.id.tv_reward_title_for_send_complete)
            val tv_reward_detail_for_send_complete:TextView = view.findViewById(R.id.tv_reward_detail_for_send_complete)


            val item_expired: LinearLayout = view.findViewById(R.id.item_expired)
            val tv_dday_date_for_expired:TextView = view.findViewById(R.id.tv_dday_date_for_expired)
            val iv_reward_for_expired:ImageView = view.findViewById(R.id.iv_reward_for_expired)
            val tv_reward_title_for_expired:TextView = view.findViewById(R.id.tv_reward_title_for_expired)
            val tv_reward_detail_for_expired:TextView = view.findViewById(R.id.tv_reward_detail_for_expired)
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
                /**
                 *   - 당첨: 수령 정보 입력 전
                 *     userDelivery == null && Date.now =< expiredAt
                 *   - 기간만료: 만료시
                 *     userDelivery == null && Date.now > expiredAt
                 *   - 입력완료: 수령 정보 입력 완료
                 *     userDelivery != null
                 *   - 발송완료: admin에서 발송 완료 값 내려올 시
                 *     userDelivery == COMPLETED
                 */

                val item = rewardResponse.items.get(position)
                item?.let{
                    if(it.userDelivery == null){
                        if(isCurrentTimeBeforeOrEqual(it.expiredAt)){
                            /**
                             * 당첨
                             */
                            holder.item_win_reward.visibility = VISIBLE
                            holder.item_expired.visibility = GONE
                            holder.item_input_completed.visibility = GONE
                            holder.item_send_completed.visibility = GONE

                            it.expiredAt?.let{ expiredAt ->
                                holder.tv_dday_date.text = formatIsoToCustomDate(expiredAt)
                                holder.tv_dday.text = daysUntil(expiredAt)

                            }


                            Glide.with(context)
                                .asBitmap()
                                .load(it.item?.files?.get(0)?.file?.url)
                                .into(object : CustomTarget<Bitmap>() {
                                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                        holder.iv_reward.setImageBitmap(resource)
                                    }

                                    override fun onLoadCleared(placeholder: Drawable?) {
                                        // 이 부분은 Glide가 리소스를 해제할 때 호출됩니다.

                                    }
                                })



                            holder.tv_reward_title.text = item.item?.brand
                            holder.tv_reward_detail.text = item.item?.name
                            holder.tv_input_info.setOnClickListener{

                            }

                        } else{
                            /**
                             * 기간 만료
                             */
                            holder.item_win_reward.visibility = GONE
                            holder.item_expired.visibility = VISIBLE
                            holder.item_input_completed.visibility = GONE
                            holder.item_send_completed.visibility = GONE

                            it.expiredAt?.let{ expiredAt ->
                                holder.tv_dday_date_for_input_complete.text = formatIsoToCustomDate(expiredAt)
                            }

                            Glide.with(context)
                            Glide.with(context)
                                .asBitmap()
                                .load(it.item?.files?.get(0)?.file?.url)
                                .into(object : CustomTarget<Bitmap>() {
                                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                        holder.iv_reward_for_input_complete.setImageBitmap(resource)
                                    }

                                    override fun onLoadCleared(placeholder: Drawable?) {
                                        // 이 부분은 Glide가 리소스를 해제할 때 호출됩니다.

                                    }
                                })


                            holder.tv_reward_title_for_input_complete.text = item.item?.brand
                            holder.tv_reward_detail_for_input_complete.text = item.item?.name

                        }
                    } else if(it.userDelivery != null){
                        if(it.userDelivery.status.equals("COMPLETE")){
                            /**
                             * 발송 완료
                             */
                            holder.item_win_reward.visibility = GONE
                            holder.item_expired.visibility = GONE
                            holder.item_input_completed.visibility = GONE
                            holder.item_send_completed.visibility = VISIBLE


                            Glide.with(context)
                                .asBitmap()
                                .load(it.item?.files?.get(0)?.file?.url)
                                .into(object : CustomTarget<Bitmap>() {
                                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {

                                        holder.iv_reward_for_send_complete.setImageBitmap(resource)
                                    }

                                    override fun onLoadCleared(placeholder: Drawable?) {
                                        // 이 부분은 Glide가 리소스를 해제할 때 호출됩니다.
                                    }
                                })

                            holder.tv_reward_title_for_send_complete.text = item.item?.brand
                            holder.tv_reward_detail_for_send_complete.text = item.item?.name
                        }else{
                            /**
                             * 입력 완료
                             */
                            holder.item_win_reward.visibility = GONE
                            holder.item_expired.visibility = GONE
                            holder.item_input_completed.visibility = VISIBLE
                            holder.item_send_completed.visibility = GONE
                            it.expiredAt?.let{ expiredAt ->
                                holder.tv_dday_date_for_expired.text = formatIsoToCustomDate(expiredAt)
                            }

                            Glide.with(context)
                                .asBitmap()
                                .load(it.item?.files?.get(0)?.file?.url)
                                .into(object : CustomTarget<Bitmap>() {
                                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                        Log.d("testsetestest","testestestestest :: " + it.item?.files?.get(0)?.file?.url)

                                        holder.iv_reward_for_expired.setImageBitmap(resource)
                                    }

                                    override fun onLoadCleared(placeholder: Drawable?) {
                                        // 이 부분은 Glide가 리소스를 해제할 때 호출됩니다.
                                        Log.d("testsetestest","testestestestest2 :: " + it.item?.files?.get(0)?.file?.url)

                                    }
                                })

                            holder.tv_reward_title_for_expired.text = item.item?.brand
                            holder.tv_reward_detail_for_expired.text = item.item?.name
                        }
                    }
                }


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

        fun isCurrentTimeBeforeOrEqual(isoTime: String?): Boolean {
            // Define the date format for ISO 8601 with milliseconds and UTC timezone
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            isoFormat.timeZone = TimeZone.getTimeZone("UTC")

            return try {
                // Parse the input ISO time string to a Date object
                val parsedDate = isoFormat.parse(isoTime)
                // Get the current time
                val currentDate = Date()

                // Compare current time with the parsed date
                !currentDate.after(parsedDate)
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle parsing errors (optional: return false or throw an exception)
                false
            }
        }

        fun formatIsoToCustomDate(isoTime: String): String {
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            isoFormat.timeZone = TimeZone.getTimeZone("UTC")

            return try {
                val date = isoFormat.parse(isoTime)
                val customFormat = SimpleDateFormat("yyyy.M.d")
                customFormat.timeZone = TimeZone.getDefault() // Use local timezone
                customFormat.format(date)
            } catch (e: ParseException) {
                e.printStackTrace()
                "Invalid date"
            }
        }

        fun daysUntil(isoTime: String): String {
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            isoFormat.timeZone = TimeZone.getTimeZone("UTC")

            return try {
                val targetDate = isoFormat.parse(isoTime)
                val currentDate = Date()

                val diffInMillis = targetDate.time - currentDate.time
                val daysRemaining = (diffInMillis / (1000 * 60 * 60 * 24)).toInt()

                if (daysRemaining >= 0) {
                    "D-${daysRemaining}"
                } else {
                    "D+${-daysRemaining}"
                }
            } catch (e: ParseException) {
                e.printStackTrace()
                "Invalid date"
            }
        }
    }
}