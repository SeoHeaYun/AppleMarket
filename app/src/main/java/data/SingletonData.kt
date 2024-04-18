package data

class SingletonData { //Singleton 생성
    companion object {
        private var INSTANCE: SingletonData? = null
        fun getDataSource(): SingletonData {
            return synchronized(SingletonData::class) {   // 멂티쓰레드 환경 대비
                val newInstance = INSTANCE ?: SingletonData()  // elvis 연산자" null일 경우, 인스턴스 생성
                newInstance// return 값
            }
        }
    }

    fun getDataList(): MutableList<ItemData> {
        return dataList()
    }

}