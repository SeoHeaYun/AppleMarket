package presentation


import android.content.Intent
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.applemarket.R
import com.example.applemarket.databinding.ActivityDetailBinding
import com.google.android.material.snackbar.Snackbar
import data.ItemData
import java.text.DecimalFormat


class DetailActivity : AppCompatActivity() {
    private val binding by lazy { ActivityDetailBinding.inflate(layoutInflater) }

    companion object {                                  // 이점: 코드 가독성 / 오타 방지 / 유지 보수 (키값 바꿔야할 경우, 한곳에서만 수정하면 됨)
        const val EXTRA_ITEM: String = "extra_item"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // 텍스트 아래에 줄 만들기
        binding.tvManner.paintFlags = Paint.UNDERLINE_TEXT_FLAG

        // Onclick: listActivity에서 던져준 값 받기

        val intent: Intent? = getIntent()
        val item = intent?.getParcelableExtra<ItemData>(EXTRA_ITEM)

        val image = item?.image
        val sellerName = item?.sellerName
        val address = item?.address
        val itemTitle = item?.title
        val itemDetail = item?.itemDetail
        val price = item?.price


        // 숫자에 소수점 추가
        val decimal = DecimalFormat("#,###")
        val num = decimal.format(price)

        binding.ivDetail.setImageResource(image!!)
        binding.tvSellerName.text = sellerName.toString()
        binding.tvSellerAddress.text = address.toString()
        binding.tvItemDetail.text = itemDetail.toString()
        binding.tvItemName.text = itemTitle.toString()
        binding.tvItemPrice.text = num.toString()


//        // Glide 통해 parcelable 타입의 이미지 로드하기 (setImageResource의 경우, intId만 받는데, 아닌 경우 Glide 사용)
//        if (image != null) {
//            Glide.with(this)
//                .load(image)
//                .into(binding.ivDetail)
//        }


        // 좋아요 구현 2단계: 좋아요 버튼 누른 후. 뒤로가기 버튼을 통해 메인 액티비티로 돌아갈 때 해당 데이터를 반환.


        // 좋아요(관심) 아이콘 클릭 시 색깔변경 & 스낵바 표시
        var isLiked = false
        binding.ivHeart.setImageResource(if (isLiked) {R.drawable.filled_heart} else {R.drawable.empty_heart})

        binding.ivHeart.setOnClickListener {
            if (!isLiked) { // false 일 경우, 눌렀을 때 true로 바뀌면서 색깔채워넣기
                binding.ivHeart.setImageResource(R.drawable.filled_heart)
                Snackbar.make(binding.detailLayout, "관심 목록에 추가되었습니다.", Snackbar.LENGTH_SHORT).show()
                isLiked = true
            } else {
                binding.ivHeart.setImageResource(R.drawable.empty_heart)
                isLiked = false
            }
        }

        // 뒤로가기 버튼
        binding.btnBack.setOnClickListener {
            exit(isLiked)
        }
    }
        // 뒤로가기 버튼 클릭시 itemListActivity에 데이터 반환: likePosition과  isLiked값
        private fun exit(isLiked: Boolean) {
            // MainActivity로 데이터를 반환하기 위한 Intent 생성
            val likePosition = intent.getIntExtra("likePosition", 0)
            val intent = Intent(this, ItemListActivity::class.java).apply {
                putExtra("likePosition", likePosition)
                putExtra("isLiked", isLiked)
            }
            // 결과 설정 및 액티비티 종료
            setResult(RESULT_OK, intent)
            if (!isFinishing) finish()
        }
    }







