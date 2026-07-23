<!--
  [화면 흐름 안내] BranchMenu
  역할: 지점 운영에서 사용자가 보는 화면이다.
  진입: /branch/menu -> 이 Vue 파일 렌더링
  데이터: 사용자 동작 -> @/api/axios -> 응답/상태 반영
  다음 이동: /branch/main
-->
<script setup>

import { ref, onMounted } from 'vue'
import api from '@/api/axios'
import { useRouter } from 'vue-router'


const router = useRouter()


const user = JSON.parse(
    localStorage.getItem('branchUser')
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


    // 현재 OFF 상태 → ON (store_flavors 추가)
    if(!target){


        await api.post(
            `/branch/status/flavor/${user.storeId}`,
            {
                flavorId: flavor.flavorId
            }
        )


    }


    // 현재 ON 상태 → OFF (store_flavors 삭제)
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

<div class="menu-page">


    <button 
        class="back-button"
        @click="goBack"
    >
        ← 메인으로 돌아가기
    </button>



    <div class="menu-box">


        <h2>
            메뉴 관리
        </h2>



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


                    <td class="flavor-name">
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


</div>


</template>

<style scoped>

/* 메뉴 페이지 */

.menu-page {

    min-height:100vh;

    padding:30px;

    box-sizing:border-box;

    background:#f8f9fa;

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

    font-weight:bold;

    cursor:pointer;

    transition:0.2s;

}



.menu-page > button:hover {

    background:#555;

}





/* 제목 */

h2 {

    margin:30px 0 20px;

    color:#333;

}





/* 메뉴 테이블 */

table {

    width:100%;

    background:white;

    border-collapse:separate;

    border-spacing:0;

    border-radius:15px;

    overflow:hidden;

    box-shadow:0 4px 12px rgba(0,0,0,0.08);

    margin-bottom:30px;

}





/* 테이블 헤더 */

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





/* 맛 이름 왼쪽 정렬 */

.flavor-name {

    text-align:left;

    padding-left:30px;

    font-weight:500;

}





/* ON/OFF 버튼 */

.toggle {

    width:80px;

    padding:8px 15px;

    border:none;

    border-radius:20px;

    font-size:13px;

    font-weight:bold;

    cursor:pointer;

    transition:0.2s;

}





.toggle:hover {

    transform:translateY(-2px);

}





/* 사용중 */

.on {

    background:#d1e7dd;

    color:#0f5132;

}





/* 미사용 */

.off {

    background:#f8d7da;

    color:#842029;

}





/* 검색 input */

input {

    padding:10px 14px;

    border-radius:8px;

    border:1px solid #ccc;

    font-size:14px;

}





/* 반응형 */

@media(max-width:900px){


    .menu-page{

        padding:15px;

    }


    table{

        font-size:13px;

    }


    th,
    td{

        padding:10px;

    }

}

</style>