package campus.tech.kakao.map.model.network

import campus.tech.kakao.map.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

interface KakaoLocalService {   //retrofit interface

    @GET("v2/local/search/keyword.json")
    fun searchKeyword(
        @Query("query") query: String,
        @Query("category_group_code") categoryGroupCode: String? = null,
        @Query("x") longitude: String? = null,
        @Query("y") latitude: String? = null,
        @Query("radius") radius: Int? = null
    ): Call<KakaoSearchResponse>
}

data class KakaoSearchResponse(
    val documents: List<Document>,
    val meta: Meta
)

//변수명 카멜케이스로 고치기! @SerializedName 활용
data class Document(
    val id: String, //장소ID
    val place_name: String, //장소명, 업체명
    val category_name: String,  //카테고리 이름
    val category_group_code: String,    // 중요 카테고리만 그룹핑한 카테고리 그룹 코드
    val category_group_name: String,    //중요 카테고리만 그룹핑한 카테고리 그룹명
    val phone: String,  //전화번호
    val address_name: String,   //전체 지번 주소
    val road_address_name: String,  //전체 도로명 주소
    val x: String,  //X 좌표값, 경위도인 경우 longitude (경도)
    val y: String,  //Y 좌표값, 경위도인 경우 latitude(위도)
    val place_url: String,  //장소 상세페이지 URL
    val distance: String    //중심좌표까지의 거리 (단, x,y 파라미터를 준 경우에만 존재) 단위 meter
)

data class Meta(
    val total_count: Int,   //검색된 문서 수
    val pageable_count: Int,    //total_count 중 노출 가능 문서 수 (최대값: 45)
    val is_end: Boolean,    //현재 페이지가 마지막 페이지인지 여부 값이 false면 다음 요청 시 page 값을 증가시켜 다음 페이지 요청 가능
    val same_name: SameName    //질의어의 지역 및 키워드 분석 정보
)

data class SameName(

    val region: List<String>,    //질의어에서 인식된 지역의 리스트 (예: '중앙로 맛집' 에서 '중앙로'에 해당하는 지역 리스트)
    val keyword: String,   //질의어에서 지역 정보를 제외한 키워드 (예: '중앙로 맛집' 에서 '맛집')
    val selected_region: String    //인식된 지역 리스트 중 현재 검색에 사용된 지역 정보
)

object RetrofitInstance { //네트워크 통신 retrofit 객체 정의
    private const val BASE_URL = "https://dapi.kakao.com/"

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->  //HTTP 요청을 가로채서 수정할 수 있도록 한다
            val apiKey = BuildConfig.KAKAO_REST_API_KEY //API 키를 가져와 Authorization 헤더에 추가
            val newRequest = chain.request().newBuilder()
                .addHeader("Authorization", "KakaoAK $apiKey")  //원래 요청에 Authorization 헤더를 추가한 새 요청을 빌드합니다.
                .build()
            chain.proceed(newRequest)   //수정된 요청을 실행
        }
        .build()

    val api: KakaoLocalService by lazy {    //지연 초기화, api객체가 처음 사용될 때 초기화
        Retrofit.Builder()
            .baseUrl(BASE_URL)  //기본 URL을 설정
            .client(client) //위에서 정의한 OkHttpClient 사용
            .addConverterFactory(GsonConverterFactory.create()) //JSON 데이터를 파싱하기 위해 Gson 변환기를 사용
            .build()
            .create(KakaoLocalService::class.java)
    }
}
