<script setup>
import { ref, onMounted } from 'vue'
import api from '@/api/axios'


const deliveries = ref([])



// 배송 목록 조회
const getDeliveries = async () => {

    try {

        const response =
            await api.get('/head/deliveries')

            console.log(response.data)

        deliveries.value =
            response.data

    } catch(error) {

        console.error(error)

    }

}



// 상태 변경
const changeStatus = async (
    deliveryId,
    status
) => {


    try {

        await api.put(
            `/head/delivery/${deliveryId}/status`,
            null,
            {
                params:{
                    status: status
                }
            }
        )


        await getDeliveries()


    } catch(error){

        console.error(error)

        alert(
            "배송 상태 변경 실패"
        )

    }

}



onMounted(() => {

    getDeliveries()

})

</script>



<template>

<div class="delivery-container">


<h2>
    배송 관리
</h2>


<table>

<thead>

<tr>
<th>지점명</th>
<th>재고품목</th>
<th>단위</th>
<th>신청수량</th>
<th>신청일시</th>
<th>신청상태</th>
<th>배송상태</th>
<th>관리</th>
</tr>

</thead>


<tbody>


    <tr
v-for="(delivery,index) in deliveries.filter(d => d)"
:key="delivery.deliveryId ?? index"
>


<td>
{{ delivery.storeName }}
</td>


<td>
{{ delivery.itemName }}
</td>


<td>
{{ delivery.unit }}
</td>


<td>
{{ delivery.requestQuantity }}
</td>


<td>
{{ formatDate(delivery.requestedAt) }}
</td>


<td>
{{ restockStatusText(delivery.restockStatus) }}
</td>


<td>
{{ deliveryStatusText(delivery.deliveryStatus) }}
</td>


<td>


<!-- READY -->
<button
v-if="delivery.deliveryStatus === 'READY'"
@click="
changeStatus(
delivery.deliveryId,
'STARTED'
)
"
>
배송 시작
</button>



<!-- STARTED -->
<button
v-if="delivery.deliveryStatus === 'STARTED'"
@click="
changeStatus(
delivery.deliveryId,
'IN_PROGRESS'
)
"
>
배송 중
</button>



<!-- IN_PROGRESS -->
<button
v-if="delivery.deliveryStatus === 'IN_PROGRESS'"
@click="
changeStatus(
delivery.deliveryId,
'COMPLETED'
)
"
>
배송 완료
</button>



<span
v-if="
delivery.deliveryStatus === 'COMPLETED'
"
>
완료
</span>


</td>


</tr>


</tbody>


</table>


</div>

</template>



<script>

function formatDate(date){

    if(!date)
        return ''

    return new Date(date)
        .toLocaleString()

}



function deliveryStatusText(status){

    const map = {

        READY:'배송 준비',

        STARTED:'배송 시작',

        IN_PROGRESS:'배송 중',

        COMPLETED:'배송 완료'

    }


    return map[status] ?? status

}



function restockStatusText(status){

    const map = {

        WAITING:'승인 대기',

        APPROVED:'승인 완료',

        SHIPPING:'배송 중',

        COMPLETED:'입고 완료',

        REJECTED:'반려',

        CANCELED:'취소'

    }


    return map[status] ?? status

}

</script>

<style scoped>

.delivery-container {

  padding: 30px;

}


table {

  width:100%;
  border-collapse:collapse;

}


th,
td {

  border:1px solid #ddd;
  padding:12px;
  text-align:center;

}


button {

  padding:6px 12px;
  cursor:pointer;

}


.READY {

  color:orange;

}


.STARTED {

  color:blue;

}


.IN_PROGRESS {

  color:purple;

}


.COMPLETED {

  color:green;

}

</style>