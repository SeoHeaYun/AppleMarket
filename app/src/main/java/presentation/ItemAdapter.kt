package presentation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.applemarket.R
import com.example.applemarket.databinding.ItemBinding
import data.ItemData
import java.text.DecimalFormat

class ItemAdapter( private val onClick: (ItemData) -> Unit, private val onLongClick: (ItemData) -> Unit, private val onHeartClick: (ItemData) -> Unit) :  // onClick은 엑티비티에서의 클릭이벤트를 담는 콜백 & 생성자 -> 곧바로 init실행 ※람다식에서 ()는 파라미터 타입인데, 람다에서는 인자가 하나여도 ()씀. unit은 반환값이 없음을 나타냄
    RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {  // RecyclerView.Adapter는 abstract class(추상 클래스)이므로 아래의 3개의 메서드를 오버라이딩해야 한다

    var itemList = mutableListOf<ItemData>()  // listActivity 118번째 줄 통해 DataList의 구체적인 값 연결시켰음

    inner class ItemViewHolder(private var binding: ItemBinding, val onClick: (ItemData) -> Unit, val onLongClick: (ItemData) -> Unit, val onHeartClick: (ItemData) -> Unit) :
        RecyclerView.ViewHolder(binding.root) {

        private var currentItem: ItemData? = null // 해당 아이템에 대한 정보 담고 있음


        // 클릭이벤트 (onBindViewHolder 내 반복 호출로 인한 메모리 누수 방지를 위해 viewholder 내에 작성)
        init {
            //  onClick 이벤트
            itemView.setOnClickListener {
                currentItem?.let(onClick) // 자신의 객체(currenItem)를 onClick 메소드에 it으로 전달
            }
            //  onLongClick 이벤트
                // var itemLongClick: ItemLongClick? = null  // 이 코드를 써준 이유는 activity 152번째 줄은 , 39번째 줄의 파라미터를 받고 구체화되어야 하는데, 받지도 않고 실행될 수도 있기때문에, 일단 null로 선언 / object할당 받으면서 null에서 벗어남.
            val longClickHandler = android.os.Handler()
            itemView.setOnLongClickListener {
                longClickHandler.postDelayed({
                    onLongClick(currentItem!!) // 인터페이스 內 함수에 인자전달 -> 후에 엑티비티에서 참조할 것임  // absoluteAdapterPosition: RecyclerView에서의 포지션을 반환해준다
                }, 2000)
                true
            }
            // onHeartClick 이벤트
            binding.itemHeart.setOnClickListener {
                currentItem?.let(onClick)
            }
        }


        fun bind(item: ItemData) {
            val decimal = DecimalFormat("#,###")
            val num = decimal.format(item.price)

            currentItem = item
            binding.itemName.text = item.title
            binding.itemImage.setImageResource(item.image)
            binding.itemAdress.text = item.address
            binding.itemPrice.text = num.toString()
            binding.itemHeartNum.text = item.heart.toString()
            binding.itemChatNum.text = item.chat.toString()
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {  // viewholder 생성
        val binding = LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false) // ActivityItemListBinding.inflate(layoutInflater) 풀어쓴 것
        return ItemViewHolder(ItemBinding.bind(binding), onClick, onLongClick, onHeartClick) // binding 객체 inflate 후, viewholder에 넘기기 + onclick 파라미터 넘기기
    }


    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) // holder는 재사용되는 뷰객체를 가지고 있다.
     {
        holder.bind(itemList[position])         // 데이터 바인딩될 때마다 호출되므로 여기서 onclick 이벤트 처리는 하지말 것.
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}

