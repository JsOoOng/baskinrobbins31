import api from '@/api/axios'


/*
 * 발주 신청
 */
export const requestRestock = (data) => {

    return api.post(
        '/branch/status/restock',
        data
    )

}