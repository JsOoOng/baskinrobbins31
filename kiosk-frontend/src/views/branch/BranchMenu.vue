<script setup>

import { ref, onMounted } from 'vue'
import api from '@/api/axios'
import { useRouter } from 'vue-router'


const router = useRouter()


const user = JSON.parse(
    localStorage.getItem('user')
)


// 본사 전체 맛
const allFlavors = ref([])


// 우리 지점 등록 맛
const storeFlavors = ref([])



// 뒤로가기
const goBack = () => {

    router.push('/branch/main')

}



// 전체 맛 조회
const loadAllFlavors = async () => {

    try {

        const response =
            await api.get(
                '/branch/status/all-flavors'
            )


        allFlavors.value = response.data


    } catch(e){

        console.error(
            '전체 맛 조회 실패',
            e
        )

    }

}



// 지점 등록 맛 조회
const loadStoreFlavors = async () => {

    try {

        const response =
            await api.get(
                `/branch/status/flavor/${user.storeId}`
            )


        storeFlavors.value = response.data


    } catch(e){

        console.error(
            '지점 맛 조회 실패',
            e
        )

    }

}



// 현재 지점 사용 여부 확인
const isActive = (flavorId) => {


    return storeFlavors.value.some(
        item =>
        item.flavorId === flavorId
    )


}



// 맛 활성화 / 비활성화 변경
const toggleFlavor = async (flavor) => {


    const target =
        storeFlavors.value.find(
            item =>
            item.flavorId === flavor.flavorId
        )


    try {


        // 현재 미사용 -> 추가
        if(!target){


            await api.post(
                `/branch/status/flavor/${user.storeId}`,
                {
                    flavorId: flavor.flavorId
                }
            )


        }


        // 현재 사용중 -> 삭제
        else{


            await api.delete(
                `/branch/status/flavor/${target.storeFlavorId}`
            )


        }



        // 변경 후 다시 조회
        await loadStoreFlavors()



    } catch(e){


        console.error(
            '맛 상태 변경 실패',
            e
        )


    }

}




onMounted(async()=>{


    await loadAllFlavors()

    await loadStoreFlavors()


})


</script>



<template>


<div class="container">


    <div class="header">

        <h2>
            메뉴 관리
        </h2>


        <button @click="goBack">
            뒤로가기
        </button>

    </div>



    <table>


        <thead>

            <tr>

                <th>
                    맛 번호
                </th>


                <th>
                    맛 이름
                </th>


                <th>
                    사용 여부
                </th>


            </tr>

        </thead>



        <tbody>


            <tr
                v-for="flavor in allFlavors"
                :key="flavor.flavorId"
            >


                <td>
                    {{ flavor.flavorId }}
                </td>


                <td>
                    {{ flavor.flavorName }}
                </td>


                <td>


                    <button
                        class="toggle"
                        :class="
                        isActive(flavor.flavorId)
                        ?
                        'on'
                        :
                        'off'
                        "
                        @click="toggleFlavor(flavor)"
                    >


                        {{
                            isActive(flavor.flavorId)
                            ?
                            'ON'
                            :
                            'OFF'
                        }}


                    </button>


                    </td>


            </tr>


        </tbody>


    </table>


</div>


</template>



<style scoped>


.container{

    padding:30px;

}


.header{

    display:flex;

    justify-content:space-between;

    align-items:center;

    margin-bottom:20px;

}



table{

    width:100%;

    border-collapse:collapse;

}



th,td{

    border:1px solid #ddd;

    padding:12px;

    text-align:center;

}



.toggle{

    width:70px;

    padding:8px;

    border:none;

    border-radius:20px;

    font-weight:bold;

    cursor:pointer;

}



.on{

    background:#198754;

    color:white;

}



.off{

    background:#ddd;

    color:#555;

}



button{

    padding:8px 15px;

    border:none;

    border-radius:8px;

    cursor:pointer;

}



</style>