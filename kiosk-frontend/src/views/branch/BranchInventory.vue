<template>

<div class="menu-page">


    <button @click="goBack">
        ← 메인으로 돌아가기
    </button>


    <h2>상품 품절 관리</h2>


    <table>

        <thead>

            <tr>
                <th>상품명</th>
                <th>재고</th>
                <th>상태</th>
                <th>변경</th>
                <th>발주</th>

            </tr>

        </thead>


        <tbody>

            <tr
                v-for="menu in menus"
                :key="menu.storeProductId"
            >

                <td>
                    {{ menu.productName }}
                </td>


                <td>
                    {{ menu.currentStock }}
                </td>

                <td>

                <button
                    @click="changeProductSoldOut(menu)"
                >

                {{
                    menu.soldOut
                    ? '판매 재개'
                    : '품절 처리'
                }}

                </button>

                </td>


                <td>

                    <span v-if="menu.soldOut">
                        품절
                    </span>

                    <span v-else>
                        판매중
                    </span>

                </td>


               <td>

                <button
                    v-if="menu.storeInventoryId"
                    @click="openRestockModal(menu,'PRODUCT')"
                >
                    재고 신청
                </button>

                </td>


            </tr>


        </tbody>


    </table>



    <hr>



    <h2>맛 품절 관리</h2>



    <table>

        <thead>

            <tr>

                <th>맛</th>

                <th>통 수</th>

                <th>상태</th>

                <th>변경</th>

                <th>발주</th>

            </tr>

        </thead>



        <tbody>


            <tr
                v-for="flavor in flavors"
                :key="flavor.storeFlavorId"
            >


                <td>
                    {{ flavor.flavorName }}
                </td>


                <td class="container-count">


                    <button
                        class="count-btn"
                        @click="changeContainer(flavor, -1)"
                    >
                        -
                    </button>


                    <span>
                        {{ flavor.container }}
                    </span>


                    <button
                        class="count-btn"
                        @click="changeContainer(flavor, 1)"
                    >
                        +
                    </button>


                </td>


                <td>

                    <span v-if="flavor.soldOut">
                        품절
                    </span>


                    <span v-else>
                        판매중
                    </span>


                </td>



                <td>


                    <button
                        @click="changeFlavorSoldOut(flavor)"
                    >

                        {{
                            flavor.soldOut
                            ? '판매 재개'
                            : '품절 처리'
                        }}


                    </button>


                </td>

                <td>

                <button
                    @click="openRestockModal(flavor,'FLAVOR')"
                >
                    재고 신청
                </button>

                </td>


            </tr>


        </tbody>


    </table>







</div>

<div
v-if="showRestockModal"
class="modal-background"
>


<div class="restock-modal">


<h3>
재고 신청
</h3>



<p>
신청 수량
</p>



<input
type="number"
v-model="restockQuantity"
min="1"
/>



<div class="modal-buttons">


<button
@click="submitRestock"
>
완료
</button>



<button
@click="closeRestockModal"
>
취소
</button>


</div>


</div>


</div>

</template>




<script setup>

import { ref, onMounted, onUnmounted } from 'vue'
import {
  useRoute,
  useRouter
} from 'vue-router'
import api from '@/api/axios'
import { requestRestock } from '@/api/branch/statusApi'

// 발주 모달
const showRestockModal = ref(false)

const selectedRestock = ref(null)

const restockQuantity = ref(0)

const router = useRouter()
const route = useRoute()

/*
 * 부족 알림에서 재고 관리 화면으로
 * 이동한 경우 전달받은 정보입니다.
 */
 const shortageContext = ref(null)


const readShortageContext = () => {

  const alertId =
    Number(route.query.alertId)

  const storeInventoryId =
    Number(route.query.storeInventoryId)

  const shortageQuantity =
    Number(route.query.shortageQuantity)


  if (
    !Number.isInteger(alertId) ||
    alertId <= 0 ||
    !Number.isInteger(storeInventoryId) ||
    storeInventoryId <= 0
  ) {

    shortageContext.value = null

    return
  }


  shortageContext.value = {

    alertId,

    storeInventoryId,

    shortageQuantity:
      Number.isInteger(shortageQuantity) &&
      shortageQuantity > 0
        ? shortageQuantity
        : 1
  }
}

let intervalId = null

const user =
JSON.parse(
    localStorage.getItem('branchUser')
)



// 상품 목록
const menus = ref([])

// 맛 목록
const flavors = ref([])




// 메인 이동
const goBack = () => {

    router.push('/branch/main')

}







// 상품 조회
const loadMenus = async()=>{


    try{


        const response =
        await api.get(
            `/branch/status/product/${user.storeId}`
        )


        menus.value =
        response.data



    }catch(e){

        console.error(
            '상품 조회 실패',
            e
        )

    }


}





// 맛 조회
const loadFlavors = async()=>{


    try{


        const response =
        await api.get(
            `/branch/status/flavor/${user.storeId}`
        )


        flavors.value =
        response.data



    }catch(e){


        console.error(
            '맛 조회 실패',
            e
        )


    }


}







// 맛 품절 변경
const changeFlavorSoldOut = async(flavor)=>{


    try{


        await api.patch(

            `/branch/status/flavor/${flavor.storeFlavorId}`,

            {

                soldOut:
                !flavor.soldOut

            }

        )



        await loadFlavors()



    }catch(e){


        console.error(e)


        alert(
            '맛 상태 변경 실패'
        )


    }


}



onMounted(async () => {

/*
 * 재고와 맛 목록을 먼저 조회합니다.
 */
await Promise.all([
  loadMenus(),
  loadFlavors()
])


/*
 * 부족 알림에서 이동한 정보 확인
 */
readShortageContext()


/*
 * alertId와 storeInventoryId가 있으면
 * 해당 일반 재고의 신청창을 자동으로 엽니다.
 */
if (shortageContext.value) {

  const targetMenu =
    menus.value.find(
      (menu) =>
        Number(menu.storeInventoryId) ===
        shortageContext.value.storeInventoryId
    )


  if (targetMenu) {

    openRestockModal(
      targetMenu,
      'PRODUCT',
      shortageContext.value
    )

  } else {

    console.error(
      '부족 알림과 일치하는 재고를 찾을 수 없습니다.',
      shortageContext.value
    )
  }
}


/*
 * 5초마다 자동 갱신
 */
intervalId = setInterval(() => {

  loadMenus()
  loadFlavors()

}, 5000)
})

onUnmounted(() => {

    clearInterval(intervalId)

})


const changeContainer = async (flavor, amount) => {

try {

    await api.patch(
        `/branch/status/flavor/${flavor.storeFlavorId}/container`,
        {
            amount: amount
        }
    )


    // 변경 후 다시 조회
    await loadFlavors()


} catch(e){

    console.error(
        '재고 변경 실패',
        e
    )

    alert('재고 변경 실패')

}

}

/*
 * 발주창 열기
 *
 * shortageInfo가 있으면
 * 재고 부족 알람과 연결된 신청입니다.
 */
 const openRestockModal = (
  item,
  type,
  shortageInfo = null
) => {

  console.log(
    '발주 선택 데이터',
    item
  )


  selectedRestock.value = {

    type,

    /*
     * 일반 수동 신청이면 null
     *
     * 부족 알람을 통해 신청하면
     * 실제 alertId가 들어갑니다.
     */
    alertId:
      type === 'PRODUCT'
        ? shortageInfo?.alertId ?? null
        : null,

    storeInventoryId:
      type === 'PRODUCT'
        ? item.storeInventoryId
        : null,

    storeFlavorId:
      type === 'FLAVOR'
        ? item.storeFlavorId
        : null
  }


  /*
   * 부족 수량이 전달됐다면
   * 기본 신청 수량으로 표시합니다.
   */
  restockQuantity.value =

    shortageInfo?.shortageQuantity > 0
      ? shortageInfo.shortageQuantity
      : 0


  showRestockModal.value = true


  console.log(
    '발주 요청 데이터',
    selectedRestock.value
  )

  restockQuantity.value = 0



showRestockModal.value = true  
}

/*
* 발주 취소
*/
const closeRestockModal = () => {


showRestockModal.value = false


selectedRestock.value = null


restockQuantity.value = 0


}





/*
* 발주 신청
*/
const submitRestock = async()=>{


if(!selectedRestock.value){

    alert(
        '발주 대상을 선택해주세요.'
    )

    return

}



if(
    restockQuantity.value <= 0
){

    alert(
        '발주 수량을 입력해주세요.'
    )

    return

}



const requestData = {

/*
 * 부족 알람에서 신청한 경우에만
 * 실제 alertId가 전달됩니다.
 */
alertId:
  selectedRestock.value.alertId ?? null,

storeInventoryId:
  selectedRestock.value.storeInventoryId ?? null,

storeFlavorId:
  selectedRestock.value.storeFlavorId ?? null,

requestQuantity:
  Number(restockQuantity.value)
}



console.log(
    '최종 발주 요청',
    requestData
)



try{


    await requestRestock(
        requestData
    )



    const requestedAlertId =
  selectedRestock.value.alertId


    alert(
    '재고 신청 완료'
    )


    closeRestockModal()


    /*
    * 부족 알람 연결 신청이었다면
    * URL의 내부 식별값을 제거합니다.
    */
    if (requestedAlertId) {

    shortageContext.value = null

    await router.replace({
        name: 'branch-inventory'
    })
    }

}catch(e){


    console.error(
        '발주 신청 실패',
        e
    )



    alert(
        '재고 신청 실패'
    )

}


}

// 상품 품절 상태 변경
const changeProductSoldOut = async(menu)=>{


try{


    await api.patch(

        `/branch/status/product/${menu.storeProductId}`,

        {
            soldOut:
                !menu.soldOut
        }

    )


    await loadMenus()


}catch(e){


    console.error(
        '상품 품절 변경 실패',
        e
    )


    alert(
        '상품 상태 변경 실패'
    )


}


}



</script>

<style scoped>

.menu-page {

    height:100vh;

    overflow-y:auto;

    padding:30px;

    box-sizing:border-box;

}

table {

    width:100%;

    background:white;

    border-collapse:separate;

    border-spacing:0;

    border-radius:15px;

    overflow:hidden;

    box-shadow:0 4px 12px rgba(0,0,0,0.08);

    margin-bottom:20px;

}

.menu-page table{

    margin-bottom:30px;

}

/* 뒤로가기 버튼 */

.menu-page > button {

    margin-bottom:25px;

    padding:10px 18px;

    border:none;

    border-radius:10px;

    background:#222;

    color:white;

    font-size:14px;

    cursor:pointer;

    transition:0.2s;

}


.menu-page > button:hover {

    background:#555;

}

.menu-page > button:first-child {

    margin-top:10px;

    margin-bottom:25px;

}



h2 {

    margin:30px 0 20px;

    color:#333;

}





/* 테이블 영역 */

table {

    width:100%;

    background:white;

    border-collapse:separate;

    border-spacing:0;

    border-radius:15px;

    overflow:hidden;

    box-shadow:0 4px 12px rgba(0,0,0,0.08);

}





thead {

    background:#333;

    color:white;

}





th {

    padding:15px;

    font-size:14px;

}





td {

    padding:15px;

    text-align:center;

    border-bottom:1px solid #eee;

    color:#444;

}





tbody tr {

    transition:0.2s;

}





tbody tr:hover {

    background:#f7f7f7;

}





tbody tr:last-child td {

    border-bottom:none;

}





/* 상태 표시 */

.status {

    display:inline-block;

    min-width:80px;

    padding:6px 15px;

    border-radius:20px;

    font-size:13px;

    font-weight:bold;

}





/* 판매중 */

.sold-on {

    background:#d1e7dd;

    color:#0f5132;

}





/* 품절 */

.sold-out {

    background:#f8d7da;

    color:#842029;

}





/* 변경 버튼 */

td button {

    padding:8px 15px;

    border:none;

    border-radius:8px;

    background:#222;

    color:white;

    cursor:pointer;

    transition:0.2s;

}





td button:hover {

    background:#555;

}





/* 상품/맛 구분 */

hr {

    margin:30px 0;

    border:none;

    border-top:1px solid #ddd;

}




/* 반응형 */

@media(max-width:900px){


    .menu-page{

        padding:15px;

    }


    table{

        font-size:13px;

    }


    th,td{

        padding:10px;

    }


}

.container-count{

    display:flex;

    justify-content:center;

    align-items:center;

    gap:10px;

}



.container-count span{

    min-width:30px;

    text-align:center;

    font-weight:bold;

}


.count-btn{

    width:32px;

    height:32px;

    padding:0;

    display:flex;

    justify-content:center;

    align-items:center;


    border:none;

    border-radius:50%;


    background:#333;

    color:white;


    font-size:18px;

    font-weight:bold;


    line-height:1;

    cursor:pointer;


    transition:0.2s;

}



.count-btn:hover{

    background:#555;

}

.modal-background{

position:fixed;

top:0;

left:0;

width:100%;

height:100%;

background:rgba(0,0,0,0.4);

display:flex;

justify-content:center;

align-items:center;

}



.restock-modal{


background:white;

padding:30px;

border-radius:15px;

width:300px;

text-align:center;


}



.restock-modal input{


width:80%;

padding:10px;

margin:20px 0;

font-size:16px;


}



.modal-buttons{


display:flex;

justify-content:center;

gap:15px;


}



.modal-buttons button{


padding:10px 20px;

border:none;

border-radius:8px;

background:#222;

color:white;

cursor:pointer;


}

</style>