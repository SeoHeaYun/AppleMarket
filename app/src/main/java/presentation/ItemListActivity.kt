package presentation

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.DialogInterface
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuView.ItemView
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.RecyclerView
import com.example.applemarket.R
import com.example.applemarket.databinding.ActivityItemListBinding
import data.ItemData
import data.SingletonData
import presentation.DetailActivity.Companion.EXTRA_ITEM

class ItemListActivity : AppCompatActivity() {

    // 뷰바인딩
    private val binding by lazy { ActivityItemListBinding.inflate(layoutInflater) }

    //  activityResultLauncher
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    // ItemAdapter 인스턴스 생성 -> 클릭 이벤트
    private val itemAdapter: ItemAdapter by lazy {  // ★ 110번째 줄에서, onCreat 때 엑티비티가 어뎁터와 연결되는 순간 초기화 된다. 그 후, 즉시, 어뎁터에 생성자 던져서, adpater의 init 실행하여 onClick 되게 만들고, 그 후 아래의 adapterOnclick 함수 실행하여 구체적인 명령어 주기.
        ItemAdapter(
            onClick = { item -> adapterOnClick(item) },   // 클릭 이벤트가 발생한 리사이클러뷰 아이템이 넘겨준다 -> 생성자 호출하여 adapter 초기화. ItemData 타입의 파라미터로만 콜백 초기화가능
            onLongClick = { item -> adapterOnLongClick(item) },
            onHeartClick = { item -> adapterOnHeartClick(item) }
        )
    }

    // ■온클릭■
    private fun adapterOnClick(item: ItemData) {
        val intent = Intent(this, DetailActivity()::class.java)
        // 선택한 항목에 따라 다른 데이터 디테일 페이지에 뿌리기
        val bundle = Bundle().apply { putParcelable(EXTRA_ITEM, item) }     // 값들을 따로따로 넘길 필요없이, itemData를 한번에 보낸다!  parcelize 경우, putExtra 에서 bundle 지원이 안되서, 따로 번들 형성한 후, 값 보내줘야 함! + 보낼 떄는 객체 자체를 통째로 보내고(bundle) 받을 때, 받을 때거기서 뽑아서 쓰면 됨.
        intent.putExtras(bundle)
        startActivity(intent)
        Log.d("debug", item.toString())
    }

    // ■롱클릭■
    private fun adapterOnLongClick(item: ItemData) {
        val itemRemove = item
        AlertDialog.Builder(this@ItemListActivity)
            .setTitle("삭제")
            .setMessage("삭제하시겠습니까?")
            .setIcon(R.drawable.chat)
            .setPositiveButton("삭제") { dialog, _ ->  // 람다식을 사용할 때, 메서드의 매개변수 중에서 사용하지 않는 매개변수를 표시하기 위해 밑줄(_)을 사용.
                itemAdapter.itemList.remove(itemRemove)
                itemAdapter.notifyDataSetChanged() // 리스트 전체 업데이트(observer pattern) ≫ data - adapter -view 구조에서 data 변경시(list 삭제) 이 함수를 호출하면 adpater에서 확인 후, view를 다시 그린다.
                dialog.dismiss()
            }
            .setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    // ■어댑터 하트클릭■
        // 좋아요 구현 1단계: 좋아요 클릭시 상세페이지에 해당 아이템의 데이터(위치정보,isLiked 상황) 넘기기
    private fun adapterOnHeartClick(item: ItemData) {
        // 디테일 액티비티로 데이터 전달
        val intent = Intent(this@ItemListActivity, DetailActivity::class.java)
        intent.putExtra("LIKE", item)
        activityResultLauncher.launch(intent) // activityResultLauncher API 사용: 디테일 액티비티에서의 결과를 콜백으로 등록(ActivityResultCallback을 통해 처리됨)
    }

        // 좋아요 구현 3단계: detail activity 에서 온 데이터 처리 (likePosition과 isLiked) -> dataList을 업데이트하고, adapter.notifyItemChanged(likePosition)를 호출하여 해당 아이템의 변경 사항을 어댑터에 알리기
    init {
            activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
                {
                    // 값 받아오기
                    if (it.resultCode == RESULT_OK) {
                        val heart = findViewById<ImageView>(R.id.item_heart) // binding보다 init이 먼저 되는 관계로 여기만 findview 사용
                        val likePosition = it.data?.getIntExtra("likePosition", 0) as Int
                        var isLiked = it.data?.getBooleanExtra("isLiked", false) as Boolean // 하트눌렸으면 true로 설정됨
                        heart.setImageResource(if (isLiked) { R.drawable.filled_heart } else { R.drawable.empty_heart })

                        // 데이터 업데이트(좋아요 수, 색깔)
                        if (isLiked) {
                            heart.setImageResource(R.drawable.filled_heart)
                            itemAdapter.itemList[likePosition].heart += 1
                            itemAdapter.itemList[likePosition].isLiked = true
                        } else {
                            heart.setImageResource(R.drawable.empty_heart)
                            itemAdapter.itemList[likePosition].heart -= 1
                            itemAdapter.itemList[likePosition].isLiked = false
                        }
                        itemAdapter.notifyItemChanged(likePosition)  // notifyDataSetChanged를 사용할 경우, 전체가 업데이트 되기 때문에, 리소스 낭비. 그에 비해 이코드는 수정한 부분만 업데이트 해준다.
                    }
                }
        }

                    // 어댑터에 변경 사항 알리기


    // notification
    private fun showNotification() {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val builder: NotificationCompat.Builder

        // ① 안드로이드 8.0 이상일 경우 채널 만들기
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 26 버전 이상
            val channelId = "one-channel"
            val channelName = "My Channel One"
            val channel = NotificationChannel( // 채널
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT // 중요도
            ).apply {
                // 채널에 다양한 정보 설정
                description = "My Channel One Description"
                setShowBadge(true) // 아이콘 빨강 숫자
                val uri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) // 오디오 설정
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build()
                setSound(uri, audioAttributes)
                enableVibration(true)
            }
            // ② 채널을 NotificationManager에 등록
            manager.createNotificationChannel(channel)

            // ③ 채널을 이용하여 builder 생성
            builder = NotificationCompat.Builder(this, channelId)

        } else {
            // 26 버전 이하
            builder = NotificationCompat.Builder(this)
        }

        // ④ 알림 기본정보
        builder.run {
            setSmallIcon(R.drawable.chat)
            setContentTitle("키워드 알림")
            setContentText("설정한 키워드에 대한 알림이 도착했습니다.")
        }
        manager.notify(1, builder.build())
        Log.d("Notification", "알림 표시됨")
    }


    // onCreat 함수
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // SingletionData 인스턴스 얻기
        val singletonData = SingletonData.getDataSource()
        itemAdapter.itemList = singletonData.getDataList()  // 아이템 목록 가져와서 사용하기

        // 리싸이클러뷰와 어뎁터 연결
        with(binding.recyclerView) { // binding.recyclerView가 null이 아님이 보장될 것!
            this.adapter = itemAdapter   // this는 생략가능
        }
        // 레이아웃 메니저 수직으로 형성
        binding.recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        // 디바이더 추가
        val decoration = DividerItemDecoration(this, VERTICAL)
        binding.recyclerView.addItemDecoration(decoration)

        // 알람버튼
        binding.btnNotification.setOnClickListener{ showNotification() }

        // 플로팅 버튼
        val floatingbtn = binding.floatingbtn
            // 클릭시 상단이동
        floatingbtn.setOnClickListener {
            binding.recyclerView.smoothScrollToPosition(0)
        }
            // fade 효과적용: AlphaAnimation- 투명도 조절 함수 / duration: 딜레이 /
        val fadeIn = AlphaAnimation(0f, 1f).apply { duration = 300 }  //float 값을 인자로 받고, from-to 형식. 즉, 시작 투명도와 종료투명도
        val fadeOut = AlphaAnimation(1f, 0f).apply { duration = 300 }

        var isTop = false // true 즉, 맨위에 있으면 보여주고, 아니면 안보여준다.

        binding.recyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if(!binding.recyclerView.canScrollVertically(-1) && newState == RecyclerView.SCROLL_STATE_IDLE) { // 최상단에 있고, 정지상태에 있음 (cf.onScrolled: 스크롤 되는 중일 때)
                    binding.floatingbtn.startAnimation(fadeOut) // 서서히
                    binding.floatingbtn.visibility = View.GONE  // 안보이게 하기
                    isTop = true // 맨위에 있다.
                } else if(isTop) { // false 즉, 맨위가 아닌경우
                    floatingbtn.visibility = View.VISIBLE // 보이게 하기
                    floatingbtn.startAnimation(fadeIn) // 서서히
                    isTop = false
                }
            }
        }
        )


        // 뒤로가기
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

    }

    // 뒤로가기 콜백함수 (onBackPressed가 deprecated 되어 OnBackPressedCallback으로 대체)
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {  // 익명클래스 + 추상메서드 구현
        // 다이얼로그
        override fun handleOnBackPressed() {
            var builder = AlertDialog.Builder(this@ItemListActivity)
            builder.setTitle("종료")
            builder.setMessage("정말 종료하시겠습니까?")
            builder.setIcon(R.drawable.chat)

            val listener = object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, pick: Int) {
                    when(pick) {
                        DialogInterface.BUTTON_POSITIVE
                        -> finish()
                        DialogInterface.BUTTON_NEGATIVE
                        -> dialog?.dismiss()
                    }
                }
            }
            builder.setPositiveButton("확인", listener)
            builder.setNegativeButton("취소", listener)
            builder.show()
        }
    }
}







// LongClick 인터페이스로 구현 시 (onCreat에 적어줄 것)
//    // 2초간 눌렀을 시(여긴 어뎁터에서 처리) 삭제 다이얼로그 띄우기(여긴 뷰영역에서 처리)
//    itemAdapter.itemLongClick = object : ItemAdapter.ItemLongClick {  // 인터페이스 상속 & 익명클래스 구현
//        override fun onLongClick(view: View, position: Int) {  // 어뎁터 39에서 호출되며 받은 값(뷰와 포지션) 구체화
//            val itemRemove = itemAdapter.itemList[position]
//            AlertDialog.Builder(this@ItemListActivity)
//                .setTitle("삭제")
//                .setMessage("삭제하시겠습니까?")
//                .setIcon(R.drawable.chat)
//                .setPositiveButton("삭제") { dialog, _ ->  // 람다식을 사용할 때, 메서드의 매개변수 중에서 사용하지 않는 매개변수를 표시하기 위해 밑줄(_)을 사용.
//                    itemAdapter.itemList.remove(itemRemove)
//                    itemAdapter.notifyDataSetChanged() // 리스트 전체 업데이트(observer pattern) ≫ data - adapter -view 구조에서 data 변경시(list 삭제) 이 함수를 호출하면 adpater에서 확인 후, view를 다시 그린다.
//                    dialog.dismiss()
//                }
//                .setNegativeButton("취소") { dialog, _ ->
//                    dialog.dismiss()
//                }
//                .show()
//        }
//    }


